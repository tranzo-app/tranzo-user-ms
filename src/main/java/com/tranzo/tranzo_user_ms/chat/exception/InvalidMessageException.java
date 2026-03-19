package com.tranzo.tranzo_user_ms.chat.exception;

import com.tranzo.tranzo_user_ms.chat.enums.ChatErrorCode;

/**
 * Exception thrown when message validation fails.
 * Status Code: 400 (Bad Request)
 */
public class InvalidMessageException extends ChatException {
    public InvalidMessageException(String message) {
        super(ChatErrorCode.INVALID_MESSAGE, message, 400);
    }
    
    public InvalidMessageException(ChatErrorCode errorCode, String message) {
        super(errorCode, message, 400);
    }
}

