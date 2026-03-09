package com.tranzo.tranzo_user_ms.user.controller;

import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.user.dto.RequestOtpDto;
import com.tranzo.tranzo_user_ms.user.dto.VerifyOtpDto;
import com.tranzo.tranzo_user_ms.user.dto.VerifyOtpResponseDto;
import com.tranzo.tranzo_user_ms.user.service.OtpService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OtpController Unit Tests")
class OtpControllerTest {

    @Mock
    private OtpService otpService;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private OtpController controller;

    @Test
    @DisplayName("Should request OTP successfully")
    void requestOtp_Success() {
        RequestOtpDto dto = new RequestOtpDto();
        dto.setEmailId("u@test.com");
        doNothing().when(otpService).sendOtp(any(RequestOtpDto.class));

        ResponseEntity<ResponseDto<Void>> res = controller.requestOtp(dto);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals("OTP sent successfully", res.getBody().getStatusMessage());
        verify(otpService).sendOtp(dto);
    }

    @Test
    @DisplayName("Should verify OTP successfully")
    void verifyOtp_Success() {
        VerifyOtpDto dto = new VerifyOtpDto();
        dto.setEmailId("u@test.com");
        dto.setOtp("123456");
        VerifyOtpResponseDto verifyResponse = VerifyOtpResponseDto.builder().userExists(true).build();
        when(otpService.verifyOtp(any(VerifyOtpDto.class), any(HttpServletResponse.class))).thenReturn(verifyResponse);

        ResponseEntity<ResponseDto<VerifyOtpResponseDto>> res = controller.verifyOtp(dto, response);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNotNull(res.getBody().getData());
        assertTrue(res.getBody().getData().isUserExists());
    }
}
