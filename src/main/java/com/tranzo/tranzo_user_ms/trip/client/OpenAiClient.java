package com.tranzo.tranzo_user_ms.trip.client;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.tranzo.tranzo_user_ms.trip.config.OpenAiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OpenAiClient {

    private final OpenAiProperties properties;

    public OpenAIClient createClient() {

        return OpenAIOkHttpClient.builder()
                .apiKey(properties.getApiKey())
                .build();
    }
}
