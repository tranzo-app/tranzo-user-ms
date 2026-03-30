package com.tranzo.tranzo_user_ms.trip.repository;

import com.tranzo.tranzo_user_ms.trip.enums.TripStatus;
import com.tranzo.tranzo_user_ms.trip.model.TripEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripRepository extends JpaRepository<TripEntity, UUID>, JpaSpecificationExecutor<TripEntity> {
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

    @Query("SELECT t FROM TripEntity t WHERE t.tripStatus = :status " +
            "AND EXISTS (SELECT 1 FROM TripMemberEntity m1 WHERE m1.trip.tripId = t.tripId AND m1.userId = :userId1) " +
            "AND EXISTS (SELECT 1 FROM TripMemberEntity m2 WHERE m2.trip.tripId = t.tripId AND m2.userId = :userId2) " +
            "ORDER BY t.tripEndDate DESC")
    List<TripEntity> findMutualCompletedTrips(
            @Param("userId1") UUID userId1,
            @Param("userId2") UUID userId2,
            @Param("status") TripStatus status);

    @Query("SELECT t FROM TripEntity t WHERE t.tripStatus IN :statuses " +
            "AND EXISTS (SELECT 1 FROM TripMemberEntity m1 WHERE m1.trip.tripId = t.tripId AND m1.userId = :userId1) " +
            "AND EXISTS (SELECT 1 FROM TripMemberEntity m2 WHERE m2.trip.tripId = t.tripId AND m2.userId = :userId2) " +
            "ORDER BY t.tripEndDate DESC")
    List<TripEntity> findMutualTrips(
            @Param("userId1") UUID userId1,
            @Param("userId2") UUID userId2,
            @Param("statuses") List<TripStatus> statuses);

    Page<TripEntity> findAll(Specification<TripEntity> spec, Pageable pageable);

    @Query("SELECT t.tripTitle FROM TripEntity t WHERE t.tripId = :tripId")
    String findTripNameByTripId(UUID tripId);
}
