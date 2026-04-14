package com.tranzo.tranzo_user_ms.user.service;

import com.tranzo.tranzo_user_ms.commons.exception.ForbiddenException;
import com.tranzo.tranzo_user_ms.user.dto.SubmitHostRatingRequest;
import com.tranzo.tranzo_user_ms.user.dto.SubmitMemberRatingItem;
import com.tranzo.tranzo_user_ms.user.dto.SubmitMemberRatingsRequest;
import com.tranzo.tranzo_user_ms.user.dto.SubmitTripRatingRequest;
import com.tranzo.tranzo_user_ms.user.model.HostRatingEntity;
import com.tranzo.tranzo_user_ms.user.model.MemberRatingEntity;
import com.tranzo.tranzo_user_ms.user.model.TripRatingEntity;
import com.tranzo.tranzo_user_ms.user.repository.HostRatingRepository;
import com.tranzo.tranzo_user_ms.user.repository.MemberRatingRepository;
import com.tranzo.tranzo_user_ms.user.repository.TripRatingRepository;
import com.tranzo.tranzo_user_ms.trip.enums.TripMemberRole;
import com.tranzo.tranzo_user_ms.trip.enums.TripMemberStatus;
import com.tranzo.tranzo_user_ms.trip.model.TripEntity;
import com.tranzo.tranzo_user_ms.trip.model.TripMemberEntity;
import com.tranzo.tranzo_user_ms.trip.repository.TripMemberRepository;
import com.tranzo.tranzo_user_ms.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RatingService {

    private static final String ELIGIBILITY_MESSAGE = "Only participants who completed this trip can submit feedback";

    private final ReputationEligibilityService eligibilityService;
    private final TripRatingRepository tripRatingRepository;
    private final HostRatingRepository hostRatingRepository;
    private final MemberRatingRepository memberRatingRepository;
    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final TrustScoreService trustScoreService;

    @Transactional
    public void submitTripRating(UUID tripId, UUID raterUserId, SubmitTripRatingRequest request) {
        if (!eligibilityService.canSubmitRatingForTrip(raterUserId, tripId)) {
            throw new ForbiddenException(ELIGIBILITY_MESSAGE);
        }
        TripEntity trip = tripRepository.findById(tripId).orElseThrow();
        TripRatingEntity entity = tripRatingRepository.findByTrip_TripIdAndRaterUserId(tripId, raterUserId)
                .orElse(TripRatingEntity.builder()
                        .trip(trip)
                        .raterUserId(raterUserId)
                        .build());
        entity.setDestinationRating(request.getDestinationRating());
        entity.setItineraryRating(request.getItineraryRating());
        entity.setOverallRating(request.getOverallRating());
        tripRatingRepository.save(entity);
        log.debug("Saved trip rating for trip {} by user {}", tripId, raterUserId);
    }

    @Transactional
    public void submitHostRating(UUID tripId, UUID raterUserId, SubmitHostRatingRequest request) {
        if (!eligibilityService.canSubmitRatingForTrip(raterUserId, tripId)) {
            throw new ForbiddenException(ELIGIBILITY_MESSAGE);
        }
        TripEntity trip = tripRepository.findById(tripId).orElseThrow();
        UUID hostUserId = tripMemberRepository
                .findFirstByTrip_TripIdAndRoleAndStatus(tripId, TripMemberRole.HOST, TripMemberStatus.ACTIVE)
                .map(TripMemberEntity::getUserId)
                .orElseThrow(() -> new IllegalStateException("Trip has no host"));
        HostRatingEntity entity = hostRatingRepository.findByTrip_TripIdAndRaterUserId(tripId, raterUserId)
                .orElse(HostRatingEntity.builder()
                        .trip(trip)
                        .hostUserId(hostUserId)
                        .raterUserId(raterUserId)
                        .build());
        entity.setCoordinationRating(request.getCoordinationRating());
        entity.setCommunicationRating(request.getCommunicationRating());
        entity.setLeadershipRating(request.getLeadershipRating());
        entity.setReviewText(request.getReviewText());
        hostRatingRepository.save(entity);
        trustScoreService.updateTrustScore(hostUserId);
        log.debug("Saved host rating for trip {} by user {}", tripId, raterUserId);
    }

    @Transactional
    public void submitMemberRatings(UUID tripId, UUID raterUserId, SubmitMemberRatingsRequest request) {
        if (!eligibilityService.canSubmitRatingForTrip(raterUserId, tripId)) {
            throw new ForbiddenException(ELIGIBILITY_MESSAGE);
        }
        TripEntity trip = tripRepository.findById(tripId).orElseThrow();
        List<TripMemberEntity> activeMembers = tripMemberRepository.findByTrip_TripIdAndStatus(tripId, TripMemberStatus.ACTIVE);
        List<UUID> validRatedIds = activeMembers.stream()
                .map(TripMemberEntity::getUserId)
                .filter(id -> !id.equals(raterUserId))
                .toList();
        LocalDateTime now = LocalDateTime.now();
        for (SubmitMemberRatingItem item : request.getRatings()) {
            if (!validRatedIds.contains(item.getRatedUserId())) {
                log.warn("Skipping member rating for non-member or self: ratedUserId={}", item.getRatedUserId());
                continue;
            }
            MemberRatingEntity entity = memberRatingRepository
                    .findByTrip_TripIdAndRaterUserIdAndRatedUserId(tripId, raterUserId, item.getRatedUserId())
                    .orElse(MemberRatingEntity.builder()
                            .trip(trip)
                            .raterUserId(raterUserId)
                            .ratedUserId(item.getRatedUserId())
                            .build());
            entity.setRatingScore(item.getRatingScore());
            entity.setVibeTag(item.getVibeTag());
            entity.setReviewText(item.getReviewText());
            memberRatingRepository.save(entity);
            trustScoreService.updateTrustScore(item.getRatedUserId());
            revealMutualRatingsIfBothSubmitted(tripId, raterUserId, item.getRatedUserId(), now);
        }
        log.debug("Saved {} member ratings for trip {} by user {}", request.getRatings().size(), tripId, raterUserId);
    }

    /**
     * Blind rule: A's rating of B becomes visible only after B has also submitted ratings for this trip.
     * When B submits, reveal A->B and B->A for any pair that now has both sides submitted.
     */
    private void revealMutualRatingsIfBothSubmitted(UUID tripId, UUID raterUserId, UUID ratedUserId, LocalDateTime now) {
        List<MemberRatingEntity> ratedUserSubmitted = memberRatingRepository.findByTripIdAndRaterUserId(tripId, ratedUserId);
        if (ratedUserSubmitted.isEmpty()) {
            return;
        }
        memberRatingRepository.findByTrip_TripIdAndRaterUserIdAndRatedUserId(tripId, raterUserId, ratedUserId)
                .filter(e -> e.getVisibleAt() == null)
                .ifPresent(e -> {
                    e.setVisibleAt(now);
                    memberRatingRepository.save(e);
                    trustScoreService.updateTrustScore(ratedUserId);
                });
        memberRatingRepository.findByTrip_TripIdAndRaterUserIdAndRatedUserId(tripId, ratedUserId, raterUserId)
                .filter(e -> e.getVisibleAt() == null)
                .ifPresent(e -> {
                    e.setVisibleAt(now);
                    memberRatingRepository.save(e);
                    trustScoreService.updateTrustScore(raterUserId);
                });
    }

    /**
     * Gets the average member rating for a user based on all visible member ratings
     */
    public BigDecimal getUserAverageRating(UUID userId) {
        List<MemberRatingEntity> ratings = memberRatingRepository.findByRatedUserIdAndVisibleAtIsNotNullOrderByCreatedAtDesc(userId);
        if (ratings.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal sum = ratings.stream()
                .map(rating -> BigDecimal.valueOf(rating.getRatingScore()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(new BigDecimal(ratings.size()), 2, RoundingMode.HALF_UP);
    }
}
