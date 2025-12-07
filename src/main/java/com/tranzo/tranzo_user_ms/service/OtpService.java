package com.tranzo.tranzo_user_ms.service;

import com.tranzo.tranzo_user_ms.configuration.TwilioConfig;
import com.tranzo.tranzo_user_ms.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.dto.RequestOtpDto;
import com.tranzo.tranzo_user_ms.dto.VerifyOtpDto;
import com.tranzo.tranzo_user_ms.exception.TooManyRequestsException;
import com.tranzo.tranzo_user_ms.model.UserProfileEntity;
import com.tranzo.tranzo_user_ms.model.UsersEntity;
import com.tranzo.tranzo_user_ms.repository.UserProfileRepository;
import com.tranzo.tranzo_user_ms.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final TwilioConfig twilioConfig;
    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Value("${otp.length}")
    private int otpLength;

    @Value("${otp.ttlSeconds}")
    private int otpTtlSeconds;

    @Value("${otp.throttleSeconds}")
    private int throttleSeconds;

    @Value("${otp.sms-template}")
    private String smsTemplate;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();


//    public ResponseDto generateOtp(RequestOtpDto dto) {
//
//        String mobile = dto.getMobileNumber();
//
//        String cooldownKey = "otp:cooldown:" + mobile;
//        Boolean canRequest = redisTemplate.opsForValue()
//                .setIfAbsent(cooldownKey, "1", Duration.ofSeconds(throttleSeconds));
//
//        if (Boolean.FALSE.equals(canRequest)) {
//            throw new TooManyRequestsException("OTP recently requested. Try again after a few seconds.");
//        }
//
//        String otp = generateNumericOtp(otpLength);
//        String otpKey = "otp:code:" + mobile;
//
//        redisTemplate.opsForValue().set(otpKey, otp, Duration.ofSeconds(otpTtlSeconds));
//        sendSms(mobile, otp);
//
//        log.info("OTP generated and sent to {}", maskNumber(mobile));
//
//        return new ResponseDto(HttpStatus.OK.value(), "OTP sent successfully.");
//    }

    @Transactional
    public boolean verifyOtp(VerifyOtpDto dto) {

        String mobile = dto.getMobileNumber();
        String key = "otp:code:" + mobile;

        String storedOtp = redisTemplate.opsForValue().get(key);
        if (storedOtp == null) {
            return false;
        }

        if (!storedOtp.equals(dto.getOtp())) {
            return false;
        }

        redisTemplate.delete(key);

        UsersEntity user = userRepository.findByMobileNumber(mobile)
                .orElseGet(() ->{
                    UsersEntity newUser = new UsersEntity();
                    newUser.setMobileNumber(mobile);
                    return userRepository.save(newUser);
                });

        if(!userProfileRepository.existsByUser(user)) {
            UserProfileEntity profile = new UserProfileEntity();
            profile.setUser(user);
            userProfileRepository.save(profile);
        }

        log.info("OTP verified successfully for {}", maskNumber(mobile));
        return true;
    }

    private String generateNumericOtp(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(SECURE_RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    private void sendSms(String to, String otp) {
        String message = String.format(smsTemplate, otp, otpTtlSeconds / 60);
        Message.creator(new PhoneNumber(to), new PhoneNumber(twilioConfig.getPhoneNumber()), message).create();
    }

    private String maskNumber(String number) {
        if (number.length() <= 4) return "";
        return "*".repeat(number.length() - 4) + number.substring(number.length() - 4);
    }
}