package com.tranzo.tranzo_user_ms.trip.repository;

import com.tranzo.tranzo_user_ms.trip.enums.JoinPolicy;
import com.tranzo.tranzo_user_ms.trip.model.TripEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Custom repository for Trip Discovery endpoints
 * Handles complex queries for Featured, Recommended, and Trending trips
 */
@Repository
public interface TripDiscoveryRepository extends JpaRepository<TripEntity, UUID> {

    /**
     * Fetch featured trip candidates
     * Filters: PUBLISHED + PUBLIC + Future start date
     * Returns: Ordered by creation date (newest first)
     */
    @Query("""
        SELECT t
        FROM TripEntity t
        WHERE t.tripStatus = com.tranzo.tranzo_user_ms.trip.enums.TripStatus.PUBLISHED
          AND t.visibilityStatus = com.tranzo.tranzo_user_ms.trip.enums.VisibilityStatus.PUBLIC
          AND t.tripStartDate >= CAST(:today AS LocalDate)
          AND (:budgetMin IS NULL OR t.estimatedBudget >= :budgetMin)
          AND (:budgetMax IS NULL OR t.estimatedBudget <= :budgetMax)
        ORDER BY t.createdAt DESC
    """)
    List<TripEntity> findFeaturedTripCandidates(
        @Param("today") LocalDate today,
        @Param("budgetMin") Double budgetMin,
        @Param("budgetMax") Double budgetMax,
        Pageable pageable
    );

    /**
     * Fetch recommended trip candidates with destination filter
     * Filters: PUBLISHED + PUBLIC + Date range + Budget + Destination + Join Policy
     */
    @Query("""
        SELECT t
        FROM TripEntity t
        WHERE t.tripStatus = com.tranzo.tranzo_user_ms.trip.enums.TripStatus.PUBLISHED
          AND t.visibilityStatus = com.tranzo.tranzo_user_ms.trip.enums.VisibilityStatus.PUBLIC
          AND t.tripStartDate BETWEEN :startMin AND :startMax
          AND (:destination IS NULL OR LOWER(t.tripDestination) LIKE LOWER(CONCAT('%', :destination, '%')))
          AND (:budgetMin IS NULL OR t.estimatedBudget >= :budgetMin)
          AND (:budgetMax IS NULL OR t.estimatedBudget <= :budgetMax)
          AND (:joinPolicy IS NULL OR t.joinPolicy = :joinPolicy)
        ORDER BY t.createdAt DESC
    """)
    List<TripEntity> findRecommendedTripCandidates(
        @Param("startMin") LocalDate startMin,
        @Param("startMax") LocalDate startMax,
        @Param("destination") String destination,
        @Param("budgetMin") Double budgetMin,
        @Param("budgetMax") Double budgetMax,
        @Param("joinPolicy") JoinPolicy joinPolicy,
        Pageable pageable
    );

    /**
     * Fetch trips created within a specific date window (for trending)
     * Used to compute trending destination metrics
     */
    @Query("""
        SELECT t
        FROM TripEntity t
        WHERE t.tripStatus = com.tranzo.tranzo_user_ms.trip.enums.TripStatus.PUBLISHED
          AND t.visibilityStatus = com.tranzo.tranzo_user_ms.trip.enums.VisibilityStatus.PUBLIC
          AND t.createdAt BETWEEN :windowStart AND :windowEnd
        ORDER BY t.createdAt DESC
    """)
    List<TripEntity> findTripsInWindow(
        @Param("windowStart") LocalDateTime windowStart,
        @Param("windowEnd") LocalDateTime windowEnd
    );

    /**
     * Count trips by destination in a time window
     * Returns: [destination, count] pairs
     */
    @Query("""
        SELECT t.tripDestination, COUNT(t.tripId) as trip_count
        FROM TripEntity t
        WHERE t.tripStatus = com.tranzo.tranzo_user_ms.trip.enums.TripStatus.PUBLISHED
          AND t.visibilityStatus = com.tranzo.tranzo_user_ms.trip.enums.VisibilityStatus.PUBLIC
          AND t.createdAt BETWEEN :windowStart AND :windowEnd
        GROUP BY t.tripDestination
        ORDER BY trip_count DESC
    """)
    List<Object[]> countTripsByDestinationInWindow(
        @Param("windowStart") LocalDateTime windowStart,
        @Param("windowEnd") LocalDateTime windowEnd
    );

    /**
     * Fetch trips for a specific destination in a time window
     * Used for destination-specific trending metrics
     */
    @Query("""
        SELECT t
        FROM TripEntity t
        WHERE t.tripStatus = com.tranzo.tranzo_user_ms.trip.enums.TripStatus.PUBLISHED
          AND t.visibilityStatus = com.tranzo.tranzo_user_ms.trip.enums.VisibilityStatus.PUBLIC
          AND t.createdAt BETWEEN :windowStart AND :windowEnd
          AND LOWER(t.tripDestination) = LOWER(:destination)
        ORDER BY t.createdAt DESC
    """)
    List<TripEntity> findTripsByDestinationInWindow(
        @Param("destination") String destination,
        @Param("windowStart") LocalDateTime windowStart,
        @Param("windowEnd") LocalDateTime windowEnd
    );
}

