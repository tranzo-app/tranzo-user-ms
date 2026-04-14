package com.tranzo.tranzo_user_ms.trip.client;

import com.tranzo.tranzo_user_ms.trip.repository.TripMemberRepository;
import com.tranzo.tranzo_user_ms.trip.enums.TripStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TripStatisticsClient {

    private final TripMemberRepository tripMemberRepository;

    /**
     * Gets the number of completed trips for a user
     */
    public Integer getCompletedTripsCount(UUID userId) {
        List<TripStatus> completedStatus = List.of(TripStatus.COMPLETED);
        List<?> completedTrips = tripMemberRepository.findTripsByUserIdAndStatusIn(userId, completedStatus);
        return completedTrips.size();
    }
}
