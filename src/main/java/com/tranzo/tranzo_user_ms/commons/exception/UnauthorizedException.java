package com.tranzo.tranzo_user_ms.commons.exception;

public class UnauthorizedException extends AuthException {
    public UnauthorizedException(String message) {
        super(message, 401);
    }
}
