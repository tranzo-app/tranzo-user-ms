package com.tranzo.tranzo_user_ms.trip.events;

public interface TripEventPublisher {
    void publishTripPublished(TripPublishedEventPayloadDto payload);
    void participantJoined(TripPublishedEventPayloadDto payload);
}
