package com.tranzo.tranzo_user_ms.chat.exception;

import com.tranzo.tranzo_user_ms.chat.enums.ChatErrorCode;

public class ConversationNotFoundException extends ChatException {
    public ConversationNotFoundException(String message) {
        super(ChatErrorCode.CONVERSATION_NOT_FOUND, message, 404);
    }
    
    public ConversationNotFoundException(ChatErrorCode errorCode, String message) {
        super(errorCode, message, 404);
    }
}

