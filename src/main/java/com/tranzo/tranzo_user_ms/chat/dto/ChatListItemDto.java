package com.tranzo.tranzo_user_ms.chat.dto;

import com.tranzo.tranzo_user_ms.chat.enums.ConversationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ChatListItemDto {

    private UUID conversationId;
    private ConversationType type;
    private String lastMessage;
    private String conversationName;
    private LocalDateTime lastMessageAt;
    private Boolean muted;
    private Long unreadCount;
    private String profilePictureUrl;

    public ChatListItemDto(
            UUID conversationId,
            ConversationType type,
            String lastMessage,
            String conversationName,
            LocalDateTime lastMessageAt,
            Boolean muted,
            Long unreadCount,
            String profilePictureUrl
    ) {
        this.conversationId = conversationId;
        this.type = type;
        this.lastMessage = lastMessage;
        this.lastMessageAt = lastMessageAt;
        this.muted = muted;
        this.unreadCount = unreadCount;
        this.conversationName = conversationName;
        this.profilePictureUrl = profilePictureUrl;
    }
}
