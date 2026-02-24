package com.tranzo.tranzo_user_ms.commons.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Spring Application Event: published by chat when a trip's group conversation is created.
 * Trip module listens to update trip.conversationID.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TripGroupChatCreatedEvent {

    private UUID tripId;
    private UUID conversationId;
}
