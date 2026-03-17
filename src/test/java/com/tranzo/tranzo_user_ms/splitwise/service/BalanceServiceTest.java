package com.tranzo.tranzo_user_ms.splitwise.service;

import com.tranzo.tranzo_user_ms.splitwise.dto.SettlementProposal;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.BalanceResponse;
import com.tranzo.tranzo_user_ms.splitwise.entity.Balance;
import com.tranzo.tranzo_user_ms.splitwise.entity.Expense;
import com.tranzo.tranzo_user_ms.splitwise.entity.Settlement;
import com.tranzo.tranzo_user_ms.splitwise.entity.SplitwiseGroup;
import com.tranzo.tranzo_user_ms.splitwise.exception.InsufficientBalanceException;
import com.tranzo.tranzo_user_ms.splitwise.repository.BalanceRepository;
import com.tranzo.tranzo_user_ms.splitwise.repository.ExpenseRepository;
import com.tranzo.tranzo_user_ms.splitwise.repository.SplitwiseGroupRepository;
import com.tranzo.tranzo_user_ms.user.model.UserProfileEntity;
import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import com.tranzo.tranzo_user_ms.user.repository.UserProfileRepository;
import com.tranzo.tranzo_user_ms.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BalanceService Unit Tests")
class BalanceServiceTest {

    @Mock
    private BalanceRepository balanceRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private SettlementOptimizationService settlementOptimizationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private SplitwiseGroupRepository splitwiseGroupRepository;

    @InjectMocks
    private BalanceService balanceService;

    private UUID groupId;
    private UUID userA;
    private UUID userB;
    private SplitwiseGroup group;
    private Balance balance;

    @BeforeEach
    void setUp() {
        groupId = UUID.randomUUID();
        userA = UUID.randomUUID();
        userB = UUID.randomUUID();
        group = SplitwiseGroup.builder().id(groupId).tripId(UUID.randomUUID()).createdBy(userA).build();
        balance = Balance.builder()
                .group(group)
                .owedBy(userA)
                .owedTo(userB)
                .amount(new BigDecimal("50.00"))
                .build();
    }

    @Test
    @DisplayName("Should get group balances from summary")
    void getGroupBalances_Success() {
        Object[] row = new Object[]{userA, "User A", "a@test.com", new BigDecimal("0"), new BigDecimal("50"), new BigDecimal("-50")};
        when(balanceRepository.getBalanceSummaryForGroup(groupId)).thenReturn(Collections.singletonList(row));

        List<BalanceResponse> list = balanceService.getGroupBalances(groupId);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(userA, list.get(0).getUser().getUserUuid());
        assertEquals(new BigDecimal("-50"), list.get(0).getNetBalance());
    }

    @Test
    @DisplayName("Should get user balance in group")
    void getUserBalanceInGroup_Success() {
        when(balanceRepository.getTotalOwedByUserInGroup(groupId, userA)).thenReturn(new BigDecimal("50"));
        when(balanceRepository.getTotalOwedToUserInGroup(groupId, userA)).thenReturn(new BigDecimal("20"));
        when(balanceRepository.findBalancesForUserInGroup(groupId, userA)).thenReturn(List.of(balance));
        UsersEntity other = new UsersEntity();
        other.setUserUuid(userB);
        other.setEmail("b@test.com");
        when(userRepository.findUserByUserUuid(userB)).thenReturn(Optional.of(other));
        when(userProfileRepository.findAllUserProfileDetailByUserId(userB)).thenReturn(Optional.of(new UserProfileEntity()));

        BalanceResponse response = balanceService.getUserBalanceInGroup(groupId, userA);

        assertNotNull(response);
        assertEquals(new BigDecimal("30"), response.getNetBalance()); // 50 - 20
        assertNotNull(response.getBalanceDetails());
    }

    @Test
    @DisplayName("Should update balances for expense")
    void updateBalancesForExpense_Success() {
        Expense expense = Expense.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("100"))
                .paidBy(userA)
                .groupId(groupId)
                .build();
        expense.addSplit(com.tranzo.tranzo_user_ms.splitwise.entity.ExpenseSplit.builder().userId(userA).amount(new BigDecimal("50")).build());
        expense.addSplit(com.tranzo.tranzo_user_ms.splitwise.entity.ExpenseSplit.builder().userId(userB).amount(new BigDecimal("50")).build());

