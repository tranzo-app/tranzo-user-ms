package com.tranzo.tranzo_user_ms.chat.repository;

import com.tranzo.tranzo_user_ms.chat.dto.ChatListItemDto;
import com.tranzo.tranzo_user_ms.chat.model.ConversationEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<ConversationEntity, UUID> {

    @Query("""
    SELECT conversation
    FROM ConversationEntity conversation
    JOIN conversation.participants participant
    WHERE conversation.type = 'ONE_TO_ONE'
      AND participant.userId IN (:firstUserId, :secondUserId)
      AND participant.leftAt IS NULL
    GROUP BY conversation.conversationId
    HAVING COUNT(DISTINCT participant.userId) = 2""")
    Optional<ConversationEntity> findOneToOneConversationBetweenUsers(
            @Param("firstUserId") UUID firstUserId,
            @Param("secondUserId") UUID secondUserId
    );

    @Query("""
        SELECT new com.tranzo.chat.dto.ChatListItemDto(
            conversation.conversationId,
            conversation.type,
            conversation.title,
            conversation.avatarUrl,
            lastMessage.content,
            COALESCE(lastMessage.createdAt, conversation.createdAt),
            COUNT(unreadMessage.messageId),
            CASE WHEN mute.id IS NOT NULL THEN true ELSE false END
        )
        FROM ConversationEntity conversation
        JOIN conversation.participants participant
            ON participant.userId = :currentUserId
            AND participant.leftAt IS NULL

        LEFT JOIN MessageEntity lastMessage
            ON lastMessage.conversation = conversation
            AND lastMessage.createdAt = (
                SELECT MAX(m.createdAt)
                FROM MessageEntity m
                WHERE m.conversation = conversation
            )

        LEFT JOIN MessageEntity unreadMessage
            ON unreadMessage.conversation = conversation
            AND unreadMessage.createdAt >
                COALESCE(participant.lastReadAt, conversation.createdAt)
            AND unreadMessage.senderId <> :currentUserId

        LEFT JOIN ConversationMuteEntity mute
            ON mute.conversation = conversation
            AND mute.userId = :currentUserId

        GROUP BY
            conversation.conversationId,
            conversation.type,
            conversation.title,
            conversation.avatarUrl,
            lastMessage.content,
            lastMessage.createdAt,
            conversation.createdAt,
            mute.id

        ORDER BY COALESCE(lastMessage.createdAt, conversation.createdAt) DESC
    """)
    List<ChatListItemDto> findChatListForUser(
            @Param("currentUserId") UUID currentUserId
    );

}
