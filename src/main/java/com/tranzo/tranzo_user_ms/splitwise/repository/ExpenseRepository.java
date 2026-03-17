package com.tranzo.tranzo_user_ms.splitwise.repository;

import com.tranzo.tranzo_user_ms.splitwise.entity.Expense;
import com.tranzo.tranzo_user_ms.splitwise.entity.Expense.SplitType;
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
 * Repository interface for Expense entity operations.
 */
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    /**
     * Finds expenses for a specific group.
     */
    List<Expense> findByGroupId(UUID groupId);

    /**
     * Finds expenses for a specific group with pagination.
     */
    Page<Expense> findByGroupId(UUID groupId, Pageable pageable);

    /**
     * Finds expenses by group ID and paid by user.
     */
    List<Expense> findByGroupIdAndPaidBy(UUID groupId, UUID paidBy);

    /**
     * Finds expenses paid by a specific user.
     */
    List<Expense> findByPaidBy(UUID paidById);

    /**
     * Finds expenses paid by a specific user with pagination.
     */
    Page<Expense> findByPaidBy(UUID paidById, Pageable pageable);

    /**
     * Finds expenses by split type.
     */
    List<Expense> findBySplitType(SplitType splitType);

    /**
     * Finds expenses by category.
     */
    List<Expense> findByCategory(String category);

    /**
     * Finds expenses by category with pagination.
     */
    Page<Expense> findByCategory(String category, Pageable pageable);

    /**
     * Finds expenses within a date range.
     */
    @Query("SELECT e FROM Expense e WHERE e.expenseDate BETWEEN :startDate AND :endDate")
    List<Expense> findByExpenseDateBetween(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    /**
     * Finds expenses for a group within a date range.
     */
    @Query("SELECT e FROM Expense e WHERE e.groupId = :groupId AND e.expenseDate BETWEEN :startDate AND :endDate")
    List<Expense> findByGroupIdAndExpenseDateBetween(@Param("groupId") UUID groupId,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    /**
     * Finds expenses involving a specific user (either as payer or in splits).
     */
    @Query("SELECT DISTINCT e FROM Expense e " +
           "LEFT JOIN e.splits s " +
           "WHERE e.paidBy = :userId OR s.userId = :userId")
    List<Expense> findExpensesInvolvingUser(@Param("userId") UUID userId);

    /**
     * Finds expenses involving a specific user with pagination.
     */
    @Query("SELECT DISTINCT e FROM Expense e " +
           "LEFT JOIN e.splits s " +
           "WHERE e.paidBy = :userId OR s.userId = :userId")
    Page<Expense> findExpensesInvolvingUser(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Finds expenses by name containing the given string (case-insensitive).
     */
    List<Expense> findByNameContainingIgnoreCase(String name);

    /**
     * Finds expenses by name with pagination.
     */
    Page<Expense> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Gets total expense amount for a group.
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.groupId = :groupId")
    BigDecimal getTotalExpenseAmountForGroup(@Param("groupId") UUID groupId);

    /**
     * Gets total expense amount for a user in a group.
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.groupId = :groupId AND e.paidBy = :userId")
    BigDecimal getTotalPaidByUserInGroup(@Param("groupId") UUID groupId, @Param("userId") UUID userId);

    /**
     * Gets total expense share for a user in a group.
     */
    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM ExpenseSplit s " +
           "WHERE s.expense.groupId = :groupId AND s.userId = :userId")
    BigDecimal getTotalShareForUserInGroup(@Param("groupId") UUID groupId, @Param("userId") UUID userId);

    /**
     * Finds expenses with amount greater than a threshold.
     */
    List<Expense> findByAmountGreaterThan(BigDecimal amount);

    /**
     * Finds expenses with amount less than a threshold.
     */
    List<Expense> findByAmountLessThan(BigDecimal amount);

    /**
     * Gets expense statistics for a group.
     */
    @Query("SELECT " +
           "COUNT(e) as expenseCount, " +
           "COALESCE(SUM(e.amount), 0) as totalAmount, " +
           "COALESCE(AVG(e.amount), 0) as averageAmount, " +
           "COALESCE(MAX(e.amount), 0) as maxAmount, " +
           "COALESCE(MIN(e.amount), 0) as minAmount " +
           "FROM Expense e WHERE e.groupId = :groupId")
    Object[] getExpenseStatisticsForGroup(@Param("groupId") UUID groupId);

    /**
     * Gets monthly expense totals for a group.
     */
    @Query("SELECT " +
           "YEAR(e.expenseDate) as year, " +
           "MONTH(e.expenseDate) as month, " +
           "COALESCE(SUM(e.amount), 0) as total " +
           "FROM Expense e " +
           "WHERE e.groupId = :groupId " +
           "GROUP BY YEAR(e.expenseDate), MONTH(e.expenseDate) " +
           "ORDER BY year DESC, month DESC")
    List<Object[]> getMonthlyExpenseTotalsForGroup(@Param("groupId") UUID groupId);

    /**
     * Gets category-wise expense totals for a group.
     */
    @Query("SELECT e.category, COALESCE(SUM(e.amount), 0) as total " +
           "FROM Expense e WHERE e.groupId = :groupId " +
           "GROUP BY e.category " +
           "ORDER BY total DESC")
    List<Object[]> getCategoryExpenseTotalsForGroup(@Param("groupId") UUID groupId);

    /**
     * Finds expenses that are not fully settled.
     */
    @Query("SELECT e FROM Expense e WHERE " +
           "e.id NOT IN (SELECT se.expense.id FROM SettlementExpense se " +
           "GROUP BY se.expense.id " +
           "HAVING SUM(se.amount) >= e.amount)")
    List<Expense> findUnsettledExpenses();

    /**
     * Finds unsettled expenses for a group.
     */
    @Query("SELECT e FROM Expense e WHERE e.groupId = :groupId AND " +
           "e.id NOT IN (SELECT se.expense.id FROM SettlementExpense se " +
           "WHERE se.expense.groupId = :groupId " +
           "GROUP BY se.expense.id " +
           "HAVING SUM(se.amount) >= e.amount)")
    List<Expense> findUnsettledExpensesForGroup(@Param("groupId") UUID groupId);

    /**
     * Searches expenses by multiple criteria.
     */
    @Query("SELECT e FROM Expense e WHERE " +
           "(:groupId IS NULL OR e.groupId = :groupId) AND " +
           "(:paidById IS NULL OR e.paidBy = :paidById) AND " +
           "(:category IS NULL OR e.category = :category) AND " +
           "(:splitType IS NULL OR e.splitType = :splitType) AND " +
           "(:name IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Expense> searchExpenses(@Param("groupId") UUID groupId,
                                 @Param("paidById") UUID paidById,
                                 @Param("category") String category,
                                 @Param("splitType") SplitType splitType,
                                 @Param("name") String name,
                                 Pageable pageable);

    /**
     * Gets top expenses by amount for a group.
     */
    @Query("SELECT e FROM Expense e WHERE e.groupId = :groupId ORDER BY e.amount DESC")
    Page<Expense> findTopExpensesByAmountForGroup(@Param("groupId") UUID groupId, Pageable pageable);

    /**
     * Gets recent expenses for a group.
     */
    @Query("SELECT e FROM Expense e WHERE e.groupId = :groupId ORDER BY e.createdAt DESC")
    Page<Expense> findRecentExpensesForGroup(@Param("groupId") UUID groupId, Pageable pageable);

    /**
     * Finds expenses where a specific user is involved in splits.
     */
    @Query("SELECT e FROM Expense e JOIN e.splits s WHERE s.userId = :userId")
    List<Expense> findBySplitUserId(@Param("userId") UUID userId);

    /**
     * Finds an expense by ID with its splits eagerly loaded.
     */
    @Query("SELECT e FROM Expense e LEFT JOIN FETCH e.splits WHERE e.id = :id")
    java.util.Optional<Expense> findByIdWithSplits(@Param("id") UUID id);
}
