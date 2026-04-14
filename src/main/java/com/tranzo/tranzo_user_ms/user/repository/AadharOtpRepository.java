package com.tranzo.tranzo_user_ms.user.repository;

import com.tranzo.tranzo_user_ms.user.model.AadharOtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@ConditionalOnProperty(name = "aadhar.enabled", havingValue = "true")
public interface AadharOtpRepository extends JpaRepository<AadharOtpEntity, UUID> {
    @Query("""
    SELECT o 
    FROM AadharOtpEntity o
    WHERE o.userId = :userId
    AND o.used = false
    AND o.expiresAt > CURRENT_TIMESTAMP
    """)
    Optional<AadharOtpEntity> findValidByUser(UUID userId);
}
