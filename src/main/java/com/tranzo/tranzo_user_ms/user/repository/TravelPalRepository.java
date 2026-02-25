package com.tranzo.tranzo_user_ms.user.repository;

import com.tranzo.tranzo_user_ms.user.model.TravelPalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TravelPalRepository extends JpaRepository<TravelPalEntity, UUID> {
    Optional<TravelPalEntity> findByUserLowIdAndUserHighId(UUID low, UUID high);

    @Query("""
        SELECT t FROM TravelPalEntity t
        WHERE (t.userLowId = :userId OR t.userHighId = :userId)
        AND t.status = 'ACCEPTED'
    """)
    List<TravelPalEntity> findAcceptedByUser(UUID userId);

    @Query("""
        SELECT t FROM TravelPalEntity t
        WHERE (t.userLowId = :userId OR t.userHighId = :userId)
        AND t.status = 'PENDING'
        AND t.requestedBy <> :userId
    """)
    List<TravelPalEntity> findIncomingPending(UUID userId);
}