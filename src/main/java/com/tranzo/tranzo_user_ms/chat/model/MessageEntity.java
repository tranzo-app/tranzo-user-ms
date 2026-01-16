package com.tranzo.tranzo_user_ms.chat.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageEntity {

    @Id
    @Column(name = "message_id", nullable = false, updatable = false)
    private UUID messageId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "conversation_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_message_conversation")
    )
    private ConversationEntity conversation;

    /**
     * NULL senderId means SYSTEM MESSAGE
     */
    @Column(name = "sender_id")
    private UUID senderId;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Private constructor to enforce valid creation
     */
    private MessageEntity(
            ConversationEntity conversation,
            UUID senderId,
            String content
    ) {
        this.messageId = UUID.randomUUID();
        this.conversation = conversation;
        this.senderId = senderId;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Factory method for USER message
     */
    public static MessageEntity userMessage(
            ConversationEntity conversation,
            UUID senderId,
            String content
    ) {
        Objects.requireNonNull(senderId, "senderId cannot be null for user message");
        Objects.requireNonNull(content, "content cannot be null");

        return new MessageEntity(conversation, senderId, content);
    }

    /**
     * Factory method for SYSTEM message
     */
    public static MessageEntity systemMessage(
            ConversationEntity conversation,
            String content
    ) {
        Objects.requireNonNull(content, "content cannot be null");

        return new MessageEntity(conversation, null, content);
    }
}

