package com.tranzo.tranzo_user_ms.commons.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/** Published when a member joins a trip (any path). Other existing members should be notified. */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemberJoinedTripEvent {

    private UUID tripId;
    private String tripTitle;
    private UUID joinedUserId;
    private List<UUID> otherMemberUserIds;
}
