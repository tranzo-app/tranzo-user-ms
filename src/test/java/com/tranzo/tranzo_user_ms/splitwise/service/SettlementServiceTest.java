package com.tranzo.tranzo_user_ms.splitwise.service;

import com.tranzo.tranzo_user_ms.splitwise.dto.SettlementProposal;
import com.tranzo.tranzo_user_ms.splitwise.dto.request.CreateSettlementRequest;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.SettlementResponse;
import com.tranzo.tranzo_user_ms.splitwise.entity.Settlement;
import com.tranzo.tranzo_user_ms.splitwise.entity.SplitwiseGroup;
import com.tranzo.tranzo_user_ms.splitwise.exception.GroupNotFoundException;
import com.tranzo.tranzo_user_ms.splitwise.exception.SettlementNotFoundException;
import com.tranzo.tranzo_user_ms.splitwise.repository.BalanceRepository;
import com.tranzo.tranzo_user_ms.splitwise.repository.SettlementRepository;
import com.tranzo.tranzo_user_ms.splitwise.repository.SplitwiseGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementService Unit Tests")
class SettlementServiceTest {

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private BalanceService balanceService;

    @Mock
    private ActivityService activityService;

    @Mock
    private BalanceRepository balanceRepository;

    @Mock
    private SplitwiseGroupRepository splitwiseGroupRepository;

    @InjectMocks
    private SettlementService settlementService;

    private Long groupId;
    private UUID payerId;
    private UUID payeeId;
    private UUID currentUserId;
    private SplitwiseGroup group;
    private Settlement settlement;
    private CreateSettlementRequest createRequest;

    @BeforeEach
    void setUp() {
        groupId = 1L;
        payerId = UUID.randomUUID();
        payeeId = UUID.randomUUID();
        currentUserId = UUID.randomUUID();
        group = SplitwiseGroup.builder().id(groupId).tripId(UUID.randomUUID()).createdBy(currentUserId).build();
        settlement = Settlement.builder()
                .id(10L)
                .group(group)
                .paidBy(payerId)
                .paidTo(payeeId)
                .amount(new BigDecimal("50.00"))
                .build();
        createRequest = CreateSettlementRequest.builder()
                .groupId(groupId)
                .paidById(payerId)
                .paidToId(payeeId)
                .amount(new BigDecimal("50.00"))
                .build();
    }

    @Test
    @DisplayName("Should create settlement successfully")
    void createSettlement_Success() {
        doNothing().when(balanceService).validateSettlementAmount(eq(groupId), eq(payerId), eq(payeeId), any(BigDecimal.class));
        when(splitwiseGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(settlementRepository.save(any(Settlement.class))).thenReturn(settlement);

        SettlementResponse response = settlementService.createSettlement(createRequest, currentUserId);

        assertNotNull(response);
        assertEquals(settlement.getId(), response.getId());
        verify(balanceService).updateBalancesForSettlement(any(Settlement.class));
        verify(activityService).logSettlementCreated(eq(currentUserId), eq(group), eq(settlement.getId()), any(BigDecimal.class));
    }

    @Test
    @DisplayName("Should throw when group not found on create")
    void createSettlement_GroupNotFound() {
        doNothing().when(balanceService).validateSettlementAmount(anyLong(), any(), any(), any());
        when(splitwiseGroupRepository.findById(groupId)).thenReturn(Optional.empty());

        assertThrows(GroupNotFoundException.class, () -> settlementService.createSettlement(createRequest, currentUserId));
        verify(settlementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get settlement by id")
    void getSettlement_Success() {
        when(settlementRepository.findById(10L)).thenReturn(Optional.of(settlement));

        SettlementResponse response = settlementService.getSettlement(10L);

        assertNotNull(response);
        assertEquals(10L, response.getId());
    }

    @Test
    @DisplayName("Should throw SettlementNotFoundException when not found")
    void getSettlement_NotFound() {
        when(settlementRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(SettlementNotFoundException.class, () -> settlementService.getSettlement(999L));
    }

    @Test
    @DisplayName("Should get group settlements")
    void getGroupSettlements_Success() {
        when(settlementRepository.findByGroupId(groupId)).thenReturn(List.of(settlement));

        List<SettlementResponse> list = settlementService.getGroupSettlements(groupId);

        assertNotNull(list);
        assertEquals(1, list.size());
    }

    @Test
    @DisplayName("Should get user settlements")
    void getUserSettlements_Success() {
        when(settlementRepository.findSettlementsInvolvingUser(currentUserId)).thenReturn(List.of(settlement));

        List<SettlementResponse> list = settlementService.getUserSettlements(currentUserId);

        assertNotNull(list);
        assertEquals(1, list.size());
    }

    @Test
    @DisplayName("Should get optimized settlements via balance service")
    void getOptimizedSettlements_Success() {
        when(balanceService.getOptimizedSettlements(groupId)).thenReturn(
                List.of(new SettlementProposal(payerId, payeeId, new BigDecimal("50"))));

        List<SettlementProposal> list = settlementService.getOptimizedSettlements(groupId);

        assertNotNull(list);
        assertEquals(1, list.size());
    }

    @Test
    @DisplayName("Should update settlement status")
    void updateSettlementStatus_Success() {
        when(settlementRepository.findById(10L)).thenReturn(Optional.of(settlement));
        when(settlementRepository.save(any(Settlement.class))).thenReturn(settlement);
        settlement.setStatus("COMPLETED");

        SettlementResponse response = settlementService.updateSettlementStatus(10L, "COMPLETED", currentUserId);

        assertNotNull(response);
        verify(settlementRepository).save(any(Settlement.class));
    }

    @Test
    @DisplayName("Should delete settlement and reverse balances")
    void deleteSettlement_Success() {
        when(settlementRepository.findById(10L)).thenReturn(Optional.of(settlement));
        doNothing().when(balanceService).reverseBalancesForSettlement(settlement);
        doNothing().when(settlementRepository).delete(any(Settlement.class));

        settlementService.deleteSettlement(10L, currentUserId);

        verify(balanceService).reverseBalancesForSettlement(settlement);
        verify(settlementRepository).delete(settlement);
        verify(activityService).logSettlementDeleted(eq(currentUserId), eq(group), eq(10L), any(BigDecimal.class));
    }
}
