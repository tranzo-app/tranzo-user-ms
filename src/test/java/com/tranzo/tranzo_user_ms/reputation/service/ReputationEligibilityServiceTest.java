package com.tranzo.tranzo_user_ms.reputation.service;

import com.tranzo.tranzo_user_ms.trip.enums.TripMemberStatus;
import com.tranzo.tranzo_user_ms.trip.enums.TripStatus;
import com.tranzo.tranzo_user_ms.trip.model.TripEntity;
import com.tranzo.tranzo_user_ms.trip.repository.TripMemberRepository;
import com.tranzo.tranzo_user_ms.trip.repository.TripRepository;
import com.tranzo.tranzo_user_ms.user.service.ReputationEligibilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReputationEligibilityService Unit Tests")
class ReputationEligibilityServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripMemberRepository tripMemberRepository;

    @InjectMocks
    private ReputationEligibilityService eligibilityService;

    private UUID userId;
    private UUID tripId;
    private TripEntity completedTrip;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        tripId = UUID.randomUUID();
        completedTrip = new TripEntity();
        completedTrip.setTripId(tripId);
        completedTrip.setTripStatus(TripStatus.COMPLETED);
    }

    @Test
    @DisplayName("Should return true when trip is COMPLETED and user is ACTIVE member")
    void canSubmitRatingForTrip_Success() {
        when(tripRepository.findByTripIdAndTripStatus(tripId, TripStatus.COMPLETED))
                .thenReturn(Optional.of(completedTrip));
        when(tripMemberRepository.existsByTrip_TripIdAndUserIdAndStatus(tripId, userId, TripMemberStatus.ACTIVE))
                .thenReturn(true);

        assertTrue(eligibilityService.canSubmitRatingForTrip(userId, tripId));
        verify(tripRepository).findByTripIdAndTripStatus(tripId, TripStatus.COMPLETED);
        verify(tripMemberRepository).existsByTrip_TripIdAndUserIdAndStatus(tripId, userId, TripMemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should return false when trip is not COMPLETED")
    void canSubmitRatingForTrip_TripNotCompleted() {
        when(tripRepository.findByTripIdAndTripStatus(tripId, TripStatus.COMPLETED))
                .thenReturn(Optional.empty());

        assertFalse(eligibilityService.canSubmitRatingForTrip(userId, tripId));
        verify(tripMemberRepository, never()).existsByTrip_TripIdAndUserIdAndStatus(any(), any(), any());
    }

    @Test
    @DisplayName("Should return false when user is not ACTIVE member")
    void canSubmitRatingForTrip_UserNotActiveMember() {
        when(tripRepository.findByTripIdAndTripStatus(tripId, TripStatus.COMPLETED))
                .thenReturn(Optional.of(completedTrip));
        when(tripMemberRepository.existsByTrip_TripIdAndUserIdAndStatus(tripId, userId, TripMemberStatus.ACTIVE))
                .thenReturn(false);

        assertFalse(eligibilityService.canSubmitRatingForTrip(userId, tripId));
    }

    @Test
    @DisplayName("Should return false when userId is null")
    void canSubmitRatingForTrip_NullUserId() {
        assertFalse(eligibilityService.canSubmitRatingForTrip(null, tripId));
        verify(tripRepository, never()).findByTripIdAndTripStatus(any(), any());
    }

    @Test
    @DisplayName("Should return false when tripId is null")
    void canSubmitRatingForTrip_NullTripId() {
        assertFalse(eligibilityService.canSubmitRatingForTrip(userId, null));
        verify(tripRepository, never()).findByTripIdAndTripStatus(any(), any());
    }
}
