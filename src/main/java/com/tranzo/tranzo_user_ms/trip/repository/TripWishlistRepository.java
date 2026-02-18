package com.tranzo.tranzo_user_ms.trip.repository;

import com.tranzo.tranzo_user_ms.trip.model.TripWishlistEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripWishlistRepository extends JpaRepository<TripWishlistEntity, UUID> {
    boolean existsByUserIdAndTrip_TripId(UUID userId, UUID tripId);

    Optional<TripWishlistEntity> findByTrip_TripIdAndUserId(UUID tripId, UUID userUuid);

    @EntityGraph(attributePaths = "trip")
    List<TripWishlistEntity> findByUserIdOrderByCreatedAtDesc(UUID userUuid);
}
