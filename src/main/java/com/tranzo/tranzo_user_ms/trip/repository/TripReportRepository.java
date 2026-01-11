package com.tranzo.tranzo_user_ms.trip.repository;

import com.tranzo.tranzo_user_ms.trip.model.TripReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TripReportRepository extends JpaRepository<TripReportEntity, UUID> {
    boolean existsByReportedByAndTrip_TripId(UUID reportedBy, UUID tripId);
}
