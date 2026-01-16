package com.tranzo.tranzo_user_ms.commons.exception;

public class EntityNotFoundException extends BaseException {
    public EntityNotFoundException(String message) {
        super(message, 404);
    }
}
