package com.tranzo.tranzo_user_ms.notification.model;

import com.tranzo.tranzo_user_ms.notification.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_notification", indexes = {
        @Index(name = "idx_user_notification_user_id", columnList = "user_id"),
        @Index(name = "idx_user_notification_read_at", columnList = "user_id, read_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "notification_id", nullable = false, updatable = false)
    private UUID notificationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "trip_id")
    private UUID tripId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "body", length = 1000)
    private String body;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
