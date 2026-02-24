package com.tranzo.tranzo_user_ms.commons.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/** Published when a member leaves or is removed. Other members should be notified. */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemberLeftOrRemovedTripEvent {

    private UUID tripId;
    private String tripTitle;
    private UUID leftOrRemovedUserId;
    private List<UUID> otherMemberUserIds;
    private boolean removedByHost; // true = removed, false = left
}
