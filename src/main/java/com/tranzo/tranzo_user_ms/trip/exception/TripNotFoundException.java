package com.tranzo.tranzo_user_ms.trip.exception;

import com.tranzo.tranzo_user_ms.trip.enums.TripErrorCode;

/**
 * Exception thrown when a trip is not found.
 */
public class TripNotFoundException extends TripException {

    public TripNotFoundException() {
        super(TripErrorCode.TRIP_NOT_FOUND, 404, "Trip not found");
    }

    public TripNotFoundException(String message) {
        super(TripErrorCode.TRIP_NOT_FOUND, 404, message);
    }
}
