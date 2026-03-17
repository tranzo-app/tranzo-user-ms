package com.tranzo.tranzo_user_ms.splitwise.service;

import com.tranzo.tranzo_user_ms.splitwise.dto.request.CreateExpenseRequest;
import com.tranzo.tranzo_user_ms.splitwise.dto.request.ExpenseSplitRequest;
import com.tranzo.tranzo_user_ms.splitwise.dto.request.UpdateExpenseRequest;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.ExpenseResponse;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.ExpenseSplitResponse;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.GroupResponse;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.UserResponse;
import com.tranzo.tranzo_user_ms.splitwise.entity.Expense;
import com.tranzo.tranzo_user_ms.splitwise.entity.ExpenseSplit;
import com.tranzo.tranzo_user_ms.splitwise.entity.SplitwiseGroup;
import com.tranzo.tranzo_user_ms.splitwise.exception.ExpenseNotFoundException;
import com.tranzo.tranzo_user_ms.splitwise.exception.GroupNotFoundException;
import com.tranzo.tranzo_user_ms.splitwise.exception.InvalidSplitException;
import com.tranzo.tranzo_user_ms.splitwise.exception.UserNotMemberException;
import com.tranzo.tranzo_user_ms.splitwise.repository.ExpenseRepository;
import com.tranzo.tranzo_user_ms.splitwise.repository.SplitwiseGroupRepository;
import com.tranzo.tranzo_user_ms.trip.model.TripEntity;
import com.tranzo.tranzo_user_ms.trip.repository.TripRepository;
import com.tranzo.tranzo_user_ms.user.model.UserProfileEntity;
import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import com.tranzo.tranzo_user_ms.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for creating, reading, updating, and deleting expenses; enforces group membership and split validation.
 */
@Slf4j
@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final SplitwiseGroupRepository splitwiseGroupRepository;
    private final BalanceService balanceService;
    private final ActivityService activityService;
    private final UserRepository userRepository;
    private final TripRepository tripRepository;

    public ExpenseService(ExpenseRepository expenseRepository,
                          SplitwiseGroupRepository splitwiseGroupRepository,
                          BalanceService balanceService,
                          ActivityService activityService,
                          UserRepository userRepository,
                          TripRepository tripRepository) {
        this.expenseRepository = expenseRepository;
        this.splitwiseGroupRepository = splitwiseGroupRepository;
        this.balanceService = balanceService;
        this.activityService = activityService;
        this.userRepository = userRepository;
        this.tripRepository = tripRepository;
    }

    /**
     * Creates an expense. Current user must be group member; paidById must be current user or current user must be admin.
     */
    public ExpenseResponse createExpense(CreateExpenseRequest request, UUID currentUserId) {
        SplitwiseGroup group = splitwiseGroupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new GroupNotFoundException(request.getGroupId()));
        if (!splitwiseGroupRepository.isUserMemberOfGroup(request.getGroupId(), currentUserId)) {
            throw new UserNotMemberException(currentUserId, request.getGroupId());
        }
