package com.tranzo.tranzo_user_ms.chat.events;

import com.tranzo.tranzo_user_ms.chat.dto.CreateConversationRequestDto;
import com.tranzo.tranzo_user_ms.chat.model.ConversationEntity;
import com.tranzo.tranzo_user_ms.chat.service.CreateAndManageConversationService;
import com.tranzo.tranzo_user_ms.commons.events.MemberLeftOrRemovedTripEvent;
import com.tranzo.tranzo_user_ms.commons.events.ParticipantJoinedTripEvent;
import com.tranzo.tranzo_user_ms.commons.events.TravelPalAcceptedEvent;
import com.tranzo.tranzo_user_ms.commons.events.TripGroupChatCreatedEvent;
import com.tranzo.tranzo_user_ms.commons.events.TripPublishedEvent;
import com.tranzo.tranzo_user_ms.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
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
    private final TripRepository tripRepository;

    @EventListener
    public void onTripPublished(TripPublishedEvent event) {
        log.info("Received TripPublishedEvent. tripId={}, hostUserId={}", event.getTripId(), event.getHostUserId());
        UUID tripId = event.getTripId();
        UUID hostUserId = event.getHostUserId();
        ConversationEntity conversation = createAndManageConversationService.createTripGroupChat(hostUserId, tripId);
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

    @EventListener
    public void onTravelPalAccepted(TravelPalAcceptedEvent event) {
        log.info("Received TravelPalAcceptedEvent. userA={}, userB={}", event.getUserA(), event.getUserB());
        CreateConversationRequestDto request = CreateConversationRequestDto.builder()
                .otherUserId(event.getUserB())
                .build();
        createAndManageConversationService.createOneToOneConversation(event.getUserA(), request);
    }

    @EventListener
    public void onMemberLeftOrRemovedTrip(MemberLeftOrRemovedTripEvent event) {
        log.info("Received MemberLeftOrRemovedTripEvent. tripId={}, userId={}", event.getTripId(), event.getLeftOrRemovedUserId());
        UUID tripId = event.getTripId();
        UUID userId = event.getLeftOrRemovedUserId();

        Optional<UUID> conversationId = tripRepository.findById(tripId)
                .map(trip -> trip.getConversationID());

        if (conversationId.isPresent()) {
            createAndManageConversationService.removeParticipantFromConversation(conversationId.get(), userId);
        } else {
            log.warn("Conversation not found for trip | tripId={} | userId={} | reason=NO_CONVERSATION_ID", tripId, userId);
        }
    }
}
