package com.tranzo.tranzo_user_ms.splitwise.service;

import com.tranzo.tranzo_user_ms.splitwise.dto.SettlementProposal;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.BalanceResponse;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.IndividualBalanceResponse;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.UserResponse;
import com.tranzo.tranzo_user_ms.splitwise.entity.Balance;
import com.tranzo.tranzo_user_ms.splitwise.entity.Expense;
import com.tranzo.tranzo_user_ms.splitwise.entity.ExpenseSplit;
import com.tranzo.tranzo_user_ms.splitwise.entity.Settlement;
import com.tranzo.tranzo_user_ms.splitwise.exception.InsufficientBalanceException;
import com.tranzo.tranzo_user_ms.splitwise.repository.BalanceRepository;
import com.tranzo.tranzo_user_ms.splitwise.repository.ExpenseRepository;
import com.tranzo.tranzo_user_ms.splitwise.repository.SplitwiseGroupRepository;
import com.tranzo.tranzo_user_ms.user.model.UserProfileEntity;
import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import com.tranzo.tranzo_user_ms.user.repository.UserProfileRepository;
import com.tranzo.tranzo_user_ms.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing and calculating balances between users in Splitwise groups.
 */
@Slf4j
@Service
@Transactional
public class BalanceService {

    private final BalanceRepository balanceRepository;
    private final ExpenseRepository expenseRepository;
    private final SettlementOptimizationService settlementOptimizationService;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final SplitwiseGroupRepository splitwiseGroupRepository;

    public BalanceService(BalanceRepository balanceRepository,
                          ExpenseRepository expenseRepository,
                          SettlementOptimizationService settlementOptimizationService,
                          UserRepository userRepository,
                          UserProfileRepository userProfileRepository,
                          SplitwiseGroupRepository splitwiseGroupRepository) {
        this.balanceRepository = balanceRepository;
        this.expenseRepository = expenseRepository;
        this.settlementOptimizationService = settlementOptimizationService;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.splitwiseGroupRepository = splitwiseGroupRepository;
    }

    @Transactional(readOnly = true)
    public List<BalanceResponse> getGroupBalances(UUID groupId) {
        log.debug("Calculating balances for group: {}", groupId);
        List<Object[]> rows = balanceRepository.getBalanceSummaryForGroup(groupId);
        List<BalanceResponse> result = new ArrayList<>();
        for (Object[] row : rows) {
            if (row.length >= 4 && row[0] != null) {
                UUID userId = (UUID) row[0];
                BigDecimal totalOwedTo = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
                BigDecimal totalOwedBy = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;
                BigDecimal netBalance = row[3] != null ? (BigDecimal) row[3] : totalOwedTo.subtract(totalOwedBy);
                UserResponse userResponse = toUserResponse(userId);
                result.add(BalanceResponse.builder()
                        .user(userResponse)
                        .totalOwed(totalOwedBy)
                        .totalOwing(totalOwedTo)
                        .netBalance(netBalance)
                        .individualBalances(null)
                        .balanceDetails(null)
                        .build());
            }
        }
        result.sort((a, b) -> b.getNetBalance().compareTo(a.getNetBalance()));
        log.debug("Calculated balances for {} users in group {}", result.size(), groupId);
        return result;
    }

    @Transactional(readOnly = true)
    public BalanceResponse getUserBalanceInGroup(UUID groupId, UUID userId) {
        log.debug("Calculating balance for user {} in group: {}", userId, groupId);
        BigDecimal totalOwed = balanceRepository.getTotalOwedByUserInGroup(groupId, userId);
        BigDecimal totalOwing = balanceRepository.getTotalOwedToUserInGroup(groupId, userId);
        BigDecimal netBalance = totalOwing.subtract(totalOwed);
        List<Balance> balances = balanceRepository.findBalancesForUserInGroup(groupId, userId);
        List<IndividualBalanceResponse> details = new ArrayList<>();
        for (Balance b : balances) {
            UUID otherId = b.getOwedBy().equals(userId) ? b.getOwedTo() : b.getOwedBy();
            BigDecimal amount = b.getAmount();
            String type = b.getOwedBy().equals(userId) ? "OWING" : "OWED";
            UserResponse other = toUserResponse(otherId);
            details.add(IndividualBalanceResponse.builder()
                    .otherUser(other)
                    .amount(amount)
                    .type(type)
                    .build());
        }
        return BalanceResponse.builder()
                .user(toUserResponse(userId))
                .totalOwed(totalOwed)
                .totalOwing(totalOwing)
                .netBalance(netBalance)
                .individualBalances(null)
                .balanceDetails(details)
                .build();
    }

    public void updateBalancesForExpense(Expense expense) {
        UUID groupId = expense.getGroupId();
        UUID payer = expense.getPaidBy();
        for (ExpenseSplit split : expense.getSplits()) {
            UUID debtor = split.getUserId();
            if (payer.equals(debtor)) continue;
            BigDecimal amount = split.getAmount();
            updateOrCreateBalance(debtor, payer, groupId, amount);
        }
    }

