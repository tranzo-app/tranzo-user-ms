package com.tranzo.tranzo_user_ms.splitwise.service;

import com.tranzo.tranzo_user_ms.splitwise.entity.Activity;
import com.tranzo.tranzo_user_ms.splitwise.entity.SplitwiseGroup;
import com.tranzo.tranzo_user_ms.splitwise.repository.ActivityRepository;
import com.tranzo.tranzo_user_ms.user.repository.UserRepository;
import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Service for logging and retrieving activities in the Splitwise system.
 */
@Slf4j
@Service
@Transactional
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    public ActivityService(ActivityRepository activityRepository, UserRepository userRepository) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
    }

    /**
     * Logs group creation activity.
     */
    public void logGroupCreated(UsersEntity user, SplitwiseGroup group) {
        Activity activity = Activity.builder()
                .group(group)
                .userId(user.getUserUuid())
                .activityType(Activity.ActivityType.GROUP_CREATED)
                .description(String.format("Created group '%s'", group.getName()))
                .relatedId(group != null ? group.getId().toString() : null) // Store group ID as string
                .relatedType("GROUP")
                .build();
        
        saveActivity(activity);
    }

    /**
     * Logs group update activity.
     */
    public void logGroupUpdated(SplitwiseGroup group, UUID currentUserId) {
        Activity activity = Activity.builder()
                .group(group)
                .userId(currentUserId)
                .activityType(Activity.ActivityType.GROUP_UPDATED)
                .description("Updated group details")
                .relatedId(group != null ? group.getId().toString() : null) // Store group ID as string
                .relatedType("GROUP")
                .build();
        
        saveActivity(activity);
    }

    /**
     * Logs group deletion activity.
     */
    public void logGroupDeleted(SplitwiseGroup group, UUID currentUserId) {
        Activity activity = Activity.builder()
                .group(group)
                .userId(currentUserId)
                .activityType(Activity.ActivityType.GROUP_DELETED)
                .description(String.format("Deleted group '%s'", group.getName()))
                .relatedId(group != null ? group.getId().toString() : null) // Store group ID as string
                .relatedType("GROUP")
                .build();
        
        saveActivity(activity);
    }

    /**
     * Logs member addition activity.
     */
    public void logMemberAdded(UUID member, SplitwiseGroup group, UUID addedByUserId) {
        String memberName = getUserName(member);
        Activity activity = Activity.builder()
                .group(group)
                .userId(addedByUserId)
                .activityType(Activity.ActivityType.MEMBER_ADDED)
                .description(String.format("Added %s to group", memberName))
                .relatedId(member != null ? member.toString() : null) // Store member UUID as string
                .relatedType("USER")
                .build();
        
        saveActivity(activity);
    }

    /**
     * Logs member removal activity.
     */
    public void logMemberRemoved(UUID member, SplitwiseGroup group, UUID removedByUserId) {
        String memberName = getUserName(member);
        Activity activity = Activity.builder()
                .group(group)
                .userId(removedByUserId)
                .activityType(Activity.ActivityType.MEMBER_REMOVED)
                .description(String.format("Removed %s from group", memberName))
                .relatedId(member != null ? member.toString() : null) // Store member UUID as string
                .relatedType("USER")
                .build();
        
        saveActivity(activity);
    }

    /**
     * Logs expense creation activity.
     */
    public void logExpenseCreated(UUID user, SplitwiseGroup group, Long expenseId, String expenseName, BigDecimal amount) {
        Activity activity = Activity.builder()
                .group(group)
                .userId(user)
                .activityType(Activity.ActivityType.EXPENSE_ADDED)
                .description(String.format("Added expense '%s' for ₹%.2f", expenseName, amount))
                .relatedId(expenseId != null ? expenseId.toString() : null) // Store expense ID as string
                .relatedType("EXPENSE")
                .build();
        
        saveActivity(activity);
    }

    /**
     * Logs expense update activity.
     */
    public void logExpenseUpdated(UUID user, SplitwiseGroup group, Long expenseId, String expenseName) {
        Activity activity = Activity.builder()
                .group(group)
                .userId(user)
                .activityType(Activity.ActivityType.EXPENSE_UPDATED)
                .description(String.format("Updated expense '%s'", expenseName))
                .relatedId(expenseId != null ? expenseId.toString() : null) // Store expense ID as string
                .relatedType("EXPENSE")
                .build();
        
        saveActivity(activity);
    }

    /**
     * Logs expense deletion activity.
     */
    public void logExpenseDeleted(UUID user, SplitwiseGroup group, Long expenseId, String expenseName) {
        Activity activity = Activity.builder()
                .group(group)
                .userId(user)
                .activityType(Activity.ActivityType.EXPENSE_DELETED)
                .description(String.format("Deleted expense '%s'", expenseName))
                .relatedId(expenseId != null ? expenseId.toString() : null) // Store expense ID as string
                .relatedType("EXPENSE")
                .build();
        
        saveActivity(activity);
    }

    /**
     * Logs settlement creation activity.
     */
    public void logSettlementCreated(UUID user, SplitwiseGroup group, Long settlementId, BigDecimal amount) {
        Activity activity = Activity.builder()
                .group(group)
                .userId(user)
                .activityType(Activity.ActivityType.SETTLEMENT_CREATED)
                .description(String.format("Created settlement for ₹%.2f", amount))
                .relatedId(settlementId != null ? settlementId.toString() : null) // Store settlement ID as string
                .relatedType("SETTLEMENT")
                .build();
        
        saveActivity(activity);
    }

    /**
     * Logs settlement deletion activity.
     */
    public void logSettlementDeleted(UUID user, SplitwiseGroup group, Long settlementId, BigDecimal amount) {
        Activity activity = Activity.builder()
                .group(group)
                .userId(user)
                .activityType(Activity.ActivityType.SETTLEMENT_DELETED)
                .description(String.format("Deleted settlement for ₹%.2f", amount))
                .relatedId(settlementId != null ? settlementId.toString() : null) // Store settlement ID as string
                .relatedType("SETTLEMENT")
                .build();
        
        saveActivity(activity);
    }

    /**
     * Logs balance update activity.
     */
    public void logBalanceUpdated(UUID user, SplitwiseGroup group, String description) {
        Activity activity = Activity.builder()
                .group(group)
                .userId(user)
                .activityType(Activity.ActivityType.BALANCE_UPDATED)
                .description(description)
                .relatedId(group != null ? group.getId().toString() : null) // Store group ID as string
                .relatedType("GROUP")
                .build();
        
        saveActivity(activity);
    }

    /**
     * Gets activities for a specific group.
     */
    @Transactional(readOnly = true)
    public List<Activity> getGroupActivities(Long groupId) {
        log.debug("Fetching activities for group: {}", groupId);
        return activityRepository.findByGroupIdOrderByCreatedAtDesc(groupId);
    }

    /**
     * Gets activities for a specific user.
     */
    @Transactional(readOnly = true)
    public List<Activity> getUserActivities(UUID userId) {
        log.debug("Fetching activities for user: {}", userId);
        return activityRepository.findByUserIdOrderByCreatedAtDesc(userId, 100, 0);
    }

    /**
     * Gets activities for a group with pagination.
     */
    @Transactional(readOnly = true)
    public List<Activity> getGroupActivities(Long groupId, int limit, int offset) {
        log.debug("Fetching activities for group: {} with limit {} and offset {}", groupId, limit, offset);
        return activityRepository.findByGroupIdOrderByCreatedAtDesc(groupId, limit, offset);
    }

    /**
     * Gets activities for a user with pagination.
     */
    @Transactional(readOnly = true)
    public List<Activity> getUserActivities(UUID userId, int limit, int offset) {
        log.debug("Fetching activities for user: {} with limit {} and offset {}", userId, limit, offset);
        return activityRepository.findByUserIdOrderByCreatedAtDesc(userId, limit, offset);
    }

    /**
     * Saves an activity and handles any errors.
     */
    private void saveActivity(Activity activity) {
        try {
            activityRepository.save(activity);
            log.debug("Logged activity: {}", activity.getDescription());
        } catch (Exception e) {
            log.error("Failed to log activity: {}", activity.getDescription(), e);
            // Don't re-throw - activity logging failures shouldn't break the main flow
        }
    }

    /**
     * Gets user name from UUID.
     */
    private String getUserName(UUID userUuid) {
        if (userUuid == null) return "Unknown";
        
        UsersEntity user = userRepository.findById(userUuid).orElse(null);
        if (user == null) return "Unknown";
        
        var profile = user.getUserProfileEntity();
        if (profile == null) return user.getEmail();
        return (profile.getFirstName() + " " + 
                (profile.getLastName() != null ? profile.getLastName() : "")).trim();
    }
}
