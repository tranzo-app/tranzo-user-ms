package com.tranzo.tranzo_user_ms.trip.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for Recommended Trips endpoint
 * Allows advanced filtering
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscoveryFilterRequest {
    
    private String destination;
    
    private Double budgetMin;
    private Double budgetMax;
    
    private LocalDate startDateMin;
    private LocalDate startDateMax;
    
    private String joinPolicy;
    
    @Min(0)
    @Builder.Default
    private Integer page = 0;
    
    @Min(1)
    @Max(50)
    @Builder.Default
    private Integer size = 20;
}

