package com.tranzo.tranzo_user_ms.commons.exception;

public class ForbiddenException extends AuthException {
    public ForbiddenException(String message) {
        super(message, 403);
    }
}
