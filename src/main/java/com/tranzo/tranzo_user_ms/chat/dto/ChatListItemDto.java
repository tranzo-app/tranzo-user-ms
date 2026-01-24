package com.tranzo.tranzo_user_ms.chat.dto;

import com.tranzo.tranzo_user_ms.chat.enums.ConversationType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ChatListItemDto {

    private UUID conversationId;
    private ConversationType type;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private boolean muted;
    private int unreadCount;

}
