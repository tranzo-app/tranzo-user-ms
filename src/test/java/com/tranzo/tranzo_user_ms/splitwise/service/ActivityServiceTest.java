package com.tranzo.tranzo_user_ms.splitwise.service;

import com.tranzo.tranzo_user_ms.splitwise.entity.Activity;
import com.tranzo.tranzo_user_ms.splitwise.entity.SplitwiseGroup;
import com.tranzo.tranzo_user_ms.splitwise.repository.ActivityRepository;
import com.tranzo.tranzo_user_ms.user.model.UserProfileEntity;
import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import com.tranzo.tranzo_user_ms.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityService Unit Tests")
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ActivityService activityService;

    private UUID userId;
    private Long groupId;
    private SplitwiseGroup group;
    private UsersEntity user;
    private Activity activity;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        groupId = 1L;
        group = SplitwiseGroup.builder().id(groupId).tripId(UUID.randomUUID()).createdBy(userId).build();
        user = new UsersEntity();
        user.setUserUuid(userId);
        user.setEmail("u@test.com");
        UserProfileEntity profile = new UserProfileEntity();
        profile.setFirstName("Test");
        profile.setLastName("User");
        user.setUserProfileEntity(profile);
        activity = Activity.builder()
                .id(1L)
                .group(group)
                .userId(userId)
                .activityType(Activity.ActivityType.EXPENSE_ADDED)
                .description("Added expense")
                .relatedId("100")
                .relatedType("EXPENSE")
                .build();
    }

    @Test
    @DisplayName("Should log group created")
    void logGroupCreated_Success() {
        when(activityRepository.save(any(Activity.class))).thenReturn(activity);

        activityService.logGroupCreated(user, group);

        verify(activityRepository).save(any(Activity.class));
    }

    @Test
    @DisplayName("Should log group updated")
    void logGroupUpdated_Success() {
        when(activityRepository.save(any(Activity.class))).thenReturn(activity);

        activityService.logGroupUpdated(group, userId);

        verify(activityRepository).save(any(Activity.class));
    }

    @Test
    @DisplayName("Should log group deleted")
    void logGroupDeleted_Success() {
        when(activityRepository.save(any(Activity.class))).thenReturn(activity);

        activityService.logGroupDeleted(group, userId);

        verify(activityRepository).save(any(Activity.class));
    }

    @Test
    @DisplayName("Should log member added")
    void logMemberAdded_Success() {
        UUID memberId = UUID.randomUUID();
        when(userRepository.findById(memberId)).thenReturn(Optional.of(user));
        when(activityRepository.save(any(Activity.class))).thenReturn(activity);

        activityService.logMemberAdded(memberId, group, userId);

        verify(activityRepository).save(any(Activity.class));
    }

    @Test
    @DisplayName("Should log member removed")
    void logMemberRemoved_Success() {
        UUID memberId = UUID.randomUUID();
        when(userRepository.findById(memberId)).thenReturn(Optional.of(user));
        when(activityRepository.save(any(Activity.class))).thenReturn(activity);

        activityService.logMemberRemoved(memberId, group, userId);

        verify(activityRepository).save(any(Activity.class));
    }

    @Test
    @DisplayName("Should log expense created")
    void logExpenseCreated_Success() {
        when(activityRepository.save(any(Activity.class))).thenReturn(activity);

        activityService.logExpenseCreated(userId, group, 100L, "Dinner", new BigDecimal("100"));

        verify(activityRepository).save(any(Activity.class));
    }

    @Test
    @DisplayName("Should log expense updated")
    void logExpenseUpdated_Success() {
        when(activityRepository.save(any(Activity.class))).thenReturn(activity);

        activityService.logExpenseUpdated(userId, group, 100L, "Dinner");

        verify(activityRepository).save(any(Activity.class));
    }

    @Test
    @DisplayName("Should log expense deleted")
    void logExpenseDeleted_Success() {
        when(activityRepository.save(any(Activity.class))).thenReturn(activity);

        activityService.logExpenseDeleted(userId, group, 100L, "Dinner");

        verify(activityRepository).save(any(Activity.class));
    }

    @Test
    @DisplayName("Should log settlement created")
    void logSettlementCreated_Success() {
        when(activityRepository.save(any(Activity.class))).thenReturn(activity);

        activityService.logSettlementCreated(userId, group, 10L, new BigDecimal("50"));

        verify(activityRepository).save(any(Activity.class));
    }

    @Test
    @DisplayName("Should log settlement deleted")
    void logSettlementDeleted_Success() {
        when(activityRepository.save(any(Activity.class))).thenReturn(activity);

        activityService.logSettlementDeleted(userId, group, 10L, new BigDecimal("50"));

        verify(activityRepository).save(any(Activity.class));
    }

    @Test
    @DisplayName("Should get group activities")
    void getGroupActivities_Success() {
        when(activityRepository.findByGroupIdOrderByCreatedAtDesc(groupId)).thenReturn(List.of(activity));

        List<Activity> list = activityService.getGroupActivities(groupId);

        assertNotNull(list);
        assertEquals(1, list.size());
    }

    @Test
    @DisplayName("Should get group activities with limit and offset")
    void getGroupActivities_WithPagination() {
        when(activityRepository.findByGroupIdOrderByCreatedAtDesc(groupId, 10, 0)).thenReturn(List.of(activity));

        List<Activity> list = activityService.getGroupActivities(groupId, 10, 0);

        assertNotNull(list);
        assertEquals(1, list.size());
    }

    @Test
    @DisplayName("Should get user activities")
    void getUserActivities_Success() {
        when(activityRepository.findByUserIdOrderByCreatedAtDesc(userId, 100, 0)).thenReturn(List.of(activity));

        List<Activity> list = activityService.getUserActivities(userId);

        assertNotNull(list);
        assertEquals(1, list.size());
    }

    @Test
    @DisplayName("Should get user activities with limit and offset")
    void getUserActivities_WithPagination() {
        when(activityRepository.findByUserIdOrderByCreatedAtDesc(userId, 20, 5)).thenReturn(List.of(activity));

        List<Activity> list = activityService.getUserActivities(userId, 20, 5);

        assertNotNull(list);
        assertEquals(1, list.size());
    }
}
