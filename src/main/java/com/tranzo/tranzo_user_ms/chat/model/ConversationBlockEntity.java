package com.tranzo.tranzo_user_ms.chat.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "conversation_block",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_block_unique",
                        columnNames = {"conversation_id", "blocked_by"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConversationBlockEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "conversation_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_block_conversation")
    )
    private ConversationEntity conversation;

    @Column(name = "blocked_by", nullable = false, updatable = false)
    private UUID blockedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private ConversationBlockEntity(
            ConversationEntity conversation,
            UUID blockedBy
    ) {
        this.id = UUID.randomUUID();
        this.conversation = conversation;
        this.blockedBy = blockedBy;
        this.createdAt = LocalDateTime.now();
    }

    public static ConversationBlockEntity create(
            ConversationEntity conversation,
            UUID blockedBy
    ) {
        return new ConversationBlockEntity(conversation, blockedBy);
    }
}
