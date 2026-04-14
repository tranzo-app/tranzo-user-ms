package com.tranzo.tranzo_user_ms.user.service;

import com.tranzo.tranzo_user_ms.user.dto.AadharNumberDto;
import com.tranzo.tranzo_user_ms.user.dto.AadharOtpRequest;
import com.tranzo.tranzo_user_ms.user.dto.AadharOtpSuccessResponse;
import com.tranzo.tranzo_user_ms.user.enums.DocumentType;
import com.tranzo.tranzo_user_ms.user.enums.OtpStatus;
import com.tranzo.tranzo_user_ms.user.enums.VerificationStatus;
import com.tranzo.tranzo_user_ms.user.exception.AadharValidationException;
import com.tranzo.tranzo_user_ms.user.model.*;
import com.tranzo.tranzo_user_ms.user.repository.AadharOtpRepository;
import com.tranzo.tranzo_user_ms.user.repository.UserRepository;
import com.tranzo.tranzo_user_ms.user.repository.VerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "aadhar.enabled", havingValue = "true")
public class AadharService {
    private final AadharClient aadharClient;
    private final AadharOtpRepository aadhaarOtpRepository;
    private final VerificationRepository verificationRepository;
    private final UserRepository userRepository;

    public void generateOtp(UUID userId, AadharNumberDto aadharNumberDto) {
        UsersEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // Prepare request
        AadharOtpRequest request = AadharOtpRequest.builder()
                .entity("in.co.sandbox.kyc.aadhaar.okyc.otp")
                .aadhaarNumber(aadharNumberDto.getAadharNumber())
                .consent("Y")
                .reason("User verification for travel platform")
                .build();
        // Call external API
        AadharOtpSuccessResponse response =
                aadharClient.sendOtp(request);
        if (response == null || response.getData() == null) {
            throw new RuntimeException("Failed to generate Aadhaar OTP");
        }
        String referenceId =
                String.valueOf(response.getData().getReferenceId());
        // Expire old OTPs
        aadhaarOtpRepository.findValidByUser(userId)
                .ifPresent(existing -> existing.setStatus(OtpStatus.EXPIRED));
        // Save new OTP entry
        AadharOtpEntity otpEntity = new AadharOtpEntity();
        otpEntity.setUserId(userId);
        otpEntity.setReferenceId(referenceId);
        otpEntity.setAadhaarNumber(maskAadhaar(aadharNumberDto.getAadharNumber()));
        otpEntity.setStatus(OtpStatus.SENT);
        otpEntity.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        aadhaarOtpRepository.save(otpEntity);
    }

    @Transactional
    public void verifyAadhaarOtp(
            UUID userId,
            String otp
    ) {
        // 1. Get stored reference id
        AadharOtpEntity otpEntity =
                aadhaarOtpRepository.findValidByUser(userId)
                        .orElseThrow(() ->
                                new RuntimeException("OTP not found"));
        // 2. Build request
        AadharOtpVerifyRequest request =
                AadharOtpVerifyRequest.builder()
                        .entity("in.co.sandbox.kyc.aadhaar.okyc.otp.verify")
                        .referenceId(otpEntity.getReferenceId())
                        .otp(otp)
                        .build();
        // 3. Call API
        AadharOtpVerifySuccessResponse response =
                aadharClient.verifyOtp(request);
        if (!"VALID".equalsIgnoreCase(
                response.getData().getStatus())) {
            throw new AadharValidationException("Invalid OTP");
        }
        // 4. Save verification
        saveVerification(userId, otpEntity, response);
        // 5. Mark OTP as used
        otpEntity.setUsed(true);
    }

    private void saveVerification(
            UUID userId,
            AadharOtpEntity otpEntity,
            AadharOtpVerifySuccessResponse response
    ) {
        UsersEntity user = userRepository.getReferenceById(userId);
        VerificationEntity verification = new VerificationEntity();
        verification.setUser(user);
        verification.setDocumentType(DocumentType.AADHAAR);
        verification.setDocumentNumber(maskAadhaar(otpEntity.getAadhaarNumber()));
        verification.setVerificationStatus(VerificationStatus.VERIFIED);
        verification.setVerifiedAt(LocalDateTime.now());
        verification.setVerifiedBy("SYSTEM");
        verificationRepository.save(verification);
    }

    private String maskAadhaar(String aadhaarNumber) {
        if (aadhaarNumber == null) {
            throw new IllegalArgumentException("Aadhaar number cannot be null");
        }
        String cleaned = aadhaarNumber.replaceAll("[^0-9]", "");
        if (!cleaned.matches("\\d{12}")) {
            throw new IllegalArgumentException("Invalid Aadhaar number");
        }
        return "XXXX-XXXX-" + cleaned.substring(8);
    }
}
