package com.tranzo.tranzo_user_ms.notification.repository;

import com.tranzo.tranzo_user_ms.notification.model.UserNotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserNotificationRepository extends JpaRepository<UserNotificationEntity, UUID> {

    Page<UserNotificationEntity> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    long countByUserIdAndReadAtIsNull(UUID userId);
}
