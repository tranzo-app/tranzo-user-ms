package com.tranzo.tranzo_user_ms.trip.repository;

import com.tranzo.tranzo_user_ms.trip.enums.JoinRequestStatus;
import com.tranzo.tranzo_user_ms.trip.model.TripJoinRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface TripJoinRequestRepository extends JpaRepository<TripJoinRequestEntity, UUID> {
    List<TripJoinRequestEntity> findByTrip_TripId(UUID tripId);

    List<TripJoinRequestEntity> findByTrip_TripIdAndStatus(UUID tripId, JoinRequestStatus status);

    boolean existsByTrip_TripIdAndUserIdAndStatusIn(UUID tripId, UUID userUuid, Set<JoinRequestStatus> statusSet);
}
