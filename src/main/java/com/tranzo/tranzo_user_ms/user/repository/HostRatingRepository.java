package com.tranzo.tranzo_user_ms.user.repository;

import com.tranzo.tranzo_user_ms.user.model.HostRatingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HostRatingRepository extends JpaRepository<HostRatingEntity, UUID> {

    Optional<HostRatingEntity> findByTrip_TripIdAndRaterUserId(UUID tripId, UUID raterUserId);

    boolean existsByTrip_TripIdAndRaterUserId(UUID tripId, UUID raterUserId);

    List<HostRatingEntity> findByHostUserIdOrderByCreatedAtDesc(UUID hostUserId);
}
