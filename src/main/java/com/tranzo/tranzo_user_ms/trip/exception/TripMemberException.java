package com.tranzo.tranzo_user_ms.trip.exception;

import com.tranzo.tranzo_user_ms.trip.enums.TripErrorCode;

/**
 * Exception thrown when trip member operations fail.
 */
public class TripMemberException extends TripException {

    public TripMemberException(TripErrorCode errorCode) {
        super(errorCode, 400, errorCode.getCode());
    }

    public TripMemberException(TripErrorCode errorCode, String message) {
        super(errorCode, 400, message);
    }

    public TripMemberException(TripErrorCode errorCode, int statusCode, String message) {
        super(errorCode, statusCode, message);
    }
}
