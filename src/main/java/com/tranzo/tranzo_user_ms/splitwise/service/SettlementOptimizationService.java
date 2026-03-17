package com.tranzo.tranzo_user_ms.splitwise.service;

import com.tranzo.tranzo_user_ms.splitwise.dto.SettlementProposal;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Service that computes an optimized list of settlements (who pays whom how much)
 * from per-user net balances in a group, minimizing the number of transactions.
 */
@Service
public class SettlementOptimizationService {

    /**
     * Given net balance per user (negative = owes money, positive = is owed),
     * returns a minimal list of settlement proposals (from, to, amount).
     * Uses a greedy approach: largest debtor pays largest creditor first.
     */
    public List<SettlementProposal> optimizeSettlements(Map<UUID, BigDecimal> netBalances) {
        if (netBalances == null || netBalances.isEmpty()) {
            return Collections.emptyList();
        }

        List<SettlementProposal> result = new ArrayList<>();
        // Debtors: users with negative net (they owe)
        PriorityQueue<Map.Entry<UUID, BigDecimal>> debtors = new PriorityQueue<>(
                (a, b) -> b.getValue().compareTo(a.getValue())
        );
        // Creditors: users with positive net (they are owed)
        PriorityQueue<Map.Entry<UUID, BigDecimal>> creditors = new PriorityQueue<>(
                (a, b) -> b.getValue().compareTo(a.getValue())
        );

        for (Map.Entry<UUID, BigDecimal> e : netBalances.entrySet()) {
            BigDecimal net = e.getValue() == null ? BigDecimal.ZERO : e.getValue().setScale(2, RoundingMode.HALF_UP);
            if (net.compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(new AbstractMap.SimpleEntry<>(e.getKey(), net.negate()));
            } else if (net.compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(new AbstractMap.SimpleEntry<>(e.getKey(), net));
            }
        }

        while (!debtors.isEmpty() && !creditors.isEmpty()) {
            Map.Entry<UUID, BigDecimal> debtor = debtors.poll();
            Map.Entry<UUID, BigDecimal> creditor = creditors.poll();
            BigDecimal pay = debtor.getValue().min(creditor.getValue()).setScale(2, RoundingMode.HALF_UP);
            if (pay.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            result.add(new SettlementProposal(debtor.getKey(), creditor.getKey(), pay));

            BigDecimal remainingDebt = debtor.getValue().subtract(pay).setScale(2, RoundingMode.HALF_UP);
            if (remainingDebt.compareTo(BigDecimal.ZERO) > 0) {
                debtors.add(new AbstractMap.SimpleEntry<>(debtor.getKey(), remainingDebt));
            }
            BigDecimal remainingCredit = creditor.getValue().subtract(pay).setScale(2, RoundingMode.HALF_UP);
            if (remainingCredit.compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(new AbstractMap.SimpleEntry<>(creditor.getKey(), remainingCredit));
            }
        }

        return result;
    }
}
