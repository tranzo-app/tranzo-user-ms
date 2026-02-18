package com.tranzo.tranzo_user_ms.chat.dto;

import com.tranzo.tranzo_user_ms.chat.enums.ConversationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class CreateConversationResponseDto {
    private UUID conversationId;
    private ConversationType type;
    private LocalDateTime createdAt;
    private boolean existing;
}
