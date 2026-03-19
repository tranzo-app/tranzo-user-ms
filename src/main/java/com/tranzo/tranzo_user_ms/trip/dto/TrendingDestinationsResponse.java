package com.tranzo.tranzo_user_ms.trip.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for Trending Destinations endpoint
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrendingDestinationsResponse {
    
    private List<TrendingDestinationDto> trendingDestinations;
    private Metadata metadata;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Metadata {
        
        private String trendingWindow;  // "7d", "14d", "30d"
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime computedAt;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime cacheExpiresAt;
    }
}

