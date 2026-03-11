package com.tranzo.tranzo_user_ms.reputation.service;

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
import com.tranzo.tranzo_user_ms.user.enums.VibeTag;
import com.tranzo.tranzo_user_ms.trip.enums.TripMemberRole;
import com.tranzo.tranzo_user_ms.trip.enums.TripMemberStatus;
import com.tranzo.tranzo_user_ms.trip.model.TripEntity;
import com.tranzo.tranzo_user_ms.trip.model.TripMemberEntity;
import com.tranzo.tranzo_user_ms.trip.repository.TripMemberRepository;
import com.tranzo.tranzo_user_ms.trip.repository.TripRepository;
import com.tranzo.tranzo_user_ms.user.service.RatingService;
import com.tranzo.tranzo_user_ms.user.service.ReputationEligibilityService;
import com.tranzo.tranzo_user_ms.user.service.TrustScoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RatingService Unit Tests")
class RatingServiceTest {

    @Mock
    private ReputationEligibilityService eligibilityService;

    @Mock
    private TripRatingRepository tripRatingRepository;

    @Mock
    private HostRatingRepository hostRatingRepository;

    @Mock
    private MemberRatingRepository memberRatingRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripMemberRepository tripMemberRepository;

    @Mock
    private TrustScoreService trustScoreService;

    @InjectMocks
    private RatingService ratingService;

    private UUID tripId;
    private UUID raterUserId;
    private UUID hostUserId;
    private UUID ratedUserId;
    private TripEntity trip;
    private TripMemberEntity hostMember;

