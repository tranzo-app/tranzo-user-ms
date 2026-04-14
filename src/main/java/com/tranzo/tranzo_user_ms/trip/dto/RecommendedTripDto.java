package com.tranzo.tranzo_user_ms.trip.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tranzo.tranzo_user_ms.trip.enums.JoinPolicy;
import com.tranzo.tranzo_user_ms.trip.enums.VisibilityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for Recommended Trips Response
 * Contains trip details + recommendation score + reasons
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendedTripDto {
    
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
    
    private JoinPolicy joinPolicy;
    private VisibilityStatus visibilityStatus;
    
    /** Recommendation score [0.0 - 1.0] */
    private Double recommendationScore;
    
    /** Human-readable reasons for recommendation */
    private List<String> reasonsForRecommendation;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}

