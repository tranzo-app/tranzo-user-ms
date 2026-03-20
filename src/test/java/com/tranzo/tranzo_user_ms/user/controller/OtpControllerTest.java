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
    void requestOtp_Success() throws Exception {
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
    void verifyOtp_Success() throws Exception {
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

    @Test
    @DisplayName("Should handle OTP request exception")
    void requestOtp_Exception() throws Exception {
        RequestOtpDto dto = new RequestOtpDto();
        dto.setEmailId("u@test.com");
        doThrow(new RuntimeException("Service unavailable")).when(otpService).sendOtp(any(RequestOtpDto.class));

        assertThrows(Exception.class, () -> controller.requestOtp(dto));
        verify(otpService).sendOtp(dto);
    }

    @Test
    @DisplayName("Should handle OTP verification exception")
    void verifyOtp_Exception() throws Exception {
        VerifyOtpDto dto = new VerifyOtpDto();
        dto.setEmailId("u@test.com");
        dto.setOtp("123456");
        doThrow(new RuntimeException("Invalid OTP")).when(otpService).verifyOtp(any(VerifyOtpDto.class), any(HttpServletResponse.class));

        assertThrows(Exception.class, () -> controller.verifyOtp(dto, response));
        verify(otpService).verifyOtp(dto, response);
    }
}
