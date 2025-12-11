package com.tranzo.tranzo_user_ms.exception;

public class UserAlreadyDeletedExeption extends  RuntimeException{
    public UserAlreadyDeletedExeption(String message){
        super(message);
    }
}
