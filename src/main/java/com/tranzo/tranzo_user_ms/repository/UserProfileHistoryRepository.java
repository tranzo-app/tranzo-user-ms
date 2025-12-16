package com.tranzo.tranzo_user_ms.repository;

import com.tranzo.tranzo_user_ms.model.UserProfileHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserProfileHistoryRepository extends JpaRepository<UserProfileHistoryEntity, UUID> {
}
