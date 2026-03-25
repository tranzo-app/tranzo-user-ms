package com.tranzo.tranzo_user_ms.trip.service;

import com.tranzo.tranzo_user_ms.trip.dto.SampleTripDto;
import com.tranzo.tranzo_user_ms.trip.dto.TrendingDestinationDto;
import com.tranzo.tranzo_user_ms.trip.dto.TrendingDestinationsResponse;
import com.tranzo.tranzo_user_ms.trip.model.TripEntity;
import com.tranzo.tranzo_user_ms.trip.repository.TripDiscoveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for Trending Destinations Discovery
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TrendingDestinationService {

    private final TripDiscoveryRepository tripDiscoveryRepository;
    private final DiscoveryScoringService scoringService;

    /**
     * Compute trending destinations
     * 
     * Algorithm:
     * - Fetch trips created in time window
     * - Group by destination
     * - Compute trend score (Velocity + Momentum + Availability)
     * - Sort by score DESC
     * - Return top N destinations with metrics
     */
    public TrendingDestinationsResponse computeTrendingDestinations(
            int limit,
            String timeWindow
    ) {
        log.info("Computing trending destinations - limit: {}, window: {}", limit, timeWindow);
        
        // Determine date range based on timeWindow
        LocalDate windowStart = getWindowStartDate(timeWindow);
        LocalDate windowEnd = LocalDate.now();
        
        // Convert to LocalDateTime for repository calls
        LocalDateTime windowStartDateTime = windowStart.atStartOfDay();
        LocalDateTime windowEndDateTime = windowEnd.atTime(23, 59, 59);
        
        // Fetch trips in window
        List<TripEntity> tripsInWindow = tripDiscoveryRepository.findTripsInWindow(windowStartDateTime, windowEndDateTime);
        
        log.debug("Found {} trips in window {}", tripsInWindow.size(), timeWindow);
        
        if (tripsInWindow.isEmpty()) {
            log.warn("No trips found in window {}", timeWindow);
            return TrendingDestinationsResponse.builder()
                .trendingDestinations(List.of())
                .metadata(TrendingDestinationsResponse.Metadata.builder()
                    .trendingWindow(timeWindow)
                    .computedAt(LocalDateTime.now())
                    .cacheExpiresAt(LocalDateTime.now().plusHours(6))
                    .build())
                .build();
        }
        
        // Group by destination
        Map<String, List<TripEntity>> tripsByDestination = tripsInWindow.stream()
            .collect(Collectors.groupingBy(TripEntity::getTripDestination));
        
        // Compute trend scores and create DTOs
        List<TrendingDestinationDto> trendingList = tripsByDestination.entrySet().stream()
            .map(entry -> {
                String destination = entry.getKey();
                List<TripEntity> trips = entry.getValue();
                
                double trendScore = scoringService.computeTrendScore(destination, trips, timeWindow);
                
                return TrendingDestinationDto.builder()
                    .destination(destination)
                    .trendScore(trendScore)
                    .momentum(scoringService.computeMomentum(destination, trips, timeWindow))
                    .participation(scoringService.computeParticipation(trips))
                    .sampleTrips(trips.stream()
                        .limit(3)
                        .map(this::mapToSampleTripDto)
                        .collect(Collectors.toList()))
                    .computedAt(LocalDateTime.now())
                    .build();
            })
            .sorted((d1, d2) -> Double.compare(d2.getTrendScore(), d1.getTrendScore()))
            .limit(limit)
            .collect(Collectors.toList());
        
        // Add ranking
        for (int i = 0; i < trendingList.size(); i++) {
            trendingList.get(i).setRank(i + 1);
        }
        
        log.info("Computed {} trending destinations", trendingList.size());
        
        return TrendingDestinationsResponse.builder()
            .trendingDestinations(trendingList)
            .metadata(TrendingDestinationsResponse.Metadata.builder()
                .trendingWindow(timeWindow)
                .computedAt(LocalDateTime.now())
                .cacheExpiresAt(LocalDateTime.now().plusHours(6))
                .build())
            .build();
    }

    /**
     * Determine window start date based on time window string
     */
    private LocalDate getWindowStartDate(String timeWindow) {
        return switch (timeWindow) {
            case "7d" -> LocalDate.now().minusDays(7);
            case "14d" -> LocalDate.now().minusDays(14);
            case "30d" -> LocalDate.now().minusDays(30);
            default -> LocalDate.now().minusDays(7);
        };
    }
    
    /**
     * Convert TripEntity to SampleTripDto for trending destinations
     */
    private SampleTripDto mapToSampleTripDto(TripEntity trip) {
        return SampleTripDto.builder()
            .tripId(trip.getTripId())
            .tripTitle(trip.getTripTitle())
            .tripDescription(trip.getTripDescription())
            .tripDestination(trip.getTripDestination())
            .tripStartDate(trip.getTripStartDate())
            .tripEndDate(trip.getTripEndDate())
            .estimatedBudget(trip.getEstimatedBudget())
            .maxParticipants(trip.getMaxParticipants())
            .currentParticipants(trip.getCurrentParticipants())
            .isFull(trip.getIsFull())
            .build();
    }
}

