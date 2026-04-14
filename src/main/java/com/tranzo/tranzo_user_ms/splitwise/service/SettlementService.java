package com.tranzo.tranzo_user_ms.splitwise.service;

import com.tranzo.tranzo_user_ms.splitwise.dto.SettlementProposal;
import com.tranzo.tranzo_user_ms.splitwise.dto.request.CreateSettlementRequest;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.ExpenseResponse;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.GroupResponse;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.SettlementResponse;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.UserResponse;
import com.tranzo.tranzo_user_ms.splitwise.entity.Expense;
import com.tranzo.tranzo_user_ms.splitwise.entity.Settlement;
import com.tranzo.tranzo_user_ms.splitwise.entity.SettlementExpense;
import com.tranzo.tranzo_user_ms.splitwise.entity.SplitwiseGroup;
import com.tranzo.tranzo_user_ms.splitwise.exception.GroupNotFoundException;
import com.tranzo.tranzo_user_ms.splitwise.exception.SettlementNotFoundException;
import com.tranzo.tranzo_user_ms.splitwise.exception.UserNotMemberException;
import com.tranzo.tranzo_user_ms.splitwise.repository.SettlementRepository;
import com.tranzo.tranzo_user_ms.splitwise.repository.SplitwiseGroupRepository;
import com.tranzo.tranzo_user_ms.trip.model.TripEntity;
import com.tranzo.tranzo_user_ms.trip.repository.TripRepository;
import com.tranzo.tranzo_user_ms.user.model.UserProfileEntity;
import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import com.tranzo.tranzo_user_ms.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for creating, reading, listing, optimizing, and deleting settlements; enforces group membership.
 */
