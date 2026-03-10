package com.tranzo.tranzo_user_ms.user.controller;

import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import com.tranzo.tranzo_user_ms.user.dto.AadharNumberDto;
import com.tranzo.tranzo_user_ms.user.service.AadharService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AadharController Unit Tests")
class AadharControllerTest {

    @Mock
    private AadharService aadharService;

    @InjectMocks
    private AadharController controller;

    private final UUID userId = UUID.randomUUID();

    @Test
    @DisplayName("requestOtp should call service and return 200")
    void requestOtp_success_returns200() throws Exception {
        AadharNumberDto dto = new AadharNumberDto();
        dto.setAadharNumber("123456789012");
        doNothing().when(aadharService).generateOtp(eq(userId), eq(dto));

        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<ResponseDto<Void>> res = controller.requestOtp(dto);

            assertEquals(HttpStatus.OK, res.getStatusCode());
            assertNotNull(res.getBody());
            assertEquals(200, res.getBody().getStatusCode());
            assertEquals("OTP sent successfully", res.getBody().getStatusMessage());
            verify(aadharService).generateOtp(userId, dto);
        }
    }
}
