package com.tranzo.tranzo_user_ms.trip.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Momentum metrics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MomentumDto {
    
    private Integer tripsCreatedInWindow;
    private Integer avgHistoricalTripsPerWindow;
    private Integer velocityPercentage;  // percentage (e.g., 150 = 150%)
}

