package com.tranzo.tranzo_user_ms.notification.service;

import com.tranzo.tranzo_user_ms.notification.enums.NotificationType;
import com.tranzo.tranzo_user_ms.notification.model.UserNotificationEntity;
import com.tranzo.tranzo_user_ms.notification.repository.UserNotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Unit Tests")
class NotificationServiceTest {

    @Mock
    private UserNotificationRepository userNotificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private UUID userId;
    private UUID tripId;
    private UUID notificationId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        tripId = UUID.randomUUID();
        notificationId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should create notification successfully")
    void testCreateNotification_Success() {
        when(userNotificationRepository.save(any(UserNotificationEntity.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        notificationService.createNotification(
            userId, tripId, NotificationType.TRIP_CANCELLED, "Trip cancelled", "Your trip was cancelled.");

        ArgumentCaptor<UserNotificationEntity> captor = ArgumentCaptor.forClass(UserNotificationEntity.class);
        verify(userNotificationRepository).save(captor.capture());
        UserNotificationEntity saved = captor.getValue();
        assertEquals(userId, saved.getUserId());
        assertEquals(tripId, saved.getTripId());
        assertEquals(NotificationType.TRIP_CANCELLED, saved.getType());
        assertEquals("Trip cancelled", saved.getTitle());
        assertEquals("Your trip was cancelled.", saved.getBody());
    }

    @Test
    @DisplayName("Should create notifications for multiple users")
    void testCreateNotificationsForUsers_Success() {
        List<UUID> userIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        when(userNotificationRepository.save(any(UserNotificationEntity.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        notificationService.createNotificationsForUsers(
            userIds, tripId, NotificationType.MEMBER_JOINED_TRIP, "New member", "Someone joined.");

        verify(userNotificationRepository, times(2)).save(any(UserNotificationEntity.class));
    }

    @Test
    @DisplayName("Should get notifications for user with pageable")
    void testGetNotificationsForUser_Success() {
        Page<UserNotificationEntity> page = mock(Page.class);
        when(userNotificationRepository.findByUserIdOrderByCreatedAtDesc(eq(userId), any(Pageable.class)))
            .thenReturn(page);

        Page<UserNotificationEntity> result = notificationService.getNotificationsForUser(userId, Pageable.unpaged());

        assertSame(page, result);
        verify(userNotificationRepository).findByUserIdOrderByCreatedAtDesc(eq(userId), any(Pageable.class));
    }

    @Test
    @DisplayName("Should return unread count")
    void testGetUnreadCount_Success() {
        when(userNotificationRepository.countByUserIdAndReadAtIsNull(userId)).thenReturn(5L);

        long count = notificationService.getUnreadCount(userId);

        assertEquals(5L, count);
    }

    @Test
    @DisplayName("Should mark notification as read when owned by user")
    void testMarkAsRead_Success() {
        UserNotificationEntity entity = UserNotificationEntity.builder()
            .notificationId(notificationId)
            .userId(userId)
            .tripId(tripId)
            .type(NotificationType.TRIP_COMPLETED)
            .title("Trip completed")
            .body("Rate your trip")
            .readAt(null)
            .build();
        when(userNotificationRepository.findById(notificationId)).thenReturn(Optional.of(entity));
        when(userNotificationRepository.save(any(UserNotificationEntity.class))).thenReturn(entity);

        notificationService.markAsRead(notificationId, userId);

        assertNotNull(entity.getReadAt());
        verify(userNotificationRepository).save(entity);
    }

    @Test
    @DisplayName("Should not mark as read when notification belongs to another user")
    void testMarkAsRead_WrongUser() {
        UUID otherUserId = UUID.randomUUID();
        UserNotificationEntity entity = UserNotificationEntity.builder()
            .notificationId(notificationId)
            .userId(otherUserId)
            .tripId(tripId)
            .type(NotificationType.TRIP_COMPLETED)
            .title("Trip completed")
            .body("Rate your trip")
            .readAt(null)
            .build();
        when(userNotificationRepository.findById(notificationId)).thenReturn(Optional.of(entity));

        notificationService.markAsRead(notificationId, userId);

        verify(userNotificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should not mark as read when already read")
    void testMarkAsRead_AlreadyRead() {
        UserNotificationEntity entity = UserNotificationEntity.builder()
            .notificationId(notificationId)
            .userId(userId)
            .readAt(LocalDateTime.now())
            .build();
        when(userNotificationRepository.findById(notificationId)).thenReturn(Optional.of(entity));

        notificationService.markAsRead(notificationId, userId);

        verify(userNotificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should mark all as read for user")
    void testMarkAllAsRead_Success() {
        UserNotificationEntity unread = UserNotificationEntity.builder()
            .notificationId(notificationId)
            .userId(userId)
            .readAt(null)
            .build();
        Page<UserNotificationEntity> page = mock(Page.class);
        when(page.getContent()).thenReturn(List.of(unread));
        when(userNotificationRepository.findByUserIdOrderByCreatedAtDesc(eq(userId), any(Pageable.class)))
            .thenReturn(page);
        when(userNotificationRepository.save(any(UserNotificationEntity.class))).thenReturn(unread);

        notificationService.markAllAsRead(userId);

        verify(userNotificationRepository).save(unread);
    }
}
