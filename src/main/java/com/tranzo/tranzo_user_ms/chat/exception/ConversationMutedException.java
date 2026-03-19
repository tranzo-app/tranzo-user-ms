package com.tranzo.tranzo_user_ms.chat.exception;

import com.tranzo.tranzo_user_ms.chat.enums.ChatErrorCode;

/**
 * Exception thrown when attempting to send a message to a muted conversation by recipient.
 * Status Code: 403 (Forbidden)
 */
public class ConversationMutedException extends ChatException {
    public ConversationMutedException(String message) {
        super(ChatErrorCode.USER_MUTED, message, 403);
    }
    
    public ConversationMutedException(ChatErrorCode errorCode, String message) {
        super(errorCode, message, 403);
    }
}

