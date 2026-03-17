package com.tranzo.tranzo_user_ms.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final SesClient sesClient;

    @Value("${ses.from-email}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp) {
        String subject = "Tranzo OTP Verification";
        String body = """
                Your OTP is %s
                
                This OTP is valid for 5 minutes.
                Do not share this code with anyone.
                """.formatted(otp);
        SendEmailRequest request =
                SendEmailRequest.builder()
                        .source(fromEmail)
                        .destination(
                                Destination.builder()
                                        .toAddresses(toEmail)
                                        .build()
                        )
                        .message(
                                Message.builder()
                                        .subject(
                                                Content.builder()
                                                        .data(subject)
                                                        .build()
                                        )
                                        .body(
                                                Body.builder()
                                                        .text(
                                                                Content.builder()
                                                                        .data(body)
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .build()
                        )
                        .build();
        sesClient.sendEmail(request);
    }
}
