package com.tranzo.tranzo_user_ms.user.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "aadhar")
@ConditionalOnProperty(name = "aadhar.enabled", havingValue = "true")
@Getter
@Setter
public class AadharProperties {
    private String baseUrl;
    private String xApiKey;
    private String xApiSecret;
    private int timeout;
}
