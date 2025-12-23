package com.tranzo.tranzo_user_ms.commons.exception;

public class UserNotFoundException extends BaseException{
    public UserNotFoundException(String message) {
        super(message, 404);
    }
}
