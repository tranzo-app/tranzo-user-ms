package com.tranzo.tranzo_user_ms.notification.controller;

import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import com.tranzo.tranzo_user_ms.notification.dto.NotificationResponseDto;
import com.tranzo.tranzo_user_ms.notification.enums.NotificationType;
import com.tranzo.tranzo_user_ms.notification.model.UserNotificationEntity;
import com.tranzo.tranzo_user_ms.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.data.domain.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationController Unit Tests")
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should get my notifications successfully")
    void testGetMyNotifications_Success() throws Exception {
        UserNotificationEntity entity = UserNotificationEntity.builder()
            .notificationId(UUID.randomUUID())
            .userId(userId)
            .tripId(UUID.randomUUID())
            .type(NotificationType.TRIP_CANCELLED)
            .title("Trip cancelled")
            .body("Your trip was cancelled.")
            .build();
        PageImpl<UserNotificationEntity> page = new PageImpl<>(List.of(entity));

        when(notificationService.getNotificationsForUser(eq(userId), any())).thenReturn(page);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<ResponseDto<Page<NotificationResponseDto>>> response = notificationController.getMyNotifications(0, 20);

            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            verify(notificationService).getNotificationsForUser(eq(userId), any());
        }
    }

    @Test
    @DisplayName("Should get unread count successfully")
    void testGetUnreadCount_Success() throws Exception {
        when(notificationService.getUnreadCount(userId)).thenReturn(3L);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<ResponseDto<Long>> response = notificationController.getUnreadCount();

            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertEquals(3L, response.getBody().getData());
        }
    }

    @Test
    @DisplayName("Should mark notification as read successfully")
    void testMarkAsRead_Success() throws Exception {
        UUID notificationId = UUID.randomUUID();
        doNothing().when(notificationService).markAsRead(eq(notificationId), eq(userId));

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<ResponseDto<Void>> response = notificationController.markAsRead(notificationId);

            assertEquals(200, response.getStatusCode().value());
            verify(notificationService).markAsRead(notificationId, userId);
        }
    }

    @Test
    @DisplayName("Should mark all as read successfully")
    void testMarkAllAsRead_Success() throws Exception {
        doNothing().when(notificationService).markAllAsRead(userId);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<ResponseDto<Void>> response = notificationController.markAllAsRead();

            assertEquals(200, response.getStatusCode().value());
            verify(notificationService).markAllAsRead(userId);
        }
    }
}
