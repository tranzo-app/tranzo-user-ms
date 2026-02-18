package com.tranzo.tranzo_user_ms.notification.dto;

import com.tranzo.tranzo_user_ms.notification.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDto {

    private UUID notificationId;
    private UUID userId;
    private UUID tripId;
    private NotificationType type;
    private String title;
    private String body;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
