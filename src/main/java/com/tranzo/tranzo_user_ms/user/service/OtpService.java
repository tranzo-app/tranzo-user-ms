package com.tranzo.tranzo_user_ms.user.service;

import com.tranzo.tranzo_user_ms.commons.service.JwtService;
import com.tranzo.tranzo_user_ms.user.dto.RequestOtpDto;
import com.tranzo.tranzo_user_ms.user.dto.VerifyOtpDto;
import com.tranzo.tranzo_user_ms.user.dto.VerifyOtpResponseDto;
import com.tranzo.tranzo_user_ms.user.enums.AccountStatus;
import com.tranzo.tranzo_user_ms.user.enums.UserRole;
import com.tranzo.tranzo_user_ms.commons.exception.OtpException;
import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import com.tranzo.tranzo_user_ms.user.repository.UserRepository;
import com.tranzo.tranzo_user_ms.user.utility.OtpUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {
    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private final OtpUtility otpUtility;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public void sendOtp(RequestOtpDto requestOtpDto)
    {
        String identifier = otpUtility.resolveIdentifier(requestOtpDto);
        String otp = otpUtility.generateOtp();

        redisTemplate.opsForValue().set(buildKey(identifier), otp, OTP_TTL);

        // TODO : Integrate SMS / Email Provider
        log.info("OTP for {} is {}", identifier, otp);
    }

    public VerifyOtpResponseDto verifyOtp(VerifyOtpDto verifyOtpDto)
    {
        String identifier = otpUtility.resolveIdentifier(verifyOtpDto);
        String key = buildKey(identifier);
        Object cachedValue = redisTemplate.opsForValue().get(buildKey(identifier));
        if (cachedValue == null) {
            throw new OtpException("OTP expired or not found");
        }
        String cachedOtp = cachedValue.toString();
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
            redisTemplate.delete(buildKey(identifier));
            return VerifyOtpResponseDto.builder()
                    .userExists(userExists)
                    .registrationToken(registrationToken)
                    .build();
        }
        redisTemplate.delete(buildKey(identifier));
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
