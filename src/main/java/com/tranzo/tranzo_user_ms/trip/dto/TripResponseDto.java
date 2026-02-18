package com.tranzo.tranzo_user_ms.trip.dto;

import com.tranzo.tranzo_user_ms.trip.enums.TripStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripResponseDto {
    private UUID tripId;
    private TripStatus tripStatus;
}
