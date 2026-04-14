package com.tranzo.tranzo_user_ms.chat.enums;

/**
 * Error codes for chat module operations
 */
public enum ChatErrorCode {
    // Conversation errors
    CONVERSATION_NOT_FOUND("CONVERSATION_NOT_FOUND"),
    RECIPIENT_NOT_FOUND("RECIPIENT_NOT_FOUND"),
    TRIP_NOT_FOUND("TRIP_NOT_FOUND"),
    
    // Message errors
    INVALID_MESSAGE("INVALID_MESSAGE"),
    MESSAGE_TOO_LONG("MESSAGE_TOO_LONG"),
    MESSAGE_EMPTY("MESSAGE_EMPTY"),
    
    // User participation errors
    USER_NOT_IN_CONVERSATION("USER_NOT_IN_CONVERSATION"),
    USER_LEFT_CONVERSATION("USER_LEFT_CONVERSATION"),
    USER_BLOCKED("USER_BLOCKED"),
    USER_MUTED("USER_MUTED"),
    
    // Operation errors
    BLOCK_NOT_ALLOWED("BLOCK_NOT_ALLOWED"),
    UNBLOCK_NOT_ALLOWED("UNBLOCK_NOT_ALLOWED"),
    SELF_CONVERSATION("SELF_CONVERSATION"),
    
    // Validation errors
    INVALID_LIMIT("INVALID_LIMIT"),
    INVALID_CONVERSATION_TYPE("INVALID_CONVERSATION_TYPE");

    private final String code;

    ChatErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
