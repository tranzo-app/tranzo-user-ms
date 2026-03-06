package com.tranzo.tranzo_user_ms.splitwise.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing the membership relationship between a user and a group.
 */
@Entity
@Table(name = "splitwise_group_members")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private SplitwiseGroup group;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private MemberRole role = MemberRole.MEMBER;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    /**
     * Enum defining member roles within a group.
     */
    public enum MemberRole {
        ADMIN("Administrator"),
        MEMBER("Member");

        private final String displayName;

        MemberRole(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Checks if this member is an admin.
     */
    public boolean isAdmin() {
        return role == MemberRole.ADMIN;
    }

    /**
     * Promotes this member to admin role.
     */
    public void promoteToAdmin() {
        this.role = MemberRole.ADMIN;
    }

    /**
     * Demotes this member to regular member role.
     */
    public void demoteToMember() {
        this.role = MemberRole.MEMBER;
    }

    public UUID getUserId() {
        return userId;
    }
}
