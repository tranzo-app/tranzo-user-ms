package com.tranzo.tranzo_user_ms.trip.service;

import com.tranzo.tranzo_user_ms.trip.dto.FeaturedTripDto;
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
 * Service for Featured Trips Discovery
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class FeaturedTripService {

    private final TripDiscoveryRepository tripDiscoveryRepository;
    private final DiscoveryScoringService scoringService;

    /**
     * Compute featured trips with pagination
     * 
     * Algorithm:
     * - Fetch 3x candidate set (to allow for scoring/filtering)
     * - Score each trip (Recency + Availability + Engagement)
     * - Sort by score DESC
     * - Limit to page size
     */
    public Page<FeaturedTripDto> computeFeaturedTrips(
            int page,
            int size,
            Double budgetMin,
            Double budgetMax
    ) {
        log.debug("Computing featured trips - page: {}, size: {}", page, size);
        
        // Fetch candidate trips (3x size to allow scoring/filtering)
        Pageable candidatePageable = PageRequest.of(0, size * 3);
        List<TripEntity> candidateTrips = tripDiscoveryRepository.findFeaturedTripCandidates(
            LocalDate.now(),
            budgetMin,
            budgetMax,
            candidatePageable
        );
        
        log.debug("Found {} candidate trips for featured list", candidateTrips.size());
        
        // Score and map to DTO
        List<FeaturedTripDto> scoredTrips = candidateTrips.stream()
            .map(trip -> {
                double score = scoringService.computeFeaturedScore(trip);
                return mapToFeaturedDto(trip, score);
            })
            .sorted((t1, t2) -> Double.compare(t2.getFeaturedScore(), t1.getFeaturedScore()))
            .limit(size)
            .collect(Collectors.toList());
        
        // Create pageable response
        Pageable responsePageable = PageRequest.of(page, size);
        return new PageImpl<>(scoredTrips, responsePageable, candidateTrips.size());
    }

    /**
     * Map TripEntity to FeaturedTripDto
     */
    private FeaturedTripDto mapToFeaturedDto(TripEntity trip, double score) {
        return FeaturedTripDto.builder()
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
            .featuredScore(score)
            .createdAt(trip.getCreatedAt())
            .build();
    }
}

