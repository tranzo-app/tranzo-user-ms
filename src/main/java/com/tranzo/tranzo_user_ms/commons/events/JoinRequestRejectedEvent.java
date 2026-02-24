package com.tranzo.tranzo_user_ms.commons.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/** Published when host rejects a join request. Requestor should be notified. */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class JoinRequestRejectedEvent {

    private UUID tripId;
    private String tripTitle;
    private UUID requestorUserId;
}
