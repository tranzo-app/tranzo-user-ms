package com.tranzo.tranzo_user_ms.trip.events;

import com.tranzo.tranzo_user_ms.commons.events.TripGroupChatCreatedEvent;
import com.tranzo.tranzo_user_ms.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listens to TripGroupChatCreatedEvent (published by chat) and updates trip.conversationID.
 * Replaces Kafka consumer for single-server deployment.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class TripGroupChatCreatedEventListener {

    private final TripRepository tripRepository;

    @EventListener
    @Transactional
    public void onTripGroupChatCreated(TripGroupChatCreatedEvent event) {
        log.info("Received TripGroupChatCreatedEvent. tripId={}, conversationId={}", event.getTripId(), event.getConversationId());
        tripRepository.findById(event.getTripId()).ifPresent(trip -> {
            if (trip.getConversationID() == null) {
                trip.setConversationID(event.getConversationId());
            }
        });
    }
}
