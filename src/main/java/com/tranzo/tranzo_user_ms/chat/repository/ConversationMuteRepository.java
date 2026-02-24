package com.tranzo.tranzo_user_ms.chat.repository;

import com.tranzo.tranzo_user_ms.chat.model.ConversationMuteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationMuteRepository
        extends JpaRepository<ConversationMuteEntity, UUID> {

    boolean existsByConversation_ConversationIdAndUserId(
            UUID conversationId,
            UUID userId
    );

    Optional<ConversationMuteEntity>
    findByConversation_ConversationIdAndUserId(
            UUID conversationId,
            UUID userId
    );
}
