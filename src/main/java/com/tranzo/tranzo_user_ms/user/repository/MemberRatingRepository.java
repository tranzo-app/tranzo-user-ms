package com.tranzo.tranzo_user_ms.user.repository;

import com.tranzo.tranzo_user_ms.user.model.MemberRatingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemberRatingRepository extends JpaRepository<MemberRatingEntity, UUID> {

    Optional<MemberRatingEntity> findByTrip_TripIdAndRaterUserIdAndRatedUserId(
            UUID tripId, UUID raterUserId, UUID ratedUserId);

    boolean existsByTrip_TripIdAndRaterUserIdAndRatedUserId(
            UUID tripId, UUID raterUserId, UUID ratedUserId);

    List<MemberRatingEntity> findByRatedUserIdAndVisibleAtIsNotNullOrderByCreatedAtDesc(UUID ratedUserId);

    @Query("SELECT mr FROM MemberRatingEntity mr WHERE mr.trip.tripId = :tripId AND mr.raterUserId = :raterUserId")
    List<MemberRatingEntity> findByTripIdAndRaterUserId(
            @Param("tripId") UUID tripId,
            @Param("raterUserId") UUID raterUserId);
}
