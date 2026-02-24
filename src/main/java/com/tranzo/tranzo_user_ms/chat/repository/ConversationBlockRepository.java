package com.tranzo.tranzo_user_ms.chat.repository;

import com.tranzo.tranzo_user_ms.chat.model.ConversationBlockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationBlockRepository extends JpaRepository<ConversationBlockEntity, UUID> {
    boolean existsByConversation_ConversationId(UUID conversationId);
    boolean existsByConversation_ConversationIdAndBlockedBy(UUID conversationId, UUID blockedBy);
    Optional<ConversationBlockEntity> findByConversation_ConversationIdAndBlockedBy(UUID conversationId, UUID blockedBy);
}
