package com.tranzo.tranzo_user_ms.commons.exception;

public class UserProfileAlreadyExistsException extends BaseException {
    public UserProfileAlreadyExistsException(String message) {
        super(message, 409);
    }
}
