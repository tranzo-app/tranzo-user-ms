package com.tranzo.tranzo_user_ms.trip.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
public class TravelPalInviteRequestDto {

    @Valid
    @NotNull(message = "Travel pal list cannot be null")
    private List<UUID> travelPalIds;
}
