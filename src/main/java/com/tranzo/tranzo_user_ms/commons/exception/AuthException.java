package com.tranzo.tranzo_user_ms.commons.exception;

public abstract class AuthException extends RuntimeException {
    private final int statusCode;

    protected AuthException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
