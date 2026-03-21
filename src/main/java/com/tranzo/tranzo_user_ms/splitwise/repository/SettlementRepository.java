package com.tranzo.tranzo_user_ms.splitwise.repository;

import com.tranzo.tranzo_user_ms.splitwise.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Settlement entity operations.
 */
@Repository
public interface SettlementRepository extends JpaRepository<Settlement, UUID> {

    /**
     * Finds settlements for a specific group.
     */
    @Query("SELECT s FROM Settlement s WHERE s.group.id = :groupId")
    List<Settlement> findByGroupId(@Param("groupId") UUID groupId);

    /**
     * Finds settlements involving a specific user (either as payer or receiver).
     */
    @Query("SELECT s FROM Settlement s WHERE s.paidBy = :userId OR s.paidTo = :userId")
    List<Settlement> findSettlementsInvolvingUser(@Param("userId") UUID userId);
}
