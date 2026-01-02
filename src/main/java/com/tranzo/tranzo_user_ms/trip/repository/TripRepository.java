package com.tranzo.tranzo_user_ms.trip.repository;

import com.tranzo.tranzo_user_ms.trip.enums.TripStatus;
import com.tranzo.tranzo_user_ms.trip.model.TripEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripRepository extends JpaRepository<TripEntity, UUID> {
    List<TripEntity> findByTripStatusAndTripStartDateLessThanEqual(
            TripStatus status, LocalDate date);

    List<TripEntity> findByTripStatusAndTripEndDateBefore(
            TripStatus status, LocalDate date);

    Optional<TripEntity> findByTripIdAndTripStatus(
            UUID tripId, TripStatus status
    );
}
