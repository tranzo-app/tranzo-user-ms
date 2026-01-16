package com.tranzo.tranzo_user_ms.chat.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "conversation_mute",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_mute_unique",
                        columnNames = {"conversation_id", "user_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConversationMuteEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "conversation_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_mute_conversation")
    )
    private ConversationEntity conversation;

    /**
     * User who muted the conversation
     */
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "muted_at", nullable = false, updatable = false)
    private LocalDateTime mutedAt;

    private ConversationMuteEntity(
            ConversationEntity conversation,
            UUID userId
    ) {
        this.id = UUID.randomUUID();
        this.conversation = conversation;
        this.userId = userId;
        this.mutedAt = LocalDateTime.now();
    }

    public static ConversationMuteEntity create(
            ConversationEntity conversation,
            UUID userId
    ) {
        return new ConversationMuteEntity(conversation, userId);
    }
}

