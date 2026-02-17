package com.tranzo.tranzo_user_ms.trip.kafka;

public interface TripEventPublisher {
    void publishTripPublished(TripPublishedEventPayloadDto payload);
    void participantJoined(TripPublishedEventPayloadDto payload);
}
