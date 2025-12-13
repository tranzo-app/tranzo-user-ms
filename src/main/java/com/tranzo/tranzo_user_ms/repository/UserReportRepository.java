package com.tranzo.tranzo_user_ms.repository;

import com.tranzo.tranzo_user_ms.model.UserReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserReportRepository extends JpaRepository<UserReportEntity, UUID> {
  boolean existsByReportedUserIdAndReporterUserId(UUID reportedUserId, UUID reporterUserId);
}
