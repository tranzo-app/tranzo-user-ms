package com.tranzo.tranzo_user_ms.trip.dto;

public record TrendingDestinationProjection(
        String destination,
        long tripsCount,
        String coverImageUrl
) {}
