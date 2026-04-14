package com.tranzo.tranzo_user_ms.user.repository;

import com.tranzo.tranzo_user_ms.user.model.UserProfileHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface UserProfileHistoryRepository extends JpaRepository<UserProfileHistoryEntity, UUID> {

    @Query("SELECT COALESCE(MAX(h.profileVersion), 0) FROM UserProfileHistoryEntity h WHERE h.userProfileUuid = :profileUuid")
    int findMaxVersionByUserProfileUuid(@Param("profileUuid") UUID profileUuid);
}
