package com.tranzo.tranzo_user_ms.user.repository;

import com.tranzo.tranzo_user_ms.user.model.TripRatingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TripRatingRepository extends JpaRepository<TripRatingEntity, UUID> {

    Optional<TripRatingEntity> findByTrip_TripIdAndRaterUserId(UUID tripId, UUID raterUserId);

    boolean existsByTrip_TripIdAndRaterUserId(UUID tripId, UUID raterUserId);
}
