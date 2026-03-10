package com.tranzo.tranzo_user_ms.user.service;

import com.tranzo.tranzo_user_ms.user.dto.AadharNumberDto;
import com.tranzo.tranzo_user_ms.user.dto.AadharOtpSuccessResponse;
import com.tranzo.tranzo_user_ms.user.exception.AadharValidationException;
import com.tranzo.tranzo_user_ms.user.model.AadharOtpEntity;
import com.tranzo.tranzo_user_ms.user.model.AadharOtpVerifySuccessResponse;
import com.tranzo.tranzo_user_ms.user.model.AadharVerifyData;
import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import com.tranzo.tranzo_user_ms.user.repository.AadharOtpRepository;
import com.tranzo.tranzo_user_ms.user.repository.UserRepository;
import com.tranzo.tranzo_user_ms.user.repository.VerificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AadharService Unit Tests")
class AadharServiceTest {

    @Mock
    private AadharClient aadharClient;

    @Mock
    private AadharOtpRepository aadhaarOtpRepository;

    @Mock
    private VerificationRepository verificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AadharService aadharService;

    private final UUID userId = UUID.randomUUID();

    @Test
    @DisplayName("generateOtp should save OTP entity with masked aadhaar when API succeeds")
    void generateOtp_success_savesOtpEntity() {
        UsersEntity user = new UsersEntity();
        user.setUserUuid(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        AadharOtpSuccessResponse.DataNode dataNode = new AadharOtpSuccessResponse.DataNode();
        dataNode.setReferenceId(12345L);
        AadharOtpSuccessResponse response = new AadharOtpSuccessResponse();
        response.setData(dataNode);
        when(aadharClient.sendOtp(any())).thenReturn(response);
        when(aadhaarOtpRepository.findValidByUser(userId)).thenReturn(Optional.empty());

        AadharNumberDto dto = new AadharNumberDto();
        dto.setAadharNumber("123456789012");

        aadharService.generateOtp(userId, dto);

        ArgumentCaptor<AadharOtpEntity> captor = ArgumentCaptor.forClass(AadharOtpEntity.class);
        verify(aadhaarOtpRepository).save(captor.capture());
        AadharOtpEntity saved = captor.getValue();
        assertEquals(userId, saved.getUserId());
        assertEquals("XXXX-XXXX-9012", saved.getAadhaarNumber());
        assertEquals("12345", saved.getReferenceId());
    }

    @Test
    @DisplayName("generateOtp when user not found throws RuntimeException")
    void generateOtp_userNotFound_throws() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        AadharNumberDto dto = new AadharNumberDto();
        dto.setAadharNumber("123456789012");

        assertThrows(RuntimeException.class, () -> aadharService.generateOtp(userId, dto));
        verify(aadharClient, never()).sendOtp(any());
    }

    @Test
    @DisplayName("generateOtp when API returns null throws RuntimeException")
    void generateOtp_nullResponse_throws() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(new UsersEntity()));
        when(aadharClient.sendOtp(any())).thenReturn(null);
        AadharNumberDto dto = new AadharNumberDto();
        dto.setAadharNumber("123456789012");

        assertThrows(RuntimeException.class, () -> aadharService.generateOtp(userId, dto));
    }

    @Test
    @DisplayName("generateOtp with invalid aadhaar number throws IllegalArgumentException")
    void generateOtp_invalidAadhaarLength_throws() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(new UsersEntity()));
        AadharOtpSuccessResponse response = new AadharOtpSuccessResponse();
        response.setData(new AadharOtpSuccessResponse.DataNode());
        response.getData().setReferenceId(1L);
        when(aadharClient.sendOtp(any())).thenReturn(response);

        AadharNumberDto dto = new AadharNumberDto();
        dto.setAadharNumber("123"); // too short

        assertThrows(IllegalArgumentException.class, () -> aadharService.generateOtp(userId, dto));
    }

    @Test
    @DisplayName("verifyAadhaarOtp when status not VALID throws AadharValidationException")
    void verifyAadhaarOtp_invalidStatus_throws() {
        AadharOtpEntity otpEntity = new AadharOtpEntity();
        otpEntity.setReferenceId("ref-1");
        when(aadhaarOtpRepository.findValidByUser(userId)).thenReturn(Optional.of(otpEntity));

        AadharOtpVerifySuccessResponse response = new AadharOtpVerifySuccessResponse();
        AadharVerifyData dataNode = new AadharVerifyData();
        dataNode.setStatus("INVALID");
        response.setData(dataNode);
        when(aadharClient.verifyOtp(any())).thenReturn(response);

        assertThrows(AadharValidationException.class, () -> aadharService.verifyAadhaarOtp(userId, "123456"));
    }
}
