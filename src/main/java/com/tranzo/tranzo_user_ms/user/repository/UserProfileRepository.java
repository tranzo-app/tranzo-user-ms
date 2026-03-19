package com.tranzo.tranzo_user_ms.user.repository;

import com.tranzo.tranzo_user_ms.user.model.UserProfileEntity;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfileEntity, UUID> {

    /**
     * Batch fetch profiles by user UUIDs (single query). Used by UserProfileClientLocalImpl.
     */
    List<UserProfileEntity> findByUser_UserUuidIn(Collection<UUID> userUuids);

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
