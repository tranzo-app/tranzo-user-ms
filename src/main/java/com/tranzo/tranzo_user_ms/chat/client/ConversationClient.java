package com.tranzo.tranzo_user_ms.chat.client;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface ConversationClient {
    UUID getConversationIdBetweenUsers(UUID userA, UUID userB);
}
