package com.tranzo.tranzo_user_ms.commons.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/** Published when host updates published trip details. Notify all joined members. */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TripDetailsChangedEvent {

    private UUID tripId;
    private String tripTitle;
    private List<UUID> memberUserIds;
}
