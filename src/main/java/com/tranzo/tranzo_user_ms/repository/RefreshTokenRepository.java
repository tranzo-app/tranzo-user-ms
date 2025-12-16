package com.tranzo.tranzo_user_ms.repository;

import com.tranzo.tranzo_user_ms.model.RefreshTokenEntity;
import com.tranzo.tranzo_user_ms.model.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByTokenHashAndRevokedFalse(String token);

    Optional<RefreshTokenEntity> findByUser_UserUuidAndRevokedFalse(UUID uuid);

    void deleteAllByUser(UsersEntity user);
}

