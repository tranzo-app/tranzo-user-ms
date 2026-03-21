package com.tranzo.tranzo_user_ms.chat.repository;

import com.tranzo.tranzo_user_ms.chat.model.MessageEntity;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, UUID> {

    /** Fetch latest messages (first page). No cursor; avoids NULL parameter binding issues on PostgreSQL. */
    @Query("""
        SELECT m
        FROM MessageEntity m
        WHERE m.conversation.conversationId = :conversationId
        ORDER BY m.createdAt ASC
    """)
    List<MessageEntity> findMessages(
            @Param("conversationId") UUID conversationId,
            Pageable pageable
    );

    /** Fetch messages before a given timestamp (pagination). */
    @Query("""
        SELECT m
        FROM MessageEntity m
        WHERE m.conversation.conversationId = :conversationId
          AND m.createdAt < :before
        ORDER BY m.createdAt ASC
    """)
    List<MessageEntity> findMessagesBefore(
            @Param("conversationId") UUID conversationId,
            @Param("before") LocalDateTime before,
            Pageable pageable
    );
}
