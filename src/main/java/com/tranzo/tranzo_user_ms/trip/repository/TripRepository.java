package com.tranzo.tranzo_user_ms.trip.repository;

import com.tranzo.tranzo_user_ms.trip.enums.TripStatus;
import com.tranzo.tranzo_user_ms.trip.model.TripEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripRepository extends JpaRepository<TripEntity, UUID> {
    List<TripEntity> findByTripStatusAndTripStartDateLessThanEqual(
            TripStatus status, LocalDate date);

    List<TripEntity> findByTripStatusAndTripEndDateBefore(
            TripStatus status, LocalDate date);

    List<TripEntity> findByTripStatusInAndTripStartDateBetween(
            List<TripStatus> statuses, LocalDate startInclusive, LocalDate endInclusive);

    List<TripEntity> findByTripStatus(TripStatus status);

    Optional<TripEntity> findByTripIdAndTripStatus(
            UUID tripId, TripStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TripEntity t where t.tripId = :tripId")
    Optional<TripEntity> findByIdForUpdate(UUID tripId);

    @Query("SELECT t FROM TripEntity t WHERE t.tripStatus <> :status")
    List<TripEntity> findAllTrips(TripStatus status);
}
