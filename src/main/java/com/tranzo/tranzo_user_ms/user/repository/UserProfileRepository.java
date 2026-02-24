package com.tranzo.tranzo_user_ms.user.repository;

import com.tranzo.tranzo_user_ms.user.model.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfileEntity, UUID> {

    @Query("""
        select up
        from UserProfileEntity up
        join fetch up.user u
        left join fetch u.socialHandleEntity
        where u.userUuid = :userUuid
        and u.accountStatus = com.tranzo.tranzo_user_ms.user.enums.AccountStatus.ACTIVE
    """)
    Optional<UserProfileEntity> findAllUserProfileDetailByUserId(@Param("userUuid") UUID userUuid);
}
