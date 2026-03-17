package com.tranzo.tranzo_user_ms.splitwise.repository;

import com.tranzo.tranzo_user_ms.splitwise.entity.SplitwiseGroup;
import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for SplitwiseGroup entity operations.
 */
@Repository
public interface SplitwiseGroupRepository extends JpaRepository<SplitwiseGroup, UUID> {

    /**
     * Finds the group associated with a trip (at most one per trip).
     */
    Optional<SplitwiseGroup> findByTripId(UUID tripId);

    /**
     * Finds groups where a user is a member.
     */
    @Query("SELECT g FROM SplitwiseGroup g JOIN g.members m WHERE m.userId = :userId")
    List<SplitwiseGroup> findByUserId(@Param("userId") UUID userId);


    /**
     * Checks if a user is a member of a specific group.
     */
    @Query("SELECT COUNT(g) > 0 FROM SplitwiseGroup g JOIN g.members m WHERE g.id = :groupId AND m.userId = :userId")
    boolean isUserMemberOfGroup(@Param("groupId") UUID groupId, @Param("userId") UUID userId);

    /**
     * Checks if a user is an admin of a specific group.
     */
    @Query("SELECT COUNT(g) > 0 FROM SplitwiseGroup g JOIN g.members m WHERE g.id = :groupId AND m.userId = :userId AND m.role = 'ADMIN'")
    boolean isUserAdminOfGroup(@Param("groupId") UUID groupId, @Param("userId") UUID userId);



}
