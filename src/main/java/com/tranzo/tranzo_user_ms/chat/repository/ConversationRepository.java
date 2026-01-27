package com.tranzo.tranzo_user_ms.chat.repository;

import com.tranzo.tranzo_user_ms.chat.dto.ChatListItemDto;
import com.tranzo.tranzo_user_ms.chat.model.ConversationEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface ConversationRepository extends JpaRepository<ConversationEntity, UUID> {

    @Query("""
    SELECT conversation
    FROM ConversationEntity conversation
    JOIN conversation.participants participant
    WHERE conversation.type = 'ONE_ON_ONE'
      AND participant.userId IN (:firstUserId, :secondUserId)
      AND participant.leftAt IS NULL
    GROUP BY conversation.conversationId
    HAVING COUNT(DISTINCT participant.userId) = 2""")
    Optional<ConversationEntity> findOneToOneConversationBetweenUsers(
            @Param("firstUserId") UUID firstUserId,
            @Param("secondUserId") UUID secondUserId
    );

    @Query("""
    SELECT new com.tranzo.tranzo_user_ms.chat.dto.ChatListItemDto(
        c.conversationId,
        c.type,

        /* Last message content */
        COALESCE((
            SELECT m.content
            FROM MessageEntity m
            WHERE m.conversation.conversationId = c.conversationId
              AND m.createdAt = (
                  SELECT MAX(m2.createdAt)
                  FROM MessageEntity m2
                  WHERE m2.conversation.conversationId = c.conversationId
              )
        ), ''),

        /* Last activity time */
        COALESCE((
            SELECT MAX(m.createdAt)
            FROM MessageEntity m
            WHERE m.conversation.conversationId = c.conversationId
        ), c.createdAt),

        /* Muted or not */
        CASE
            WHEN EXISTS (
                SELECT 1
                FROM ConversationMuteEntity mute
                WHERE mute.conversation.conversationId = c.conversationId
                  AND mute.userId = :currentUserId
            )
            THEN true
            ELSE false
        END,

        /* Unread count */
        (
            SELECT COUNT(m.messageId)
            FROM MessageEntity m
            WHERE m.conversation.conversationId = c.conversationId
              AND m.senderId <> :currentUserId
              AND m.createdAt > (
                  SELECT COALESCE(cp.lastReadAt, c.createdAt)
                  FROM ConversationParticipantEntity cp
                  WHERE cp.conversation.conversationId = c.conversationId
                    AND cp.userId = :currentUserId
              )
        )
    )
    FROM ConversationEntity c
    WHERE EXISTS (
        SELECT 1
        FROM ConversationParticipantEntity cp
        WHERE cp.conversation.conversationId = c.conversationId
          AND cp.userId = :currentUserId
          AND cp.leftAt IS NULL
    )
    ORDER BY COALESCE((
        SELECT MAX(m.createdAt)
        FROM MessageEntity m
        WHERE m.conversation.conversationId = c.conversationId
    ), c.createdAt) DESC
""")
    List<ChatListItemDto> findChatListForUser(
            @Param("currentUserId") UUID currentUserId
    );



}
