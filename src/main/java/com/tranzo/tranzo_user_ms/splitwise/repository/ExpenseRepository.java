package com.tranzo.tranzo_user_ms.splitwise.repository;

import com.tranzo.tranzo_user_ms.splitwise.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
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
     * Finds expenses involving a specific user (either as payer or in splits).
     */
    @Query("SELECT DISTINCT e FROM Expense e " +
           "LEFT JOIN e.splits s " +
           "WHERE e.paidBy = :userId OR s.userId = :userId")
    List<Expense> findExpensesInvolvingUser(@Param("userId") UUID userId);

    /**
     * Finds an expense by ID with its splits eagerly loaded.
     */
    @Query("SELECT e FROM Expense e LEFT JOIN FETCH e.splits WHERE e.id = :id")
    Optional<Expense> findByIdWithSplits(@Param("id") UUID id);
}
