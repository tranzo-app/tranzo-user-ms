package com.tranzo.tranzo_user_ms.trip.controller;

import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.trip.dto.DiscoveryFilterRequest;
import com.tranzo.tranzo_user_ms.trip.dto.FeaturedTripDto;
import com.tranzo.tranzo_user_ms.trip.dto.RecommendedTripDto;
import com.tranzo.tranzo_user_ms.trip.dto.TrendingDestinationsResponse;
import com.tranzo.tranzo_user_ms.trip.service.DiscoveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Trip Discovery Endpoints
 * - Featured Trips
 * - Recommended Trips
 * - Trending Destinations
 */
@RestController
@RequestMapping("/trips")
@Tag(name = "Trip Discovery", description = "Discover and explore trips")
@RequiredArgsConstructor
@Slf4j
public class DiscoveryController {

    private final DiscoveryService discoveryService;

    /**
     * GET /trips/featured
     * Fetch featured trips with optional budget filtering
     */
//    @GetMapping("/featured")
//    @Operation(
//        summary = "Get featured trips",
//        description = "Fetch curated featured trips. Sorted by recency, availability, and engagement."
//    )
//    public ResponseEntity<ResponseDto<Page<FeaturedTripDto>>> getFeaturedTrips(
//            @Parameter(description = "Page number (0-indexed)", example = "0")
//            @RequestParam(defaultValue = "0") int page,
//
//            @Parameter(description = "Page size (max 50)", example = "20")
//            @RequestParam(defaultValue = "20") int size,
//
//            @Parameter(description = "Minimum budget (INR)", required = false)
//            @RequestParam(required = false) Double budgetMin,
//
//            @Parameter(description = "Maximum budget (INR)", required = false)
//            @RequestParam(required = false) Double budgetMax
//    ) {
//        log.info("GET /trips/featured - page: {}, size: {}", page, size);
//
//        Page<FeaturedTripDto> trips = discoveryService.getFeaturedTrips(page, size, budgetMin, budgetMax);
//
//        return ResponseEntity.ok(
//            ResponseDto.success("Featured trips fetched successfully", trips)
//        );
//    }

    /**
     * POST /trips/recommended
     * Fetch recommended trips with advanced filtering
     */
//    @PostMapping("/recommended")
//    @Operation(
//        summary = "Get recommended trips",
//        description = "Fetch recommended trips based on popularity, trends, availability, and booking timing."
//    )
//    public ResponseEntity<ResponseDto<Page<RecommendedTripDto>>> getRecommendedTrips(
//            @RequestBody(required = false) DiscoveryFilterRequest request
//    ) {
//        log.info("POST /trips/recommended - filters: {}", request);
//
//        // Default request if not provided
//        DiscoveryFilterRequest validRequest = request != null ? request : new DiscoveryFilterRequest();
//
//        Page<RecommendedTripDto> trips = discoveryService.getRecommendedTrips(validRequest);
//
//        return ResponseEntity.ok(
//            ResponseDto.success("Recommended trips fetched successfully", trips)
//        );
//    }

    /**
     * GET /trips/trending-destinations
     * Fetch trending destinations based on recent activity and momentum
     */
    @GetMapping("/trending-destinations")
    @Operation(
        summary = "Get trending destinations",
        description = "Fetch trending destinations based on trip creation velocity, participant momentum, and availability."
    )
    public ResponseEntity<ResponseDto<TrendingDestinationsResponse>> getTrendingDestinations(
            @Parameter(description = "Number of destinations to return (max 20)", example = "10")
            @RequestParam(defaultValue = "10") int limit,
            
            @Parameter(description = "Time window for trend analysis: 7d, 14d, or 30d", example = "7d")
            @RequestParam(defaultValue = "7d") String timeWindow
    ) {
        log.info("GET /trips/trending-destinations - limit: {}, timeWindow: {}", limit, timeWindow);
        
        TrendingDestinationsResponse response = discoveryService.getTrendingDestinations(limit, timeWindow);
        
        return ResponseEntity.ok(
            ResponseDto.success("Trending destinations fetched successfully", response)
        );
    }
}

