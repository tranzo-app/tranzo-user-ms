package com.tranzo.tranzo_user_ms.splitwise.service;

import com.tranzo.tranzo_user_ms.splitwise.dto.SettlementProposal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for optimizing expense settlements using enhanced greedy algorithm with heaps.
 * This service calculates the minimum number of transactions required to settle all balances
 * within a group, considering selective participation in expenses.
 */
@Slf4j
@Service
public class SettlementOptimizationService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal MIN_SETTLEMENT_AMOUNT = new BigDecimal("0.01");

    /**
     * Optimizes settlements for a group using enhanced greedy algorithm.
     * 
     * @param netBalances Map of user IDs to their net balances (positive = should receive, negative = should pay)
     * @return List of optimized settlement proposals
     */
    public List<SettlementProposal> optimizeSettlements(Map<UUID, BigDecimal> netBalances) {
        log.info("Starting settlement optimization for {} users", netBalances.size());
        
        // Validate input
        if (netBalances == null || netBalances.isEmpty()) {
            log.warn("No balances provided for optimization");
            return Collections.emptyList();
        }

        // Log initial state
        logInitialBalances(netBalances);

        // Step 1: Apply heap-based greedy algorithm
        List<SettlementProposal> basicSettlements = greedyWithHeaps(netBalances);
        log.info("Generated {} basic settlements", basicSettlements.size());

        // Step 2: Apply local optimization
        List<SettlementProposal> optimizedSettlements = optimizeLocally(basicSettlements);
        log.info("Optimized to {} final settlements", optimizedSettlements.size());

        // Log final result
        logFinalSettlements(optimizedSettlements);

        return optimizedSettlements;
    }

    /**
     * Core greedy algorithm using min-max heaps for optimal pairing.
     */
    private List<SettlementProposal> greedyWithHeaps(Map<UUID, BigDecimal> netBalances) {
        log.debug("Starting heap-based greedy algorithm");

        // Create max-heap for creditors (users who should receive money)
        PriorityQueue<UserBalance> creditors = new PriorityQueue<>(
            (a, b) -> b.getAmount().compareTo(a.getAmount()));

        // Create min-heap for debtors (users who should pay money)
        PriorityQueue<UserBalance> debtors = new PriorityQueue<>(
            Comparator.comparing(UserBalance::getAmount));

        // Populate heaps with users who have non-zero balances
        netBalances.forEach((userId, balance) -> {
            BigDecimal scaledBalance = balance.setScale(2, RoundingMode.HALF_UP);
            
            if (scaledBalance.compareTo(ZERO) > 0) {
                creditors.offer(new UserBalance(userId, scaledBalance));
                log.debug("Added creditor: User {} with amount {}", userId, scaledBalance);
            } else if (scaledBalance.compareTo(ZERO) < 0) {
                debtors.offer(new UserBalance(userId, scaledBalance.abs()));
                log.debug("Added debtor: User {} with amount {}", userId, scaledBalance.abs());
            }
        });

        log.info("Initialized heaps with {} creditors and {} debtors", 
                creditors.size(), debtors.size());

        List<SettlementProposal> settlements = new ArrayList<>();
        int settlementCount = 0;

        // Main greedy matching loop
        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            settlementCount++;
            UserBalance creditor = creditors.poll();
            UserBalance debtor = debtors.poll();

            // Calculate settlement amount (minimum of creditor's claim and debtor's debt)
            BigDecimal settlementAmount = creditor.getAmount().min(debtor.getAmount());
            
            log.debug("Settlement {}: User {} pays User {} amount {}", 
                     settlementCount, debtor.getUserId(), creditor.getUserId(), settlementAmount);

            // Create settlement proposal
            settlements.add(new SettlementProposal(
                debtor.getUserId(),
                creditor.getUserId(),
                settlementAmount
            ));

            // Update creditor balance and re-insert if needed
            BigDecimal creditorRemaining = creditor.getAmount().subtract(settlementAmount);
            if (creditorRemaining.compareTo(MIN_SETTLEMENT_AMOUNT) >= 0) {
                creditor.setAmount(creditorRemaining);
                creditors.offer(creditor);
                log.debug("Creditor {} still needs to receive {}", creditor.getUserId(), creditorRemaining);
            } else {
                log.debug("Creditor {} fully settled", creditor.getUserId());
            }

            // Update debtor balance and re-insert if needed
            BigDecimal debtorRemaining = debtor.getAmount().subtract(settlementAmount);
            if (debtorRemaining.compareTo(MIN_SETTLEMENT_AMOUNT) >= 0) {
                debtor.setAmount(debtorRemaining);
                debtors.offer(debtor);
                log.debug("Debtor {} still needs to pay {}", debtor.getUserId(), debtorRemaining);
            } else {
                log.debug("Debtor {} fully settled", debtor.getUserId());
            }
        }

        log.info("Heap-based greedy algorithm completed with {} settlements", settlements.size());
        return settlements;
    }

    /**
     * Local optimization layer to clean up and optimize the basic settlements.
     */
    private List<SettlementProposal> optimizeLocally(List<SettlementProposal> settlements) {
        log.debug("Starting local optimization of {} settlements", settlements.size());

        // Step 1: Merge settlements between same user pairs
        Map<String, List<SettlementProposal>> groupedSettlements = settlements.stream()
            .collect(Collectors.groupingBy(s -> generateSettlementKey(s.getFrom(), s.getTo())));

        log.debug("Grouped settlements into {} unique user pairs", groupedSettlements.size());

        // Step 2: Merge and filter settlements
        List<SettlementProposal> mergedSettlements = groupedSettlements.values().stream()
            .map(this::mergeSettlements)
            .filter(s -> s.getAmount().compareTo(MIN_SETTLEMENT_AMOUNT) >= 0)
            .collect(Collectors.toList());

        log.debug("After merging and filtering: {} settlements remain", mergedSettlements.size());

        // Step 3: Sort by amount (largest first for better user experience)
        mergedSettlements.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));

        log.debug("Local optimization completed");
        return mergedSettlements;
    }

    /**
     * Merges multiple settlements between the same user pair into a single settlement.
     */
    private SettlementProposal mergeSettlements(List<SettlementProposal> settlements) {
        if (settlements.size() == 1) {
            return settlements.get(0);
        }

        UUID fromUserId = settlements.get(0).getFrom();
        UUID toUserId = settlements.get(0).getTo();
        
        BigDecimal totalAmount = settlements.stream()
            .map(SettlementProposal::getAmount)
            .reduce(ZERO, BigDecimal::add);

        BigDecimal scaledTotal = totalAmount.setScale(2, RoundingMode.HALF_UP);

        log.debug("Merged {} settlements from User {} to User {} into single settlement of amount {}", 
                 settlements.size(), fromUserId, toUserId, scaledTotal);

        return new SettlementProposal(fromUserId, toUserId, scaledTotal);
    }

    /**
     * Generates a unique key for settlements between two users.
     */
    private String generateSettlementKey(UUID fromUserId, UUID toUserId) {
        return fromUserId + "->" + toUserId;
    }

    /**
     * Logs the initial balance state for debugging.
     */
    private void logInitialBalances(Map<UUID, BigDecimal> netBalances) {
        if (log.isDebugEnabled()) {
            log.debug("Initial balances:");
            netBalances.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(entry -> {
                    String type = entry.getValue().compareTo(ZERO) >= 0 ? "RECEIVE" : "PAY";
                    log.debug("  User {}: {} {}", entry.getKey(), type, entry.getValue().abs());
                });
        }
    }

    /**
     * Logs the final settlement proposals for debugging.
     */
    private void logFinalSettlements(List<SettlementProposal> settlements) {
        if (log.isInfoEnabled()) {
            log.info("Final optimized settlements:");
            settlements.forEach(settlement -> 
                log.info("  User {} pays User {} amount {}", 
                        settlement.getFrom(), settlement.getTo(), settlement.getAmount()));
            
            BigDecimal totalAmount = settlements.stream()
                .map(SettlementProposal::getAmount)
                .reduce(ZERO, BigDecimal::add);
            
            log.info("Total settlement amount: {} across {} transactions", totalAmount, settlements.size());
        }
    }

    /**
     * Inner class to represent user balance information.
     */
    private static class UserBalance {
        private final UUID userId;
        private BigDecimal amount;

        public UserBalance(UUID userId, BigDecimal amount) {
            this.userId = userId;
            this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        }

        public UUID getUserId() {
            return userId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        }
    }
}
