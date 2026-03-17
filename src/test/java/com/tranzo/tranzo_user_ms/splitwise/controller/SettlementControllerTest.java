package com.tranzo.tranzo_user_ms.splitwise.controller;

import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import com.tranzo.tranzo_user_ms.splitwise.dto.SettlementProposal;
import com.tranzo.tranzo_user_ms.splitwise.dto.request.CreateSettlementRequest;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.SettlementResponse;
import com.tranzo.tranzo_user_ms.splitwise.service.SettlementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementController Unit Tests")
class SettlementControllerTest {

    @Mock
    private SettlementService settlementService;

    @InjectMocks
    private SettlementController controller;

    private UUID userId;
    private UUID groupId;
    private UUID settlementId;
    private SettlementResponse settlementResponse;
    private CreateSettlementRequest createRequest;
    private SettlementProposal proposal;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        groupId = UUID.randomUUID();
        settlementId = UUID.randomUUID();
        settlementResponse = SettlementResponse.builder().id(settlementId).amount(new BigDecimal("50")).build();
        createRequest = CreateSettlementRequest.builder()
                .groupId(groupId)
                .paidById(userId)
                .paidToId(UUID.randomUUID())
                .amount(new BigDecimal("50"))
                .build();
        proposal = new SettlementProposal(userId, UUID.randomUUID(), new BigDecimal("50"));
    }

    @Test
    @DisplayName("Should create settlement and return 200")
    void createSettlement_Success() throws Exception {
        when(settlementService.createSettlement(any(CreateSettlementRequest.class), eq(userId))).thenReturn(settlementResponse);
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<SettlementResponse> res = controller.createSettlement(createRequest);

            assertEquals(HttpStatus.OK, res.getStatusCode());
            assertNotNull(res.getBody());
            assertEquals(settlementId, res.getBody().getId());
        }
    }

    @Test
    @DisplayName("Should get settlement by id")
    void getSettlement_Success() {
        when(settlementService.getSettlement(settlementId)).thenReturn(settlementResponse);

        ResponseEntity<SettlementResponse> res = controller.getSettlement(settlementId);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNotNull(res.getBody());
    }

    @Test
    @DisplayName("Should get group settlements")
    void getGroupSettlements_Success() {
        when(settlementService.getGroupSettlements(groupId)).thenReturn(List.of(settlementResponse));

        ResponseEntity<List<SettlementResponse>> res = controller.getGroupSettlements(groupId);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNotNull(res.getBody());
        assertEquals(1, res.getBody().size());
    }

    @Test
    @DisplayName("Should get my settlements")
    void getUserSettlements_Success() throws Exception {
        when(settlementService.getUserSettlements(userId)).thenReturn(List.of(settlementResponse));
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<List<SettlementResponse>> res = controller.getUserSettlements();

            assertEquals(HttpStatus.OK, res.getStatusCode());
            assertEquals(1, res.getBody().size());
        }
    }

    @Test
    @DisplayName("Should get optimized settlements for group")
    void getOptimizedSettlements_Success() {
        when(settlementService.getOptimizedSettlements(groupId)).thenReturn(List.of(proposal));

        ResponseEntity<List<SettlementProposal>> res = controller.getOptimizedSettlements(groupId);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNotNull(res.getBody());
        assertEquals(1, res.getBody().size());
    }
}
