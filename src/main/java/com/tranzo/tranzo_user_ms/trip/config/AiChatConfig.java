package com.tranzo.tranzo_user_ms.trip.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
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
import org.springframework.web.client.RestClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class AiChatConfig {

    private RestClient.Builder restClientWithTimeout(int readTimeoutSeconds) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10_000)
                .responseTimeout(Duration.ofSeconds(readTimeoutSeconds))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(readTimeoutSeconds, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS)));
        return RestClient.builder()
                .requestFactory(new ReactorClientHttpRequestFactory(httpClient));
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "ai.provider", havingValue = "openai")
    public ChatClient openAiChatClient(
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.model}") String model) {

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

        log.info("AI config | provider=gemini | model={} | apiKeySet={}", model, !apiKey.isBlank());

        // Google AI Studio's OpenAI-compatible endpoint; completionsPath overrides
        // Spring AI's default "/v1/chat/completions" so the final URL resolves correctly
        OpenAiApi api = OpenAiApi.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta/openai")
                .apiKey(apiKey)
                .completionsPath("/chat/completions")
                .restClientBuilder(restClientWithTimeout(120))
                .build();

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder().model(model).build())
                .build();

        return ChatClient.create(chatModel);
    }
}
