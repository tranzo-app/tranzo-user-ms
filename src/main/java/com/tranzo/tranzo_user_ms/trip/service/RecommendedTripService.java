package com.tranzo.tranzo_user_ms.trip.service;

import com.tranzo.tranzo_user_ms.trip.dto.DiscoveryFilterRequest;
import com.tranzo.tranzo_user_ms.trip.dto.RecommendedTripDto;
import com.tranzo.tranzo_user_ms.trip.enums.JoinPolicy;
import com.tranzo.tranzo_user_ms.trip.model.TripEntity;
import com.tranzo.tranzo_user_ms.trip.repository.TripDiscoveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Recommended Trips Discovery
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RecommendedTripService {

    private final TripDiscoveryRepository tripDiscoveryRepository;
    private final DiscoveryScoringService scoringService;

    /**
     * Compute recommended trips with filtering
     * 
     * Algorithm:
     * - Apply user filters (destination, budget, date range, join policy)
     * - Fetch 2x candidate set
     * - Score each trip (Popularity + Trend + Availability + Timing)
     * - Sort by score DESC
     * - Generate recommendation reasons
     * - Limit to page size
     */
    public Page<RecommendedTripDto> computeRecommendedTrips(DiscoveryFilterRequest request, Pageable pageable) {
        log.debug("Computing recommended trips with filters: {}", request.getDestination());
        
        // Set default date range if not provided
        LocalDate startMin = request.getStartDateMin() != null 
            ? request.getStartDateMin() 
            : LocalDate.now().plusDays(7);
        LocalDate startMax = request.getStartDateMax() != null 
            ? request.getStartDateMax() 
            : LocalDate.now().plusDays(365);
        
        // Parse join policy if provided
        JoinPolicy joinPolicy = null;
        if (request.getJoinPolicy() != null) {
            try {
                joinPolicy = JoinPolicy.valueOf(request.getJoinPolicy().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid joinPolicy: {}", request.getJoinPolicy());
            }
        }
        
        // Fetch candidate trips with filters
        Pageable candidatePageable = PageRequest.of(0, pageable.getPageSize() * 2);
        List<TripEntity> candidateTrips = tripDiscoveryRepository.findRecommendedTripCandidates(
            startMin,
            startMax,
            request.getDestination(),
            request.getBudgetMin(),
            request.getBudgetMax(),
            joinPolicy,
            candidatePageable
        );
        
        log.debug("Found {} candidate trips for recommendations", candidateTrips.size());
        
        // Score, map, and sort
        List<RecommendedTripDto> scoredTrips = candidateTrips.stream()
            .map(trip -> {
                double score = scoringService.computeRecommendedScore(trip);
                List<String> reasons = scoringService.generateRecommendationReasons(trip, score);
                return mapToRecommendedDto(trip, score, reasons);
            })
            .sorted((t1, t2) -> Double.compare(t2.getRecommendationScore(), t1.getRecommendationScore()))
            .limit(pageable.getPageSize())
            .collect(Collectors.toList());
        
        return new PageImpl<>(scoredTrips, pageable, candidateTrips.size());
    }

    /**
     * Map TripEntity to RecommendedTripDto
     */
    private RecommendedTripDto mapToRecommendedDto(TripEntity trip, double score, List<String> reasons) {
        return RecommendedTripDto.builder()
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
            .joinPolicy(trip.getJoinPolicy())
            .visibilityStatus(trip.getVisibilityStatus())
            .recommendationScore(score)
            .reasonsForRecommendation(reasons)
            .createdAt(trip.getCreatedAt())
            .build();
    }
}

