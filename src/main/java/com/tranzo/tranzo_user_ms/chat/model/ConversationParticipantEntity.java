package com.tranzo.tranzo_user_ms.chat.model;

import com.tranzo.tranzo_user_ms.chat.enums.ConversationRole;
import com.twilio.rest.conversations.v1.Conversation;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "conversation_participant",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_conversation_user",
                        columnNames = {"conversation_id", "user_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConversationParticipantEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "conversation_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_cp_conversation")
    )
    private ConversationEntity conversation;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private ConversationRole role;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;

    private ConversationParticipantEntity(
            ConversationEntity conversation,
            UUID userId,
            ConversationRole role
    ) {
        this.id = UUID.randomUUID();
        this.conversation = conversation;
        this.userId = userId;
        this.role = role;
        this.joinedAt = LocalDateTime.now();
    }

    public static ConversationParticipantEntity create(
            ConversationEntity conversation,
            UUID userId,
            ConversationRole role
    ) {
        return new ConversationParticipantEntity(conversation, userId, role);
    }

    public boolean isActive() {
        return leftAt == null;
    }

    public void leave() {
        this.leftAt = LocalDateTime.now();
    }

    public void rejoin() {
        this.leftAt = null;
    }

    public void markAsRead() {
        this.lastReadAt = LocalDateTime.now();
    }

}
