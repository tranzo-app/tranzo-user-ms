package com.tranzo.tranzo_user_ms.commons.exception;

public class OtpException extends BaseException{
    public OtpException(String message)
    {
        super(message, 400);
    }
}
