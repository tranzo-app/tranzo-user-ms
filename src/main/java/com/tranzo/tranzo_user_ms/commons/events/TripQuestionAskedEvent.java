package com.tranzo.tranzo_user_ms.commons.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/** Published when someone asks a question on a trip. Notify all joined members. */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TripQuestionAskedEvent {

    private UUID tripId;
    private String tripTitle;
    private UUID askedByUserId;
    private List<UUID> memberUserIds;
}
