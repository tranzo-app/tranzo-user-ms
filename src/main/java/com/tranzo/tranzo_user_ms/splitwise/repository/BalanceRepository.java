package com.tranzo.tranzo_user_ms.splitwise.repository;

import com.tranzo.tranzo_user_ms.splitwise.entity.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Balance entity operations.
 */
@Repository
public interface BalanceRepository extends JpaRepository<Balance, UUID> {

    /**
     * Finds balances for a specific group.
     */
    @Query("SELECT b FROM Balance b WHERE b.group.id = :groupId")
    List<Balance> findByGroupId(@Param("groupId") UUID groupId);

    /**
     * Finds balances between two specific users in a group.
     */
    @Query("SELECT b FROM Balance b WHERE b.group.id = :groupId AND b.owedBy = :owedBy AND b.owedTo = :owedTo")
    Optional<Balance> findByGroupIdAndOwedByAndOwedTo(@Param("groupId") UUID groupId, @Param("owedBy") UUID owedBy, @Param("owedTo") UUID owedTo);

    /**
     * Finds all balances involving a specific user in a group.
     */
    @Query("SELECT b FROM Balance b WHERE b.group.id = :groupId AND " +
           "(b.owedBy = :userId OR b.owedTo = :userId)")
    List<Balance> findBalancesForUserInGroup(@Param("groupId") UUID groupId, @Param("userId") UUID userId);

    /**
     * Gets total amount owed by a user in a group.
     */
    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Balance b " +
           "WHERE b.group.id = :groupId AND b.owedBy = :userId")
    BigDecimal getTotalOwedByUserInGroup(@Param("groupId") UUID groupId, @Param("userId") UUID userId);

    /**
     * Gets total amount owed to a user in a group.
     */
    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Balance b " +
           "WHERE b.group.id = :groupId AND b.owedTo = :userId")
    BigDecimal getTotalOwedToUserInGroup(@Param("groupId") UUID groupId, @Param("userId") UUID userId);

    /**
     * Gets balance summary for all users in a group (for getGroupBalances).
     * Returns: userId, totalOwedTo, totalOwedBy, netBalance per group member.
     * Service layer resolves user details (name, email) via UserRepository.
     */
    @Query("SELECT gm.userId, " +
           "COALESCE(SUM(CASE WHEN b.owedTo = gm.userId THEN b.amount ELSE 0 END), 0), " +
           "COALESCE(SUM(CASE WHEN b.owedBy = gm.userId THEN b.amount ELSE 0 END), 0), " +
           "COALESCE(SUM(CASE WHEN b.owedTo = gm.userId THEN b.amount ELSE 0 END), 0) - " +
           "COALESCE(SUM(CASE WHEN b.owedBy = gm.userId THEN b.amount ELSE 0 END), 0) " +
           "FROM GroupMember gm " +
           "LEFT JOIN Balance b ON b.group = gm.group AND (b.owedBy = gm.userId OR b.owedTo = gm.userId) " +
           "WHERE gm.group.id = :groupId " +
           "GROUP BY gm.userId")
    List<Object[]> getBalanceSummaryForGroup(@Param("groupId") UUID groupId);

    /**
     * Deletes all balances for a group.
     */
    @Modifying
    @Query("DELETE FROM Balance b WHERE b.group.id = :groupId")
    void deleteByGroupId(@Param("groupId") UUID groupId);
}