    @BeforeEach
    void setUp() {
        tripId = UUID.randomUUID();
        raterUserId = UUID.randomUUID();
        hostUserId = UUID.randomUUID();
        ratedUserId = UUID.randomUUID();
        trip = new TripEntity();
        trip.setTripId(tripId);
        hostMember = new TripMemberEntity();
        hostMember.setUserId(hostUserId);
        hostMember.setRole(TripMemberRole.HOST);
        hostMember.setStatus(TripMemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should throw ForbiddenException when user cannot submit rating")
    void submitTripRating_Forbidden() {
        when(eligibilityService.canSubmitRatingForTrip(raterUserId, tripId)).thenReturn(false);

        assertThrows(ForbiddenException.class, () ->
                ratingService.submitTripRating(tripId, raterUserId,
                        SubmitTripRatingRequest.builder().destinationRating(5).itineraryRating(5).overallRating(5).build()));
        verify(tripRatingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should save new trip rating")
    void submitTripRating_NewRating() {
        when(eligibilityService.canSubmitRatingForTrip(raterUserId, tripId)).thenReturn(true);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripRatingRepository.findByTrip_TripIdAndRaterUserId(tripId, raterUserId)).thenReturn(Optional.empty());
        when(tripRatingRepository.save(any(TripRatingEntity.class))).thenAnswer(i -> i.getArgument(0));

        SubmitTripRatingRequest request = SubmitTripRatingRequest.builder()
                .destinationRating(4)
                .itineraryRating(5)
                .overallRating(5)
                .build();
        ratingService.submitTripRating(tripId, raterUserId, request);

        verify(tripRatingRepository).save(argThat(e ->
                e.getDestinationRating() == 4 && e.getItineraryRating() == 5 && e.getOverallRating() == 5
                        && e.getRaterUserId().equals(raterUserId)));
    }

    @Test
    @DisplayName("Should update existing trip rating")
    void submitTripRating_UpdateExisting() {
        when(eligibilityService.canSubmitRatingForTrip(raterUserId, tripId)).thenReturn(true);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        TripRatingEntity existing = TripRatingEntity.builder().trip(trip).raterUserId(raterUserId)
                .destinationRating(3).itineraryRating(3).overallRating(3).build();
        when(tripRatingRepository.findByTrip_TripIdAndRaterUserId(tripId, raterUserId)).thenReturn(Optional.of(existing));
        when(tripRatingRepository.save(any(TripRatingEntity.class))).thenAnswer(i -> i.getArgument(0));

        SubmitTripRatingRequest request = SubmitTripRatingRequest.builder()
                .destinationRating(5).itineraryRating(5).overallRating(5).build();
        ratingService.submitTripRating(tripId, raterUserId, request);

        verify(tripRatingRepository).save(existing);
        assertEquals(5, existing.getDestinationRating());
        assertEquals(5, existing.getOverallRating());
    }

    @Test
    @DisplayName("Should save host rating and update trust score")
    void submitHostRating_Success() {
        when(eligibilityService.canSubmitRatingForTrip(raterUserId, tripId)).thenReturn(true);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripMemberRepository.findFirstByTrip_TripIdAndRoleAndStatus(tripId, TripMemberRole.HOST, TripMemberStatus.ACTIVE))
                .thenReturn(Optional.of(hostMember));
        when(hostRatingRepository.findByTrip_TripIdAndRaterUserId(tripId, raterUserId)).thenReturn(Optional.empty());
        when(hostRatingRepository.save(any(HostRatingEntity.class))).thenAnswer(i -> i.getArgument(0));

        SubmitHostRatingRequest request = SubmitHostRatingRequest.builder()
                .coordinationRating(5)
                .communicationRating(4)
                .leadershipRating(5)
                .reviewText("Great host")
                .build();
        ratingService.submitHostRating(tripId, raterUserId, request);

        verify(hostRatingRepository).save(any(HostRatingEntity.class));
        verify(trustScoreService).updateTrustScore(hostUserId);
    }

    @Test
    @DisplayName("Should throw ForbiddenException on host rating when not eligible")
    void submitHostRating_Forbidden() {
        when(eligibilityService.canSubmitRatingForTrip(raterUserId, tripId)).thenReturn(false);

        assertThrows(ForbiddenException.class, () ->
                ratingService.submitHostRating(tripId, raterUserId,
                        SubmitHostRatingRequest.builder().coordinationRating(5).communicationRating(5).leadershipRating(5).build()));
        verify(hostRatingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should save member ratings and skip non-members")
    void submitMemberRatings_Success() {
        when(eligibilityService.canSubmitRatingForTrip(raterUserId, tripId)).thenReturn(true);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        TripMemberEntity raterMember = new TripMemberEntity();
        raterMember.setUserId(raterUserId);
        TripMemberEntity otherMember = new TripMemberEntity();
        otherMember.setUserId(ratedUserId);
        when(tripMemberRepository.findByTrip_TripIdAndStatus(tripId, TripMemberStatus.ACTIVE))
                .thenReturn(List.of(raterMember, otherMember));
        when(memberRatingRepository.findByTrip_TripIdAndRaterUserIdAndRatedUserId(any(), any(), any())).thenReturn(Optional.empty());
        when(memberRatingRepository.findByTripIdAndRaterUserId(tripId, ratedUserId)).thenReturn(List.of());
        when(memberRatingRepository.save(any(MemberRatingEntity.class))).thenAnswer(i -> i.getArgument(0));

        SubmitMemberRatingsRequest request = SubmitMemberRatingsRequest.builder()
                .ratings(List.of(
                        SubmitMemberRatingItem.builder()
                                .ratedUserId(ratedUserId)
                                .ratingScore(5)
                                .vibeTag(VibeTag.RELIABLE)
                                .reviewText("Nice")
                                .build()))
                .build();
        ratingService.submitMemberRatings(tripId, raterUserId, request);

        verify(memberRatingRepository).save(argThat(e ->
                e.getRaterUserId().equals(raterUserId) && e.getRatedUserId().equals(ratedUserId)
                        && e.getRatingScore() == 5 && e.getVibeTag() == VibeTag.RELIABLE));
        verify(trustScoreService).updateTrustScore(ratedUserId);
    }

    @Test
    @DisplayName("Should throw ForbiddenException on member ratings when not eligible")
    void submitMemberRatings_Forbidden() {
        when(eligibilityService.canSubmitRatingForTrip(raterUserId, tripId)).thenReturn(false);

        assertThrows(ForbiddenException.class, () ->
                ratingService.submitMemberRatings(tripId, raterUserId,
                        SubmitMemberRatingsRequest.builder().ratings(List.of()).build()));
        verify(memberRatingRepository, never()).save(any());
    }
}
