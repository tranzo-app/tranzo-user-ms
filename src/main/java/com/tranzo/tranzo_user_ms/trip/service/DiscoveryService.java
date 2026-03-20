package com.tranzo.tranzo_user_ms.trip.service;

import com.tranzo.tranzo_user_ms.trip.dto.DiscoveryFilterRequest;
import com.tranzo.tranzo_user_ms.trip.dto.FeaturedTripDto;
import com.tranzo.tranzo_user_ms.trip.dto.RecommendedTripDto;
import com.tranzo.tranzo_user_ms.trip.dto.TrendingDestinationsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade Service for Trip Discovery
 * Orchestrates calls to individual discovery services
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DiscoveryService {

    private final FeaturedTripService featuredTripService;
    private final RecommendedTripService recommendedTripService;
    private final TrendingDestinationService trendingDestinationService;

    /**
     * Fetch featured trips
     */
    public Page<FeaturedTripDto> getFeaturedTrips(
            int page,
            int size,
            Double budgetMin,
            Double budgetMax
    ) {
        log.info("Fetching featured trips - page: {}, size: {}", page, size);
        
        // Cap size at 50
        int validatedSize = Math.min(Math.max(size, 1), 50);
        
        return featuredTripService.computeFeaturedTrips(page, validatedSize, budgetMin, budgetMax);
    }

    /**
     * Fetch recommended trips
     */
    public Page<RecommendedTripDto> getRecommendedTrips(DiscoveryFilterRequest request) {
        log.info("Fetching recommended trips - destination: {}", request.getDestination());
        
        // Validate and cap page size
        int validatedPage = Math.max(request.getPage(), 0);
        int validatedSize = Math.min(Math.max(request.getSize(), 1), 50);
        
        Pageable pageable = PageRequest.of(validatedPage, validatedSize);
        
        return recommendedTripService.computeRecommendedTrips(request, pageable);
    }

    /**
     * Fetch trending destinations
     */
    public TrendingDestinationsResponse getTrendingDestinations(int limit, String timeWindow) {
        log.info("Fetching trending destinations - limit: {}, timeWindow: {}", limit, timeWindow);
        
        // Cap limit at 20
        int validatedLimit = Math.min(Math.max(limit, 1), 20);
        
        // Validate timeWindow
        String validatedWindow = switch (timeWindow) {
            case "7d", "14d", "30d" -> timeWindow;
            default -> "7d";
        };
        
        return trendingDestinationService.computeTrendingDestinations(validatedLimit, validatedWindow);
    }
}

