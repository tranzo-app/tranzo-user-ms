package com.tranzo.tranzo_user_ms.trip.dto;

import com.tranzo.tranzo_user_ms.trip.enums.TripMemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripMemberResponseDto {
    private String firstName;
    private String middleName;
    private String lastName;
    private UUID membershipId;
    private UUID userId;
    private TripMemberRole role;
    private LocalDateTime joinedAt;
}
