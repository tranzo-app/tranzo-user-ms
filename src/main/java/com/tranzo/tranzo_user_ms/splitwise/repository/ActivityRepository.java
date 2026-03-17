package com.tranzo.tranzo_user_ms.splitwise.repository;

import com.tranzo.tranzo_user_ms.splitwise.entity.Activity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Activity entity operations.
 */
@Repository
public interface ActivityRepository extends JpaRepository<Activity, UUID> {

    /**
     * Finds activities for a specific group ordered by creation date (newest first).
     */
    @Query("SELECT a FROM Activity a WHERE a.group.id = :groupId ORDER BY a.createdAt DESC")
    List<Activity> findByGroupIdOrderByCreatedAtDesc(@Param("groupId") UUID groupId);

    /**
     * Finds activities for a specific group with pagination.
     */
    @Query("SELECT a FROM Activity a WHERE a.group.id = :groupId ORDER BY a.createdAt DESC")
    List<Activity> findByGroupIdOrderByCreatedAtDesc(@Param("groupId") UUID groupId, Pageable pageable);

    /**
     * Finds activities for a specific user with pagination.
     */
    @Query("SELECT a FROM Activity a WHERE a.userId = :userId ORDER BY a.createdAt DESC")
    List<Activity> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId, Pageable pageable);
}
