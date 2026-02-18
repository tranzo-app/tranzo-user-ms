package com.tranzo.tranzo_user_ms.chat.events;

import com.tranzo.tranzo_user_ms.chat.model.ConversationEntity;
import com.tranzo.tranzo_user_ms.chat.service.CreateAndManageConversationService;
import com.tranzo.tranzo_user_ms.commons.events.ParticipantJoinedTripEvent;
import com.tranzo.tranzo_user_ms.commons.events.TripGroupChatCreatedEvent;
import com.tranzo.tranzo_user_ms.commons.events.TripPublishedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Listens to trip-related Spring Application Events and creates/updates chat accordingly.
 * Replaces Kafka consumer for single-server deployment.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class TripEventsListener {

    private final CreateAndManageConversationService createAndManageConversationService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @EventListener
    public void onTripPublished(TripPublishedEvent event) {
        log.info("Received TripPublishedEvent. tripId={}, hostUserId={}", event.getTripId(), event.getHostUserId());
        UUID tripId = event.getTripId();
        UUID hostUserId = event.getHostUserId();
        ConversationEntity conversation = createAndManageConversationService.createTripGroupChat(hostUserId);
        applicationEventPublisher.publishEvent(new TripGroupChatCreatedEvent(tripId, conversation.getConversationId()));
    }

    @EventListener
    public void onParticipantJoinedTrip(ParticipantJoinedTripEvent event) {
        log.info("Received ParticipantJoinedTripEvent. tripId={}, userId={}, conversationId={}",
                event.getTripId(), event.getUserId(), event.getConversationId());
        if (event.getConversationId() != null) {
            createAndManageConversationService.addParticipantToConversation(event.getConversationId(), event.getUserId());
        }
    }
}
