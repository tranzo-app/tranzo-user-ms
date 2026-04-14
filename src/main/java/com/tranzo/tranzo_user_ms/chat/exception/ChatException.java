package com.tranzo.tranzo_user_ms.chat.exception;

import com.tranzo.tranzo_user_ms.chat.enums.ChatErrorCode;
import com.tranzo.tranzo_user_ms.commons.exception.BaseException;

/**
 * Base exception for chat module operations with error codes
 */
public class ChatException extends BaseException {
    private final ChatErrorCode errorCode;

    public ChatException(ChatErrorCode errorCode, String message, int statusCode) {
        super(message, statusCode);
        this.errorCode = errorCode;
    }

    public ChatErrorCode getErrorCode() {
        return errorCode;
    }
}
