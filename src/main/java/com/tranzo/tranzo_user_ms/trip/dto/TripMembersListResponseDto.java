package com.tranzo.tranzo_user_ms.trip.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripMembersListResponseDto {
    private UUID tripId;
    private UUID hostUserId;
    private List<UUID> coHostUserIds;
    private List<TripMemberResponseDto> members;
    private int totalJoined;
}
