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
public interface SplitwiseGroupRepository extends JpaRepository<SplitwiseGroup, Long> {

    /**
     * Finds the group associated with a trip (at most one per trip).
     */
    Optional<SplitwiseGroup> findByTripId(UUID tripId);

    /**
     * Finds groups created by a specific user.
     */
    List<SplitwiseGroup> findByCreatedBy(UsersEntity createdBy);

    /**
     * Finds groups where a user is a member.
     */
    @Query("SELECT g FROM SplitwiseGroup g JOIN g.members m WHERE m.userId = :userId")
    List<SplitwiseGroup> findByUserId(@Param("userId") UUID userId);

    /**
     * Finds groups where a user is a member with pagination.
     */
    @Query("SELECT g FROM SplitwiseGroup g JOIN g.members m WHERE m.userId = :userId")
    Page<SplitwiseGroup> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Finds groups where a user is an admin.
     */
    @Query("SELECT g FROM SplitwiseGroup g JOIN g.members m WHERE m.userId = :userId AND m.role = 'ADMIN'")
    List<SplitwiseGroup> findAdminGroupsByUserId(@Param("userId") UUID userId);

    /**
     * Finds groups by description (case-insensitive search).
     */
    List<SplitwiseGroup> findByDescriptionContainingIgnoreCase(String description);

    /**
     * Finds groups by description with pagination.
     */
    Page<SplitwiseGroup> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);

    /**
     * Checks if a user is a member of a specific group.
     */
    @Query("SELECT COUNT(g) > 0 FROM SplitwiseGroup g JOIN g.members m WHERE g.id = :groupId AND m.userId = :userId")
    boolean isUserMemberOfGroup(@Param("groupId") Long groupId, @Param("userId") UUID userId);

    /**
     * Checks if a user is an admin of a specific group.
     */
    @Query("SELECT COUNT(g) > 0 FROM SplitwiseGroup g JOIN g.members m WHERE g.id = :groupId AND m.userId = :userId AND m.role = 'ADMIN'")
    boolean isUserAdminOfGroup(@Param("groupId") Long groupId, @Param("userId") UUID userId);

    /**
     * Gets the member count for a specific group.
     */
    @Query("SELECT COUNT(m) FROM SplitwiseGroup g JOIN g.members m WHERE g.id = :groupId")
    long getMemberCount(@Param("groupId") Long groupId);

    /**
     * Finds groups with member count greater than a threshold.
     */
    @Query("SELECT g FROM SplitwiseGroup g WHERE (SELECT COUNT(m) FROM g.members m) > :memberCount")
    List<SplitwiseGroup> findByMemberCountGreaterThan(@Param("memberCount") int memberCount);

    /**
     * Finds groups created within a date range.
     */
    @Query("SELECT g FROM SplitwiseGroup g WHERE g.createdAt BETWEEN :startDate AND :endDate")
    List<SplitwiseGroup> findByCreatedAtBetween(@Param("startDate") java.time.LocalDateTime startDate,
                                               @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * Finds groups with their member count.
     */
    @Query("SELECT g, COUNT(m) as memberCount FROM SplitwiseGroup g LEFT JOIN g.members m GROUP BY g")
    List<Object[]> findGroupsWithMemberCount();

    /**
     * Finds groups by multiple member IDs (groups where all specified users are members).
     */
    @Query("SELECT g FROM SplitwiseGroup g WHERE " +
           "(SELECT COUNT(m) FROM g.members m WHERE m.userId IN :userIds) = :userCount")
    List<SplitwiseGroup> findByAllUserIds(@Param("userIds") List<UUID> userIds, @Param("userCount") long userCount);

    /**
     * Finds groups that have any of the specified users as members.
     */
    @Query("SELECT DISTINCT g FROM SplitwiseGroup g JOIN g.members m WHERE m.userId IN :userIds")
    List<SplitwiseGroup> findByAnyUserIds(@Param("userIds") List<UUID> userIds);

    /**
     * Gets group statistics for a user.
     */
    @Query("SELECT " +
           "COUNT(g) as totalGroups, " +
           "SUM(CASE WHEN m.role = 'ADMIN' THEN 1 ELSE 0 END) as adminGroups, " +
           "AVG(CASE WHEN (SELECT COUNT(m2) FROM g.members m2) > 0 THEN (SELECT COUNT(m2) FROM g.members m2) ELSE 0 END) as avgMemberCount " +
           "FROM SplitwiseGroup g JOIN g.members m WHERE m.userId = :userId")
    Object[] getUserGroupStatistics(@Param("userId") UUID userId);
}
