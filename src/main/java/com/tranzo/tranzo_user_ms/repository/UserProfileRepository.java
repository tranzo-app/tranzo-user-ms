package com.tranzo.tranzo_user_ms.repository;

import com.tranzo.tranzo_user_ms.model.UserProfileEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfileEntity, UUID> {

    @Query("""
    select up
    from UserProfileEntity up
    join fetch up.user u
    left join fetch u.socialHandelEntity
    where u.userUuid = :userUuid
      and u.accountStatus = com.tranzo.tranzo_user_ms.model.AccountStatus.ACTIVE
    """)
    Optional<UserProfileEntity> findWithSocialHandlesByUserUuid(@Param("userUuid") UUID userUuid);
}
