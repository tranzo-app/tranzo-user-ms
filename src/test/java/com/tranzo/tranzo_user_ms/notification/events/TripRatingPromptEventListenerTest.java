package com.tranzo.tranzo_user_ms.notification.events;

import com.tranzo.tranzo_user_ms.commons.events.TripCompletedEvent;
import com.tranzo.tranzo_user_ms.notification.enums.NotificationType;
import com.tranzo.tranzo_user_ms.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TripRatingPromptEventListener Unit Tests")
class TripRatingPromptEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private TripRatingPromptEventListener listener;

    private UUID tripId;
    private List<UUID> memberUserIds;
    private String tripTitle;

    @BeforeEach
    void setUp() {
        tripId = UUID.randomUUID();
        memberUserIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        tripTitle = "Beach Trip";
    }

    @Test
    @DisplayName("onTripCompleted creates RATE_YOUR_JOURNEY notifications for all members")
    void onTripCompleted_CreatesNotifications() {
        TripCompletedEvent event = new TripCompletedEvent(tripId, tripTitle, memberUserIds);

        listener.onTripCompleted(event);

        verify(notificationService).createNotificationsForUsers(
                eq(memberUserIds),
                eq(tripId),
                eq(NotificationType.RATE_YOUR_JOURNEY),
                eq("Rate your Journey"),
                contains("Share your feedback"));
    }

    @Test
    @DisplayName("onTripCompleted sends STOMP payload to each member when messagingTemplate present")
    void onTripCompleted_SendsStompWhenTemplatePresent() throws Exception {
        TripCompletedEvent event = new TripCompletedEvent(tripId, tripTitle, memberUserIds);
        var field = TripRatingPromptEventListener.class.getDeclaredField("messagingTemplate");
        field.setAccessible(true);
        field.set(listener, messagingTemplate);

        listener.onTripCompleted(event);

        verify(notificationService).createNotificationsForUsers(any(), any(), any(), any(), any());
        verify(messagingTemplate, times(memberUserIds.size())).convertAndSendToUser(
                anyString(),
                eq("/queue/notifications"),
                any());
    }

    @Test
    @DisplayName("onTripCompleted handles null trip title")
    void onTripCompleted_NullTripTitle() {
        TripCompletedEvent event = new TripCompletedEvent(tripId, null, memberUserIds);

        listener.onTripCompleted(event);

        verify(notificationService).createNotificationsForUsers(
                eq(memberUserIds),
                eq(tripId),
                eq(NotificationType.RATE_YOUR_JOURNEY),
                eq("Rate your Journey"),
                anyString());
    }
}
