package com.tranzo.tranzo_user_ms.commons.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Spring Application Event: published when a trip is published.
 * Chat module listens to create the group conversation and publish TripGroupChatCreatedEvent.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TripPublishedEvent {

    private UUID tripId;
    private UUID hostUserId;
}
