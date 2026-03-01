package com.tranzo.tranzo_user_ms.user.service;

import com.tranzo.tranzo_user_ms.user.enums.DocumentType;
import com.tranzo.tranzo_user_ms.user.enums.VerificationStatus;
import com.tranzo.tranzo_user_ms.user.exception.AadharValidationException;
import com.tranzo.tranzo_user_ms.user.model.*;
import com.tranzo.tranzo_user_ms.user.repository.AadharOtpRepository;
import com.tranzo.tranzo_user_ms.user.repository.UserRepository;
import com.tranzo.tranzo_user_ms.user.repository.VerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AadharService {
    private final AadharClient aadharClient;
    private final AadharOtpRepository aadhaarOtpRepository;
    private final VerificationRepository verificationRepository;
    private final UserRepository userRepository;

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
