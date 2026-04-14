package com.tranzo.tranzo_user_ms.chat.exception;

import com.tranzo.tranzo_user_ms.chat.enums.ChatErrorCode;

/**
 * Exception thrown when a user who has left the conversation attempts an action.
 * Status Code: 410 (Gone)
 */
public class UserLeftConversationException extends ChatException {
    public UserLeftConversationException(String message) {
        super(ChatErrorCode.USER_LEFT_CONVERSATION, message, 410);
    }
    
    public UserLeftConversationException(ChatErrorCode errorCode, String message) {
        super(errorCode, message, 410);
    }
}

