package com.tranzo.tranzo_user_ms.commons.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/** Published when a user creates a join request. Host should be notified. */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class JoinRequestCreatedEvent {

    private UUID tripId;
    private String tripTitle;
    private UUID requestorUserId;
    private UUID hostUserId;
}
