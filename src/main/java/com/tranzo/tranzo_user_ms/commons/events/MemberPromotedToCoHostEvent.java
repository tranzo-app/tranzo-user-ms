package com.tranzo.tranzo_user_ms.commons.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/** Published when a member is promoted to co-host. All trip members should be notified. */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemberPromotedToCoHostEvent {

    private UUID tripId;
    private String tripTitle;
    private UUID promotedUserId;
    private List<UUID> allMemberUserIds;
}
