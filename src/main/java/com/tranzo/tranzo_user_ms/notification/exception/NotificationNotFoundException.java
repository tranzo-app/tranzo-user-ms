package com.tranzo.tranzo_user_ms.notification.exception;

import com.tranzo.tranzo_user_ms.notification.enums.NotificationErrorCode;

import java.util.UUID;

/**
 * Exception thrown when a notification is not found.
 */
public class NotificationNotFoundException extends NotificationException {

    public NotificationNotFoundException(UUID notificationId) {
        super(NotificationErrorCode.NOTIFICATION_NOT_FOUND, 404, "Notification not found with ID: " + notificationId);
    }

    public NotificationNotFoundException(String message) {
        super(NotificationErrorCode.NOTIFICATION_NOT_FOUND, 404, message);
    }
}
