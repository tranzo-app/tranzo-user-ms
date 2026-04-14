package com.tranzo.tranzo_user_ms.notification.exception;

import com.tranzo.tranzo_user_ms.notification.enums.NotificationErrorCode;

/**
 * Exception thrown when user tries to access notifications they don't own.
 */
public class NotificationAccessDeniedException extends NotificationException {

    public NotificationAccessDeniedException(String message) {
        super(NotificationErrorCode.NOTIFICATION_ACCESS_DENIED, 403, message);
    }
}
