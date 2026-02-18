package com.tranzo.tranzo_user_ms.chat.socket.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class MessageReceivedPayload {

    private UUID messageId;
    private UUID conversationId;
    private UUID senderId;
    private String content;
    private LocalDateTime createdAt;
}
