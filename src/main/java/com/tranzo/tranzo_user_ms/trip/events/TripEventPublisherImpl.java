package com.tranzo.tranzo_user_ms.trip.events;

import com.tranzo.tranzo_user_ms.commons.events.ParticipantJoinedTripEvent;
import com.tranzo.tranzo_user_ms.commons.events.TripPublishedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Publishes trip-related events via Spring Application Events (in-process).
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class TripEventPublisherImpl implements TripEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publishTripPublished(TripPublishedEventPayloadDto payload) {
        if ("TRIP_PUBLISHED".equals(payload.getEventType())) {
            log.info("Publishing TripPublishedEvent. tripId={}, hostUserId={}", payload.getTripId(), payload.getUserId());
            applicationEventPublisher.publishEvent(new TripPublishedEvent(payload.getTripId(), payload.getUserId()));
        } else if ("PARTICIPANT_JOINED".equals(payload.getEventType())) {
            log.info("Publishing ParticipantJoinedTripEvent. tripId={}, userId={}, conversationId={}",
                    payload.getTripId(), payload.getUserId(), payload.getConversationId());
            applicationEventPublisher.publishEvent(
                    new ParticipantJoinedTripEvent(payload.getTripId(), payload.getUserId(), payload.getConversationId()));
        }
    }

    @Override
    public void participantJoined(TripPublishedEventPayloadDto payload) {
        publishTripPublished(payload);
    }
}
