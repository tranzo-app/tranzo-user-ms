package com.tranzo.tranzo_user_ms.trip.dto;

import com.tranzo.tranzo_user_ms.trip.enums.JoinRequestSource;
import com.tranzo.tranzo_user_ms.trip.enums.JoinRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripJoinRequestResponseDto {
    private UUID joinRequestId;
    private UUID tripId;
    private UUID requestorUserId;
    private JoinRequestStatus status;
    private JoinRequestSource requestedChannel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
