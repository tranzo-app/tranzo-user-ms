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
     * Finds balances where a specific user owes money.
     */
    List<Balance> findByOwedBy(UUID owedBy);

    /**
     * Finds balances where a specific user is owed money.
     */
    List<Balance> findByOwedTo(UUID owedTo);

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
     * Gets net balance for a user in a group (owed to - owed by).
     */
    @Query("SELECT " +
           "COALESCE((SELECT SUM(b.amount) FROM Balance b WHERE b.group.id = :groupId AND b.owedTo = :userId), 0) - " +
           "COALESCE((SELECT SUM(b.amount) FROM Balance b WHERE b.group.id = :groupId AND b.owedBy = :userId), 0)")
    BigDecimal getNetBalanceForUserInGroup(@Param("groupId") UUID groupId, @Param("userId") UUID userId);

    /**
     * Finds balances with amount greater than a threshold.
     */
    List<Balance> findByAmountGreaterThan(BigDecimal amount);

    /**
     * Finds balances with amount less than a threshold.
     */
    List<Balance> findByAmountLessThan(BigDecimal amount);

    /**
     * Gets all user pairs with balances in a group.
     */
    @Query("SELECT DISTINCT b.owedBy, b.owedTo FROM Balance b WHERE b.group.id = :groupId")
    List<Object[]> findUserPairsWithBalancesInGroup(@Param("groupId") UUID groupId);

    /**
     * Gets balance summary for all users in a group.
     * Simplified query to avoid UserProfileEntity path resolution issues.
     */
    @Query("SELECT " +
           "gm.userId as userId, " +
           "'User' as userName, " +
           "u.email as userEmail, " +
           "COALESCE(SUM(CASE WHEN b.owedTo = gm.userId THEN b.amount ELSE 0 END), 0) as totalOwedTo, " +
           "COALESCE(SUM(CASE WHEN b.owedBy = gm.userId THEN b.amount ELSE 0 END), 0) as totalOwedBy, " +
           "COALESCE(SUM(CASE WHEN b.owedTo = gm.userId THEN b.amount ELSE 0 END), 0) - " +
           "COALESCE(SUM(CASE WHEN b.owedBy = gm.userId THEN b.amount ELSE 0 END), 0) as netBalance " +
           "FROM GroupMember gm " +
           "LEFT JOIN UsersEntity u ON u.userUuid = gm.userId " +
           "LEFT JOIN Balance b ON (b.owedBy = gm.userId OR b.owedTo = gm.userId) AND b.group.id = :groupId " +
           "WHERE gm.group.id = :groupId " +
           "GROUP BY gm.userId " +
           "ORDER BY netBalance DESC")
    List<Object[]> getBalanceSummaryForGroup(@Param("groupId") UUID groupId);

    /**
     * Finds balances that need settlement (amount > 0).
     */
    @Query("SELECT b FROM Balance b WHERE b.amount > 0 ORDER BY b.amount DESC")
    List<Balance> findBalancesNeedingSettlement();

    /**
     * Gets top debtors in a group (users who owe the most).
     */
    @Query("SELECT b.owedBy, SUM(b.amount) as totalOwed " +
           "FROM Balance b WHERE b.group.id = :groupId " +
           "GROUP BY b.owedBy " +
           "ORDER BY totalOwed DESC")
    List<Object[]> getTopDebtorsInGroup(@Param("groupId") UUID groupId);

    /**
     * Gets top creditors in a group (users who are owed the most).
     */
    @Query("SELECT b.owedTo, SUM(b.amount) as totalOwed " +
           "FROM Balance b WHERE b.group.id = :groupId " +
           "GROUP BY b.owedTo " +
           "ORDER BY totalOwed DESC")
    List<Object[]> getTopCreditorsInGroup(@Param("groupId") UUID groupId);

    /**
     * Checks if there are any balances for a group.
     */
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Balance b WHERE b.group.id = :groupId")
    boolean existsByGroupId(@Param("groupId") UUID groupId);

    /**
     * Checks if there are any balances between two users in a group.
     */
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Balance b WHERE b.group.id = :groupId AND b.owedBy = :owedBy AND b.owedTo = :owedTo")
    boolean existsByGroupIdAndOwedByAndOwedTo(@Param("groupId") UUID groupId, @Param("owedBy") UUID owedBy, @Param("owedTo") UUID owedTo);

    /**
     * Deletes all balances for a group.
     */
    @Modifying
    @Query("DELETE FROM Balance b WHERE b.group.id = :groupId")
    void deleteByGroupId(@Param("groupId") UUID groupId);

    /**
     * Deletes balances between two users in a group.
     */
    @Modifying
    @Query("DELETE FROM Balance b WHERE b.group.id = :groupId AND b.owedBy = :owedBy AND b.owedTo = :owedTo")
    void deleteByGroupIdAndOwedByAndOwedTo(@Param("groupId") UUID groupId, @Param("owedBy") UUID owedBy, @Param("owedTo") UUID owedTo);

    /**
     * Gets balance count for a group.
     */
    @Query("SELECT COUNT(b) FROM Balance b WHERE b.group.id = :groupId")
    long getBalanceCountForGroup(@Param("groupId") UUID groupId);

    /**
     * Gets total balance amount for a group.
     */
    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Balance b WHERE b.group.id = :groupId")
    BigDecimal getTotalBalanceAmountForGroup(@Param("groupId") UUID groupId);

    /**
     * Finds stale balances (balances that haven't been updated recently).
     */
    @Query("SELECT b FROM Balance b WHERE b.lastUpdated < :cutoffDate ORDER BY b.lastUpdated ASC")
    List<Balance> findStaleBalances(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);

    /**
     * Gets balance trends for a user over time.
     */
    @Query("SELECT " +
           "DATE(b.lastUpdated) as date, " +
           "COUNT(b) as transactionCount, " +
           "SUM(b.amount) as totalAmount " +
           "FROM Balance b " +
           "WHERE (b.owedBy = :userId OR b.owedTo = :userId) " +
           "GROUP BY DATE(b.lastUpdated) " +
           "ORDER BY date DESC")
    List<Object[]> getBalanceTrendsForUser(@Param("userId") UUID userId);
}
