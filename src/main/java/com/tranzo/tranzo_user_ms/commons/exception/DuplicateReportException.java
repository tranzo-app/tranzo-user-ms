package com.tranzo.tranzo_user_ms.commons.exception;

public class DuplicateReportException extends BaseException {
    public DuplicateReportException(String message) {
        super(message, 409);
    }
}
