package com.tranzo.tranzo_user_ms.commons.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Published when a trip is cancelled. All members should be notified.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TripCancelledEvent {

    private UUID tripId;
    private String tripTitle;
    private List<UUID> memberUserIds;
}
