package com.tranzo.tranzo_user_ms.commons.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Spring Application Event: published when a travel pal request is accepted.
 * Chat module listens to create a one-to-one conversation between the two users.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TravelPalAcceptedEvent {

    private UUID userA;
    private UUID userB;
}
