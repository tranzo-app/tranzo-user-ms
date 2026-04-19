package com.tranzo.tranzo_user_ms.chat.client;

import com.tranzo.tranzo_user_ms.chat.model.ConversationEntity;
import com.tranzo.tranzo_user_ms.chat.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ConversationClientLocalImpl implements ConversationClient {
    private final ConversationRepository conversationRepository;

    @Override
    public UUID getConversationIdBetweenUsers(UUID userA, UUID userB) {
        return conversationRepository.findOneToOneConversationBetweenUsers(userA, userB)
                .map(ConversationEntity::getConversationId)
                .orElse(null);
    }
}
