package com.tranzo.tranzo_user_ms.splitwise.exception;

/**
 * Exception thrown when an expense is not found.
 */
public class ExpenseNotFoundException extends SplitwiseException {

    public ExpenseNotFoundException(Long expenseId) {
        super("Expense not found with ID: " + expenseId);
    }

    public ExpenseNotFoundException(String message) {
        super(message);
    }
}
