package com.tranzo.tranzo_user_ms.repository;

import com.tranzo.tranzo_user_ms.model.UserProfile;
import com.tranzo.tranzo_user_ms.model.UsersEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository  extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUser(UsersEntity user);
    boolean existsByUser(UsersEntity user);
}
