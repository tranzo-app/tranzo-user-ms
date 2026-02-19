package com.tranzo.tranzo_user_ms.commons.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/** Published when host manually marks trip as full. Notify all members except host. */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TripMarkedFullByHostEvent {

    private UUID tripId;
    private String tripTitle;
    private List<UUID> memberUserIdsExcludingHost;
}
