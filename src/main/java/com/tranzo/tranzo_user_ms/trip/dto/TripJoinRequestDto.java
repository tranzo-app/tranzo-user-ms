package com.tranzo.tranzo_user_ms.trip.dto;

import com.tranzo.tranzo_user_ms.trip.enums.JoinRequestSource;
import com.tranzo.tranzo_user_ms.trip.enums.JoinRequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripJoinRequestDto {
    @NotNull
    private JoinRequestSource requestSource;
}

