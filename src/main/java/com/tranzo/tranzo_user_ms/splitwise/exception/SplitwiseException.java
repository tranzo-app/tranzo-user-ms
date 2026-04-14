package com.tranzo.tranzo_user_ms.splitwise.exception;

import com.tranzo.tranzo_user_ms.splitwise.enums.SplitwiseErrorCode;
import lombok.Getter;

/**
 * Base exception class for Splitwise module.
 */
@Getter
public class SplitwiseException extends RuntimeException {

    private final SplitwiseErrorCode errorCode;
    private final int statusCode;

    public SplitwiseException(SplitwiseErrorCode errorCode, int statusCode) {
        super(errorCode.getCode());
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }

    public SplitwiseException(SplitwiseErrorCode errorCode, int statusCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }

    public SplitwiseException(SplitwiseErrorCode errorCode, int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }
}
