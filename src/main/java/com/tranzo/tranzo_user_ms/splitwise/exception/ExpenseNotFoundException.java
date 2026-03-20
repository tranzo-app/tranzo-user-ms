package com.tranzo.tranzo_user_ms.splitwise.exception;

import com.tranzo.tranzo_user_ms.splitwise.enums.SplitwiseErrorCode;

import java.util.UUID;

/**
 * Exception thrown when an expense is not found.
 */
public class ExpenseNotFoundException extends SplitwiseException {

    public ExpenseNotFoundException(UUID expenseId) {
        super(SplitwiseErrorCode.EXPENSE_NOT_FOUND, 404, "Expense not found with ID: " + expenseId);
    }

    public ExpenseNotFoundException(String message) {
        super(SplitwiseErrorCode.EXPENSE_NOT_FOUND, 404, message);
    }
}
