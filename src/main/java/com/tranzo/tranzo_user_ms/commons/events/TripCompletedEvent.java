package com.tranzo.tranzo_user_ms.commons.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Published when a trip is marked completed. All members get notification to rate and give feedback.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TripCompletedEvent {

    private UUID tripId;
    private String tripTitle;
    private List<UUID> memberUserIds;
}
