package com.tranzo.tranzo_user_ms.commons.exception;

public class RateLimitExceededException extends BaseException {
    public RateLimitExceededException(String message) {
        super(message, 429);
    }
}
