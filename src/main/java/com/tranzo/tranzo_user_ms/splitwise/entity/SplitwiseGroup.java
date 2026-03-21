package com.tranzo.tranzo_user_ms.splitwise.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a group in the Splitwise system.
 * Groups contain members who can share expenses among themselves.
 * This entity has a 1-1 relationship with TripEntity using logical foreign key.
 */
@Entity
@Table(name = "splitwise_groups")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SplitwiseGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "trip_id", nullable = false, unique = true)
    private UUID tripId;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GroupMember> members = new ArrayList<>();

    /**
     * Gets the group name from the associated trip (would need service call to fetch trip details).
     */
    public String getName() {
        // This would be populated by service layer when fetching with trip details
        return null; // Will be set by service
    }

    /**
     * Adds a member to the group.
     */
    public void addMember(GroupMember member) {
        members.add(member);
        member.setGroup(this);
    }

    /**
     * Removes a member from the group.
     */
    public void removeMember(GroupMember member) {
        members.remove(member);
        member.setGroup(null);
    }

    /**
     * Checks if a user is a member of this group.
     */
    public boolean isMember(UUID userId) {
        return members.stream()
                .anyMatch(member -> member.getUserId().equals(userId));
    }

    /**
     * Gets the admin members of this group.
     */
    public List<GroupMember> getAdminMembers() {
        return members.stream()
                .filter(member -> member.getRole() == GroupMember.MemberRole.ADMIN)
                .toList();
    }


}
