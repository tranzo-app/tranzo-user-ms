package com.tranzo.tranzo_user_ms.splitwise.exception;

import java.math.BigDecimal;

/**
 * Exception thrown when settlement amount exceeds available balance.
 */
public class InsufficientBalanceException extends SplitwiseException {

    public InsufficientBalanceException(BigDecimal available, BigDecimal requested) {
        super(String.format("Insufficient balance. Available: %.2f, Requested: %.2f", 
                        available, requested));
    }

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
