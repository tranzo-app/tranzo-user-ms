package com.tranzo.tranzo_user_ms.user.repository;

import com.tranzo.tranzo_user_ms.user.model.UserReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserReportRepository extends JpaRepository<UserReportEntity, UUID> {
  boolean existsByReportedUserIdAndReportingUserId(UUID reportedUserId, UUID reportingUserId);
}
