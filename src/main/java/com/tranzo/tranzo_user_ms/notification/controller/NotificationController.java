package com.tranzo.tranzo_user_ms.notification.controller;

import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import com.tranzo.tranzo_user_ms.notification.dto.NotificationResponseDto;
import com.tranzo.tranzo_user_ms.notification.model.UserNotificationEntity;
import com.tranzo.tranzo_user_ms.notification.service.NotificationService;
import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ResponseDto<Page<NotificationResponseDto>>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        Pageable pageable = PageRequest.of(page, size);
        Page<UserNotificationEntity> entities = notificationService.getNotificationsForUser(userId, pageable);
        Page<NotificationResponseDto> dtos = entities.map(this::toDto);
        return ResponseEntity.ok(ResponseDto.success("Notifications retrieved", dtos));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ResponseDto<Long>> getUnreadCount() throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ResponseDto.success("Unread count", count));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ResponseDto<Void>> markAsRead(@PathVariable UUID notificationId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        notificationService.markAsRead(notificationId, userId);
        return ResponseEntity.ok(ResponseDto.success("Marked as read", null));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ResponseDto<Void>> markAllAsRead() throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ResponseDto.success("All marked as read", null));
    }

    private NotificationResponseDto toDto(UserNotificationEntity entity) {
        return NotificationResponseDto.builder()
                .notificationId(entity.getNotificationId())
                .userId(entity.getUserId())
                .tripId(entity.getTripId())
                .type(entity.getType())
                .title(entity.getTitle())
                .body(entity.getBody())
                .readAt(entity.getReadAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
