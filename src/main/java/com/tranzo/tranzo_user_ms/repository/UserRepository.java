package com.tranzo.tranzo_user_ms.repository;

import com.tranzo.tranzo_user_ms.dto.VerifyOtpDto;
import com.tranzo.tranzo_user_ms.model.UsersEntity;
import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UsersEntity, UUID> {
    Optional<UsersEntity> findUserByUserUuid(UUID userUuid);

    Optional<UsersEntity> findByEmail(String email);

    Optional<UsersEntity> findByMobileNumber(String mobileNumber);
}
