package com.tranzo.tranzo_user_ms.notification.events;

import com.tranzo.tranzo_user_ms.commons.events.TripCompletedEvent;
import com.tranzo.tranzo_user_ms.notification.enums.NotificationType;
import com.tranzo.tranzo_user_ms.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * On trip completion, creates "Rate your Journey" in-app notifications and optionally
 * sends a real-time payload over STOMP so the app can show the prompt immediately.
 */
@Component
@Slf4j
public class TripRatingPromptEventListener {

    private final NotificationService notificationService;

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    public TripRatingPromptEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    @Transactional
    public void onTripCompleted(TripCompletedEvent event) {
        log.info("Creating RATE_YOUR_JOURNEY notifications for tripId={}, memberCount={}",
                event.getTripId(), event.getMemberUserIds().size());
        String title = "Rate your Journey";
        String body = "How was \"" + (event.getTripTitle() != null ? event.getTripTitle() : "your trip") + "\"? Share your feedback.";
        notificationService.createNotificationsForUsers(
                event.getMemberUserIds(),
                event.getTripId(),
                NotificationType.RATE_YOUR_JOURNEY,
                title,
                body
        );
        if (messagingTemplate != null) {
            for (UUID userId : event.getMemberUserIds()) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("type", "RATE_YOUR_JOURNEY");
                payload.put("tripId", event.getTripId().toString());
                payload.put("tripTitle", event.getTripTitle());
                try {
                    messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/notifications", payload);
                } catch (Exception e) {
                    log.warn("Failed to send STOMP RATE_YOUR_JOURNEY to user {}: {}", userId, e.getMessage());
                }
            }
        }
    }
}
