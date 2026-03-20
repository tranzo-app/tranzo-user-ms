package com.tranzo.tranzo_user_ms.notification.exception;

import com.tranzo.tranzo_user_ms.notification.enums.NotificationErrorCode;
import lombok.Getter;

/**
 * Base exception class for Notification module.
 */
@Getter
public class NotificationException extends RuntimeException {

    private final NotificationErrorCode errorCode;
    private final int statusCode;

    public NotificationException(NotificationErrorCode errorCode, int statusCode) {
        super(errorCode.getCode());
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }

    public NotificationException(NotificationErrorCode errorCode, int statusCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }

    public NotificationException(NotificationErrorCode errorCode, int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }
}
