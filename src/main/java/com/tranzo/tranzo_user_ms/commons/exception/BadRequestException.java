package com.tranzo.tranzo_user_ms.commons.exception;

public class BadRequestException extends BaseException {
    public BadRequestException(String message)
    {
        super(message, 400);
    }
}
