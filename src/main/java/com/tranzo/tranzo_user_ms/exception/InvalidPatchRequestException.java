package com.tranzo.tranzo_user_ms.exception;

public class InvalidPatchRequestException  extends UserProfileNotFoundException {
    public InvalidPatchRequestException(String message) {
        super(message);
    }
}