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
        log.info("Processing started | operation=getFeaturedTrips | page={} | size={} | budgetMin={} | budgetMax={}", page, size, budgetMin, budgetMax);
        
        try {
            // Cap size at 50
            int validatedSize = Math.min(Math.max(size, 1), 50);
            
            log.info("Calling external service | service=FeaturedTripService | operation=computeFeaturedTrips | page={} | size={}", page, validatedSize);
            Page<FeaturedTripDto> result = featuredTripService.computeFeaturedTrips(page, validatedSize, budgetMin, budgetMax);
            
            log.info("Processing completed | operation=getFeaturedTrips | page={} | size={} | tripsCount={} | status=SUCCESS", page, validatedSize, result.getTotalElements());
            return result;
        } catch (Exception e) {
            log.error("Operation failed | operation=getFeaturedTrips | page={} | size={} | reason={}", page, size, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Fetch recommended trips
     */
    public Page<RecommendedTripDto> getRecommendedTrips(DiscoveryFilterRequest request) {
        log.info("Processing started | operation=getRecommendedTrips | destination={} | page={} | size={}", 
                 request.getDestination(), request.getPage(), request.getSize());
        
        try {
            // Validate and cap page size
            int validatedPage = Math.max(request.getPage(), 0);
            int validatedSize = Math.min(Math.max(request.getSize(), 1), 50);
            
            Pageable pageable = PageRequest.of(validatedPage, validatedSize);
            
            log.info("Calling external service | service=RecommendedTripService | operation=computeRecommendedTrips | destination={}", request.getDestination());
            Page<RecommendedTripDto> result = recommendedTripService.computeRecommendedTrips(request, pageable);
            
            log.info("Processing completed | operation=getRecommendedTrips | destination={} | tripsCount={} | status=SUCCESS", request.getDestination(), result.getTotalElements());
            return result;
        } catch (Exception e) {
            log.error("Operation failed | operation=getRecommendedTrips | destination={} | reason={}", request.getDestination(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Fetch trending destinations
     */
    public TrendingDestinationsResponse getTrendingDestinations(int limit, String timeWindow) {
        log.info("Processing started | operation=getTrendingDestinations | limit={} | timeWindow={}", limit, timeWindow);
        
        try {
            // Cap limit at 20
            int validatedLimit = Math.min(Math.max(limit, 1), 20);
            
            // Validate timeWindow
            String validatedWindow = switch (timeWindow) {
                case "7d", "14d", "30d" -> timeWindow;
                default -> "7d";
            };
            
            log.info("Calling external service | service=TrendingDestinationService | operation=computeTrendingDestinations | limit={} | timeWindow={}", validatedLimit, validatedWindow);
            TrendingDestinationsResponse result = trendingDestinationService.computeTrendingDestinations(validatedLimit, validatedWindow);
            
            log.info("Processing completed | operation=getTrendingDestinations | limit={} | timeWindow={} | destinationsCount={} | status=SUCCESS", validatedLimit, validatedWindow, result.getTrendingDestinations().size());
            return result;
        } catch (Exception e) {
            log.error("Operation failed | operation=getTrendingDestinations | limit={} | timeWindow={} | reason={}", limit, timeWindow, e.getMessage(), e);
            throw e;
        }
    }
}