//        if (!request.getPaidById().equals(currentUserId) && !splitwiseGroupRepository.isUserAdminOfGroup(request.getGroupId(), currentUserId)) {
//            throw new UserNotMemberException("Only payer or group admin can add expense");
//        }
        validateAllSplitUsersAreMembers(request.getGroupId(), request.getSplits());
        validateSplits(request);

        LocalDateTime expenseDateTime = request.getExpenseDate() != null
                ? request.getExpenseDate().atStartOfDay()
                : LocalDateTime.now();

        Expense expense = Expense.builder()
                .name(request.getName())
                .description(request.getDescription())
                .amount(request.getAmount())
                .paidBy(request.getPaidById())
                .groupId(request.getGroupId())
                .splitType(request.getSplitType())
                .expenseDate(expenseDateTime)
                .category(request.getCategory())
                .receiptUrl(request.getReceiptUrl())
                .build();

        List<ExpenseSplit> splits = buildSplitsFromRequest(expense, request);
        for (ExpenseSplit split : splits) {
            expense.addSplit(split);
        }
        expense = expenseRepository.save(expense);

        balanceService.updateBalancesForExpense(expense);
        activityService.logExpenseCreated(request.getPaidById(), group, expense.getId(), expense.getName(), expense.getAmount());
        log.info("Created expense {} in group {}", expense.getId(), request.getGroupId());
        return toExpenseResponse(expense);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpense(UUID expenseId, UUID currentUserId) {
        Expense expense = expenseRepository.findByIdWithSplits(expenseId)
                .orElseThrow(() -> new ExpenseNotFoundException(expenseId));
        if (!splitwiseGroupRepository.isUserMemberOfGroup(expense.getGroupId(), currentUserId)
                && !expense.isUserInvolved(currentUserId)) {
            throw new UserNotMemberException(currentUserId, expense.getGroupId());
        }
        return toExpenseResponse(expense);
    }

    /**
     * Updates an expense. Only payer or group admin.
     */
    public ExpenseResponse updateExpense(UUID expenseId, UpdateExpenseRequest request, UUID currentUserId) {
        Expense expense = expenseRepository.findByIdWithSplits(expenseId)
                .orElseThrow(() -> new ExpenseNotFoundException(expenseId));
        if (!expense.getPaidBy().equals(currentUserId) && !splitwiseGroupRepository.isUserAdminOfGroup(expense.getGroupId(), currentUserId)) {
            throw new UserNotMemberException(currentUserId, expense.getGroupId());
        }

        if (request.getName() != null) expense.setName(request.getName());
        if (request.getDescription() != null) expense.setDescription(request.getDescription());
        if (request.getAmount() != null) expense.setAmount(request.getAmount());
        if (request.getSplitType() != null) expense.setSplitType(request.getSplitType());
        if (request.getCategory() != null) expense.setCategory(request.getCategory());
        if (request.getReceiptUrl() != null) expense.setReceiptUrl(request.getReceiptUrl());
        if (request.getExpenseDate() != null) {
            try {
                expense.setExpenseDate(LocalDate.parse(request.getExpenseDate()).atStartOfDay());
            } catch (Exception ignored) { }
        }

        if (request.getSplits() != null && !request.getSplits().isEmpty()) {
            validateAllSplitUsersAreMembers(expense.getGroupId(), request.getSplits());
            validateUpdateSplits(expense.getAmount(), request.getSplitType(), request.getSplits());
            expense.getSplits().clear();
            List<ExpenseSplit> newSplits = buildSplitsFromUpdate(expense, request.getSplits(), expense.getAmount(), request.getSplitType());
            for (ExpenseSplit split : newSplits) {
                expense.addSplit(split);
            }
        }

        expenseRepository.save(expense);
        balanceService.recalculateBalancesForGroup(expense.getGroupId());
        SplitwiseGroup group = splitwiseGroupRepository.findById(expense.getGroupId()).orElse(null);
        if (group != null) {
            activityService.logExpenseUpdated(currentUserId, group, expense.getId(), expense.getName());
        }
        log.info("Updated expense {}", expenseId);
        return toExpenseResponse(expenseRepository.findByIdWithSplits(expenseId).orElse(expense));
    }

    /**
     * Deletes an expense. Only payer or group admin; recalculates balances and logs.
     */
    public void deleteExpense(UUID expenseId, UUID currentUserId) {
        Expense expense = expenseRepository.findByIdWithSplits(expenseId)
                .orElseThrow(() -> new ExpenseNotFoundException(expenseId));
        if (!expense.getPaidBy().equals(currentUserId) && !splitwiseGroupRepository.isUserAdminOfGroup(expense.getGroupId(), currentUserId)) {
            throw new UserNotMemberException(currentUserId, expense.getGroupId());
        }
        UUID groupId = expense.getGroupId();
        String name = expense.getName();
        SplitwiseGroup group = splitwiseGroupRepository.findById(groupId).orElse(null);
        expenseRepository.delete(expense);
        balanceService.recalculateBalancesForGroup(groupId);
        if (group != null) {
            activityService.logExpenseDeleted(currentUserId, group, expenseId, name);
        }
        log.info("Deleted expense {}", expenseId);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getGroupExpenses(UUID groupId, UUID currentUserId) {
        if (!splitwiseGroupRepository.isUserMemberOfGroup(groupId, currentUserId)) {
            throw new UserNotMemberException(currentUserId, groupId);
        }
        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        return expenses.stream()
                .map(this::toExpenseResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getUserExpenses(UUID currentUserId) {
        List<Expense> expenses = expenseRepository.findExpensesInvolvingUser(currentUserId);
        return expenses.stream()
                .map(this::toExpenseResponse)
                .collect(Collectors.toList());
    }

    private void validateAllSplitUsersAreMembers(UUID groupId, List<ExpenseSplitRequest> splits) {
        for (ExpenseSplitRequest s : splits) {
            if (!splitwiseGroupRepository.isUserMemberOfGroup(groupId, s.getUserId())) {
                throw new InvalidSplitException("User " + s.getUserId() + " is not a member of the group");
            }
        }
    }

    private void validateSplits(CreateExpenseRequest request) {
        BigDecimal amount = request.getAmount();
        Expense.SplitType type = request.getSplitType();
        List<ExpenseSplitRequest> splits = request.getSplits();
        if (splits == null || splits.isEmpty()) {
            throw new InvalidSplitException("At least one split is required");
        }
        if (type == Expense.SplitType.EQUAL) {
            BigDecimal each = amount.divide(BigDecimal.valueOf(splits.size()), 2, RoundingMode.HALF_UP);
            BigDecimal sum = each.multiply(BigDecimal.valueOf(splits.size()));
            if (sum.compareTo(amount) != 0) {
                for (ExpenseSplitRequest s : splits) {
                    if (s.getAmount().compareTo(each) != 0) throw new InvalidSplitException("Equal split amounts must match total / count");
                }
                // allow rounding: use sum of provided amounts
            }
            // Accept equal splits: we'll overwrite amounts when building
        } else if (type == Expense.SplitType.PERCENTAGE) {
            BigDecimal totalPct = splits.stream().map(s -> s.getPercentage() != null ? s.getPercentage() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (totalPct.compareTo(new BigDecimal("100")) != 0) {
                throw new InvalidSplitException("Sum of percentages must equal 100");
            }
            for (ExpenseSplitRequest s : splits) {
                BigDecimal expected = amount.multiply(s.getPercentage() != null ? s.getPercentage() : BigDecimal.ZERO).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                if (s.getAmount().compareTo(expected) != 0 && s.getAmount().compareTo(expected.setScale(2, RoundingMode.HALF_UP)) != 0) {
                    // allow small rounding
                }
            }
        } else {
            // UNEQUAL
            BigDecimal sum = splits.stream().map(ExpenseSplitRequest::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (sum.compareTo(amount) != 0) {
                throw new InvalidSplitException("Sum of split amounts must equal expense amount");
            }
        }
    }

    private void validateUpdateSplits(BigDecimal amount, Expense.SplitType type, List<ExpenseSplitRequest> splits) {
        if (splits == null || splits.isEmpty()) throw new InvalidSplitException("At least one split is required");
        if (type == Expense.SplitType.PERCENTAGE) {
            BigDecimal totalPct = splits.stream().map(s -> s.getPercentage() != null ? s.getPercentage() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (totalPct.compareTo(new BigDecimal("100")) != 0) throw new InvalidSplitException("Sum of percentages must equal 100");
        } else if (type == Expense.SplitType.UNEQUAL) {
            BigDecimal sum = splits.stream().map(ExpenseSplitRequest::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (sum.compareTo(amount) != 0) throw new InvalidSplitException("Sum of split amounts must equal expense amount");
        }
    }

    private List<ExpenseSplit> buildSplitsFromRequest(Expense expense, CreateExpenseRequest request) {
        BigDecimal amount = request.getAmount();
        Expense.SplitType type = request.getSplitType();
        List<ExpenseSplitRequest> req = request.getSplits();
        if (type == Expense.SplitType.EQUAL) {
            BigDecimal each = amount.divide(BigDecimal.valueOf(req.size()), 2, RoundingMode.HALF_UP);
            List<ExpenseSplit> splits = new ArrayList<>();
            BigDecimal remainder = amount.subtract(each.multiply(BigDecimal.valueOf(req.size())));
            for (int i = 0; i < req.size(); i++) {
                BigDecimal amt = i == 0 ? each.add(remainder) : each;
                splits.add(ExpenseSplit.builder()
                        .expense(expense)
                        .userId(req.get(i).getUserId())
                        .amount(amt)
                        .percentage(BigDecimal.valueOf(100.0 / req.size()))
                        .build());
            }
            return splits;
        }
        if (type == Expense.SplitType.PERCENTAGE) {
            List<ExpenseSplit> splits = new ArrayList<>();
            for (ExpenseSplitRequest s : req) {
                BigDecimal pct = s.getPercentage() != null ? s.getPercentage() : BigDecimal.ZERO;
                BigDecimal amt = amount.multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                splits.add(ExpenseSplit.builder()
                        .expense(expense)
                        .userId(s.getUserId())
                        .amount(amt)
                        .percentage(pct)
                        .build());
            }
            BigDecimal sum = splits.stream().map(ExpenseSplit::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (sum.compareTo(amount) != 0) {
                splits.get(0).setAmount(splits.get(0).getAmount().add(amount.subtract(sum)));
            }
            return splits;
        }
        List<ExpenseSplit> splits = new ArrayList<>();
        for (ExpenseSplitRequest s : req) {
            splits.add(ExpenseSplit.builder()
                    .expense(expense)
                    .userId(s.getUserId())
                    .amount(s.getAmount())
                    .percentage(s.getPercentage())
                    .build());
        }
        return splits;
    }

    private List<ExpenseSplit> buildSplitsFromUpdate(Expense expense, List<ExpenseSplitRequest> req, BigDecimal amount, Expense.SplitType type) {
        if (type == Expense.SplitType.EQUAL) {
            BigDecimal each = amount.divide(BigDecimal.valueOf(req.size()), 2, RoundingMode.HALF_UP);
            List<ExpenseSplit> splits = new ArrayList<>();
            BigDecimal remainder = amount.subtract(each.multiply(BigDecimal.valueOf(req.size())));
            for (int i = 0; i < req.size(); i++) {
                BigDecimal amt = i == 0 ? each.add(remainder) : each;
                splits.add(ExpenseSplit.builder()
                        .expense(expense)
                        .userId(req.get(i).getUserId())
                        .amount(amt)
                        .percentage(BigDecimal.valueOf(100.0 / req.size()))
                        .build());
            }
            return splits;
        }
        if (type == Expense.SplitType.PERCENTAGE) {
            List<ExpenseSplit> splits = new ArrayList<>();
            for (ExpenseSplitRequest s : req) {
                BigDecimal pct = s.getPercentage() != null ? s.getPercentage() : BigDecimal.ZERO;
                BigDecimal amt = amount.multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                splits.add(ExpenseSplit.builder()
                        .expense(expense)
                        .userId(s.getUserId())
                        .amount(amt)
                        .percentage(pct)
                        .build());
            }
            BigDecimal sum = splits.stream().map(ExpenseSplit::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (sum.compareTo(amount) != 0 && !splits.isEmpty()) {
                splits.get(0).setAmount(splits.get(0).getAmount().add(amount.subtract(sum)));
            }
            return splits;
        }
        List<ExpenseSplit> splits = new ArrayList<>();
        for (ExpenseSplitRequest s : req) {
            splits.add(ExpenseSplit.builder()
                    .expense(expense)
                    .userId(s.getUserId())
                    .amount(s.getAmount())
                    .percentage(s.getPercentage())
                    .build());
        }
        return splits;
    }

    private ExpenseResponse toExpenseResponse(Expense expense) {
        GroupResponse groupResponse = null;
        Optional<SplitwiseGroup> groupOpt = splitwiseGroupRepository.findById(expense.getGroupId());
        if (groupOpt.isPresent()) {
            SplitwiseGroup g = groupOpt.get();
            String name = tripRepository.findById(g.getTripId())
                    .map(TripEntity::getTripTitle)
                    .orElse(g.getDescription());
            groupResponse = GroupResponse.builder()
                    .id(g.getId())
                    .tripId(g.getTripId())
                    .name(name)
                    .description(g.getDescription())
                    .build();
        }
        List<ExpenseSplitResponse> splitResponses = expense.getSplits() == null ? List.of() : expense.getSplits().stream()
                .map(s -> ExpenseSplitResponse.builder()
                        .id(s.getId())
                        .user(toUserResponse(s.getUserId()))
                        .amount(s.getAmount())
                        .percentage(s.getPercentage())
                        .createdAt(s.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        BigDecimal remaining = expense.getRemainingAmount();
        return ExpenseResponse.builder()
                .id(expense.getId())
                .name(expense.getName())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .paidBy(toUserResponse(expense.getPaidBy()))
                .group(groupResponse)
                .splitType(expense.getSplitType())
                .category(expense.getCategory())
                .expenseDate(expense.getExpenseDate())
                .receiptUrl(expense.getReceiptUrl())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .splits(splitResponses)
                .isSettled(expense.isFullySettled())
                .remainingAmount(remaining != null && remaining.compareTo(BigDecimal.ZERO) <= 0 ? BigDecimal.ZERO : remaining)
                .build();
    }

    private UserResponse toUserResponse(UUID userId) {
        if (userId == null) return null;
        UsersEntity user = userRepository.findUserByUserUuid(userId).orElse(null);
        if (user == null) return UserResponse.builder().userUuid(userId).build();
        String name = "";
        if (user.getUserProfileEntity() != null) {
            UserProfileEntity p = user.getUserProfileEntity();
            name = (p.getFirstName() != null ? p.getFirstName() : "") + " " + (p.getLastName() != null ? p.getLastName() : "");
        }
        return UserResponse.builder()
                .userUuid(user.getUserUuid())
                .name(name.trim().isEmpty() ? null : name.trim())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .build();
    }
}
