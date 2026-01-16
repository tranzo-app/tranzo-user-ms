package com.tranzo.tranzo_user_ms.trip.repository;

import com.tranzo.tranzo_user_ms.trip.enums.TripMemberRole;
import com.tranzo.tranzo_user_ms.trip.enums.TripMemberStatus;
import com.tranzo.tranzo_user_ms.trip.model.TripMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TripMemberRepository extends JpaRepository<TripMemberEntity, UUID> {
    Optional<TripMemberEntity> findByTrip_TripIdAndUserIdAndRole(UUID tripId, UUID userUuid, TripMemberRole role);

    Optional<TripMemberEntity> findByTrip_TripIdAndUserIdAndStatus(UUID tripId, UUID userUuid, TripMemberStatus status);

    boolean existsByTrip_TripIdAndUserIdAndRoleAndStatus(UUID tripId, UUID userId, TripMemberRole role, TripMemberStatus status);

    boolean existsByTrip_TripIdAndUserIdAndStatus(UUID tripId, UUID userId, TripMemberStatus status);
}
