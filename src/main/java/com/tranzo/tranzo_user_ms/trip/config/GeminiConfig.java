package com.tranzo.tranzo_user_ms.trip.config;

import com.google.genai.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    @Bean
    public Client geminiClient(GeminiProperties properties) {

        return Client.builder()
                .apiKey(properties.getApiKey())
                .build();
    }
}
