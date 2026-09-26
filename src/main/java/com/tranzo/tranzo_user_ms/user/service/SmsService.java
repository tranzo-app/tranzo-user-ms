package com.tranzo.tranzo_user_ms.user.service;

import com.tranzo.tranzo_user_ms.user.configuration.TwilioConfig;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.twilio.rest.api.v2010.account.Message;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private final TwilioConfig twilioConfig;

    @Value("${spring.profiles.active}")
    private String env;

    public void sendOtp(String phone, String otp) {
        if (!twilioConfig.isEnabled()) {
            log.info("Twilio is disabled | phone={} | otp={}", phone, otp);
            return;
        }

        String message = String.format(twilioConfig.getSmsTemplate(), otp, 5);

        if ("dev".equals(env) || "test".equals(env)) {
            log.info("Dev/Test mode: SMS not sent | phone={} | otp={}", phone, otp);
            return;
        }

        try {
            Message.creator(
                    new PhoneNumber(phone),
                    new PhoneNumber(twilioConfig.getPhoneNumber()),
                    message
            ).create();

            log.info("SMS sent via Twilio | phone={}", phone);
        } catch (Exception e) {
            log.error("Failed to send OTP SMS via Twilio | phone={} | error={}", phone, e.getMessage());
            throw new RuntimeException("Failed to send OTP SMS: " + e.getMessage(), e);
        }
    }
}
