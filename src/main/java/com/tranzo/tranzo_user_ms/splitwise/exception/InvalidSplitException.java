package com.tranzo.tranzo_user_ms.splitwise.exception;

import com.tranzo.tranzo_user_ms.splitwise.enums.SplitwiseErrorCode;

/**
 * Exception thrown when expense split validation fails.
 */
public class InvalidSplitException extends SplitwiseException {

    public InvalidSplitException(String message) {
        super(SplitwiseErrorCode.EXPENSE_SPLIT_INVALID, 400, message);
    }

    public InvalidSplitException(String message, Throwable cause) {
        super(SplitwiseErrorCode.EXPENSE_SPLIT_INVALID, 400, message, cause);
    }
}
