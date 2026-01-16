package com.tranzo.tranzo_user_ms.trip.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripWishlistRequestDto {
    @NotNull(message = "Trip id is mandatory attribute")
    private UUID tripId;
}
