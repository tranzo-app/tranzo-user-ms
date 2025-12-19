package com.tranzo.tranzo_user_ms.commons.exception;

public class UserAlreadyDeletedExeption extends  BaseException{
    public UserAlreadyDeletedExeption(String message){
        super(message, 409);
    }
}
