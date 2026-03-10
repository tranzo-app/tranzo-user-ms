package com.tranzo.tranzo_user_ms.media.exception;

/**
 * Thrown when S3 media storage is not configured (e.g. AWS_S3_MEDIA_BUCKET not set).
 */
public class S3MediaNotConfiguredException extends RuntimeException {

    public S3MediaNotConfiguredException(String message) {
        super(message);
    }
}
