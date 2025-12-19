package com.tranzo.tranzo_user_ms.commons.exception;

public class UserProfileNotFoundException extends BaseException {
    public UserProfileNotFoundException(String message) {
        super(message, 404);
    }
}
