package com.tranzo.tranzo_user_ms.notification.service;

import com.tranzo.tranzo_user_ms.notification.enums.NotificationType;
import com.tranzo.tranzo_user_ms.notification.exception.NotificationNotFoundException;
import com.tranzo.tranzo_user_ms.notification.exception.NotificationAccessDeniedException;
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
        log.info("Processing started | operation=createNotification | userId={} | tripId={} | type={}", userId, tripId, type);
        
        try {
            UserNotificationEntity entity = UserNotificationEntity.builder()
                    .userId(userId)
                    .tripId(tripId)
                    .type(type)
                    .title(title)
                    .body(body)
                    .build();
            userNotificationRepository.save(entity);
            
            log.info("Processing completed | operation=createNotification | userId={} | tripId={} | type={} | status=SUCCESS", userId, tripId, type);
        } catch (Exception e) {
            log.error("Operation failed | operation=createNotification | userId={} | tripId={} | type={} | reason={}", userId, tripId, type, e.getMessage(), e);
            throw e;
        }
    }

    public void createNotificationsForUsers(List<UUID> userIds, UUID tripId, NotificationType type, String title, String body) {
        log.info("Processing started | operation=createNotificationsForUsers | tripId={} | type={} | userCount={}", tripId, type, userIds.size());
        
        try {
            for (UUID userId : userIds) {
                createNotification(userId, tripId, type, title, body);
            }
            
            log.info("Processing completed | operation=createNotificationsForUsers | tripId={} | type={} | userCount={} | status=SUCCESS", tripId, type, userIds.size());
        } catch (Exception e) {
            log.error("Operation failed | operation=createNotificationsForUsers | tripId={} | type={} | userCount={} | reason={}", tripId, type, userIds.size(), e.getMessage(), e);
            throw e;
        }
    }

    public Page<UserNotificationEntity> getNotificationsForUser(UUID userId, Pageable pageable) {
        log.info("Processing started | operation=getNotificationsForUser | userId={} | page={} | size={}", userId, pageable.getPageNumber(), pageable.getPageSize());
        
        try {
            Page<UserNotificationEntity> notifications = userNotificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
            
            log.info("Processing completed | operation=getNotificationsForUser | userId={} | notificationsCount={} | status=SUCCESS", userId, notifications.getTotalElements());
            return notifications;
        } catch (Exception e) {
            log.error("Operation failed | operation=getNotificationsForUser | userId={} | reason={}", userId, e.getMessage(), e);
            throw e;
        }
    }

    public long getUnreadCount(UUID userId) {
        log.info("Processing started | operation=getUnreadCount | userId={}", userId);
        
        try {
            long count = userNotificationRepository.countByUserIdAndReadAtIsNull(userId);
            
            log.info("Processing completed | operation=getUnreadCount | userId={} | unreadCount={} | status=SUCCESS", userId, count);
            return count;
        } catch (Exception e) {
            log.error("Operation failed | operation=getUnreadCount | userId={} | reason={}", userId, e.getMessage(), e);
            throw e;
        }
    }

    public void markAsRead(UUID notificationId, UUID userId) {
        log.info("Processing started | operation=markAsRead | userId={} | notificationId={}", userId, notificationId);
        
        try {
            UserNotificationEntity notification = userNotificationRepository.findById(notificationId)
                    .orElseThrow(() -> new NotificationNotFoundException(notificationId));
            
            if (!notification.getUserId().equals(userId)) {
                log.warn("Access denied | operation=markAsRead | userId={} | notificationId={} | reason=NOT_OWNER", userId, notificationId);
                throw new NotificationAccessDeniedException("Access denied to notification " + notificationId);
            }
            
            if (notification.getReadAt() == null) {
                notification.setReadAt(LocalDateTime.now());
                userNotificationRepository.save(notification);
                log.info("Notification marked as read | userId={} | notificationId={} | status=SUCCESS", userId, notificationId);
            } else {
                log.info("Notification already read | userId={} | notificationId={} | status=NOOP", userId, notificationId);
            }
            
            log.info("Processing completed | operation=markAsRead | userId={} | notificationId={} | status=SUCCESS", userId, notificationId);
        } catch (NotificationNotFoundException | NotificationAccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Operation failed | operation=markAsRead | userId={} | notificationId={} | reason={}", userId, notificationId, e.getMessage(), e);
            throw e;
        }
    }

    public void markAllAsRead(UUID userId) {
        log.info("Processing started | operation=markAllAsRead | userId={}", userId);
        
        try {
            List<UserNotificationEntity> unreadNotifications = userNotificationRepository.findByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged())
                    .getContent()
                    .stream()
                    .filter(n -> n.getReadAt() == null)
                    .toList();
            
            for (UserNotificationEntity notification : unreadNotifications) {
                notification.setReadAt(LocalDateTime.now());
                userNotificationRepository.save(notification);
            }
            
            log.info("Processing completed | operation=markAllAsRead | userId={} | markedCount={} | status=SUCCESS", userId, unreadNotifications.size());
        } catch (Exception e) {
            log.error("Operation failed | operation=markAllAsRead | userId={} | reason={}", userId, e.getMessage(), e);
            throw e;
        }
    }
}
