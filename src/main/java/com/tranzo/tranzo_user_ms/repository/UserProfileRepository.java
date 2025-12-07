package com.tranzo.tranzo_user_ms.repository;

import com.tranzo.tranzo_user_ms.model.UserProfileEntity;
import com.tranzo.tranzo_user_ms.model.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository  extends JpaRepository<UserProfileEntity, Long> {
    Optional<UserProfileEntity> findByUser(UsersEntity user);
    boolean existsByUser(UsersEntity user);
}
