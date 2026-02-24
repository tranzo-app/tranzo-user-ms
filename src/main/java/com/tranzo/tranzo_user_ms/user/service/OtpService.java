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

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {
    private final OtpUtility otpUtility;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final SessionService sessionService;
    private final Cache<String, String> otpCache;

    public void sendOtp(RequestOtpDto requestOtpDto)
    {
        String identifier = otpUtility.resolveIdentifier(requestOtpDto);
        String otp = otpUtility.generateOtp();

        otpCache.put(buildKey(identifier), otp);

        // TODO : Integrate SMS / Email Provider
        log.info("OTP for {} is {}", identifier, otp);
    }

    public VerifyOtpResponseDto verifyOtp(VerifyOtpDto verifyOtpDto, HttpServletResponse response)
    {
        String identifier = otpUtility.resolveIdentifier(verifyOtpDto);
        String key = buildKey(identifier);
        String cachedOtp = otpCache.getIfPresent(key);
        if (cachedOtp == null) {
            throw new OtpException("OTP expired or not found");
        }
        log.info("Cached OTP for {} from map is {}", identifier, cachedOtp);
        if (!verifyOtpDto.getOtp().equals(cachedOtp))
        {
            throw new OtpException("Invalid OTP");
        }
        Optional<UsersEntity> user = findUserByIdentifier(verifyOtpDto);
        boolean userExists = user.isPresent();
        if (!userExists)
        {
            createNewUser(verifyOtpDto);
            String registrationToken = jwtService.generateRegistrationToken(identifier);
            otpCache.invalidate(buildKey(identifier));
            return VerifyOtpResponseDto.builder()
                    .userExists(userExists)
                    .registrationToken(registrationToken)
                    .build();
        }
        otpCache.invalidate(buildKey(identifier));
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

    private Optional<UsersEntity> findUserByIdentifier(VerifyOtpDto dto) {
        if (dto.getEmailId() != null && !dto.getEmailId().isBlank()) {
            return userRepository.findByEmail(dto.getEmailId().toLowerCase());
        }
        return userRepository.findByMobileNumber(dto.getMobileNumber());
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
