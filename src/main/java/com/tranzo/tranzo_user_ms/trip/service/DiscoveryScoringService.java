package com.tranzo.tranzo_user_ms.trip.service;

import com.tranzo.tranzo_user_ms.trip.dto.MomentumDto;
import com.tranzo.tranzo_user_ms.trip.dto.ParticipationDto;
import com.tranzo.tranzo_user_ms.trip.model.TripEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Scoring Service for Discovery Endpoints
 * Computes scores for Featured, Recommended, and Trending trips
 */
@Service
@Slf4j
public class DiscoveryScoringService {

    // ============ FEATURED TRIP WEIGHTS ============
    private static final double FEATURED_RECENCY_WEIGHT = 0.40;
    private static final double FEATURED_AVAILABILITY_WEIGHT = 0.35;
    private static final double FEATURED_ENGAGEMENT_WEIGHT = 0.25;
    private static final int RECENCY_DECAY_DAYS = 30;

    // ============ RECOMMENDED TRIP WEIGHTS ============
    private static final double RECOMMENDED_POPULARITY_WEIGHT = 0.35;
    private static final double RECOMMENDED_TREND_WEIGHT = 0.30;
    private static final double RECOMMENDED_AVAILABILITY_WEIGHT = 0.25;
    private static final double RECOMMENDED_TIMING_WEIGHT = 0.10;
    private static final int IDEAL_BOOKING_WINDOW_MIN = 14;   // days
    private static final int IDEAL_BOOKING_WINDOW_MAX = 90;   // days

    // ============ TRENDING DESTINATION WEIGHTS ============
    private static final double TRENDING_VELOCITY_WEIGHT = 0.40;
    private static final double TRENDING_MOMENTUM_WEIGHT = 0.35;
    private static final double TRENDING_AVAILABILITY_WEIGHT = 0.25;
    private static final double TRENDING_VELOCITY_CAP = 2.0;
    private static final double BASELINE_TRIPS_PER_WEEK = 8.0;

    /**
     * Featured Score Calculation:
     * score = (recency × 0.40) + (availability × 0.35) + (engagement × 0.25)
     * Range: [0.0, 1.0]
     */
    public double computeFeaturedScore(TripEntity trip) {
        double recencyScore = computeRecencyScore(trip.getCreatedAt().toLocalDate());
        double availabilityScore = trip.getIsFull() ? 0.5 : 1.0;
        double engagementScore = trip.getMaxParticipants() > 0 
            ? (double) trip.getCurrentParticipants() / trip.getMaxParticipants()
            : 0.0;
        
        double score = (recencyScore * FEATURED_RECENCY_WEIGHT)
                     + (availabilityScore * FEATURED_AVAILABILITY_WEIGHT)
                     + (engagementScore * FEATURED_ENGAGEMENT_WEIGHT);
        
        return Math.min(1.0, Math.max(0.0, score));
    }

    /**
     * Recommended Score Calculation:
     * score = (popularity × 0.35) + (trend × 0.30) + (availability × 0.25) + (timing × 0.10)
     * Range: [0.0, 1.0]
     * 
     * Note: Trend score is a placeholder (0.7) and should be enriched with destination-level data
     */
    public double computeRecommendedScore(TripEntity trip) {
        double popularityScore = trip.getMaxParticipants() > 0 
            ? (double) trip.getCurrentParticipants() / trip.getMaxParticipants()
            : 0.0;
        
        // Placeholder trend score (MVP: no historical trending data yet)
        double trendScore = 0.7;
        
        double availabilityScore = trip.getIsFull() ? 0.3 : 1.0;
        
        // Timing score: prefer trips in ideal booking window (14-90 days)
        long daysUntilStart = ChronoUnit.DAYS.between(LocalDate.now(), trip.getTripStartDate());
        double timingScore = (daysUntilStart >= IDEAL_BOOKING_WINDOW_MIN && daysUntilStart <= IDEAL_BOOKING_WINDOW_MAX) 
            ? 1.0 
            : 0.5;
        
        double score = (popularityScore * RECOMMENDED_POPULARITY_WEIGHT)
                     + (trendScore * RECOMMENDED_TREND_WEIGHT)
                     + (availabilityScore * RECOMMENDED_AVAILABILITY_WEIGHT)
                     + (timingScore * RECOMMENDED_TIMING_WEIGHT);
        
        return Math.min(1.0, Math.max(0.0, score));
    }

