package com.tranzo.tranzo_user_ms.splitwise.service;

import com.tranzo.tranzo_user_ms.splitwise.entity.Activity;
import com.tranzo.tranzo_user_ms.splitwise.entity.SplitwiseGroup;
import com.tranzo.tranzo_user_ms.splitwise.exception.UserNotMemberException;
import com.tranzo.tranzo_user_ms.splitwise.repository.ActivityRepository;
import com.tranzo.tranzo_user_ms.splitwise.repository.SplitwiseGroupRepository;
import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import com.tranzo.tranzo_user_ms.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
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
    private final SplitwiseGroupRepository splitwiseGroupRepository;

    public ActivityService(ActivityRepository activityRepository,
                           UserRepository userRepository,
                           SplitwiseGroupRepository splitwiseGroupRepository) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.splitwiseGroupRepository = splitwiseGroupRepository;
    }

    public void logGroupCreated(UsersEntity user, SplitwiseGroup group) {
        Activity activity = Activity.builder()
                .group(group)
                .userId(user != null ? user.getUserUuid() : null)
                .activityType(Activity.ActivityType.GROUP_CREATED)
                .description(group != null ? String.format("Created group for trip") : "Created group")
                .relatedId(group != null ? group.getId() : null)
                .relatedType("GROUP")
                .build();
        saveActivity(activity);
    }

    public void logGroupUpdated(SplitwiseGroup group, UUID updatedByUserId) {
        Activity activity = Activity.builder()
                .group(group)
                .userId(updatedByUserId)
                .activityType(Activity.ActivityType.GROUP_UPDATED)
                .description("Updated group")
                .relatedId(group != null ? group.getId() : null)
                .relatedType("GROUP")
                .build();
        saveActivity(activity);
    }

    public void logGroupDeleted(SplitwiseGroup group, UUID deletedByUserId) {
        Activity activity = Activity.builder()
                .group(group)
                .userId(deletedByUserId)
                .activityType(Activity.ActivityType.GROUP_DELETED)
                .description("Deleted group")
                .relatedId(group != null ? group.getId() : null)
                .relatedType("GROUP")
                .build();
        saveActivity(activity);
    }

    public void logMemberAdded(UUID member, SplitwiseGroup group, UUID addedByUserId) {
        String memberName = getUserName(member);
        Activity activity = Activity.builder()
                .group(group)
                .userId(addedByUserId)
                .activityType(Activity.ActivityType.MEMBER_ADDED)
                .description(String.format("Added %s to group", memberName != null ? memberName : member))
                .relatedId(member)
                .relatedType("USER")
                .build();
        saveActivity(activity);
    }

    public void logMemberRemoved(UUID member, SplitwiseGroup group, UUID removedByUserId) {
        String memberName = getUserName(member);
        Activity activity = Activity.builder()
                .group(group)
                .userId(removedByUserId)
                .activityType(Activity.ActivityType.MEMBER_REMOVED)
                .description(String.format("Removed %s from group", memberName != null ? memberName : member))
                .relatedId(member)
                .relatedType("USER")
                .build();
        saveActivity(activity);
    }

    public void logExpenseCreated(UUID user, SplitwiseGroup group, UUID expenseId, String expenseName, BigDecimal amount) {
        Activity activity = Activity.builder()
                .group(group)
                .userId(user)
                .activityType(Activity.ActivityType.EXPENSE_ADDED)
                .description(String.format("Added expense '%s' for ₹%.2f", expenseName, amount != null ? amount : BigDecimal.ZERO))
                .relatedId(expenseId)
                .relatedType("EXPENSE")
                .build();
        saveActivity(activity);
    }

    public void logExpenseUpdated(UUID user, SplitwiseGroup group, UUID expenseId, String expenseName) {
        Activity activity = Activity.builder()
                .group(group)
                .userId(user)
                .activityType(Activity.ActivityType.EXPENSE_UPDATED)
                .description(String.format("Updated expense '%s'", expenseName))
                .relatedId(expenseId)
                .relatedType("EXPENSE")
                .build();
        saveActivity(activity);
    }

    public void logExpenseDeleted(UUID user, SplitwiseGroup group, UUID expenseId, String expenseName) {
        Activity activity = Activity.builder()
                .group(group)
                .userId(user)
                .activityType(Activity.ActivityType.EXPENSE_DELETED)
                .description(String.format("Deleted expense '%s'", expenseName))
                .relatedId(expenseId)
                .relatedType("EXPENSE")
                .build();
        saveActivity(activity);
    }

    public void logSettlementCreated(UUID user, SplitwiseGroup group, UUID settlementId, BigDecimal amount) {
        Activity activity = Activity.builder()
                .group(group)
                .userId(user)
                .activityType(Activity.ActivityType.SETTLEMENT_CREATED)
                .description(String.format("Created settlement for ₹%.2f", amount != null ? amount : BigDecimal.ZERO))
                .relatedId(settlementId)
                .relatedType("SETTLEMENT")
                .build();
        saveActivity(activity);
    }

    public void logSettlementDeleted(UUID user, SplitwiseGroup group, UUID settlementId, BigDecimal amount) {
        Activity activity = Activity.builder()
                .group(group)
                .userId(user)
                .activityType(Activity.ActivityType.SETTLEMENT_DELETED)
                .description(String.format("Deleted settlement for ₹%.2f", amount != null ? amount : BigDecimal.ZERO))
                .relatedId(settlementId)
                .relatedType("SETTLEMENT")
                .build();
        saveActivity(activity);
    }

    @Transactional(readOnly = true)
    public List<Activity> getGroupActivities(UUID groupId, UUID currentUserId) {
        if (currentUserId != null && !splitwiseGroupRepository.isUserMemberOfGroup(groupId, currentUserId)) {
            throw new UserNotMemberException(currentUserId, groupId);
        }
        return activityRepository.findByGroupIdOrderByCreatedAtDesc(groupId);
    }

    @Transactional(readOnly = true)
    public List<Activity> getGroupActivities(UUID groupId, UUID currentUserId, int limit, int offset) {
        if (currentUserId != null && !splitwiseGroupRepository.isUserMemberOfGroup(groupId, currentUserId)) {
            throw new UserNotMemberException(currentUserId, groupId);
        }
        if (limit <= 0) limit = 10;
        int page = offset / limit;
        return activityRepository.findByGroupIdOrderByCreatedAtDesc(groupId, PageRequest.of(page, limit));
    }

    @Transactional(readOnly = true)
    public List<Activity> getUserActivities(UUID userId, int limit, int offset) {
        if (limit <= 0) limit = 10;
        int page = offset / limit;
        return activityRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, limit));
    }

    private void saveActivity(Activity activity) {
        activityRepository.save(activity);
    }

    private String getUserName(UUID userId) {
        if (userId == null) return null;
        return userRepository.findUserByUserUuid(userId)
                .map(u -> u.getUserProfileEntity() != null
                        ? (u.getUserProfileEntity().getFirstName() + " " + (u.getUserProfileEntity().getLastName() != null ? u.getUserProfileEntity().getLastName() : "")).trim()
                        : null)
                .orElse(null);
    }
}
