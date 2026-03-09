package com.tranzo.tranzo_user_ms.user.controller;

import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import com.tranzo.tranzo_user_ms.user.service.TravelPalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TravelPalController Unit Tests")
class TravelPalControllerTest {

    @Mock
    private TravelPalService service;

    @InjectMocks
    private TravelPalController controller;

    private UUID userId;
    private UUID otherId;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        otherId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should send travel pal request")
    void sendRequest_Success() throws Exception {
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);
            doNothing().when(service).sendRequest(userId, otherId);

            ResponseEntity<?> res = controller.sendRequest(otherId);

            assertEquals(HttpStatus.OK, res.getStatusCode());
            assertEquals("Request sent", res.getBody());
        }
    }

    @Test
    @DisplayName("Should accept request")
    void accept_Success() throws Exception {
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);
            doNothing().when(service).acceptRequest(userId, otherId);

            ResponseEntity<?> res = controller.accept(otherId);

            assertEquals(HttpStatus.OK, res.getStatusCode());
        }
    }

    @Test
    @DisplayName("Should reject request")
    void reject_Success() throws Exception {
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);
            doNothing().when(service).rejectRequest(userId, otherId);

            ResponseEntity<?> res = controller.reject(otherId);

            assertEquals(HttpStatus.OK, res.getStatusCode());
        }
    }

    @Test
    @DisplayName("Should remove travel pal")
    void remove_Success() throws Exception {
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);
            doNothing().when(service).removeTravelPal(userId, otherId);

            ResponseEntity<?> res = controller.remove(otherId);

            assertEquals(HttpStatus.OK, res.getStatusCode());
        }
    }

    @Test
    @DisplayName("Should get my travel pals")
    void myPals_Success() throws Exception {
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);
            when(service.getMyTravelPals(userId)).thenReturn(List.of(otherId));


            ResponseEntity<ResponseDto<List<UUID>>> res = controller.myPals();

            assertEquals(HttpStatus.OK, res.getStatusCode());
            assertNotNull(res.getBody());
            assertEquals(1, res.getBody().getData().size());
        }
    }

    @Test
    @DisplayName("Should get pending requests")
    void pending_Success() throws Exception {
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);
            when(service.getIncomingPendingRequests(userId)).thenReturn(List.of());

            ResponseEntity<?> res = controller.pending();

            assertEquals(HttpStatus.OK, res.getStatusCode());
        }
    }
}
