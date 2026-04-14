package com.tranzo.tranzo_user_ms.trip.exception;

import com.tranzo.tranzo_user_ms.trip.enums.TripErrorCode;
import lombok.Getter;

/**
 * Base exception class for Trip module.
 */
@Getter
public class TripException extends RuntimeException {

    private final TripErrorCode errorCode;
    private final int statusCode;

    public TripException(TripErrorCode errorCode, int statusCode) {
        super(errorCode.getCode());
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }

    public TripException(TripErrorCode errorCode, int statusCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }

    public TripException(TripErrorCode errorCode, int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }
}
