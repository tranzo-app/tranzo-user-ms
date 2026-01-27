package com.tranzo.tranzo_user_ms.chat.repository;

import com.tranzo.tranzo_user_ms.chat.model.MessageEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, UUID> {
    @Query("""
        SELECT m
        FROM MessageEntity m
        WHERE m.conversation.conversationId = :conversationId
          AND (:before IS NULL OR m.createdAt < :before)
        ORDER BY m.createdAt DESC
    """)
    List<MessageEntity> findMessages(
            @Param("conversationId") UUID conversationId,
            @Param("before") LocalDateTime before,
            Pageable pageable
    );
}
