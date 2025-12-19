package com.tranzo.tranzo_user_ms.commons.exception;

public class InvalidReportRequestException extends BaseException {
    public InvalidReportRequestException(String message) {
        super(message, 400);
    }
}
