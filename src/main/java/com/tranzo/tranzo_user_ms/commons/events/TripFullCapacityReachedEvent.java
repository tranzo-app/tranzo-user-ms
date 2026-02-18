package com.tranzo.tranzo_user_ms.commons.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/** Published when trip becomes full because current participants reached max capacity. Notify all joined members including host. */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TripFullCapacityReachedEvent {

    private UUID tripId;
    private String tripTitle;
    private List<UUID> memberUserIds;
}
