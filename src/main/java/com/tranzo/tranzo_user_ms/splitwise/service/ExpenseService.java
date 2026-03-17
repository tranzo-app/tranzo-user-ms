package com.tranzo.tranzo_user_ms.splitwise.service;

import com.tranzo.tranzo_user_ms.splitwise.dto.request.CreateExpenseRequest;
import com.tranzo.tranzo_user_ms.splitwise.dto.request.ExpenseSplitRequest;
import com.tranzo.tranzo_user_ms.splitwise.dto.request.UpdateExpenseRequest;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.ExpenseResponse;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.ExpenseSplitResponse;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.GroupResponse;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.UserResponse;
import com.tranzo.tranzo_user_ms.splitwise.entity.*;
import com.tranzo.tranzo_user_ms.splitwise.exception.*;
import com.tranzo.tranzo_user_ms.splitwise.mapper.UserMapper;
import com.tranzo.tranzo_user_ms.splitwise.repository.ExpenseRepository;
import com.tranzo.tranzo_user_ms.splitwise.repository.SplitwiseGroupRepository;
import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import com.tranzo.tranzo_user_ms.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing expenses and their splits.
 */
@Slf4j
@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final BalanceService balanceService;
    private final ActivityService activityService;
    private final SplitwiseGroupRepository splitwiseGroupRepository;

    public ExpenseService(ExpenseRepository expenseRepository,
                        UserRepository userRepository,
                        BalanceService balanceService,
                        ActivityService activityService,
                        SplitwiseGroupRepository splitwiseGroupRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.balanceService = balanceService;
        this.activityService = activityService;
        this.splitwiseGroupRepository = splitwiseGroupRepository;
    }

    /**
     * Creates a new expense with proper validation and balance updates.
     */
    public ExpenseResponse createExpense(CreateExpenseRequest request, UUID currentUserId) {
        UUID groupId = request.getGroupId();
        log.info("Creating expense '{}' for trip/group {} by user {}", request.getName(), groupId, currentUserId);

        SplitwiseGroup group = splitwiseGroupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));

        // Validate users exist
        UsersEntity paidBy = userRepository.findById(request.getPaidById())
                .orElseThrow(() -> new SplitwiseException("Paid by user not found: " + request.getPaidById()));

        // Validate splits
        validateExpenseSplits(request);

        // Create expense entity (store group's Long id for FK)
        Expense expense = Expense.builder()
                .name(request.getName())
                .description(request.getDescription())
                .amount(request.getAmount())
                .groupId(group.getId())
                .paidBy(paidBy.getUserUuid())
                .splitType(request.getSplitType())
                .category(request.getCategory())
                .receiptUrl(request.getReceiptUrl())
                .expenseDate(request.getExpenseDate() != null
                        ? request.getExpenseDate().atStartOfDay()
                        : LocalDateTime.now())
                .build();

        // Create expense splits
        for (ExpenseSplitRequest splitRequest : request.getSplits()) {
            UsersEntity splitUser = userRepository.findById(splitRequest.getUserId())
                    .orElseThrow(() -> new SplitwiseException("Split user not found: " + splitRequest.getUserId()));

            ExpenseSplit split = ExpenseSplit.builder()
                    .userId(splitUser.getUserUuid())
                    .amount(splitRequest.getAmount())
                    .percentage(splitRequest.getPercentage())
                    .build();
            
            expense.addSplit(split);
        }

        // Validate total splits match expense amount
        if (!expense.hasValidSplits()) {
            throw new InvalidSplitException("Split amounts do not equal total expense amount");
        }

        Expense savedExpense = expenseRepository.save(expense);

        // Update balances
        balanceService.updateBalancesForExpense(savedExpense);

        // Log activity (group already resolved above)
        activityService.logExpenseCreated(paidBy.getUserUuid(), group, savedExpense.getId(),
                savedExpense.getName(), savedExpense.getAmount());

        log.info("Successfully created expense '{}' with ID: {}", savedExpense.getName(), savedExpense.getId());
        return convertToExpenseResponse(savedExpense);
    }

    /**
     * Gets an expense by ID with authorization check.
     */
    @Transactional(readOnly = true)
    public ExpenseResponse getExpense(UUID expenseId, UUID currentUserId) {
        log.debug("Fetching expense {} for user {}", expenseId, currentUserId);

        final Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ExpenseNotFoundException(expenseId));

        // Check if user is involved in this expense
        if (!expense.isUserInvolved(currentUserId)) {
            throw new UserNotMemberException(currentUserId, expense.getGroupId());
        }

        log.debug("Successfully retrieved expense: {}", expense.getName());
        return convertToExpenseResponse(expense);
    }

    /**
     * Updates an existing expense.
     */
    public ExpenseResponse updateExpense(UUID expenseId, UpdateExpenseRequest request, UUID currentUserId) {
        log.info("Updating expense {} by user {}", expenseId, currentUserId);

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ExpenseNotFoundException(expenseId));

        // Check if user can edit this expense (paid by user or group admin)
        if (!expense.getPaidBy().equals(currentUserId)) {
            // TODO: Add admin check if needed
            throw new SplitwiseException("Only the expense creator can edit this expense");
        }

        // Store old values for activity logging
        String oldName = expense.getName();
        BigDecimal oldAmount = expense.getAmount();

        // Update expense fields
        if (request.getName() != null) {
            expense.setName(request.getName());
        }
        if (request.getDescription() != null) {
            expense.setDescription(request.getDescription());
        }
        if (request.getAmount() != null) {
            expense.setAmount(request.getAmount());
        }
        if (request.getCategory() != null) {
            expense.setCategory(request.getCategory());
        }
        if (request.getReceiptUrl() != null) {
            expense.setReceiptUrl(request.getReceiptUrl());
        }

        // Update splits if provided
        if (request.getSplits() != null && !request.getSplits().isEmpty()) {
            // Clear existing splits
            expense.getSplits().clear();
            
            // Add new splits
            for (ExpenseSplitRequest splitRequest : request.getSplits()) {
                UsersEntity splitUser = userRepository.findById(splitRequest.getUserId())
                        .orElseThrow(() -> new SplitwiseException("Split user not found: " + splitRequest.getUserId()));

                ExpenseSplit split = ExpenseSplit.builder()
                        .userId(splitUser.getUserUuid())
                        .amount(splitRequest.getAmount())
                        .percentage(splitRequest.getPercentage())
                        .build();
                
                expense.addSplit(split);
            }

            // Validate updated splits
            if (!expense.hasValidSplits()) {
                throw new InvalidSplitException("Updated split amounts do not equal total expense amount");
            }
        }

        Expense savedExpense = expenseRepository.save(expense);

        // Update balances (recalculate)
        SplitwiseGroup group = splitwiseGroupRepository.findById(savedExpense.getGroupId())
                .orElseThrow(() -> new GroupNotFoundException("Group not found with ID: " + savedExpense.getGroupId()));
        balanceService.recalculateBalancesForGroup(group.getId());

        // Log activity
        activityService.logExpenseUpdated(savedExpense.getPaidBy(), group, expenseId, savedExpense.getName());

        log.info("Successfully updated expense: {}", expenseId);
        return convertToExpenseResponse(savedExpense);
    }

    /**
     * Deletes an expense and updates balances.
     */
    public void deleteExpense(UUID expenseId, UUID currentUserId) {
        log.info("Deleting expense {} by user {}", expenseId, currentUserId);

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ExpenseNotFoundException(expenseId));

        // Check if user can delete this expense
        if (!expense.getPaidBy().equals(currentUserId)) {
            // TODO: Add admin check if needed
            throw new SplitwiseException("Only expense creator can delete this expense");
        }

        // Delete expense (this will cascade delete splits)
        expenseRepository.delete(expense);

        // Recalculate balances for the group
        SplitwiseGroup group = splitwiseGroupRepository.findById(expense.getGroupId())
                .orElseThrow(() -> new GroupNotFoundException("Group not found with ID: " + expense.getGroupId()));
        balanceService.recalculateBalancesForGroup(group.getId());

        // Log activity
        activityService.logExpenseDeleted(expense.getPaidBy(), group, expenseId, expense.getName());

        log.info("Successfully deleted expense: {}", expenseId);
    }

    /**
     * Gets all expenses for a group.
     */
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getGroupExpenses(UUID groupId, UUID currentUserId) {
        log.debug("Fetching expenses for group {} by user {}", groupId, currentUserId);

        // Verify user is member of the group
        if (!splitwiseGroupRepository.isUserMemberOfGroup(groupId, currentUserId)) {
            throw new UserNotMemberException(currentUserId, groupId);
        }

        List<Expense> expenses = expenseRepository.findByGroupId(groupId);

        log.debug("Found {} expenses for group {}", expenses.size(), groupId);
        return expenses.stream()
                .map(this::convertToExpenseResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets expenses involving a specific user.
     */
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getUserExpenses(UUID userId) {
        log.debug("Fetching expenses for user: {}", userId);

        List<Expense> expenses = expenseRepository.findExpensesInvolvingUser(userId);

        log.debug("Found {} expenses for user {}", expenses.size(), userId);
        return expenses.stream()
                .map(this::convertToExpenseResponse)
                .collect(Collectors.toList());
    }

    /**
     * Validates expense splits based on split type.
     */
    private void validateExpenseSplits(CreateExpenseRequest request) {
        if (request.getSplits() == null || request.getSplits().isEmpty()) {
            throw new InvalidSplitException("At least one split is required");
        }

        BigDecimal totalSplitAmount = request.getSplits().stream()
                .map(ExpenseSplitRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalSplitAmount.compareTo(request.getAmount()) != 0) {
            throw new InvalidSplitException("Split amounts must equal total expense amount");
        }

        // Additional validation based on split type
        switch (request.getSplitType()) {
            case EQUAL:
                validateEqualSplits(request);
                break;
            case PERCENTAGE:
                validatePercentageSplits(request);
                break;
            case UNEQUAL:
                // Unequal splits are already validated by amount check above
                break;
        }
    }

    /**
     * Validates equal splits - all amounts should be the same.
     */
    private void validateEqualSplits(CreateExpenseRequest request) {
        BigDecimal firstAmount = request.getSplits().get(0).getAmount();
        for (ExpenseSplitRequest split : request.getSplits()) {
            if (split.getAmount().compareTo(firstAmount) != 0) {
                throw new InvalidSplitException("Equal splits must have the same amount for all users");
            }
        }
    }

    /**
     * Validates percentage splits - percentages should sum to 100.
     */
    private void validatePercentageSplits(CreateExpenseRequest request) {
        BigDecimal totalPercentage = request.getSplits().stream()
                .map(ExpenseSplitRequest::getPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPercentage.compareTo(new BigDecimal("100")) != 0) {
            throw new InvalidSplitException("Percentage splits must sum to 100%");
        }
    }

    /**
     * Converts Expense entity to ExpenseResponse DTO.
     */
    private ExpenseResponse convertToExpenseResponse(Expense expense) {
        List<ExpenseSplitResponse> splitResponses = expense.getSplits().stream()
                .map(split -> {
                    UsersEntity user = userRepository.findById(split.getUserId())
                            .orElse(null);
                    return ExpenseSplitResponse.builder()
                            .id(split.getId())
                            .user(user != null ? UserResponse.builder()
                                    .userUuid(user.getUserUuid())
                                    .name(user.getUserProfileEntity() != null ? 
                                        (user.getUserProfileEntity().getFirstName() + " " + 
                                         (user.getUserProfileEntity().getLastName() != null ? user.getUserProfileEntity().getLastName() : "")).trim() 
                                        : "Unknown")
                                    .email(user.getEmail())
                                    .build() : null)
                            .amount(split.getAmount())
                            .percentage(split.getPercentage())
                            .createdAt(split.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        SplitwiseGroup group = splitwiseGroupRepository.findById(expense.getGroupId()).orElse(null);

        return ExpenseResponse.builder()
                .id(expense.getId())
                .name(expense.getName())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .paidBy(userRepository.findById(expense.getPaidBy())
                        .map(UserMapper::toResponse)
                        .orElse(null))
                .group(GroupResponse.builder()
                        .id(group != null ? group.getId() : null)
                        .tripId(group != null ? group.getTripId() : null)
                        .name(group != null ? group.getName() : null)
                        .build())
                .splitType(expense.getSplitType())
                .category(expense.getCategory())
                .expenseDate(expense.getExpenseDate())
                .receiptUrl(expense.getReceiptUrl())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .splits(splitResponses)
                .isSettled(expense.isFullySettled())
                .remainingAmount(expense.getRemainingAmount())
                .build();
    }
}
