package com.tranzo.tranzo_user_ms.user.configuration;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "twilio")
@Data
public class TwilioConfig {

    @Value("${twilio.enabled}")
    private boolean enabled;

    @Value("${twilio.username}")
    private String accountSSD;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String phoneNumber;

    @Value("${twilio.sms-template}")
    private String smsTemplate;

    @PostConstruct
    public void init() {
        Twilio.init(this.accountSSD, this.authToken);
    }
}
