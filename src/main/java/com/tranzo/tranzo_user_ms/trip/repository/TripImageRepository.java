package com.tranzo.tranzo_user_ms.trip.repository;

import com.tranzo.tranzo_user_ms.trip.enums.ImageSource;
import com.tranzo.tranzo_user_ms.trip.model.TripImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripImageRepository extends JpaRepository<TripImageEntity, UUID> {

    List<TripImageEntity> findByDestination(String destination);

    List<TripImageEntity> findByDestinationAndSource(String destination, ImageSource source);

    @Query("SELECT ti FROM TripImageEntity ti WHERE ti.destination = :destination ORDER BY ti.usageCount DESC")
    List<TripImageEntity> findByDestinationOrderByUsageCountDesc(@Param("destination") String destination);

    Optional<TripImageEntity> findByImageUrl(String imageUrl);

    boolean existsByImageUrl(String imageUrl);
}
