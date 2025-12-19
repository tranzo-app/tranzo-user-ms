package com.tranzo.tranzo_user_ms.commons.exception;

public class InvalidUserIdException extends BaseException {
    public InvalidUserIdException(String message) {
        super(message, 400);
    }
}
