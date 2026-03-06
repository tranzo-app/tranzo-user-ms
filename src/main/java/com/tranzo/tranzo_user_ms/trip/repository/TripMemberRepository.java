package com.tranzo.tranzo_user_ms.trip.repository;

import com.tranzo.tranzo_user_ms.trip.enums.TripMemberRole;
import com.tranzo.tranzo_user_ms.trip.enums.TripMemberStatus;
import com.tranzo.tranzo_user_ms.trip.enums.TripStatus;
import com.tranzo.tranzo_user_ms.trip.model.TripEntity;
import com.tranzo.tranzo_user_ms.trip.model.TripMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripMemberRepository extends JpaRepository<TripMemberEntity, UUID> {

    List<TripMemberEntity> findByTrip_TripIdAndStatus(UUID tripId, TripMemberStatus status);
    Optional<TripMemberEntity> findByTrip_TripIdAndUserIdAndRole(UUID tripId, UUID userUuid, TripMemberRole role);

    Optional<TripMemberEntity> findByTrip_TripIdAndUserIdAndStatus(UUID tripId, UUID userUuid, TripMemberStatus status);

    boolean existsByTrip_TripIdAndUserIdAndRoleAndStatus(UUID tripId, UUID userId, TripMemberRole role, TripMemberStatus status);

    boolean existsByTrip_TripIdAndUserIdAndStatus(UUID tripId, UUID userId, TripMemberStatus status);

    @Query("""
        SELECT tm.trip
        FROM TripMemberEntity tm
        WHERE tm.userId = :userId
        AND tm.trip.tripStatus IN :statuses
    """)
    List<TripEntity> findTripsByUserIdAndStatusIn(
            @Param("userId") UUID userId,
            @Param("statuses") List<TripStatus> statuses
    );
}
