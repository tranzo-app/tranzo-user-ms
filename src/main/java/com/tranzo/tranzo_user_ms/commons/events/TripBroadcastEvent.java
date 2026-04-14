package com.tranzo.tranzo_user_ms.commons.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/** Published when a host broadcasts a trip to all travel pals. Recipients should be notified. */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TripBroadcastEvent {

    private UUID tripId;
    private String tripTitle;
    private UUID hostUserId;
    private List<UUID> broadcastToUserIds;
}
