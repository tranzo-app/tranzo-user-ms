package com.tranzo.tranzo_user_ms.trip.exception;

import com.tranzo.tranzo_user_ms.trip.enums.TripErrorCode;

/**
 * Exception thrown when trip validation fails.
 */
public class TripValidationException extends TripException {

    public TripValidationException(TripErrorCode errorCode) {
        super(errorCode, 400, errorCode.getCode());
    }

    public TripValidationException(TripErrorCode errorCode, String message) {
        super(errorCode, 400, message);
    }
}