    /**
     * Trend Score Calculation:
     * score = (velocity × 0.40) + (momentum × 0.35) + (availability × 0.25)
     * Range: [0.0, 1.0]
     */
    public double computeTrendScore(String destination, List<TripEntity> trips, String timeWindow) {
        if (trips.isEmpty()) {
            return 0.0;
        }
        
        double velocity = computeVelocity(trips, timeWindow);
        double momentum = trips.stream()
            .mapToDouble(t -> t.getMaxParticipants() > 0 
                ? (double) t.getCurrentParticipants() / t.getMaxParticipants()
                : 0.0)
            .average()
            .orElse(0.0);
        
        double availability = (double) trips.stream().filter(t -> !t.getIsFull()).count() / trips.size();
        
        double score = (velocity * TRENDING_VELOCITY_WEIGHT)
                     + (momentum * TRENDING_MOMENTUM_WEIGHT)
                     + (availability * TRENDING_AVAILABILITY_WEIGHT);
        
        return Math.min(1.0, Math.max(0.0, score));
    }

    /**
     * Recency Score: decay from 1.0 to 0.0 over RECENCY_DECAY_DAYS
     * Newer trips score higher
     */
    private double computeRecencyScore(LocalDate createdDate) {
        long daysSinceCreation = ChronoUnit.DAYS.between(createdDate, LocalDate.now());
        double score = Math.max(0.0, 1.0 - (double) daysSinceCreation / RECENCY_DECAY_DAYS);
        return Math.min(1.0, score);
    }

    /**
     * Velocity: ratio of trips in window vs historical average
     * Capped at 2.0 to prevent outliers from dominating
     * 
     * MVP: Uses baseline constant (BASELINE_TRIPS_PER_WEEK)
     * Production: Should query historical destination data
     */
    private double computeVelocity(List<TripEntity> trips, String timeWindow) {
        double tripsInWindow = trips.size();
        
        // Convert baseline to match time window
        double expectedTripsInWindow = switch (timeWindow) {
            case "7d" -> BASELINE_TRIPS_PER_WEEK;
            case "14d" -> BASELINE_TRIPS_PER_WEEK * 2;
            case "30d" -> BASELINE_TRIPS_PER_WEEK * 4.3;
            default -> BASELINE_TRIPS_PER_WEEK;
        };
        
        double velocity = expectedTripsInWindow > 0 
            ? tripsInWindow / expectedTripsInWindow
            : 0.0;
        
        return Math.min(TRENDING_VELOCITY_CAP, velocity);
    }

    /**
     * Compute momentum metrics for a destination
     */
    public MomentumDto computeMomentum(String destination, List<TripEntity> trips, String timeWindow) {
        int tripsInWindow = trips.size();
        
        int avgHistoricalTripsPerWindow = switch (timeWindow) {
            case "7d" -> (int) BASELINE_TRIPS_PER_WEEK;
            case "14d" -> (int) (BASELINE_TRIPS_PER_WEEK * 2);
            case "30d" -> (int) (BASELINE_TRIPS_PER_WEEK * 4.3);
            default -> (int) BASELINE_TRIPS_PER_WEEK;
        };
        
        int velocityPercentage = avgHistoricalTripsPerWindow > 0 
            ? (int) ((tripsInWindow * 100.0) / avgHistoricalTripsPerWindow)
            : 0;
        
        return MomentumDto.builder()
            .tripsCreatedInWindow(tripsInWindow)
            .avgHistoricalTripsPerWindow(avgHistoricalTripsPerWindow)
            .velocityPercentage(velocityPercentage)
            .build();
    }

    /**
     * Compute participation metrics for trips
     */
    public ParticipationDto computeParticipation(List<TripEntity> trips) {
        double avgParticipants = trips.stream()
            .mapToInt(TripEntity::getCurrentParticipants)
            .average()
            .orElse(0.0);
        
        long availableTrips = trips.stream().filter(t -> !t.getIsFull()).count();
        
        return ParticipationDto.builder()
            .avgParticipants(avgParticipants)
            .availableTrips((int) availableTrips)
            .totalTripsPublished(trips.size())
            .build();
    }

    /**
     * Generate human-readable recommendation reasons
     */
    public List<String> generateRecommendationReasons(TripEntity trip, double score) {
        List<String> reasons = new ArrayList<>();
        
        double engagementRatio = trip.getMaxParticipants() > 0 
            ? (double) trip.getCurrentParticipants() / trip.getMaxParticipants()
            : 0.0;
        
        if (engagementRatio > 0.8) {
            reasons.add(String.format("%.0f%% capacity - high engagement", engagementRatio * 100));
        }
        
        long daysUntilStart = ChronoUnit.DAYS.between(LocalDate.now(), trip.getTripStartDate());
        if (daysUntilStart >= IDEAL_BOOKING_WINDOW_MIN && daysUntilStart <= IDEAL_BOOKING_WINDOW_MAX) {
            reasons.add(String.format("Leaves in %d days (ideal booking window)", daysUntilStart));
        }
        
        if (score >= 0.85) {
            reasons.add("Trending in your region");
        }
        
        if (reasons.isEmpty()) {
            reasons.add("Matches your preferences");
        }
        
        return reasons;
    }
}