@Slf4j
@Service
@Transactional
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final SplitwiseGroupRepository splitwiseGroupRepository;
    private final BalanceService balanceService;
    private final ActivityService activityService;
    private final UserRepository userRepository;
    private final TripRepository tripRepository;

    public SettlementService(SettlementRepository settlementRepository,
                            SplitwiseGroupRepository splitwiseGroupRepository,
                            BalanceService balanceService,
                            ActivityService activityService,
                            UserRepository userRepository,
                            TripRepository tripRepository) {
        this.settlementRepository = settlementRepository;
        this.splitwiseGroupRepository = splitwiseGroupRepository;
        this.balanceService = balanceService;
        this.activityService = activityService;
        this.userRepository = userRepository;
        this.tripRepository = tripRepository;
    }

    /**
     * Creates a settlement. Current user must be group member and typically the payer (paidById = currentUserId).
     */
    public SettlementResponse createSettlement(CreateSettlementRequest request, UUID currentUserId) {
        log.info("Processing started | operation=createSettlement | userId={} | groupId={} | paidById={} | paidToId={} | amount={}", 
                 currentUserId, request.getGroupId(), request.getPaidById(), request.getPaidToId(), request.getAmount());

        try {
            if (!splitwiseGroupRepository.isUserMemberOfGroup(request.getGroupId(), currentUserId)) {
                log.warn("Access denied | operation=createSettlement | userId={} | groupId={} | reason=NOT_MEMBER", currentUserId, request.getGroupId());
                throw new UserNotMemberException(currentUserId, request.getGroupId());
            }
            if (!request.getPaidById().equals(currentUserId)) {
                log.warn("Access denied | operation=createSettlement | userId={} | paidById={} | reason=NOT_PAYER", currentUserId, request.getPaidById());
                throw new UserNotMemberException("Only the payer can create a settlement for themselves");
            }
            
            log.info("Calling external service | service=BalanceService | operation=validateSettlementAmount | groupId={}", request.getGroupId());
            balanceService.validateSettlementAmount(request.getGroupId(), request.getPaidById(), request.getPaidToId(), request.getAmount());

            SplitwiseGroup group = splitwiseGroupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new GroupNotFoundException(request.getGroupId()));

            Settlement settlement = Settlement.builder()
                    .group(group)
                    .paidBy(request.getPaidById())
                    .paidTo(request.getPaidToId())
                    .amount(request.getAmount())
                    .paymentMethod(request.getPaymentMethod())
                    .transactionId(request.getTransactionId())
                    .notes(request.getNotes())
                    .status("COMPLETED")
                    .build();
            settlement = settlementRepository.save(settlement);

            log.info("Calling external service | service=BalanceService | operation=updateBalancesForSettlement | settlementId={}", settlement.getId());
            balanceService.updateBalancesForSettlement(settlement);
            
            log.info("Calling external service | service=ActivityService | operation=logSettlementCreated | settlementId={}", settlement.getId());
            activityService.logSettlementCreated(currentUserId, group, settlement.getId(), settlement.getAmount());
            
            log.info("Processing completed | operation=createSettlement | userId={} | settlementId={} | status=SUCCESS", currentUserId, settlement.getId());
            return toSettlementResponse(settlement);
        } catch (UserNotMemberException | GroupNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Operation failed | operation=createSettlement | userId={} | groupId={} | reason={}", currentUserId, request.getGroupId(), e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public SettlementResponse getSettlement(UUID settlementId) {
        log.info("Processing started | operation=getSettlement | settlementId={}", settlementId);

        try {
            Settlement settlement = settlementRepository.findById(settlementId)
                    .orElseThrow(() -> new SettlementNotFoundException("Settlement not found with ID: " + settlementId));
            
            log.info("Processing completed | operation=getSettlement | settlementId={} | status=SUCCESS", settlementId);
            return toSettlementResponse(settlement);
        } catch (SettlementNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Operation failed | operation=getSettlement | settlementId={} | reason={}", settlementId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<SettlementResponse> getGroupSettlements(UUID groupId) {
        log.info("Processing started | operation=getGroupSettlements | groupId={}", groupId);

        try {
            List<Settlement> settlements = settlementRepository.findByGroupId(groupId);
            List<SettlementResponse> response = settlements.stream().map(this::toSettlementResponse).collect(Collectors.toList());
            
            log.info("Processing completed | operation=getGroupSettlements | groupId={} | settlementsCount={} | status=SUCCESS", groupId, response.size());
            return response;
        } catch (Exception e) {
            log.error("Operation failed | operation=getGroupSettlements | groupId={} | reason={}", groupId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<SettlementResponse> getUserSettlements(UUID userId) {
        log.info("Processing started | operation=getUserSettlements | userId={}", userId);

        try {
            List<Settlement> settlements = settlementRepository.findSettlementsInvolvingUser(userId);
            List<SettlementResponse> response = settlements.stream().map(this::toSettlementResponse).collect(Collectors.toList());
            
            log.info("Processing completed | operation=getUserSettlements | userId={} | settlementsCount={} | status=SUCCESS", userId, response.size());
            return response;
        } catch (Exception e) {
            log.error("Operation failed | operation=getUserSettlements | userId={} | reason={}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<SettlementProposal> getOptimizedSettlements(UUID groupId) {
        log.info("Processing started | operation=getOptimizedSettlements | groupId={}", groupId);

        try {
            log.info("Calling external service | service=BalanceService | operation=getOptimizedSettlements | groupId={}", groupId);
            List<SettlementProposal> proposals = balanceService.getOptimizedSettlements(groupId);
            
            log.info("Processing completed | operation=getOptimizedSettlements | groupId={} | proposalsCount={} | status=SUCCESS", groupId, proposals.size());
            return proposals;
        } catch (Exception e) {
            log.error("Operation failed | operation=getOptimizedSettlements | groupId={} | reason={}", groupId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Updates settlement status (e.g. PENDING -> COMPLETED). Optional; controller may not expose.
     */
    public SettlementResponse updateSettlementStatus(UUID settlementId, String status, UUID updatedBy) {
        log.info("Processing started | operation=updateSettlementStatus | settlementId={} | updatedBy={} | newStatus={}", settlementId, updatedBy, status);

        try {
            Settlement settlement = settlementRepository.findById(settlementId)
                    .orElseThrow(() -> new SettlementNotFoundException("Settlement not found with ID: " + settlementId));
            if (!splitwiseGroupRepository.isUserMemberOfGroup(settlement.getGroup().getId(), updatedBy)) {
                log.warn("Access denied | operation=updateSettlementStatus | settlementId={} | updatedBy={} | reason=NOT_MEMBER", settlementId, updatedBy);
                throw new UserNotMemberException(updatedBy, settlement.getGroup().getId());
            }
            settlement.setStatus(status != null ? status : settlement.getStatus());
            settlement = settlementRepository.save(settlement);
            
            log.info("Processing completed | operation=updateSettlementStatus | settlementId={} | updatedBy={} | status=SUCCESS", settlementId, updatedBy);
            return toSettlementResponse(settlement);
        } catch (SettlementNotFoundException | UserNotMemberException e) {
            throw e;
        } catch (Exception e) {
            log.error("Operation failed | operation=updateSettlementStatus | settlementId={} | updatedBy={} | reason={}", settlementId, updatedBy, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Deletes a settlement and reverses balance updates.
     */
    public void deleteSettlement(UUID settlementId, UUID deletedBy) {
        log.info("Processing started | operation=deleteSettlement | settlementId={} | deletedBy={}", settlementId, deletedBy);

        try {
            Settlement settlement = settlementRepository.findById(settlementId)
                    .orElseThrow(() -> new SettlementNotFoundException("Settlement not found with ID: " + settlementId));
            if (!splitwiseGroupRepository.isUserMemberOfGroup(settlement.getGroup().getId(), deletedBy)) {
                log.warn("Access denied | operation=deleteSettlement | settlementId={} | deletedBy={} | reason=NOT_MEMBER", settlementId, deletedBy);
                throw new UserNotMemberException(deletedBy, settlement.getGroup().getId());
            }
            BigDecimal amount = settlement.getAmount();
            SplitwiseGroup group = settlement.getGroup();
            
            log.info("Calling external service | service=BalanceService | operation=reverseBalancesForSettlement | settlementId={}", settlementId);
            balanceService.reverseBalancesForSettlement(settlement);
            settlementRepository.delete(settlement);
            
            log.info("Calling external service | service=ActivityService | operation=logSettlementDeleted | settlementId={}", settlementId);
            activityService.logSettlementDeleted(deletedBy, group, settlementId, amount);
            
            log.info("Processing completed | operation=deleteSettlement | settlementId={} | deletedBy={} | status=SUCCESS", settlementId, deletedBy);
        } catch (SettlementNotFoundException | UserNotMemberException e) {
            throw e;
        } catch (Exception e) {
            log.error("Operation failed | operation=deleteSettlement | settlementId={} | deletedBy={} | reason={}", settlementId, deletedBy, e.getMessage(), e);
            throw e;
        }
    }

    private SettlementResponse toSettlementResponse(Settlement settlement) {
        SplitwiseGroup g = settlement.getGroup();
        String groupName = g != null && g.getTripId() != null
                ? tripRepository.findById(g.getTripId()).map(TripEntity::getTripTitle).orElse(g.getDescription())
                : (g != null ? g.getDescription() : null);
        GroupResponse groupResponse = g != null ? GroupResponse.builder()
                .id(g.getId())
                .tripId(g.getTripId())
                .name(groupName)
                .description(g.getDescription())
                .build() : null;

        List<ExpenseResponse> settledExpenseResponses = new ArrayList<>();
        if (settlement.getSettledExpenses() != null) {
            for (SettlementExpense se : settlement.getSettledExpenses()) {
                Expense exp = se.getExpense();
                if (exp != null) {
                    settledExpenseResponses.add(ExpenseResponse.builder()
                            .id(exp.getId())
                            .name(exp.getName())
                            .amount(se.getAmount())
                            .build());
                }
            }
        }

        return SettlementResponse.builder()
                .id(settlement.getId())
                .group(groupResponse)
                .paidBy(toUserResponse(settlement.getPaidBy()))
                .paidTo(toUserResponse(settlement.getPaidTo()))
                .amount(settlement.getAmount())
                .paymentMethod(settlement.getPaymentMethod())
                .transactionId(settlement.getTransactionId())
                .notes(settlement.getNotes())
                .settledAt(settlement.getSettledAt())
                .settledExpenses(settledExpenseResponses)
                .isFullyAllocated(settlement.isFullyAllocated())
                .remainingAmount(settlement.getRemainingAmount())
                .status(settlement.getStatus())
                .build();
    }

    private UserResponse toUserResponse(UUID userId) {
        if (userId == null) return null;
        UsersEntity user = userRepository.findUserByUserUuid(userId).orElse(null);
        if (user == null) return UserResponse.builder().userUuid(userId).build();
        String name = "";
        if (user.getUserProfileEntity() != null) {
            UserProfileEntity p = user.getUserProfileEntity();
            name = (p.getFirstName() != null ? p.getFirstName() : "") + " " + (p.getLastName() != null ? p.getLastName() : "");
        }
        return UserResponse.builder()
                .userUuid(user.getUserUuid())
                .name(name.trim().isEmpty() ? null : name.trim())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .build();
    }
}
