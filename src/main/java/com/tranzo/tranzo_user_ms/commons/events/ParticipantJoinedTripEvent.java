package com.tranzo.tranzo_user_ms.commons.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Spring Application Event: published when a participant joins a trip.
 * Chat module listens to add the user as participant of the trip's conversation.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantJoinedTripEvent {

    private UUID tripId;
    private UUID userId;
    private UUID conversationId;
}
