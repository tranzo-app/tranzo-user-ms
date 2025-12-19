package com.tranzo.tranzo_user_ms.commons.exception;

public class InvalidPatchRequestException  extends BaseException {
    public InvalidPatchRequestException(String message) {
        super(message, 400);
    }
}