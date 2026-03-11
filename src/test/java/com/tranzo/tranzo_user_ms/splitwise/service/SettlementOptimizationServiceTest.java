package com.tranzo.tranzo_user_ms.splitwise.service;

import com.tranzo.tranzo_user_ms.splitwise.dto.SettlementProposal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementOptimizationService Unit Tests")
class SettlementOptimizationServiceTest {

    @InjectMocks
    private SettlementOptimizationService settlementOptimizationService;

    @Test
    @DisplayName("Should return empty list when netBalances is null")
    void optimizeSettlements_NullBalances() {
        List<SettlementProposal> result = settlementOptimizationService.optimizeSettlements(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when netBalances is empty")
    void optimizeSettlements_EmptyBalances() {
        List<SettlementProposal> result = settlementOptimizationService.optimizeSettlements(Collections.emptyMap());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should optimize two users - one debtor one creditor")
    void optimizeSettlements_TwoUsers() {
        UUID debtor = UUID.randomUUID();
        UUID creditor = UUID.randomUUID();
        Map<UUID, BigDecimal> netBalances = Map.of(
                debtor, new BigDecimal("-100.00"),
                creditor, new BigDecimal("100.00")
        );

        List<SettlementProposal> result = settlementOptimizationService.optimizeSettlements(netBalances);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(debtor, result.get(0).getFrom());
        assertEquals(creditor, result.get(0).getTo());
        assertEquals(0, new BigDecimal("100.00").compareTo(result.get(0).getAmount()));
    }

    @Test
    @DisplayName("Should optimize three users - one pays one")
    void optimizeSettlements_ThreeUsers() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();
        Map<UUID, BigDecimal> netBalances = Map.of(
                a, new BigDecimal("-60.00"),
                b, new BigDecimal("40.00"),
                c, new BigDecimal("20.00")
        );

        List<SettlementProposal> result = settlementOptimizationService.optimizeSettlements(netBalances);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        BigDecimal totalFromA = result.stream()
                .filter(s -> s.getFrom().equals(a))
                .map(SettlementProposal::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, new BigDecimal("60.00").compareTo(totalFromA));
    }

    @Test
    @DisplayName("Should merge multiple settlements between same pair")
    void optimizeSettlements_MergesSamePair() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        Map<UUID, BigDecimal> netBalances = Map.of(
                a, new BigDecimal("-100.00"),
                b, new BigDecimal("100.00")
        );

        List<SettlementProposal> result = settlementOptimizationService.optimizeSettlements(netBalances);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isValid());
    }

    @Test
    @DisplayName("Should ignore zero balances")
    void optimizeSettlements_ZeroBalancesExcluded() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();
        Map<UUID, BigDecimal> netBalances = Map.of(
                a, new BigDecimal("-50.00"),
                b, BigDecimal.ZERO,
                c, new BigDecimal("50.00")
        );

        List<SettlementProposal> result = settlementOptimizationService.optimizeSettlements(netBalances);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
