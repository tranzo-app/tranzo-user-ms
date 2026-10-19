package com.tranzo.tranzo_user_ms.trip.dto;

import com.tranzo.tranzo_user_ms.trip.enums.Season;
import com.tranzo.tranzo_user_ms.trip.enums.TripType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record GenerateItineraryRequest(
        @NotBlank
        String destination,

        @NotNull
        @Min(1)
        @Max(30)
        Integer numberOfDays,

        @NotNull
        TripType tripType,

        String tripTitle,

        String description,

        Integer maxParticipants,

        Double costPerPerson,

        Season season,

        List<String> tripTags
) {
}
