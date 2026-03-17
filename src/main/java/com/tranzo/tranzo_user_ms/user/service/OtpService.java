package com.tranzo.tranzo_user_ms.user.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.tranzo.tranzo_user_ms.commons.service.JwtService;
import com.tranzo.tranzo_user_ms.user.dto.*;
import com.tranzo.tranzo_user_ms.user.enums.AccountStatus;
import com.tranzo.tranzo_user_ms.user.enums.UserRole;
import com.tranzo.tranzo_user_ms.commons.exception.OtpException;
import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import com.tranzo.tranzo_user_ms.user.repository.UserRepository;
import com.tranzo.tranzo_user_ms.user.utility.OtpUtility;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {
    private final OtpUtility otpUtility;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final SessionService sessionService;
    private final Cache<String, Integer> rateLimitCache;
    private final OtpCacheService cacheService;
    private final SmsService smsService;
    private final EmailService emailService;

    private static final int MAX_ATTEMPTS = 3;
    private static final int MAX_REQUESTS = 3;

    public void sendOtp(RequestOtpDto requestOtpDto) throws Exception {
        String identifier = otpUtility.resolveIdentifier(requestOtpDto);
        String otpRateLimitKey = buildKeyForRateLimiting(identifier);

        // Rate Limit logic
        Integer count = rateLimitCache.getIfPresent(otpRateLimitKey);
        if (count != null && count >= MAX_REQUESTS) {
            throw new OtpException("Too many OTP requests. Try later.");
        }
        rateLimitCache.put(otpRateLimitKey, count == null ? 1 : count + 1);

        String otpKey = buildKey(identifier);
        OtpData existing = cacheService.get(otpKey);

        // TODO
        // OTP reuse trick
        long now = System.currentTimeMillis();
        if (existing != null) {
            long sentAt = existing.getSentAt(); // store sent timestamp in OtpData
            if (now - sentAt < 30_000) { // 30 seconds in milliseconds
                throw new OtpException("Please wait before requesting a new OTP.");
            } else {
                // resend same OTP
                String otpHash = existing.getOtpHash();
//                smsService.sendOtp(identifier, existing.getPlainOtp()); // need to store plain OTP temporarily
                existing.setSentAt(now);
                cacheService.put(identifier, existing);
                return;
            }
        }
        String otp = otpUtility.generateOtp();
        String hash = hashOtp(otp);
        cacheService.put(
                otpKey,
                new OtpData(otp, hash, 0, System.currentTimeMillis())
        );
        log.info("OTP for {} is {}", identifier, otp);
        // Sending SMS via AWS SNS
//        smsService.sendOtp(identifier, otp);
        // Sending SMS via AWS SNS
        if (requestOtpDto.getEmailId() != null)
        {
            emailService.sendOtpEmail(identifier, otp);
        }
        else
        {
            smsService.sendOtp(identifier, otp);
        }
    }

    public VerifyOtpResponseDto verifyOtp(VerifyOtpDto verifyOtpDto, HttpServletResponse response) throws Exception {
        String identifier = otpUtility.resolveIdentifier(verifyOtpDto);
        String key = buildKey(identifier);
        log.info("Key for fetching OTP is {}", key);
        OtpData cachedOtp = (OtpData) cacheService.get(key);
        if (cachedOtp == null) {
            throw new OtpException("OTP expired or not found");
        }
        if (cachedOtp.getAttempts() >= MAX_ATTEMPTS)
        {
            throw new OtpException("Maximum number of attempts");
        }
        String hash = hashOtp(verifyOtpDto.getOtp());
        if (!hash.equals(cachedOtp.getOtpHash()))
        {
            cachedOtp.setAttempts(cachedOtp.getAttempts() + 1);
            cacheService.put(key, cachedOtp);
            throw new OtpException("Invalid OTP");
        }
        cacheService.remove(key);
        Optional<UsersEntity> user = findUserByIdentifier(verifyOtpDto);
        boolean userExists = user.isPresent();
        if (!userExists)
        {
            createNewUser(verifyOtpDto);
            String registrationToken = jwtService.generateRegistrationToken(identifier);
            return VerifyOtpResponseDto.builder()
                    .userExists(userExists)
                    .registrationToken(registrationToken)
                    .build();
        }
        else if (user.get().getUserProfileEntity() == null)
        {
            String registrationToken = jwtService.generateRegistrationToken(identifier);
            return VerifyOtpResponseDto.builder()
                    .userExists(false)
                    .registrationToken(registrationToken)
                    .build();
        }
        SessionRequestDto sessionRequestDto = SessionRequestDto.builder().mobileNumber(verifyOtpDto.getMobileNumber()).emailId(verifyOtpDto.getEmailId()).build();
        com.tranzo.tranzo_user_ms.user.dto.SessionResponseDto sessionResponseDto = sessionService.createSession(sessionRequestDto, response);
        return VerifyOtpResponseDto.builder()
                .userExists(userExists)
                .build();
    }

    private String buildKey(String identifier)
    {
        return "OTP:" + identifier;
    }

    private String buildKeyForRateLimiting(String identifier)
    {
        return "OTPRL:" + identifier;
    }

    private Optional<UsersEntity> findUserByIdentifier(VerifyOtpDto dto) {
        if (dto.getEmailId() != null && !dto.getEmailId().isBlank()) {
            return userRepository.findByEmail(dto.getEmailId().toLowerCase());
        }
        return userRepository.findByMobileNumber(dto.getMobileNumber());
    }

    private String hashOtp(String otp) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(otp.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }

    @Transactional
    private void createNewUser(VerifyOtpDto verifyOtpDto)
    {
        UsersEntity usersEntity = new UsersEntity();
        if (verifyOtpDto.getEmailId() != null && !verifyOtpDto.getEmailId().isBlank()) usersEntity.setEmail(verifyOtpDto.getEmailId());
        if (verifyOtpDto.getCountryCode() != null && !verifyOtpDto.getCountryCode().isBlank()) usersEntity.setCountryCode(verifyOtpDto.getCountryCode());
        if (verifyOtpDto.getMobileNumber() != null && !verifyOtpDto.getMobileNumber().isBlank()) usersEntity.setMobileNumber(verifyOtpDto.getMobileNumber());
        usersEntity.setUserRole(UserRole.NORMAL_USER);
        usersEntity.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(usersEntity);
    }
}
