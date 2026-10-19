package com.tranzo.tranzo_user_ms.trip.config;

import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Slf4j
@Configuration
public class AiChatConfig {

    // Uses responseTimeout only — no ReadTimeoutHandler, which fires on zero-byte
    // inactivity and conflicts with responseTimeout during AI model "thinking" time.
    private RestClient.Builder restClientWithTimeout(int seconds) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10_000)
                .responseTimeout(Duration.ofSeconds(seconds));
        ReactorClientHttpRequestFactory factory = new ReactorClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(seconds));
        return RestClient.builder().requestFactory(factory);
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "ai.provider", havingValue = "openai")
    public ChatClient openAiChatClient(
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.model}") String model) {

        Assert.hasText(apiKey,
            "OPENAI_API_KEY is not set — add it to IntelliJ Run Config > Environment variables");

        log.info("AI config | provider=openai | model={}", model);

        OpenAiApi api = OpenAiApi.builder()
                .apiKey(apiKey)
                .restClientBuilder(restClientWithTimeout(60))
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

        Assert.hasText(apiKey,
            "GEMINI_API_KEY is not set — add it to IntelliJ Run Config > Environment variables");

        log.info("AI config | provider=gemini | model={} | responseTimeout=300s", model);

        // Google AI Studio's OpenAI-compatible endpoint; completionsPath overrides
        // Spring AI's default "/v1/chat/completions" so the final URL resolves correctly
        OpenAiApi api = OpenAiApi.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta/openai")
                .apiKey(apiKey)
                .completionsPath("/chat/completions")
                .restClientBuilder(restClientWithTimeout(300))
                .build();

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder().model(model).build())
                .build();

        return ChatClient.create(chatModel);
    }
}
