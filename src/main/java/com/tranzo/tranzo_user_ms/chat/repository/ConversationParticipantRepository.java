package com.tranzo.tranzo_user_ms.chat.repository;

import com.tranzo.tranzo_user_ms.chat.model.ConversationParticipantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipantEntity, UUID> {

    Optional<ConversationParticipantEntity> findByConversation_ConversationIdAndUserId(UUID conversationId, UUID userId);
    Optional<ConversationParticipantEntity> findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(UUID conversationId, UUID userId);

}
