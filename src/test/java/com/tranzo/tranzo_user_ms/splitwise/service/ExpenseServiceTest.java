package com.tranzo.tranzo_user_ms.splitwise.service;

import com.tranzo.tranzo_user_ms.splitwise.dto.request.CreateExpenseRequest;
import com.tranzo.tranzo_user_ms.splitwise.dto.request.ExpenseSplitRequest;
import com.tranzo.tranzo_user_ms.splitwise.dto.request.UpdateExpenseRequest;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.ExpenseResponse;
import com.tranzo.tranzo_user_ms.splitwise.entity.Expense;
import com.tranzo.tranzo_user_ms.splitwise.entity.ExpenseSplit;
import com.tranzo.tranzo_user_ms.splitwise.entity.SplitwiseGroup;
import com.tranzo.tranzo_user_ms.splitwise.exception.ExpenseNotFoundException;
import com.tranzo.tranzo_user_ms.splitwise.exception.GroupNotFoundException;
import com.tranzo.tranzo_user_ms.splitwise.exception.InvalidSplitException;
import com.tranzo.tranzo_user_ms.splitwise.exception.UserNotMemberException;
import com.tranzo.tranzo_user_ms.splitwise.repository.ExpenseRepository;
import com.tranzo.tranzo_user_ms.splitwise.repository.SplitwiseGroupRepository;
import com.tranzo.tranzo_user_ms.user.model.UserProfileEntity;
import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import com.tranzo.tranzo_user_ms.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExpenseService Unit Tests")
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BalanceService balanceService;

    @Mock
    private ActivityService activityService;

    @Mock
    private SplitwiseGroupRepository splitwiseGroupRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private UUID payerId;
    private UUID memberId;
    private UUID tripId;
    private UUID groupId;
    private UsersEntity payer;
    private Expense expense;
    private SplitwiseGroup group;
    private CreateExpenseRequest createRequest;

    @BeforeEach
    void setUp() {
        payerId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        tripId = UUID.randomUUID();
        groupId = UUID.randomUUID();
        payer = new UsersEntity();
        payer.setUserUuid(payerId);
        payer.setEmail("payer@test.com");
        UserProfileEntity profile = new UserProfileEntity();
        profile.setFirstName("Payer");
        profile.setLastName("User");
        payer.setUserProfileEntity(profile);

        group = SplitwiseGroup.builder().id(groupId).tripId(tripId).createdBy(payerId).build();

        UUID expenseId = UUID.randomUUID();
        expense = Expense.builder()
                .id(expenseId)
                .name("Dinner")
                .amount(new BigDecimal("100.00"))
                .paidBy(payerId)
                .groupId(groupId)
                .splitType(Expense.SplitType.EQUAL)
                .build();
        expense.addSplit(ExpenseSplit.builder().userId(payerId).amount(new BigDecimal("50.00")).build());
        expense.addSplit(ExpenseSplit.builder().userId(memberId).amount(new BigDecimal("50.00")).build());

        createRequest = CreateExpenseRequest.builder()
                .name("Dinner")
                .amount(new BigDecimal("100.00"))
                .groupId(tripId)
                .paidById(payerId)
                .splitType(Expense.SplitType.EQUAL)
                .splits(List.of(
                        ExpenseSplitRequest.builder().userId(payerId).amount(new BigDecimal("50.00")).build(),
                        ExpenseSplitRequest.builder().userId(memberId).amount(new BigDecimal("50.00")).build()
                ))
                .build();
    }

    @Test
    @DisplayName("Should create expense successfully")
    void createExpense_Success() {
        UsersEntity member = new UsersEntity();
        member.setUserUuid(memberId);
        member.setEmail("m@test.com");
        when(splitwiseGroupRepository.findByTripId(tripId)).thenReturn(Optional.of(group));
        when(userRepository.findById(payerId)).thenReturn(Optional.of(payer));
        when(userRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

        ExpenseResponse response = expenseService.createExpense(createRequest, payerId);

        assertNotNull(response);
        assertEquals(expense.getName(), response.getName());
        verify(splitwiseGroupRepository).findByTripId(tripId);
        verify(balanceService).updateBalancesForExpense(any(Expense.class));
        verify(activityService).logExpenseCreated(eq(payerId), eq(group), eq(expense.getId()), anyString(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("Should throw GroupNotFoundException when no group exists for trip")
    void createExpense_GroupNotFoundForTrip() {
        when(splitwiseGroupRepository.findByTripId(tripId)).thenReturn(Optional.empty());

        assertThrows(GroupNotFoundException.class, () -> expenseService.createExpense(createRequest, payerId));
        verify(expenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw when paid-by user not found")
    void createExpense_PaidByNotFound() {
        when(splitwiseGroupRepository.findByTripId(tripId)).thenReturn(Optional.of(group));
        when(userRepository.findById(payerId)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> expenseService.createExpense(createRequest, payerId));
        verify(expenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InvalidSplitException when splits do not sum to amount")
    void createExpense_InvalidSplits() {
        createRequest.getSplits().get(0).setAmount(new BigDecimal("60.00"));
        createRequest.getSplits().get(1).setAmount(new BigDecimal("50.00"));
        when(splitwiseGroupRepository.findByTripId(tripId)).thenReturn(Optional.of(group));
        when(userRepository.findById(payerId)).thenReturn(Optional.of(payer));

        assertThrows(InvalidSplitException.class, () -> expenseService.createExpense(createRequest, payerId));
    }

    @Test
    @DisplayName("Should get expense when user involved")
    void getExpense_Success() {
        UUID expenseId = expense.getId();
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(payer));
        when(splitwiseGroupRepository.findById(groupId)).thenReturn(Optional.of(group));

        ExpenseResponse response = expenseService.getExpense(expenseId, payerId);

        assertNotNull(response);
        assertEquals(expenseId, response.getId());
    }

    @Test
    @DisplayName("Should throw ExpenseNotFoundException when expense not found")
    void getExpense_NotFound() {
        UUID missingId = UUID.randomUUID();
        when(expenseRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThrows(ExpenseNotFoundException.class, () -> expenseService.getExpense(missingId, payerId));
    }

    @Test
    @DisplayName("Should throw UserNotMemberException when user not involved")
    void getExpense_UserNotInvolved() {
        UUID otherId = UUID.randomUUID();
        when(expenseRepository.findById(expense.getId())).thenReturn(Optional.of(expense));

        assertThrows(UserNotMemberException.class, () -> expenseService.getExpense(expense.getId(), otherId));
    }

    @Test
    @DisplayName("Should update expense when user is payer")
    void updateExpense_Success() {
        UpdateExpenseRequest updateRequest = UpdateExpenseRequest.builder()
                .name("Updated Dinner")
                .amount(new BigDecimal("120.00"))
                .build();
        when(expenseRepository.findById(expense.getId())).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);
        when(splitwiseGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(payer));

        ExpenseResponse response = expenseService.updateExpense(expense.getId(), updateRequest, payerId);

        assertNotNull(response);
        verify(balanceService).recalculateBalancesForGroup(groupId);
        verify(activityService).logExpenseUpdated(eq(payerId), eq(group), eq(expense.getId()), anyString());
    }

    @Test
    @DisplayName("Should delete expense when user is payer")
    void deleteExpense_Success() {
        when(expenseRepository.findById(expense.getId())).thenReturn(Optional.of(expense));
        when(splitwiseGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        doNothing().when(expenseRepository).delete(any(Expense.class));

        expenseService.deleteExpense(expense.getId(), payerId);

        verify(expenseRepository).delete(expense);
        verify(balanceService).recalculateBalancesForGroup(groupId);
        verify(activityService).logExpenseDeleted(eq(payerId), eq(group), eq(expense.getId()), anyString());
    }

    @Test
    @DisplayName("Should get group expenses when user is member")
    void getGroupExpenses_Success() {
        when(splitwiseGroupRepository.isUserMemberOfGroup(groupId, payerId)).thenReturn(true);
        when(expenseRepository.findByGroupId(groupId)).thenReturn(List.of(expense));
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(payer));
        when(splitwiseGroupRepository.findById(groupId)).thenReturn(Optional.of(group));

        List<ExpenseResponse> list = expenseService.getGroupExpenses(groupId, payerId);

        assertNotNull(list);
        assertEquals(1, list.size());
    }

    @Test
    @DisplayName("Should throw UserNotMemberException when getting group expenses and not member")
    void getGroupExpenses_NotMember() {
        when(splitwiseGroupRepository.isUserMemberOfGroup(groupId, payerId)).thenReturn(false);

        assertThrows(UserNotMemberException.class, () -> expenseService.getGroupExpenses(groupId, payerId));
    }

    @Test
    @DisplayName("Should get user expenses")
    void getUserExpenses_Success() {
        when(expenseRepository.findExpensesInvolvingUser(payerId)).thenReturn(List.of(expense));
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(payer));
        when(splitwiseGroupRepository.findById(groupId)).thenReturn(Optional.of(group));

        List<ExpenseResponse> list = expenseService.getUserExpenses(payerId);

        assertNotNull(list);
        assertEquals(1, list.size());
    }
}