        when(balanceRepository.findByGroupIdAndOwedByAndOwedTo(eq(groupId), any(UUID.class), any(UUID.class))).thenReturn(Optional.empty());
        when(balanceRepository.findByGroupIdAndOwedByAndOwedTo(eq(groupId), eq(userB), eq(userA))).thenReturn(Optional.empty());
        when(splitwiseGroupRepository.findById(groupId)).thenReturn(Optional.of(group));

        balanceService.updateBalancesForExpense(expense);

        verify(balanceRepository, atLeastOnce()).save(any(Balance.class));
    }

    @Test
    @DisplayName("Should recalculate balances for group")
    void recalculateBalancesForGroup_Success() {
        Expense expense = Expense.builder().id(UUID.randomUUID()).amount(new BigDecimal("100")).paidBy(userA).groupId(groupId).build();
        expense.addSplit(com.tranzo.tranzo_user_ms.splitwise.entity.ExpenseSplit.builder().userId(userA).amount(new BigDecimal("50")).build());
        expense.addSplit(com.tranzo.tranzo_user_ms.splitwise.entity.ExpenseSplit.builder().userId(userB).amount(new BigDecimal("50")).build());

        doNothing().when(balanceRepository).deleteByGroupId(groupId);
        when(expenseRepository.findByGroupId(groupId)).thenReturn(List.of(expense));
        when(balanceRepository.findByGroupIdAndOwedByAndOwedTo(any(UUID.class), any(UUID.class), any(UUID.class))).thenReturn(Optional.empty());
        when(splitwiseGroupRepository.findById(groupId)).thenReturn(Optional.of(group));

        balanceService.recalculateBalancesForGroup(groupId);

        verify(balanceRepository).deleteByGroupId(groupId);
        verify(expenseRepository).findByGroupId(groupId);
    }

    @Test
    @DisplayName("Should get optimized settlements")
    void getOptimizedSettlements_Success() {
        Object[] row = new Object[]{userA, "A", "a@test.com", new BigDecimal("0"), new BigDecimal("50"), new BigDecimal("-50")};
        when(balanceRepository.getBalanceSummaryForGroup(groupId)).thenReturn(Collections.singletonList(row));
        when(settlementOptimizationService.optimizeSettlements(anyMap())).thenReturn(
                List.of(new SettlementProposal(userA, userB, new BigDecimal("50"))));

        List<SettlementProposal> list = balanceService.getOptimizedSettlements(groupId);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(userA, list.get(0).getFrom());
        assertEquals(userB, list.get(0).getTo());
    }

    @Test
    @DisplayName("Should update balances for settlement")
    void updateBalancesForSettlement_Success() {
        Settlement settlement = Settlement.builder()
                .id(UUID.randomUUID())
                .group(group)
                .paidBy(userA)
                .paidTo(userB)
                .amount(new BigDecimal("30"))
                .build();
        when(balanceRepository.findByGroupIdAndOwedByAndOwedTo(groupId, userA, userB)).thenReturn(Optional.of(balance));
        when(balanceRepository.save(any(Balance.class))).thenReturn(balance);

        balanceService.updateBalancesForSettlement(settlement);

        verify(balanceRepository).save(any(Balance.class));
    }

    @Test
    @DisplayName("Should validate settlement amount and throw when exceeds owed")
    void validateSettlementAmount_ExceedsOwed() {
        when(balanceRepository.findByGroupIdAndOwedByAndOwedTo(groupId, userA, userB)).thenReturn(Optional.of(balance));

        assertThrows(InsufficientBalanceException.class, () ->
                balanceService.validateSettlementAmount(groupId, userA, userB, new BigDecimal("100")));
    }

    @Test
    @DisplayName("Should validate settlement amount when within owed")
    void validateSettlementAmount_WithinOwed() {
        when(balanceRepository.findByGroupIdAndOwedByAndOwedTo(groupId, userA, userB)).thenReturn(Optional.of(balance));

        assertDoesNotThrow(() ->
                balanceService.validateSettlementAmount(groupId, userA, userB, new BigDecimal("30")));
    }
}
