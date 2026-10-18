package com.tranzo.tranzo_user_ms.trip.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AiChatConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "ai.provider", havingValue = "openai")
    public ChatClient openAiChatClient(
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.model}") String model) {

        OpenAiApi api = OpenAiApi.builder()
                .apiKey(apiKey)
                .build();

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder().model(model).build())
                .build();

        return ChatClient.create(chatModel);
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "ai.provider", havingValue = "gemini")
    public ChatClient geminiChatClient(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model}") String model) {

        // Google AI Studio exposes an OpenAI-compatible endpoint
        OpenAiApi api = OpenAiApi.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta/openai/")
                .apiKey(apiKey)
                .build();

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder().model(model).build())
                .build();

        return ChatClient.create(chatModel);
    }
}
