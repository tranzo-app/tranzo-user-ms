package com.tranzo.tranzo_user_ms.user.repository;

import com.tranzo.tranzo_user_ms.user.model.VerificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VerificationRepository extends JpaRepository<VerificationEntity, UUID> {
}
