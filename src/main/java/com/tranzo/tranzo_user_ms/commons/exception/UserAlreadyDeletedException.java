package com.tranzo.tranzo_user_ms.commons.exception;

public class UserAlreadyDeletedException extends BaseException{
    public UserAlreadyDeletedException(String message){
        super(message, 409);
    }
}
