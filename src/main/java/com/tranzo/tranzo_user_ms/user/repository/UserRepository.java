package com.tranzo.tranzo_user_ms.user.repository;

import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UsersEntity, UUID> {
    Optional<UsersEntity> findUserByUserUuid(UUID userUuid);

    Optional<UsersEntity> findByEmail(String email);

    Optional<UsersEntity> findByMobileNumber(String mobileNumber);
}
