package com.tranzo.tranzo_user_ms.splitwise.service;

import com.tranzo.tranzo_user_ms.splitwise.dto.SettlementProposal;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.BalanceResponse;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.IndividualBalanceResponse;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.UserResponse;
import com.tranzo.tranzo_user_ms.splitwise.entity.*;
import com.tranzo.tranzo_user_ms.splitwise.exception.SplitwiseException;
import com.tranzo.tranzo_user_ms.splitwise.repository.BalanceRepository;
import com.tranzo.tranzo_user_ms.splitwise.repository.ExpenseRepository;
import com.tranzo.tranzo_user_ms.splitwise.repository.SplitwiseGroupRepository;
import com.tranzo.tranzo_user_ms.user.repository.UserRepository;
import com.tranzo.tranzo_user_ms.user.repository.UserProfileRepository;
import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import com.tranzo.tranzo_user_ms.user.model.UserProfileEntity;
import com.tranzo.tranzo_user_ms.splitwise.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing and calculating balances between users.
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

    /**
     * Gets balance summary for all users in a group.
     */
    @Transactional(readOnly = true)
    public List<BalanceResponse> getGroupBalances(Long groupId) {
        log.debug("Calculating balances for group: {}", groupId);

        List<Object[]> balanceData = balanceRepository.getBalanceSummaryForGroup(groupId);

        List<BalanceResponse> responses = balanceData.stream()
                .map(this::convertToBalanceResponse)
                .collect(Collectors.toList());

        log.debug("Calculated balances for {} users in group {}", responses.size(), groupId);
        return responses;
    }

    /**
     * Gets balance summary for a specific user in a group.
     */
    @Transactional(readOnly = true)
    public BalanceResponse getUserBalanceInGroup(Long groupId, UUID userId) {
        log.debug("Calculating balance for user {} in group: {}", userId, groupId);

        BigDecimal totalOwed = balanceRepository.getTotalOwedByUserInGroup(groupId, userId);
        BigDecimal totalOwing = balanceRepository.getTotalOwedToUserInGroup(groupId, userId);
        BigDecimal netBalance = totalOwed.subtract(totalOwing);
        
        // Get individual balances
        List<Balance> userBalances = balanceRepository.findBalancesForUserInGroup(groupId, userId);
        List<IndividualBalanceResponse> balanceDetails = userBalances.stream()
                .map(balance -> convertToIndividualBalance(balance, userId))
                .collect(Collectors.toList());

        BalanceResponse response = BalanceResponse.builder()
                .user(UserResponse.builder().userUuid(userId).build())
                .totalOwed(totalOwed)
                .totalOwing(totalOwing)
                .netBalance(netBalance)
                .balanceDetails(balanceDetails)
                .build();

        log.debug("Calculated balance for user {} in group {}: owed={}, owing={}, net={}", 
                 userId, groupId, totalOwed, totalOwing, netBalance);
        return response;
    }

    /**
     * Updates balances when an expense is added.
     */
    public void updateBalancesForExpense(Expense expense) {
        log.info("Updating balances for expense: {}", expense.getId());

        // Calculate how much each user should pay/receive
        Map<UUID, BigDecimal> netChanges = calculateNetChangesForExpense(expense);

        // Update or create balance records
        for (Map.Entry<UUID, BigDecimal> entry : netChanges.entrySet()) {
            UUID userId = entry.getKey();
            BigDecimal change = entry.getValue();

            if (change.compareTo(BigDecimal.ZERO) > 0) {
                // User should receive money (positive balance) - others owe them
                // Find users who owe money and update their balances to this user
                for (Map.Entry<UUID, BigDecimal> otherEntry : netChanges.entrySet()) {
                    UUID otherUserId = otherEntry.getKey();
                    BigDecimal otherChange = otherEntry.getValue();
                    
                    if (!otherUserId.equals(userId) && otherChange.compareTo(BigDecimal.ZERO) < 0) {
                        // otherUser owes userId money
                        BigDecimal amountToTransfer = change.min(otherChange.abs());
                        if (amountToTransfer.compareTo(BigDecimal.ZERO) > 0) {
                            updateOrCreateBalance(otherUserId, userId, expense.getGroupId(), amountToTransfer);
                        }
                    }
                }
            }
        }

        log.info("Successfully updated balances for expense: {}", expense.getId());
    }

    /**
     * Recalculates all balances for a group from scratch.
     */
    public void recalculateBalancesForGroup(Long groupId) {
        log.info("Recalculating all balances for group: {}", groupId);

        // Delete existing balances
        balanceRepository.deleteByGroupId(groupId);

        // Get all expenses for the group
        List<Expense> expenses = expenseRepository.findByGroupId(groupId);

        // Recalculate balances for each expense
        for (Expense expense : expenses) {
            updateBalancesForExpense(expense);
        }

        log.info("Successfully recalculated balances for group: {}", groupId);
    }

    /**
     * Gets optimized settlement proposals for a group.
     */
    @Transactional(readOnly = true)
    public List<SettlementProposal> getOptimizedSettlements(Long groupId) {
        log.info("Calculating optimized settlements for group: {}", groupId);

        // Get current net balances for all users
        Map<UUID, BigDecimal> netBalances = calculateNetBalancesForGroup(groupId);

        // Use settlement optimization service
        List<SettlementProposal> proposals = settlementOptimizationService.optimizeSettlements(netBalances);

        log.info("Generated {} optimized settlement proposals for group {}", proposals.size(), groupId);
        return proposals;
    }

    /**
     * Updates balances when a settlement is created.
     */
    public void updateBalancesForSettlement(Settlement settlement) {
        log.info("Updating balances for settlement: {}", settlement.getId());
        
        // Reduce the balance between the two users
        updateOrCreateBalance(settlement.getPaidBy(), settlement.getPaidTo(), 
                             settlement.getGroup().getId(), settlement.getAmount().negate());
        
        log.info("Successfully updated balances for settlement: {}", settlement.getId());
    }

    /**
     * Reverses balance updates when a settlement is deleted.
     */
    public void reverseBalancesForSettlement(Settlement settlement) {
        log.info("Reversing balances for settlement: {}", settlement.getId());
        
        // Add back the balance that was settled
        updateOrCreateBalance(settlement.getPaidBy(), settlement.getPaidTo(), 
                             settlement.getGroup().getId(), settlement.getAmount());
        
        log.info("Successfully reversed balances for settlement: {}", settlement.getId());
    }

    /**
     * Calculates net balance changes for an expense.
     */
    private Map<UUID, BigDecimal> calculateNetChangesForExpense(Expense expense) {
        Map<UUID, BigDecimal> netChanges = new HashMap<>();

        // Payer paid the full amount (positive for them)
        netChanges.put(expense.getPaidBy(), expense.getAmount());

        // Each split user owes their split amount (negative for them)
        for (ExpenseSplit split : expense.getSplits()) {
            UUID userId = split.getUserId();
            BigDecimal currentChange = netChanges.getOrDefault(userId, BigDecimal.ZERO);
            netChanges.put(userId, currentChange.subtract(split.getAmount()));
        }

        return netChanges;
    }

    /**
     * Calculates net balances for all users in a group.
     */
    private Map<UUID, BigDecimal> calculateNetBalancesForGroup(Long groupId) {
        Map<UUID, BigDecimal> netBalances = new HashMap<>();

        List<Object[]> balanceData = balanceRepository.getBalanceSummaryForGroup(groupId);
        for (Object[] data : balanceData) {
            UUID userId = (UUID) data[0];
            BigDecimal netBalance = (BigDecimal) data[4]; // Net balance is at index 4
            netBalances.put(userId, netBalance);
        }

        return netBalances;
    }

    /**
     * Updates or creates a balance record between two users.
     */
    private void updateOrCreateBalance(UUID fromUserId, UUID toUserId, Long groupId, BigDecimal amount) {
        log.debug("Updating balance: {} -> {} amount {} in group {}", fromUserId, toUserId, amount, groupId);
        
        // Find existing balance
        Optional<Balance> existingBalance = balanceRepository.findByGroupIdAndOwedByAndOwedTo(groupId, fromUserId, toUserId);
        
        if (existingBalance.isPresent()) {
            // Update existing balance
            Balance balance = existingBalance.get();
            balance.addAmount(amount);
            
            // Remove balance if amount becomes zero or negative
            if (balance.shouldDelete()) {
                balanceRepository.delete(balance);
                log.debug("Deleted zero/negative balance between {} and {}", fromUserId, toUserId);
            } else {
                balanceRepository.save(balance);
                log.debug("Updated balance between {} and {} to {}", fromUserId, toUserId, balance.getAmount());
            }
        } else {
            // Check if reverse balance exists
            Optional<Balance> reverseBalance = balanceRepository.findByGroupIdAndOwedByAndOwedTo(groupId, toUserId, fromUserId);
            
            if (reverseBalance.isPresent()) {
                // Update reverse balance (subtract amount)
                Balance reverse = reverseBalance.get();
                reverse.subtractAmount(amount);
                
                // Remove if becomes zero or negative
                if (reverse.shouldDelete()) {
                    balanceRepository.delete(reverse);
                    log.debug("Deleted reverse balance between {} and {}", toUserId, fromUserId);
                } else {
                    balanceRepository.save(reverse);
                    log.debug("Updated reverse balance between {} and {} to {}", toUserId, fromUserId, reverse.getAmount());
                }
            } else if (amount.compareTo(BigDecimal.ZERO) > 0) {
                // Create new balance only if amount is positive
                SplitwiseGroup group = splitwiseGroupRepository.findById(groupId)
                    .orElseThrow(() -> new SplitwiseException("Group not found: " + groupId));
                
                Balance newBalance = Balance.builder()
                    .group(group)
                    .owedBy(fromUserId)
                    .owedTo(toUserId)
                    .amount(amount)
                    .build();
                
                balanceRepository.save(newBalance);
                log.debug("Created new balance between {} and {} with amount {}", fromUserId, toUserId, amount);
            }
        }
    }

    /**
     * Converts balance data array to BalanceResponse.
     */
    private BalanceResponse convertToBalanceResponse(Object[] data) {
        UUID userId = (UUID) data[0];
        String userName = (String) data[1];
        String userEmail = (String) data[2];
        BigDecimal totalOwed = (BigDecimal) data[3];
        BigDecimal totalOwing = (BigDecimal) data[4];
        BigDecimal netBalance = (BigDecimal) data[5];

        return BalanceResponse.builder()
                .user(UserResponse.builder()
                        .userUuid(userId)
                        .name(userName)
                        .email(userEmail)
                        .build())
                .totalOwed(totalOwed)
                .totalOwing(totalOwing)
                .netBalance(netBalance)
                .build();
    }

    /**
     * Converts Balance entity to IndividualBalanceResponse.
     */
    private IndividualBalanceResponse convertToIndividualBalance(Balance balance, UUID currentUserId) {
        String type = balance.getOwedBy().equals(currentUserId) ? 
                     "OWING" : "OWED";

        UUID otherUserId = balance.getOwedBy().equals(currentUserId) ? 
                          balance.getOwedTo() : balance.getOwedBy();
        
        UsersEntity otherUser = userRepository.findUserByUserUuid(otherUserId)
                .orElseThrow(() -> new SplitwiseException("User not found: " + otherUserId));
        
        UserProfileEntity userProfile = userProfileRepository.findAllUserProfileDetailByUserId(otherUserId)
                .orElse(null);

        return IndividualBalanceResponse.builder()
                .otherUser(UserMapper.toResponse(otherUser, userProfile))
                .amount(balance.getAmount())
                .type(type)
                .build();
    }

    /**
     * Validates that a settlement amount is valid for the given users.
     */
    public void validateSettlementAmount(Long groupId, UUID fromUserId, UUID toUserId, BigDecimal amount) {
        log.debug("Validating settlement amount: {} from user {} to user {} in group {}", 
                 amount, fromUserId, toUserId, groupId);

        // Get current balance between these users
        BigDecimal currentBalance = balanceRepository.getNetBalanceForUserInGroup(groupId, fromUserId);
        
        // If fromUserId has negative balance (owes money), they can pay
        // If fromUserId has positive balance (is owed money), they can only pay if they have enough
        if (currentBalance.compareTo(BigDecimal.ZERO) >= 0) {
            // User is owed money or has zero balance
            // They can pay up to their available balance
            if (amount.compareTo(currentBalance) > 0) {
                throw new SplitwiseException(
                    String.format("User %d can only pay up to %.2f, but requested %.2f", 
                            fromUserId, currentBalance, amount));
            }
        }

        log.debug("Settlement amount validation passed");
    }
}
