package com.tranzo.tranzo_user_ms.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendMessageResponseDto {
    private UUID messageId;
    private UUID conversationId;
    private UUID senderId;      // null for system messages (not here)
    private String content;
    private LocalDateTime createdAt;
}
