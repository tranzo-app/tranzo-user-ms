package com.tranzo.tranzo_user_ms.splitwise.enums;

/**
 * Error codes for splitwise module operations
 */
public enum SplitwiseErrorCode {
    // Group errors
    GROUP_NOT_FOUND("GROUP_NOT_FOUND"),
    
    // Expense errors
    EXPENSE_NOT_FOUND("EXPENSE_NOT_FOUND"),
    EXPENSE_SPLIT_INVALID("EXPENSE_SPLIT_INVALID"),
    
    // Settlement errors
    SETTLEMENT_NOT_FOUND("SETTLEMENT_NOT_FOUND"),
    
    // Member errors
    USER_NOT_MEMBER("USER_NOT_MEMBER"),
    
    // Balance errors
    INSUFFICIENT_BALANCE("INSUFFICIENT_BALANCE");

    private final String code;

    SplitwiseErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
