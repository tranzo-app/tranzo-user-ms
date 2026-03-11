package com.tranzo.tranzo_user_ms.splitwise.repository;

import com.tranzo.tranzo_user_ms.splitwise.entity.Settlement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Settlement entity operations.
 */
@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    /**
     * Finds settlements for a specific group.
     */
    @Query("SELECT s FROM Settlement s WHERE s.group.id = :groupId")
    List<Settlement> findByGroupId(@Param("groupId") Long groupId);

    /**
     * Finds settlements for a specific group with pagination.
     */
    @Query("SELECT s FROM Settlement s WHERE s.group.id = :groupId")
    Page<Settlement> findByGroupId(@Param("groupId") Long groupId, Pageable pageable);

    /**
     * Finds settlements where a specific user paid.
     */
    List<Settlement> findByPaidBy(UUID paidBy);

    /**
     * Finds settlements where a specific user received payment.
     */
    List<Settlement> findByPaidTo(UUID paidTo);

    /**
     * Finds settlements involving a specific user (either as payer or receiver).
     */
    @Query("SELECT s FROM Settlement s WHERE s.paidBy = :userId OR s.paidTo = :userId")
    List<Settlement> findSettlementsInvolvingUser(@Param("userId") UUID userId);

    /**
     * Finds settlements involving a specific user with pagination.
     */
    @Query("SELECT s FROM Settlement s WHERE s.paidBy = :userId OR s.paidTo = :userId")
    Page<Settlement> findSettlementsInvolvingUser(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Finds settlements between two specific users in a group.
     */
    @Query("SELECT s FROM Settlement s WHERE s.group.id = :groupId AND " +
           "((s.paidBy = :userId1 AND s.paidTo = :userId2) OR " +
           "(s.paidBy = :userId2 AND s.paidTo = :userId1))")
    List<Settlement> findSettlementsBetweenUsers(@Param("groupId") Long groupId,
                                              @Param("userId1") UUID userId1,
                                              @Param("userId2") UUID userId2);

    /**
     * Finds settlements within a date range.
     */
    @Query("SELECT s FROM Settlement s WHERE s.settledAt BETWEEN :startDate AND :endDate")
    List<Settlement> findBySettledAtBetween(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    /**
     * Finds settlements for a group within a date range.
     */
    @Query("SELECT s FROM Settlement s WHERE s.group.id = :groupId AND s.settledAt BETWEEN :startDate AND :endDate")
    List<Settlement> findByGroupIdAndSettledAtBetween(@Param("groupId") Long groupId,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Finds settlements by payment method.
     */
    List<Settlement> findByPaymentMethod(String paymentMethod);

    /**
     * Finds settlements by transaction ID.
     */
    List<Settlement> findByTransactionId(String transactionId);

    /**
     * Gets total settlement amount for a group.
     */
    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM Settlement s WHERE s.group.id = :groupId")
    BigDecimal getTotalSettlementAmountForGroup(@Param("groupId") Long groupId);

    /**
     * Gets total amount paid by a user in a group.
     */
    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM Settlement s " +
           "WHERE s.group.id = :groupId AND s.paidBy = :userId")
    BigDecimal getTotalPaidByUserInGroup(@Param("groupId") Long groupId, @Param("userId") UUID userId);

    /**
     * Gets total amount received by a user in a group.
     */
    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM Settlement s " +
           "WHERE s.group.id = :groupId AND s.paidTo = :userId")
    BigDecimal getTotalReceivedByUserInGroup(@Param("groupId") Long groupId, @Param("userId") UUID userId);

    /**
     * Gets settlement statistics for a group.
     */
    @Query("SELECT " +
           "COUNT(s) as settlementCount, " +
           "COALESCE(SUM(s.amount), 0) as totalAmount, " +
           "COALESCE(AVG(s.amount), 0) as averageAmount, " +
           "COALESCE(MAX(s.amount), 0) as maxAmount, " +
           "COALESCE(MIN(s.amount), 0) as minAmount " +
           "FROM Settlement s WHERE s.group.id = :groupId")
    Object[] getSettlementStatisticsForGroup(@Param("groupId") Long groupId);

    /**
     * Gets monthly settlement totals for a group.
     */
    @Query("SELECT " +
           "YEAR(s.settledAt) as year, " +
           "MONTH(s.settledAt) as month, " +
           "COALESCE(SUM(s.amount), 0) as total " +
           "FROM Settlement s " +
           "WHERE s.group.id = :groupId " +
           "GROUP BY YEAR(s.settledAt), MONTH(s.settledAt) " +
           "ORDER BY year DESC, month DESC")
    List<Object[]> getMonthlySettlementTotalsForGroup(@Param("groupId") Long groupId);

    /**
     * Gets payment method-wise settlement totals for a group.
     */
    @Query("SELECT s.paymentMethod, COALESCE(SUM(s.amount), 0) as total " +
           "FROM Settlement s WHERE s.group.id = :groupId " +
           "GROUP BY s.paymentMethod " +
           "ORDER BY total DESC")
    List<Object[]> getPaymentMethodTotalsForGroup(@Param("groupId") Long groupId);

    /**
     * Finds settlements with amount greater than a threshold.
     */
    List<Settlement> findByAmountGreaterThan(BigDecimal amount);

    /**
     * Finds settlements with amount less than a threshold.
     */
    List<Settlement> findByAmountLessThan(BigDecimal amount);

    /**
     * Gets recent settlements for a group.
     */
    @Query("SELECT s FROM Settlement s WHERE s.group.id = :groupId ORDER BY s.settledAt DESC")
    Page<Settlement> findRecentSettlementsForGroup(@Param("groupId") Long groupId, Pageable pageable);

    /**
     * Gets top settlements by amount for a group.
     */
    @Query("SELECT s FROM Settlement s WHERE s.group.id = :groupId ORDER BY s.amount DESC")
    Page<Settlement> findTopSettlementsByAmountForGroup(@Param("groupId") Long groupId, Pageable pageable);

    /**
     * Searches settlements by multiple criteria.
     */
    @Query("SELECT s FROM Settlement s WHERE " +
           "(:groupId IS NULL OR s.group.id = :groupId) AND " +
           "(:paidById IS NULL OR s.paidBy = :paidById) AND " +
           "(:paidToId IS NULL OR s.paidTo = :paidToId) AND " +
           "(:paymentMethod IS NULL OR s.paymentMethod = :paymentMethod)")
    Page<Settlement> searchSettlements(@Param("groupId") Long groupId,
                                       @Param("paidById") UUID paidById,
                                       @Param("paidToId") UUID paidToId,
                                       @Param("paymentMethod") String paymentMethod,
                                       Pageable pageable);

    /**
     * Gets settlement trends for a user over time.
     */
    @Query("SELECT " +
           "DATE(s.settledAt) as date, " +
           "COUNT(s) as settlementCount, " +
           "SUM(s.amount) as totalAmount " +
           "FROM Settlement s " +
           "WHERE (s.paidBy = :userId OR s.paidTo = :userId) " +
           "GROUP BY DATE(s.settledAt) " +
           "ORDER BY date DESC")
    List<Object[]> getSettlementTrendsForUser(@Param("userId") UUID userId);

    /**
     * Gets net settlement amount for a user in a group (received - paid).
     */
    @Query("SELECT " +
           "COALESCE((SELECT SUM(s.amount) FROM Settlement s WHERE s.group.id = :groupId AND s.paidTo = :userId), 0) - " +
           "COALESCE((SELECT SUM(s.amount) FROM Settlement s WHERE s.group.id = :groupId AND s.paidBy = :userId), 0)")
    BigDecimal getNetSettlementForUserInGroup(@Param("groupId") Long groupId, @Param("userId") UUID userId);

    /**
     * Checks if there are any settlements for a group.
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Settlement s WHERE s.group.id = :groupId")
    boolean existsByGroupId(@Param("groupId") Long groupId);

    /**
     * Gets settlement count for a group.
     */
    @Query("SELECT COUNT(s) FROM Settlement s WHERE s.group.id = :groupId")
    long getSettlementCountForGroup(@Param("groupId") Long groupId);

    /**
     * Finds settlements that are not fully allocated to expenses.
     */
    @Query("SELECT s FROM Settlement s WHERE " +
           "s.amount > (SELECT COALESCE(SUM(se.amount), 0) FROM SettlementExpense se WHERE se.settlement.id = s.id)")
    List<Settlement> findUnallocatedSettlements();

    /**
     * Gets users with the most settlements in a group.
     */
    @Query("SELECT " +
           "CASE WHEN s.paidBy = :userId THEN s.paidTo ELSE s.paidBy END as otherUser, " +
           "COUNT(s) as settlementCount, " +
           "SUM(s.amount) as totalAmount " +
           "FROM Settlement s " +
           "WHERE s.group.id = :groupId AND (s.paidBy = :userId OR s.paidTo = :userId) " +
           "GROUP BY otherUser " +
           "ORDER BY settlementCount DESC")
    List<Object[]> getUserSettlementPartners(@Param("groupId") Long groupId, @Param("userId") UUID userId);
}
