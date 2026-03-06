package com.tranzo.tranzo_user_ms.splitwise.repository;

import com.tranzo.tranzo_user_ms.splitwise.entity.Activity;
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
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    /**
     * Finds activities for a specific group ordered by creation date (newest first).
     */
    List<Activity> findByGroupIdOrderByCreatedAtDesc(Long groupId);

    /**
     * Finds activities for a specific user ordered by creation date (newest first).
     */
    List<Activity> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Finds activities for a specific group with pagination.
     */
    @Query("SELECT a FROM Activity a WHERE a.group.id = :groupId ORDER BY a.createdAt DESC")
    List<Activity> findByGroupIdOrderByCreatedAtDesc(@Param("groupId") Long groupId, 
                                                org.springframework.data.domain.Pageable pageable);

    /**
     * Finds activities for a specific group with limit and offset.
     */
    @Query("SELECT a FROM Activity a WHERE a.group.id = :groupId ORDER BY a.createdAt DESC " +
           "LIMIT :limit OFFSET :offset")
    List<Activity> findByGroupIdOrderByCreatedAtDesc(@Param("groupId") Long groupId, 
                                                @Param("limit") int limit, 
                                                @Param("offset") int offset);

    /**
     * Finds activities for a specific user with pagination.
     */
    @Query("SELECT a FROM Activity a WHERE a.userId = :userId ORDER BY a.createdAt DESC")
    List<Activity> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId, 
                                                org.springframework.data.domain.Pageable pageable);

    /**
     * Finds activities for a specific user with limit and offset.
     */
    @Query("SELECT a FROM Activity a WHERE a.userId = :userId ORDER BY a.createdAt DESC " +
           "LIMIT :limit OFFSET :offset")
    List<Activity> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId,
                                                @Param("limit") int limit, 
                                                @Param("offset") int offset);

    /**
     * Finds activities by activity type.
     */
    List<Activity> findByActivityType(Activity.ActivityType activityType);

    /**
     * Finds activities by activity type for a specific group.
     */
    List<Activity> findByGroupIdAndActivityType(Long groupId, Activity.ActivityType activityType);

    /**
     * Finds activities by activity type for a specific user.
     */
    List<Activity> findByUserIdAndActivityType(UUID userId, Activity.ActivityType activityType);

    /**
     * Finds activities related to a specific entity.
     */
    List<Activity> findByRelatedIdAndRelatedType(String relatedId, String relatedType);

    /**
     * Gets activity count for a group.
     */
    @Query("SELECT COUNT(a) FROM Activity a WHERE a.group.id = :groupId")
    long countByGroupId(@Param("groupId") Long groupId);

    /**
     * Gets activity count for a user.
     */
    @Query("SELECT COUNT(a) FROM Activity a WHERE a.userId = :userId")
    long countByUserId(@Param("userId") UUID userId);

    /**
     * Finds recent activities for a group.
     */
    @Query("SELECT a FROM Activity a WHERE a.group.id = :groupId ORDER BY a.createdAt DESC " +
           "LIMIT 10")
    List<Activity> findRecentActivitiesByGroupId(@Param("groupId") Long groupId);

    /**
     * Finds recent activities for a user.
     */
    @Query("SELECT a FROM Activity a WHERE a.userId = :userId ORDER BY a.createdAt DESC " +
           "LIMIT 10")
    List<Activity> findRecentActivitiesByUserId(@Param("userId") UUID userId);

    /**
     * Deletes activities older than specified date.
     */
    @Query("DELETE FROM Activity a WHERE a.createdAt < :cutoffDate")
    void deleteActivitiesOlderThan(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}
