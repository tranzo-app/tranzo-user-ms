package com.tranzo.tranzo_user_ms.trip.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tranzo.tranzo_user_ms.trip.model.TripEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Trending Destination
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrendingDestinationDto {
    
    private Integer rank;
    private String destination;
    
    /** Trend score [0.0 - 1.0] */
    private Double trendScore;
    
    private MomentumDto momentum;
    private ParticipationDto participation;
    
    /** Sample trips for this destination */
    private List<TripEntity> sampleTrips;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime computedAt;
}

