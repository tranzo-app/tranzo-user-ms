//package com.tranzo.tranzo_user_ms.user.service;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import software.amazon.awssdk.services.sns.SnsClient;
//import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
//import software.amazon.awssdk.services.sns.model.PublishRequest;
//import software.amazon.awssdk.services.sns.model.PublishResponse;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//public class SmsService {
//
//    private final SnsClient snsClient;
//
//    public void sendOtp(String phone, String otp) {
//        String message = "Your Tranzo OTP is " + otp + ". Valid for 5 minutes.";
//
//        Map<String, MessageAttributeValue> attributes = new HashMap<>();
//
//        // Transactional SMS
//        attributes.put("AWS.SNS.SMS.SMSType",
//                MessageAttributeValue.builder()
//                        .dataType("String")
//                        .stringValue("Transactional")
//                        .build());
//
//        // SenderID (optional for dev, required for production)
//        String senderId = "TRNZO"; // your registered 6-char SenderID
////        if (!devMode)
////        { // in dev you may skip senderID
////            attributes.put("AWS.SNS.SMS.SenderID",
////                    MessageAttributeValue.builder()
////                            .dataType("String")
////                            .stringValue(senderId)
////                            .build());
////        }
//
//        // Max price (optional)
//        attributes.put("AWS.SNS.SMS.MaxPrice",
//                MessageAttributeValue.builder()
//                        .dataType("String")
//                        .stringValue("0.50") // ₹0.50 max per SMS
//                        .build());
//
//        PublishRequest request = PublishRequest.builder()
//                .phoneNumber(phone)
//                .message(message)
//                .messageAttributes(attributes)
//                .build();
//
//        try {
//            PublishResponse result = snsClient.publish(request);
//            System.out.println("SMS sent. MessageId: " + result.messageId());
//        } catch (Exception e) {
////            if (devMode) {
////                System.out.println("Dev mode: SMS not sent, but continuing. " + e.getMessage());
////            } else {
////                throw new RuntimeException("Failed to send OTP SMS: " + e.getMessage(), e);
////            }
//            System.out.println("Dev mode: SMS not sent, but continuing. " + e.getMessage());
//        }
//    }
//}
