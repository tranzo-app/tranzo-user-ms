package com.tranzo.tranzo_user_ms.commons.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Published to remind host to complete and publish their draft trip.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DraftTripReminderEvent {

    private UUID tripId;
    private String tripTitle;
    private UUID hostUserId;
}
