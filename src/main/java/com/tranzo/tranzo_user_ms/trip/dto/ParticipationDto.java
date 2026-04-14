package com.tranzo.tranzo_user_ms.trip.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Participation metrics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipationDto {
    
    private Double avgParticipants;
    private Integer availableTrips;
    private Integer totalTripsPublished;
}

