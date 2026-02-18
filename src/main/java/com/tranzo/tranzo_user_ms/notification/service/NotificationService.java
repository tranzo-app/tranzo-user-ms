package com.tranzo.tranzo_user_ms.notification.service;

import com.tranzo.tranzo_user_ms.notification.enums.NotificationType;
import com.tranzo.tranzo_user_ms.notification.model.UserNotificationEntity;
import com.tranzo.tranzo_user_ms.notification.repository.UserNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final UserNotificationRepository userNotificationRepository;

    public void createNotification(UUID userId, UUID tripId, NotificationType type, String title, String body) {
        UserNotificationEntity entity = UserNotificationEntity.builder()
                .userId(userId)
                .tripId(tripId)
                .type(type)
                .title(title)
                .body(body)
                .build();
        userNotificationRepository.save(entity);
        log.debug("Created notification type={} for user={} trip={}", type, userId, tripId);
    }

    public void createNotificationsForUsers(List<UUID> userIds, UUID tripId, NotificationType type, String title, String body) {
        for (UUID userId : userIds) {
            createNotification(userId, tripId, type, title, body);
        }
    }

    public Page<UserNotificationEntity> getNotificationsForUser(UUID userId, Pageable pageable) {
        return userNotificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public long getUnreadCount(UUID userId) {
        return userNotificationRepository.countByUserIdAndReadAtIsNull(userId);
    }

    public void markAsRead(UUID notificationId, UUID userId) {
        userNotificationRepository.findById(notificationId).ifPresent(notification -> {
            if (notification.getUserId().equals(userId) && notification.getReadAt() == null) {
                notification.setReadAt(LocalDateTime.now());
                userNotificationRepository.save(notification);
            }
        });
    }

    public void markAllAsRead(UUID userId) {
        userNotificationRepository.findByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged())
                .getContent()
                .stream()
                .filter(n -> n.getReadAt() == null)
                .forEach(n -> {
                    n.setReadAt(LocalDateTime.now());
                    userNotificationRepository.save(n);
                });
    }
}
