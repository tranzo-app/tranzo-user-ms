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
    public ResponseEntity<ExpenseResponse> createExpense(
            @Valid @RequestBody CreateExpenseRequest request) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Received request to create expense: {}", request.getName());
        ExpenseResponse response = expenseService.createExpense(request, userId);
        
        log.info("Successfully created expense with ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Gets an expense by ID.
     */
    @GetMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponse> getExpense(
            @PathVariable Long expenseId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.debug("Received request to get expense: {}", expenseId);
        ExpenseResponse response = expenseService.getExpense(expenseId, userId);
        
        log.debug("Successfully retrieved expense: {}", response.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing expense.
     */
    @PutMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable Long expenseId,
            @Valid @RequestBody UpdateExpenseRequest request) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Received request to update expense: {}", expenseId);
        ExpenseResponse response = expenseService.updateExpense(expenseId, request, userId);
        
        log.info("Successfully updated expense: {}", expenseId);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes an expense.
     */
    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Long expenseId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Received request to delete expense: {}", expenseId);
        expenseService.deleteExpense(expenseId, userId);
        
        log.info("Successfully deleted expense: {}", expenseId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gets all expenses for a group.
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<ExpenseResponse>> getGroupExpenses(
            @PathVariable Long groupId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.debug("Received request to get expenses for group: {}", groupId);
        List<ExpenseResponse> response = expenseService.getGroupExpenses(groupId, userId);
        
        log.debug("Retrieved {} expenses for group: {}", response.size(), groupId);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets expenses for the current user.
     */
    @GetMapping("/my-expenses")
    public ResponseEntity<List<ExpenseResponse>> getUserExpenses() throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.debug("Received request to get expenses for current user");
        List<ExpenseResponse> response = expenseService.getUserExpenses(userId);
        
        log.debug("Retrieved {} expenses for user: {}", response.size(), userId);
        return ResponseEntity.ok(response);
    }
}
