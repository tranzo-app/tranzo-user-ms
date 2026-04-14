package com.tranzo.tranzo_user_ms.trip.exception;

import com.tranzo.tranzo_user_ms.trip.enums.TripErrorCode;

/**
 * Exception thrown when trip join request operations fail.
 */
public class TripJoinRequestException extends TripException {

    public TripJoinRequestException(TripErrorCode errorCode) {
        super(errorCode, 400, errorCode.getCode());
    }

    public TripJoinRequestException(TripErrorCode errorCode, String message) {
        super(errorCode, 400, message);
    }

    public TripJoinRequestException(TripErrorCode errorCode, int statusCode, String message) {
        super(errorCode, statusCode, message);
    }
}
