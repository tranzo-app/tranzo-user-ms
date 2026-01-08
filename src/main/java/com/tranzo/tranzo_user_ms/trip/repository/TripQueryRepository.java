package com.tranzo.tranzo_user_ms.trip.repository;

import com.tranzo.tranzo_user_ms.trip.model.TripQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TripQueryRepository extends JpaRepository<TripQueryEntity, UUID> {
}
