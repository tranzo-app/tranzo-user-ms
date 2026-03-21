package com.tranzo.tranzo_user_ms.trip.exception;

import com.tranzo.tranzo_user_ms.trip.enums.TripErrorCode;

/**
 * Exception thrown when user tries to access a trip they don't have permission for.
 */
public class TripAccessDeniedException extends TripException {

    public TripAccessDeniedException() {
        super(TripErrorCode.TRIP_ACCESS_DENIED, 403, "Access denied to this trip");
    }

    public TripAccessDeniedException(String message) {
        super(TripErrorCode.TRIP_ACCESS_DENIED, 403, message);
    }
}
