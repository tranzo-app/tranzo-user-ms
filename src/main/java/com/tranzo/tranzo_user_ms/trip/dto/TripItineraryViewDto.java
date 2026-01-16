package com.tranzo.tranzo_user_ms.trip.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripItineraryViewDto {
    private UUID itineraryId;
    private Integer dayNumber;
    private String title;
    private String description;
    private Map<String, Object> activities;
    private Map<String, Object> meals;
    private Map<String, Object> stay;
}