    public void recalculateBalancesForGroup(UUID groupId) {
        log.debug("Recalculating balances for group: {}", groupId);
        balanceRepository.deleteByGroupId(groupId);
        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        for (Expense expense : expenses) {
            updateBalancesForExpense(expense);
        }
    }

    private void updateOrCreateBalance(UUID fromUserId, UUID toUserId, UUID groupId, BigDecimal amount) {
        if (fromUserId.equals(toUserId) || amount.compareTo(BigDecimal.ZERO) <= 0) return;
        Optional<Balance> opt = balanceRepository.findByGroupIdAndOwedByAndOwedTo(groupId, fromUserId, toUserId);
        if (opt.isPresent()) {
            Balance b = opt.get();
            b.addAmount(amount);
            if (b.shouldDelete()) {
                balanceRepository.delete(b);
            } else {
                balanceRepository.save(b);
            }
        } else {
            Optional<Balance> reverseOpt = balanceRepository.findByGroupIdAndOwedByAndOwedTo(groupId, toUserId, fromUserId);
            if (reverseOpt.isPresent()) {
                Balance reverse = reverseOpt.get();
                reverse.subtractAmount(amount);
                if (reverse.shouldDelete()) {
                    balanceRepository.delete(reverse);
                } else {
                    balanceRepository.save(reverse);
                }
            } else {
                var group = splitwiseGroupRepository.findById(groupId).orElseThrow();
                Balance newBalance = Balance.builder()
                        .group(group)
                        .owedBy(fromUserId)
                        .owedTo(toUserId)
                        .amount(amount)
                        .build();
                balanceRepository.save(newBalance);
            }
        }
    }

    public void validateSettlementAmount(UUID groupId, UUID fromUserId, UUID toUserId, BigDecimal amount) {
        Optional<Balance> opt = balanceRepository.findByGroupIdAndOwedByAndOwedTo(groupId, fromUserId, toUserId);
        BigDecimal available = opt.map(Balance::getAmount).orElse(BigDecimal.ZERO);
        if (available.compareTo(amount) < 0) {
            throw new InsufficientBalanceException(available, amount);
        }
    }

    public void updateBalancesForSettlement(Settlement settlement) {
        UUID groupId = settlement.getGroup().getId();
        UUID paidBy = settlement.getPaidBy();
        UUID paidTo = settlement.getPaidTo();
        BigDecimal amount = settlement.getAmount();
        Optional<Balance> opt = balanceRepository.findByGroupIdAndOwedByAndOwedTo(groupId, paidBy, paidTo);
        if (opt.isPresent()) {
            Balance b = opt.get();
            b.subtractAmount(amount);
            if (b.shouldDelete()) {
                balanceRepository.delete(b);
            } else {
                balanceRepository.save(b);
            }
        } else {
            Optional<Balance> reverseOpt = balanceRepository.findByGroupIdAndOwedByAndOwedTo(groupId, paidTo, paidBy);
            if (reverseOpt.isPresent()) {
                Balance reverse = reverseOpt.get();
                reverse.addAmount(amount);
                balanceRepository.save(reverse);
            } else {
                Balance newBalance = Balance.builder()
                        .group(settlement.getGroup())
                        .owedBy(paidTo)
                        .owedTo(paidBy)
                        .amount(amount)
                        .build();
                balanceRepository.save(newBalance);
            }
        }
    }

    public void reverseBalancesForSettlement(Settlement settlement) {
        UUID groupId = settlement.getGroup().getId();
        UUID paidBy = settlement.getPaidBy();
        UUID paidTo = settlement.getPaidTo();
        BigDecimal amount = settlement.getAmount();
        Optional<Balance> opt = balanceRepository.findByGroupIdAndOwedByAndOwedTo(groupId, paidBy, paidTo);
        if (opt.isPresent()) {
            Balance b = opt.get();
            b.addAmount(amount);
            balanceRepository.save(b);
        } else {
            Optional<Balance> reverseOpt = balanceRepository.findByGroupIdAndOwedByAndOwedTo(groupId, paidTo, paidBy);
            if (reverseOpt.isPresent()) {
                Balance reverse = reverseOpt.get();
                reverse.subtractAmount(amount);
                if (reverse.shouldDelete()) {
                    balanceRepository.delete(reverse);
                } else {
                    balanceRepository.save(reverse);
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public List<SettlementProposal> getOptimizedSettlements(UUID groupId) {
        Map<UUID, BigDecimal> netBalances = calculateNetBalancesForGroup(groupId);
        return settlementOptimizationService.optimizeSettlements(netBalances);
    }

    private Map<UUID, BigDecimal> calculateNetBalancesForGroup(UUID groupId) {
        List<Object[]> rows = balanceRepository.getBalanceSummaryForGroup(groupId);
        Map<UUID, BigDecimal> net = new HashMap<>();
        for (Object[] row : rows) {
            if (row.length >= 4 && row[0] != null && row[3] != null) {
                net.put((UUID) row[0], (BigDecimal) row[3]);
            }
        }
        return net;
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
