package com.tranzo.tranzo_user_ms.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private final SnsClient snsClient;

    @Value("${spring.profiles.active}")
    private String env;

    public void sendOtp(String phone, String otp) {
        String message = "Your Tranzo OTP is " + otp + ". Valid for 5 minutes.";

        Map<String, MessageAttributeValue> attributes = new HashMap<>();

        // Transactional SMS
        attributes.put("AWS.SNS.SMS.SMSType",
                MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue("Transactional")
                        .build());

        // SenderID (optional for dev, required for production)
        String senderId = "TRNZO"; // your registered 6-char SenderID
        if (!"dev".equals(env)) { // in dev you may skip senderID
            attributes.put("AWS.SNS.SMS.SenderID",
                    MessageAttributeValue.builder()
                            .dataType("String")
                            .stringValue(senderId)
                            .build());
        }

        // Max price (optional)
        attributes.put("AWS.SNS.SMS.MaxPrice",
                MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue("0.50") // ₹0.50 max per SMS
                        .build());

        PublishRequest request = PublishRequest.builder()
                .phoneNumber(phone)
                .message(message)
                .messageAttributes(attributes)
                .build();

        if ("dev".equals(env) || "test".equals(env)) {
            log.info("Dev/Test mode: SMS not sent | phone={} | otp={}", phone, otp);
            return;
        }

        try {
            PublishResponse result = snsClient.publish(request);
            log.info("SMS sent via AWS SNS | phone={} | messageId={}", phone, result.messageId());
        } catch (Exception e) {
            log.error("Failed to send OTP SMS via AWS SNS | phone={} | error={}", phone, e.getMessage());
            throw new RuntimeException("Failed to send OTP SMS: " + e.getMessage(), e);
        }
    }
}
