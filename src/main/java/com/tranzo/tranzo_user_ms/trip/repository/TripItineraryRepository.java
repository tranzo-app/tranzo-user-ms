package com.tranzo.tranzo_user_ms.trip.repository;

import com.tranzo.tranzo_user_ms.trip.model.TripItineraryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TripItineraryRepository extends JpaRepository<TripItineraryEntity, UUID> {
    Optional<TripItineraryEntity> findByTrip_TripIdAndDayNumber(UUID tripId, Integer dayNumber);
}
