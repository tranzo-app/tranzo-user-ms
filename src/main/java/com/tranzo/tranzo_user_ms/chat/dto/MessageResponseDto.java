package com.tranzo.tranzo_user_ms.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@AllArgsConstructor
@Getter
public class MessageResponseDto {
    private UUID messageId;
    private UUID conversationId;
    private UUID senderId;
    private String content;
    private LocalDateTime createdAt;
}
