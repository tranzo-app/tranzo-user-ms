package com.tranzo.tranzo_user_ms.splitwise.controller;

import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.BalanceResponse;
import com.tranzo.tranzo_user_ms.splitwise.service.BalanceService;
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
@DisplayName("BalanceController Unit Tests")
class BalanceControllerTest {

    @Mock
    private BalanceService balanceService;

    @InjectMocks
    private BalanceController controller;

    private Long groupId;
    private UUID userId;
    private BalanceResponse balanceResponse;

    @BeforeEach
    void setUp() {
        groupId = 1L;
        userId = UUID.randomUUID();
        balanceResponse = BalanceResponse.builder()
                .netBalance(new BigDecimal("-50"))
                .totalOwed(BigDecimal.ZERO)
                .totalOwing(new BigDecimal("50"))
                .build();
    }

    @Test
    @DisplayName("Should get group balances")
    void getGroupBalances_Success() {
        when(balanceService.getGroupBalances(groupId)).thenReturn(List.of(balanceResponse));

        ResponseEntity<List<BalanceResponse>> res = controller.getGroupBalances(groupId);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNotNull(res.getBody());
        assertEquals(1, res.getBody().size());
    }

    @Test
    @DisplayName("Should get user balance in group")
    void getUserBalanceInGroup_Success() {
        when(balanceService.getUserBalanceInGroup(groupId, userId)).thenReturn(balanceResponse);

        ResponseEntity<BalanceResponse> res = controller.getUserBalanceInGroup(groupId, userId);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNotNull(res.getBody());
    }

    @Test
    @DisplayName("Should get my balance in group")
    void getMyBalanceInGroup_Success() throws Exception {
        when(balanceService.getUserBalanceInGroup(groupId, userId)).thenReturn(balanceResponse);
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<BalanceResponse> res = controller.getMyBalanceInGroup(groupId);

            assertEquals(HttpStatus.OK, res.getStatusCode());
        }
    }
}
