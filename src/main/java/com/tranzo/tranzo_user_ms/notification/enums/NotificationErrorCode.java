package com.tranzo.tranzo_user_ms.notification.enums;

/**
 * Error codes for notification module operations
 */
public enum NotificationErrorCode {
    // Notification errors
    NOTIFICATION_NOT_FOUND("NOTIFICATION_NOT_FOUND"),
    NOTIFICATION_ACCESS_DENIED("NOTIFICATION_ACCESS_DENIED");

    private final String code;

    NotificationErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
