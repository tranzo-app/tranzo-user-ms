package com.tranzo.tranzo_user_ms.repository;

import com.tranzo.tranzo_user_ms.model.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository {
    Optional<UsersEntity> findUserByUserUuid(UUID userUuid);
}
