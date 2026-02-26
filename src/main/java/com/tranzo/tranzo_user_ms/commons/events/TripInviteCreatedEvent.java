package com.tranzo.tranzo_user_ms.commons.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/** Published when a host/co-host invites a travel pal to a trip. Invitee should be notified. */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TripInviteCreatedEvent {

    private UUID tripId;
    private String tripTitle;
    private UUID invitedUserId;
    private UUID invitedByUserId;
}
