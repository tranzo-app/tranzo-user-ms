package com.tranzo.tranzo_user_ms.splitwise.exception;

/**
 * Exception thrown when expense split validation fails.
 */
public class InvalidSplitException extends SplitwiseException {

    public InvalidSplitException(String message) {
        super(message);
    }

    public InvalidSplitException(String message, Throwable cause) {
        super(message, cause);
    }
}
