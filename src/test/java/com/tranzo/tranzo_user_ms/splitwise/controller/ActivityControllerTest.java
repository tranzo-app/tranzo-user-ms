package com.tranzo.tranzo_user_ms.splitwise.controller;

import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.ActivityResponse;
import com.tranzo.tranzo_user_ms.splitwise.entity.Activity;
import com.tranzo.tranzo_user_ms.splitwise.entity.SplitwiseGroup;
import com.tranzo.tranzo_user_ms.splitwise.service.ActivityService;
import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import com.tranzo.tranzo_user_ms.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityController Unit Tests")
class ActivityControllerTest {

    @Mock
    private ActivityService activityService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ActivityController controller;

    private Long groupId;
    private UUID userId;
    private Activity activity;
    private List<Activity> activityList;

    @BeforeEach
    void setUp() {
        groupId = 1L;
        userId = UUID.randomUUID();
        SplitwiseGroup group = SplitwiseGroup.builder().id(groupId).tripId(UUID.randomUUID()).createdBy(userId).build();
        activity = Activity.builder()
                .id(1L)
                .group(group)
                .userId(userId)
                .activityType(Activity.ActivityType.EXPENSE_ADDED)
                .description("Added expense")
                .relatedId("100")
                .relatedType("EXPENSE")
                .build();
        activityList = List.of(activity);
    }

    @Test
    @DisplayName("Should get group activities")
    void getGroupActivities_Success() {
        when(activityService.getGroupActivities(groupId, 10, 0)).thenReturn(activityList);
        when(userRepository.findById(userId)).thenReturn(Optional.of(new UsersEntity()));

        ResponseEntity<List<ActivityResponse>> res = controller.getGroupActivities(groupId, 10, 0);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNotNull(res.getBody());
        assertEquals(1, res.getBody().size());
    }

    @Test
    @DisplayName("Should get my activities")
    void getUserActivities_Success() throws Exception {
        when(activityService.getUserActivities(userId, 10, 0)).thenReturn(activityList);
        when(userRepository.findById(userId)).thenReturn(Optional.of(new UsersEntity()));
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<List<ActivityResponse>> res = controller.getUserActivities(10, 0);

            assertEquals(HttpStatus.OK, res.getStatusCode());
            assertNotNull(res.getBody());
        }
    }

    @Test
    @DisplayName("Should return 401 when auth fails for my activities")
    void getUserActivities_Unauthorized() throws Exception {
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenThrow(new jakarta.security.auth.message.AuthException("Unauthenticated"));

            ResponseEntity<List<ActivityResponse>> res = controller.getUserActivities(10, 0);

            assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode());
        }
    }
}
