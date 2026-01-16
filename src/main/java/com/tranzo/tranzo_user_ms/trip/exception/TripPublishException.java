package com.tranzo.tranzo_user_ms.trip.exception;

import com.tranzo.tranzo_user_ms.trip.enums.TripPublishErrorCode;

public class TripPublishException extends RuntimeException {
    private final TripPublishErrorCode errorCode;

    public TripPublishException(TripPublishErrorCode errorCode) {
        super(errorCode.name());
        this.errorCode = errorCode;
    }

    public TripPublishErrorCode getErrorCode() {
        return errorCode;
    }
}
