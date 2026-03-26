package com.tranzo.tranzo_user_ms.config;

import com.tranzo.tranzo_user_ms.user.configuration.TwilioConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public TwilioConfig testTwilioConfig() {
        TwilioConfig config = new TwilioConfig();
        // Set disabled state for tests
        config.setEnabled(false);
        config.setAccountSSD("test-account");
        config.setAuthToken("test-token");
        config.setPhoneNumber("test-phone");
        config.setSmsTemplate("Test template: %s");
        return config;
    }
}
