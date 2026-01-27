package com.tranzo.tranzo_user_ms.chat.model;


import com.tranzo.tranzo_user_ms.chat.enums.ConversationRole;
import com.tranzo.tranzo_user_ms.chat.enums.ConversationType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "conversation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConversationEntity {

    @Id
    @Column(name = "conversation_id", nullable = false, updatable = false)
    private UUID conversationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ConversationType type;

    @Column(name = "created_by", nullable = false, updatable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * IMPORTANT:
     * - Using Set to prevent duplicate participants
     * - Matches DB unique constraint (conversation_id, user_id)
     */
    @OneToMany(
            mappedBy = "conversation",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<ConversationParticipantEntity> participants = new HashSet<>();

    protected ConversationEntity(
            UUID conversationId,
            ConversationType type,
            UUID createdBy
    ) {
        this.conversationId = conversationId;
        this.type = type;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    public static ConversationEntity createOneToOneChat(UUID createdBy) {
        return new ConversationEntity(UUID.randomUUID(), ConversationType.ONE_ON_ONE, createdBy);
    }

    public static ConversationEntity createGroup(UUID createdBy) {
        return new ConversationEntity(UUID.randomUUID(), ConversationType.GROUP_CHAT, createdBy);
    }

    public void addParticipant(UUID userId, ConversationRole role) {
        ConversationParticipantEntity participant =
                ConversationParticipantEntity.create(this, userId, role);
        participant.setLastReadAt(null);
        participants.add(participant); // Set prevents duplicates
    }
}
