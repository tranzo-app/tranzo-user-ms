package com.tranzo.tranzo_user_ms.user.service;

import com.tranzo.tranzo_user_ms.commons.exception.OtpException;
import com.tranzo.tranzo_user_ms.commons.service.JwtService;
import com.tranzo.tranzo_user_ms.user.dto.RequestOtpDto;
import com.tranzo.tranzo_user_ms.user.dto.SessionRequestDto;
import com.tranzo.tranzo_user_ms.user.dto.SessionResponseDto;
import com.tranzo.tranzo_user_ms.user.dto.VerifyOtpDto;
import com.tranzo.tranzo_user_ms.user.dto.VerifyOtpResponseDto;
import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import com.tranzo.tranzo_user_ms.user.repository.UserRepository;
import com.tranzo.tranzo_user_ms.user.utility.OtpUtility;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OtpService Unit Tests")
class OtpServiceTest {

    @Mock
    private OtpUtility otpUtility;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private SessionService sessionService;

    @Mock
    private com.github.benmanes.caffeine.cache.Cache<String, String> otpCache;

    @InjectMocks
    private OtpService otpService;

    @Mock
    private HttpServletResponse response;

    @Test
    @DisplayName("sendOtp should cache OTP and use OtpUtility")
    void sendOtp_cachesOtp() {
        RequestOtpDto dto = new RequestOtpDto();
        dto.setEmailId("u@test.com");
        when(otpUtility.resolveIdentifier(any())).thenReturn("u@test.com");
        when(otpUtility.generateOtp()).thenReturn("123456");

        otpService.sendOtp(dto);

        verify(otpUtility).resolveIdentifier(dto);
        verify(otpUtility).generateOtp();
        verify(otpCache).put(eq("OTP:u@test.com"), eq("123456"));
    }

    @Test
    @DisplayName("verifyOtp when OTP expired or not found throws OtpException")
    void verifyOtp_expiredOrNotFound_throwsOtpException() {
        VerifyOtpDto dto = VerifyOtpDto.builder().emailId("u@test.com").otp("123456").build();
        when(otpUtility.resolveIdentifier(any())).thenReturn("u@test.com");
        when(otpCache.getIfPresent(anyString())).thenReturn(null);

        assertThrows(OtpException.class, () -> otpService.verifyOtp(dto, response));
        verify(otpCache, never()).invalidate(anyString());
    }

    @Test
    @DisplayName("verifyOtp when OTP does not match throws OtpException")
    void verifyOtp_invalidOtp_throwsOtpException() {
        VerifyOtpDto dto = VerifyOtpDto.builder().emailId("u@test.com").otp("123456").build();
        when(otpUtility.resolveIdentifier(any())).thenReturn("u@test.com");
        when(otpCache.getIfPresent(anyString())).thenReturn("999999");

        assertThrows(OtpException.class, () -> otpService.verifyOtp(dto, response));
    }

    @Test
    @DisplayName("verifyOtp when user does not exist creates user and returns registration token")
    void verifyOtp_newUser_returnsRegistrationToken() {
        VerifyOtpDto dto = VerifyOtpDto.builder().emailId("u@test.com").otp("123456").build();
        when(otpUtility.resolveIdentifier(any())).thenReturn("u@test.com");
        when(otpCache.getIfPresent(anyString())).thenReturn("123456");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(jwtService.generateRegistrationToken(anyString())).thenReturn("reg-token");

        VerifyOtpResponseDto result = otpService.verifyOtp(dto, response);

        assertFalse(result.isUserExists());
        assertEquals("reg-token", result.getRegistrationToken());
        verify(otpCache).invalidate(anyString());
        verify(userRepository).save(any(UsersEntity.class));
    }

    @Test
    @DisplayName("verifyOtp when existing user with profile creates session")
    void verifyOtp_existingUserWithProfile_returnsUserExists() {
        VerifyOtpDto dto = VerifyOtpDto.builder().emailId("u@test.com").otp("123456").build();
        UsersEntity user = new UsersEntity();
        user.setUserUuid(java.util.UUID.randomUUID());
        user.setUserProfileEntity(new com.tranzo.tranzo_user_ms.user.model.UserProfileEntity());
        when(otpUtility.resolveIdentifier(any())).thenReturn("u@test.com");
        when(otpCache.getIfPresent(anyString())).thenReturn("123456");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(sessionService.createSession(any(SessionRequestDto.class), eq(response)))
                .thenReturn(SessionResponseDto.builder().build());

        VerifyOtpResponseDto result = otpService.verifyOtp(dto, response);

        assertTrue(result.isUserExists());
        verify(sessionService).createSession(any(SessionRequestDto.class), eq(response));
        verify(otpCache).invalidate(anyString());
    }
}
