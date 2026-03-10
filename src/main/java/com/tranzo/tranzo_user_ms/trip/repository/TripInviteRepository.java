package com.tranzo.tranzo_user_ms.trip.repository;

import com.tranzo.tranzo_user_ms.trip.model.TripInviteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TripInviteRepository extends JpaRepository<TripInviteEntity, UUID> {

    boolean existsByTrip_TripIdAndInvitedUserId(UUID tripId, UUID invitedUserId);

    Optional<TripInviteEntity> findByTrip_TripIdAndInvitedUserId(UUID tripId, UUID invitedUserId);
}
