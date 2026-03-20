package com.tranzo.tranzo_user_ms.trip.exception;

import com.tranzo.tranzo_user_ms.trip.enums.TripErrorCode;

/**
 * Exception thrown when trip Q&A operations fail.
 */
public class TripQnaException extends TripException {

    public TripQnaException(TripErrorCode errorCode) {
        super(errorCode, 400, errorCode.getCode());
    }

    public TripQnaException(TripErrorCode errorCode, String message) {
        super(errorCode, 400, message);
    }

    public TripQnaException(TripErrorCode errorCode, int statusCode, String message) {
        super(errorCode, statusCode, message);
    }
}
