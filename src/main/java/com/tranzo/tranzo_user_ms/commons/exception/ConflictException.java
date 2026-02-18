package com.tranzo.tranzo_user_ms.commons.exception;

public class ConflictException extends BaseException {
    public ConflictException(String message)
    {
        super(message, 409);
    }
}
