package com.tranzo.tranzo_user_ms.splitwise.exception;

/**
 * Base exception class for Splitwise module.
 */
public class SplitwiseException extends RuntimeException {

    public SplitwiseException(String message) {
        super(message);
    }

    public SplitwiseException(String message, Throwable cause) {
        super(message, cause);
    }
}
