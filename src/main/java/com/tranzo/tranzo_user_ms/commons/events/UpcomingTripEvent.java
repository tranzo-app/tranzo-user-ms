package com.tranzo.tranzo_user_ms.commons.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Published for trips starting soon. All members get "gear up" notification.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpcomingTripEvent {

    private UUID tripId;
    private String tripTitle;
    private LocalDate startDate;
    private List<UUID> memberUserIds;
}
