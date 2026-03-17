package com.tranzo.tranzo_user_ms.splitwise.controller;

import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import com.tranzo.tranzo_user_ms.splitwise.dto.request.CreateExpenseRequest;
import com.tranzo.tranzo_user_ms.splitwise.dto.request.ExpenseSplitRequest;
import com.tranzo.tranzo_user_ms.splitwise.dto.request.UpdateExpenseRequest;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.ExpenseResponse;
import com.tranzo.tranzo_user_ms.splitwise.entity.Expense;
import com.tranzo.tranzo_user_ms.splitwise.service.ExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExpenseController Unit Tests")
class ExpenseControllerTest {

    @Mock
    private ExpenseService expenseService;

    @InjectMocks
    private ExpenseController controller;

    private UUID userId;
    private UUID tripId;
    private UUID groupId;
    private UUID expenseId;
    private ExpenseResponse expenseResponse;
    private CreateExpenseRequest createRequest;
    private UpdateExpenseRequest updateRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        tripId = UUID.randomUUID();
        groupId = UUID.randomUUID();
        expenseId = UUID.randomUUID();
        expenseResponse = ExpenseResponse.builder().id(expenseId).name("Dinner").amount(new BigDecimal("100")).build();
        createRequest = CreateExpenseRequest.builder()
                .name("Dinner")
                .amount(new BigDecimal("100"))
                .groupId(tripId)
                .paidById(userId)
                .splitType(Expense.SplitType.EQUAL)
                .splits(List.of(
                        ExpenseSplitRequest.builder().userId(userId).amount(new BigDecimal("50")).build(),
                        ExpenseSplitRequest.builder().userId(UUID.randomUUID()).amount(new BigDecimal("50")).build()))
                .build();
        updateRequest = UpdateExpenseRequest.builder().name("Updated Dinner").amount(new BigDecimal("120")).build();
    }

    @Test
    @DisplayName("Should create expense and return 200")
    void createExpense_Success() throws Exception {
        when(expenseService.createExpense(any(CreateExpenseRequest.class), eq(userId))).thenReturn(expenseResponse);
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<ExpenseResponse> res = controller.createExpense(createRequest);

            assertEquals(HttpStatus.OK, res.getStatusCode());
            assertNotNull(res.getBody());
            assertEquals(expenseId, res.getBody().getId());
        }
    }

    @Test
    @DisplayName("Should get expense by id")
    void getExpense_Success() throws Exception {
        when(expenseService.getExpense(expenseId, userId)).thenReturn(expenseResponse);
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<ExpenseResponse> res = controller.getExpense(expenseId);

            assertEquals(HttpStatus.OK, res.getStatusCode());
        }
    }

    @Test
    @DisplayName("Should update expense")
    void updateExpense_Success() throws Exception {
        when(expenseService.updateExpense(expenseId, updateRequest, userId)).thenReturn(expenseResponse);
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<ExpenseResponse> res = controller.updateExpense(expenseId, updateRequest);

            assertEquals(HttpStatus.OK, res.getStatusCode());
        }
    }

    @Test
    @DisplayName("Should delete expense and return 204")
    void deleteExpense_Success() throws Exception {
        doNothing().when(expenseService).deleteExpense(expenseId, userId);
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<Void> res = controller.deleteExpense(expenseId);

            assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
        }
    }

    @Test
    @DisplayName("Should get group expenses")
    void getGroupExpenses_Success() throws Exception {
        when(expenseService.getGroupExpenses(groupId, userId)).thenReturn(List.of(expenseResponse));
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<List<ExpenseResponse>> res = controller.getGroupExpenses(groupId);

            assertEquals(HttpStatus.OK, res.getStatusCode());
            assertNotNull(res.getBody());
            assertEquals(1, res.getBody().size());
        }
    }

    @Test
    @DisplayName("Should get my expenses")
    void getUserExpenses_Success() throws Exception {
        when(expenseService.getUserExpenses(userId)).thenReturn(List.of(expenseResponse));
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<List<ExpenseResponse>> res = controller.getUserExpenses();

            assertEquals(HttpStatus.OK, res.getStatusCode());
            assertEquals(1, res.getBody().size());
        }
    }
}
