package com.tranzo.tranzo_user_ms.trip.dto;

import com.tranzo.tranzo_user_ms.trip.enums.TripType;

import java.util.List;

public record GenerateItineraryResponse(
        String destination,
        Integer numberOfDays,
        TripType tripType,
        List<DayPlanDto> itinerary
) {
}
