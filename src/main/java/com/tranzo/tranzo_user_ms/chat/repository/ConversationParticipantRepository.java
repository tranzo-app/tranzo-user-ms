package com.tranzo.tranzo_user_ms.chat.repository;

import com.tranzo.tranzo_user_ms.chat.model.ConversationParticipantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipantEntity, UUID> {

    Optional<ConversationParticipantEntity> findByConversation_ConversationIdAndUserId(UUID conversationId, UUID userId);
    Optional<ConversationParticipantEntity> findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(UUID conversationId, UUID userId);

}
