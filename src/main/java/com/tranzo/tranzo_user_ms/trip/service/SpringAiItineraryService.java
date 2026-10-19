package com.tranzo.tranzo_user_ms.trip.service;

import com.tranzo.tranzo_user_ms.trip.dto.GenerateItineraryRequest;
import com.tranzo.tranzo_user_ms.trip.dto.GenerateItineraryResponse;
import com.tranzo.tranzo_user_ms.trip.prompt.PromptBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpringAiItineraryService implements AiItineraryService {

    private final ChatClient chatClient;
    private final PromptBuilder promptBuilder;

    @Override
    public GenerateItineraryResponse generate(GenerateItineraryRequest request) {

        GenerateItineraryResponse result = chatClient.prompt()
                .system(promptBuilder.getSystemPrompt())
                .user(promptBuilder.buildUserPrompt(request))
                .call()
                .entity(GenerateItineraryResponse.class);

        if (result == null
                || result.itinerary() == null
                || result.itinerary().size() != request.numberOfDays()) {

            throw new IllegalStateException("AI returned incorrect number of days");
        }

        return result;
    }
}
