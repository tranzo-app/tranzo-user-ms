package com.tranzo.tranzo_user_ms.chat.repository;

import com.tranzo.tranzo_user_ms.chat.model.ConversationBlockEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConversationBlockRepository extends JpaRepository<ConversationBlockEntity, UUID> {
    boolean existsByConversation_ConversationIdAndUserId(UUID conversationId, UUID userId);
    Optional<ConversationBlockEntity> findByConversation_ConversationIdAndUserId(UUID conversationId, UUID userId);
}
