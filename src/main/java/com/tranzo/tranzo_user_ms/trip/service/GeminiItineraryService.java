package com.tranzo.tranzo_user_ms.trip.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.GenerateContentConfig;
import com.tranzo.tranzo_user_ms.trip.dto.GenerateItineraryRequest;
import com.tranzo.tranzo_user_ms.trip.dto.GenerateItineraryResponse;
import com.tranzo.tranzo_user_ms.trip.prompt.PromptBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        name = "ai.provider",
        havingValue = "gemini"
)
@RequiredArgsConstructor
public class GeminiItineraryService
        implements AiItineraryService {

    private final Client client;
    private final PromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    @Value("${gemini.model}")
    private String model;

    @Override
    public GenerateItineraryResponse generate(
            GenerateItineraryRequest request) {

        String prompt = promptBuilder.getSystemPrompt()
                + "\n\n"
                + promptBuilder.buildUserPrompt(request);

        GenerateContentResponse response =
                client.models.generateContent(
                        model,
                        prompt,
                        GenerateContentConfig.builder()
                                .responseMimeType("application/json")
                                .build()
                );

        String json = response.text();

        try {
            return objectMapper.readValue(
                    json,
                    GenerateItineraryResponse.class);

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to parse Gemini response",
                    ex);
        }
    }
}