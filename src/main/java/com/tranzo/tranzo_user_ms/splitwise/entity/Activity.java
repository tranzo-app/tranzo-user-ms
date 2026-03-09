package com.tranzo.tranzo_user_ms.splitwise.entity;

import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing an activity log in the Splitwise system.
 * Tracks all significant actions for audit and notification purposes.
 */
@Entity
@Table(name = "splitwise_activities")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private SplitwiseGroup group;

    @Column(name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    private ActivityType activityType;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "related_id")
    private String relatedId; // expense_id, settlement_id, group_id, user_id, etc.

    @Column(name = "related_type", length = 50)
    private String relatedType; // "EXPENSE", "SETTLEMENT", "GROUP", "USER"

    @Column(name = "old_value", length = 500)
    private String oldValue;

    @Column(name = "new_value", length = 500)
    private String newValue;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Enum defining types of activities that can be logged.
     */
    public enum ActivityType {
        EXPENSE_ADDED("Expense Added"),
        EXPENSE_UPDATED("Expense Updated"),
        EXPENSE_DELETED("Expense Deleted"),
        SETTLEMENT_CREATED("Settlement Created"),
        SETTLEMENT_UPDATED("Settlement Updated"),
        SETTLEMENT_DELETED("Settlement Deleted"),
        GROUP_CREATED("Group Created"),
        GROUP_UPDATED("Group Updated"),
        GROUP_DELETED("Group Deleted"),
        MEMBER_ADDED("Member Added"),
        MEMBER_REMOVED("Member Removed"),
        MEMBER_ROLE_CHANGED("Member Role Changed"),
        BALANCE_UPDATED("Balance Updated");

        private final String displayName;

        ActivityType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Creates an activity for expense-related actions.
     */
    public static Activity createExpenseActivity(ActivityType activityType, UsersEntity user,
                                               SplitwiseGroup group, Long expenseId, 
                                               String description) {
        return Activity.builder()
                .group(group)
                .userId(user != null ? user.getUserUuid() : null)
                .activityType(activityType)
                .description(description)
                .relatedId(expenseId != null ? expenseId.toString() : null)
                .relatedType("EXPENSE")
                .build();
    }

    /**
     * Creates an activity for settlement-related actions.
     */
    public static Activity createSettlementActivity(ActivityType activityType, UsersEntity user, 
                                                   SplitwiseGroup group, Long settlementId, 
                                                   String description) {
        return Activity.builder()
                .group(group)
                .userId(user != null ? user.getUserUuid() : null)
                .activityType(activityType)
                .description(description)
                .relatedId(settlementId != null ? settlementId.toString() : null)
                .relatedType("SETTLEMENT")
                .build();
    }

    /**
     * Creates an activity for group-related actions.
     */
    public static Activity createGroupActivity(ActivityType activityType, UsersEntity user, 
                                              SplitwiseGroup group, String description) {
        return Activity.builder()
                .group(group)
                .userId(user != null ? user.getUserUuid() : null)
                .activityType(activityType)
                .description(description)
                .relatedId(group != null ? group.getId().toString() : null)
                .relatedType("GROUP")
                .build();
    }

    /**
     * Creates an activity for member-related actions.
     */
    public static Activity createMemberActivity(ActivityType activityType, UsersEntity user, 
                                               SplitwiseGroup group, Long memberId, 
                                               String description) {
        return Activity.builder()
                .group(group)
                .userId(user != null ? user.getUserUuid() : null)
                .activityType(activityType)
                .description(description)
                .relatedId(memberId != null ? memberId.toString() : null)
                .relatedType("USER")
                .build();
    }

    /**
     * Sets old and new values for update activities.
     */
    public void setChangeValues(String oldValue, String newValue) {
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * Checks if this activity involves a specific user.
     */
    public boolean involvesUser(UUID userId) {
        return (this.userId != null && this.userId.equals(userId)) ||
               (relatedId != null && relatedId.equals(userId.toString()) && "USER".equals(relatedType));
    }

    /**
     * Gets a human-readable description of this activity.
     */
    public String getFullDescription() {
        StringBuilder fullDesc = new StringBuilder();
        
        // Note: Since we only store userId, we can't get user name here
        // The user name should be fetched at the service layer if needed
        fullDesc.append(activityType.getDisplayName().toLowerCase());
        
        if (description != null && !description.trim().isEmpty()) {
            fullDesc.append(": ").append(description);
        }
        
        return fullDesc.toString();
    }

    /**
     * Checks if this activity is related to a specific entity.
     */
    public boolean isRelatedTo(Long entityId, String entityType) {
        return relatedId != null && 
               relatedId.equals(entityId) && 
               relatedType != null && 
               relatedType.equals(entityType);
    }

    /**
     * Checks if this activity is a create operation.
     */
    public boolean isCreateOperation() {
        return activityType == ActivityType.EXPENSE_ADDED ||
               activityType == ActivityType.SETTLEMENT_CREATED ||
               activityType == ActivityType.GROUP_CREATED ||
               activityType == ActivityType.MEMBER_ADDED;
    }

    /**
     * Checks if this activity is an update operation.
     */
    public boolean isUpdateOperation() {
        return activityType == ActivityType.EXPENSE_UPDATED ||
               activityType == ActivityType.SETTLEMENT_UPDATED ||
               activityType == ActivityType.GROUP_UPDATED ||
               activityType == ActivityType.MEMBER_ROLE_CHANGED ||
               activityType == ActivityType.BALANCE_UPDATED;
    }

    /**
     * Checks if this activity is a delete operation.
     */
    public boolean isDeleteOperation() {
        return activityType == ActivityType.EXPENSE_DELETED ||
               activityType == ActivityType.SETTLEMENT_DELETED ||
               activityType == ActivityType.GROUP_DELETED ||
               activityType == ActivityType.MEMBER_REMOVED;
    }

    /**
     * Validates that this activity is valid.
     */
    public boolean isValid() {
        return activityType != null && 
               userId != null && 
               createdAt != null;
    }
}
