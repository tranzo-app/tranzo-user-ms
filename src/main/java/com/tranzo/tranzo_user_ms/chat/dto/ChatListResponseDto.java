package com.tranzo.tranzo_user_ms.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ChatListResponseDto {
    private List<ChatListItemDto> conversations;
    private int totalUnreadCount;
}

