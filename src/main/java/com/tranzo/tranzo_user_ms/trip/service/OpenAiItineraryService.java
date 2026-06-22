package com.tranzo.tranzo_user_ms.trip.service;

import com.openai.client.OpenAIClient;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.StructuredResponse;
import com.openai.models.responses.StructuredResponseCreateParams;
import com.tranzo.tranzo_user_ms.trip.config.OpenAiProperties;
import com.tranzo.tranzo_user_ms.trip.dto.GenerateItineraryRequest;
import com.tranzo.tranzo_user_ms.trip.dto.GenerateItineraryResponse;
import com.tranzo.tranzo_user_ms.trip.prompt.PromptBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        name = "ai.provider",
        havingValue = "openai"
)
@RequiredArgsConstructor
public class OpenAiItineraryService implements AiItineraryService {

    private final OpenAIClient client;
    private final OpenAiProperties properties;
    private final PromptBuilder promptBuilder;

    @Override
    public GenerateItineraryResponse generate(
            GenerateItineraryRequest request) {

        StructuredResponseCreateParams<GenerateItineraryResponse> params =
                ResponseCreateParams.builder()
                        .model(properties.getModel())
                        .instructions(promptBuilder.getSystemPrompt())
                        .input(promptBuilder.buildUserPrompt(request))
                        .text(GenerateItineraryResponse.class)
                        .build();

        StructuredResponse<GenerateItineraryResponse> response = client.responses().create(params);

        GenerateItineraryResponse result = response.output().stream()
                .flatMap(item -> item.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(content -> content.outputText().stream())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No response content from AI"));

        validateResult(request, result);

        return result;
    }

    private void validateResult(
            GenerateItineraryRequest request,
            GenerateItineraryResponse response) {

        if (response.itinerary() == null
                || response.itinerary().size() != request.numberOfDays()) {

            throw new IllegalStateException(
                    "AI returned incorrect number of days"
            );
        }
    }
}