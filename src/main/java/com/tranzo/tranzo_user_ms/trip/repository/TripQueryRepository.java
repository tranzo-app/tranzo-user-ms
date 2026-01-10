package com.tranzo.tranzo_user_ms.trip.repository;

import com.tranzo.tranzo_user_ms.trip.model.TripQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripQueryRepository extends JpaRepository<TripQueryEntity, UUID> {
    List<TripQueryEntity> findByTripIdOrderByCreatedAtDesc(UUID tripId);
    Optional<TripQueryEntity> findByQueryIdAndTripId(UUID queryId, UUID tripId);
}
