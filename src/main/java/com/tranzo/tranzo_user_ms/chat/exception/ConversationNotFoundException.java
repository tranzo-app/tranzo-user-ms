package com.tranzo.tranzo_user_ms.chat.exception;


public class ConversationNotFoundException  extends BaseException{
    public ConversationNotFoundException(String message) {
        super(message, 404);
    }
}

