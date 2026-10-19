package com.tranzo.tranzo_user_ms.trip.dto;

import java.util.List;

public record GenerateItineraryResponse(
        String destination,
        Integer numberOfDays,
        String tripType,
        List<DayPlanDto> itinerary
) {
}
