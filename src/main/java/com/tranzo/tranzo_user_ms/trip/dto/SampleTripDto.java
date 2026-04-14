package com.tranzo.tranzo_user_ms.trip.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Lightweight DTO for sample trips in trending destinations
 * Contains only essential trip information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SampleTripDto {
    
    private UUID tripId;
    private String tripTitle;
    private String tripDescription;
    private String tripDestination;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate tripStartDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate tripEndDate;
    
    private Double estimatedBudget;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private Boolean isFull;
}
