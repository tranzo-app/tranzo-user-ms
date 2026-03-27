package com.tranzo.tranzo_user_ms.splitwise.controller;

import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import com.tranzo.tranzo_user_ms.splitwise.dto.request.CreateExpenseRequest;
import com.tranzo.tranzo_user_ms.splitwise.dto.request.UpdateExpenseRequest;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.ExpenseResponse;
import com.tranzo.tranzo_user_ms.splitwise.service.ExpenseService;
import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing expenses.
 */
@Slf4j
@RestController
@RequestMapping("/api/splitwise/expenses")
@RequiredArgsConstructor
@Validated
public class ExpenseController {

    private final ExpenseService expenseService;

    /**
     * Creates a new expense.
     */
    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(@Valid @RequestBody CreateExpenseRequest request) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/api/splitwise/expenses | method=POST | userId={} | expenseName={}", userId, request.getName());
        ExpenseResponse response = expenseService.createExpense(request, userId);
        
        log.info("Expense created | userId={} | expenseId={} | status=SUCCESS", userId, response.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Gets an expense by ID.
     */
    @GetMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponse> getExpense(@PathVariable UUID expenseId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/api/splitwise/expenses/{} | method=GET | userId={}", expenseId, userId);
        ExpenseResponse response = expenseService.getExpense(expenseId, userId);
        
        log.info("Expense retrieved | userId={} | expenseId={} | status=SUCCESS", userId, expenseId);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing expense.
     */
    @PutMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponse> updateExpense(@PathVariable UUID expenseId, @Valid @RequestBody UpdateExpenseRequest request) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/api/splitwise/expenses/{} | method=PUT | userId={}", expenseId, userId);
        ExpenseResponse response = expenseService.updateExpense(expenseId, request, userId);
        
        log.info("Expense updated | userId={} | expenseId={} | status=SUCCESS", userId, expenseId);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes an expense.
     */
    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable UUID expenseId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/api/splitwise/expenses/{} | method=DELETE | userId={}", expenseId, userId);
        expenseService.deleteExpense(expenseId, userId);
        
        log.info("Expense deleted | userId={} | expenseId={} | status=SUCCESS", userId, expenseId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gets all expenses for a group.
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<ExpenseResponse>> getGroupExpenses(@PathVariable UUID groupId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/api/splitwise/expenses/group/{} | method=GET | userId={}", groupId, userId);
        List<ExpenseResponse> response = expenseService.getGroupExpenses(groupId, userId);
        
        log.info("Group expenses retrieved | userId={} | groupId={} | expensesCount={} | status=SUCCESS", userId, groupId, response.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Gets expenses for the current user.
     */
    @GetMapping("/my-expenses")
    public ResponseEntity<List<ExpenseResponse>> getUserExpenses() throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/api/splitwise/expenses/my-expenses | method=GET | userId={}", userId);
        List<ExpenseResponse> response = expenseService.getUserExpenses(userId);
        
        log.info("User expenses retrieved | userId={} | expensesCount={} | status=SUCCESS", userId, response.size());
        return ResponseEntity.ok(response);
    }
}
