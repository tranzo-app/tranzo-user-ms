package com.tranzo.tranzo_user_ms.repository;

import com.tranzo.tranzo_user_ms.model.UsersEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UsersEntity , UUID>{
     Optional<UsersEntity> findByMobileNumber(String mobileNumber);
     boolean existsByMobileNumber(String mobileNumber);

     @Query("""
    select u from UsersEntity  u left join fetch u.userProfile where u.id = :id
    """)
    Optional<UsersEntity> findWithProfile(@Param("id") UUID id);

}
