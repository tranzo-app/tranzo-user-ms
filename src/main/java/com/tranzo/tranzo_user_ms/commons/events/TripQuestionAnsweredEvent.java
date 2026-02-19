package com.tranzo.tranzo_user_ms.commons.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/** Published when a trip question is answered. Notify the user who asked the question. */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TripQuestionAnsweredEvent {

    private UUID tripId;
    private String tripTitle;
    private UUID askedByUserId;
}
