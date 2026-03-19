package com.tranzo.tranzo_user_ms.splitwise.exception;

import com.tranzo.tranzo_user_ms.splitwise.enums.SplitwiseErrorCode;

import java.math.BigDecimal;

/**
 * Exception thrown when settlement amount exceeds available balance.
 */
public class InsufficientBalanceException extends SplitwiseException {

    public InsufficientBalanceException(BigDecimal available, BigDecimal requested) {
        super(SplitwiseErrorCode.INSUFFICIENT_BALANCE, 400, String.format("Insufficient balance. Available: %.2f, Requested: %.2f", 
                        available, requested));
    }

    public InsufficientBalanceException(String message) {
        super(SplitwiseErrorCode.INSUFFICIENT_BALANCE, 400, message);
    }
}
