package com.tranzo.tranzo_user_ms.service;

import com.tranzo.tranzo_user_ms.dto.RequestOtpDto;
import com.tranzo.tranzo_user_ms.dto.VerifyOtpDto;
import com.tranzo.tranzo_user_ms.dto.VerifyOtpResponseDto;
import com.tranzo.tranzo_user_ms.exception.OtpException;
import com.tranzo.tranzo_user_ms.model.UsersEntity;
import com.tranzo.tranzo_user_ms.repository.UserRepository;
import com.tranzo.tranzo_user_ms.utility.OtpUtility;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {
    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private final OtpUtility otpUtility;
    private final StringRedisTemplate stringRedisTemplate;
    private final UserRepository userRepository;

    private Map<String, String> otpMap = new HashMap<>();

    public void sendOtp(RequestOtpDto requestOtpDto)
    {
        String identifier = otpUtility.resolveIdentifier(requestOtpDto);
        String otp = otpUtility.generateOtp();

//        stringRedisTemplate.opsForValue()
//                .set(
//                        buildKey(identifier),
//                        otp,
//                        OTP_TTL
//                );
        otpMap.put(buildKey(identifier), otp);

        // TODO : Integrate SMS / Email Provider
        log.info("OTP for {} is {}", identifier, otp);
    }

    @Transactional()
    public VerifyOtpResponseDto verifyOtp(VerifyOtpDto verifyOtpDto)
    {
        String identifier = otpUtility.resolveIdentifier(verifyOtpDto);
        String key = buildKey(identifier);
        String cachedOtp = otpMap.get(key);
        log.info("Cached OTP for {} from map is {}", identifier, cachedOtp);
        if (cachedOtp == null)
        {
            throw new OtpException("OTP expired or not found");
        }
        else if (!verifyOtpDto.getOtp().equals(cachedOtp))
        {
            throw new OtpException("Invalid OTP");
        }
        otpMap.remove(key);
        Optional<UsersEntity> user = findUserByIdentifier(verifyOtpDto);
        boolean userExists = user.isPresent();
        return VerifyOtpResponseDto.builder()
                .userExists(userExists)
                .build();
//        if (verifyOtpDto.getOtp().equals(cachedOtp))
//        {
//            UsersEntity usersEntity = new UsersEntity();
//            if (verifyOtpDto.getEmailId() != null && !verifyOtpDto.getEmailId().isBlank()) usersEntity.setEmail(verifyOtpDto.getEmailId());
//            if (verifyOtpDto.getMobileNumber() != null && !verifyOtpDto.getMobileNumber().isBlank()) usersEntity.setMobileNumber(verifyOtpDto.getMobileNumber());
//            usersEntity.setUserRole(UserRole.NORMAL_USER);
//            usersEntity.setAccountStatus(AccountStatus.ACTIVE);
//            userRepository.save(usersEntity);
//        }
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
}
