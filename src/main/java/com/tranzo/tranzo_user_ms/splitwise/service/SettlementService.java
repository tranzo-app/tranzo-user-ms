package com.tranzo.tranzo_user_ms.splitwise.service;

import com.tranzo.tranzo_user_ms.splitwise.dto.SettlementProposal;
import com.tranzo.tranzo_user_ms.splitwise.dto.request.CreateSettlementRequest;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.GroupResponse;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.SettlementResponse;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.UserResponse;
import com.tranzo.tranzo_user_ms.splitwise.entity.*;
import com.tranzo.tranzo_user_ms.splitwise.exception.*;
import com.tranzo.tranzo_user_ms.splitwise.repository.BalanceRepository;
import com.tranzo.tranzo_user_ms.splitwise.repository.SettlementRepository;
import com.tranzo.tranzo_user_ms.splitwise.repository.SplitwiseGroupRepository;
import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing settlements between users.
 */
@Slf4j
@Service
@Transactional
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final BalanceService balanceService;
    private final ActivityService activityService;
    private final BalanceRepository balanceRepository;
    private final SplitwiseGroupRepository splitwiseGroupRepository;

    public SettlementService(SettlementRepository settlementRepository,
                         BalanceService balanceService,
                         ActivityService activityService,
                         BalanceRepository balanceRepository,
                         SplitwiseGroupRepository splitwiseGroupRepository) {
        this.settlementRepository = settlementRepository;
        this.balanceService = balanceService;
        this.activityService = activityService;
        this.balanceRepository = balanceRepository;
        this.splitwiseGroupRepository = splitwiseGroupRepository;
    }

    /**
     * Creates a new settlement with proper validation and balance updates.
     */
    public SettlementResponse createSettlement(CreateSettlementRequest request, UUID currentUserId) {
        log.info("Creating settlement: {} -> {} amount {} by user {}", 
                 request.getPaidById(), request.getPaidToId(), request.getAmount(), currentUserId);

        // Validate settlement amount
        balanceService.validateSettlementAmount(request.getGroupId(), 
                                          request.getPaidById(), 
                                          request.getPaidToId(), 
                                          request.getAmount());

        // Create settlement entity
        SplitwiseGroup group = splitwiseGroupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new GroupNotFoundException("Group not found: " + request.getGroupId()));
                
        Settlement settlement = Settlement.builder()
                .paidBy(request.getPaidById())
                .paidTo(request.getPaidToId())
                .group(group)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .transactionId(request.getTransactionId())
                .notes(request.getNotes())
                .build();

        settlement = settlementRepository.save(settlement);

        // Update balances
        balanceService.updateBalancesForSettlement(settlement);

        // Log activity
        activityService.logSettlementCreated(
                currentUserId,
                settlement.getGroup(),
                settlement.getId(),
                settlement.getAmount()
        );

        log.info("Successfully created settlement with ID: {}", settlement.getId());
        return convertToSettlementResponse(settlement);
    }

    /**
     * Gets a settlement by ID.
     */
    @Transactional(readOnly = true)
    public SettlementResponse getSettlement(Long settlementId) {
        log.debug("Fetching settlement: {}", settlementId);

        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new SettlementNotFoundException("Settlement not found: " + settlementId));

        log.debug("Successfully retrieved settlement: {}", settlement.getId());
        return convertToSettlementResponse(settlement);
    }

    /**
     * Gets all settlements for a group.
     */
    @Transactional(readOnly = true)
    public List<SettlementResponse> getGroupSettlements(Long groupId) {
        log.debug("Fetching settlements for group: {}", groupId);

        List<Settlement> settlements = settlementRepository.findByGroupId(groupId);

        log.debug("Found {} settlements for group {}", settlements.size(), groupId);
        return settlements.stream()
                .map(this::convertToSettlementResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets settlements involving a specific user.
     */
    @Transactional(readOnly = true)
    public List<SettlementResponse> getUserSettlements(UUID userId) {
        log.debug("Fetching settlements for user: {}", userId);

        List<Settlement> settlements = settlementRepository.findSettlementsInvolvingUser(userId);

        log.debug("Found {} settlements for user {}", settlements.size(), userId);
        return settlements.stream()
                .map(this::convertToSettlementResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets optimized settlement proposals for a group.
     */
    @Transactional(readOnly = true)
    public List<SettlementProposal> getOptimizedSettlements(Long groupId) {
        log.info("Calculating optimized settlements for group: {}", groupId);

        List<SettlementProposal> proposals = balanceService.getOptimizedSettlements(groupId);

        log.info("Generated {} optimized settlement proposals for group {}", proposals.size(), groupId);
        return proposals;
    }

    /**
     * Updates balances when a settlement is created.
     * This method is now handled by BalanceService.updateBalancesForSettlement()
     */
    private void updateBalancesForSettlement(Settlement settlement) {
        // Delegate to BalanceService which has the proper balance update logic
        balanceService.updateBalancesForSettlement(settlement);
    }

    /**
     * Updates the status of a settlement.
     */
    public SettlementResponse updateSettlementStatus(Long settlementId, String newStatus, UUID updatedBy) {
        log.info("Updating settlement status: {} -> {} by user {}", settlementId, newStatus, updatedBy);

        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new SettlementNotFoundException("Settlement not found: " + settlementId));

        settlement.setStatus(newStatus);
        settlement = settlementRepository.save(settlement);

        log.info("Successfully updated settlement status: {} -> {}", settlementId, newStatus);
        return convertToSettlementResponse(settlement);
    }

    /**
     * Deletes a settlement by ID and reverses balance updates.
     */
    public void deleteSettlement(Long settlementId, UUID deletedBy) {
        log.info("Deleting settlement: {} by user {}", settlementId, deletedBy);

        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new SettlementNotFoundException("Settlement not found: " + settlementId));

        // Reverse the balance updates before deleting
        reverseBalanceUpdatesForSettlement(settlement);

        settlementRepository.delete(settlement);

        // Log activity
        activityService.logSettlementDeleted(deletedBy, settlement.getGroup(), settlementId, settlement.getAmount());

        log.info("Successfully deleted settlement: {}", settlementId);
    }

    /**
     * Reverses balance updates when a settlement is deleted.
     */
    private void reverseBalanceUpdatesForSettlement(Settlement settlement) {
        log.debug("Reversing balance updates for settlement: {}", settlement.getId());
        
        // Add back the balance that was settled
        balanceService.reverseBalancesForSettlement(settlement);
        
        log.debug("Successfully reversed balance updates for settlement: {}", settlement.getId());
    }

    /**
     * Converts Settlement entity to SettlementResponse DTO.
     */
    private SettlementResponse convertToSettlementResponse(Settlement settlement) {
        return SettlementResponse.builder()
                .id(settlement.getId())
                .group(GroupResponse.builder()
                        .id(settlement.getGroup().getId())
                        .name(settlement.getGroup().getName())
                        .build())
                .paidBy(UserResponse.builder()
                        .userUuid(settlement.getPaidBy())
                        .build())
                .paidTo(UserResponse.builder()
                        .userUuid(settlement.getPaidTo())
                        .build())
                .amount(settlement.getAmount())
                .paymentMethod(settlement.getPaymentMethod())
                .transactionId(settlement.getTransactionId())
                .notes(settlement.getNotes())
                .settledAt(settlement.getSettledAt())
                .isFullyAllocated(settlement.isFullyAllocated())
                .remainingAmount(settlement.getRemainingAmount())
                .status(settlement.getStatus())
                .build();
    }
}
