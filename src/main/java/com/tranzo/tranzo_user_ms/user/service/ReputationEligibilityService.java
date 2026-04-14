package com.tranzo.tranzo_user_ms.user.service;

import com.tranzo.tranzo_user_ms.trip.enums.TripMemberStatus;
import com.tranzo.tranzo_user_ms.trip.enums.TripStatus;
import com.tranzo.tranzo_user_ms.trip.repository.TripMemberRepository;
import com.tranzo.tranzo_user_ms.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Eligibility to submit ratings: only ACTIVE members of a COMPLETED trip.
 * No Aadhaar verification.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReputationEligibilityService {

    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;

    /**
     * Returns true if the user can submit any rating (trip, host, member) for this trip:
     * - Trip exists and has status COMPLETED
     * - User is an ACTIVE member of the trip
     */
    public boolean canSubmitRatingForTrip(UUID userId, UUID tripId) {
        if (userId == null || tripId == null) {
            return false;
        }
        boolean tripCompleted = tripRepository.findByTripIdAndTripStatus(tripId, TripStatus.COMPLETED).isPresent();
        if (!tripCompleted) {
            log.debug("Trip {} is not COMPLETED", tripId);
            return false;
        }
        boolean activeMember = tripMemberRepository.existsByTrip_TripIdAndUserIdAndStatus(
                tripId, userId, TripMemberStatus.ACTIVE);
        if (!activeMember) {
            log.debug("User {} is not an ACTIVE member of trip {}", userId, tripId);
            return false;
        }
        return true;
    }
}
