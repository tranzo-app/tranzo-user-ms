package com.tranzo.tranzo_user_ms.trip.controller;

import com.github.benmanes.caffeine.cache.Cache;
import com.tranzo.tranzo_user_ms.commons.exception.RateLimitExceededException;
import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import com.tranzo.tranzo_user_ms.trip.dto.GenerateItineraryRequest;
import com.tranzo.tranzo_user_ms.trip.dto.GenerateItineraryResponse;
import com.tranzo.tranzo_user_ms.trip.service.AiItineraryService;
import jakarta.security.auth.message.AuthException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Slf4j
public class AiItineraryController {

    private static final int MAX_REQUESTS_PER_HOUR = 5;

    private final AiItineraryService service;
    private final Cache<String, Integer> aiItineraryRateLimitCache;

    @PostMapping("/itinerary")
    public ResponseEntity<GenerateItineraryResponse> generateItinerary(
            @Valid @RequestBody GenerateItineraryRequest request) throws AuthException {

        UUID userId = SecurityUtils.getCurrentUserUuid();

        String key = "AIRL:" + userId;
        Integer count = aiItineraryRateLimitCache.getIfPresent(key);
        if (count != null && count >= MAX_REQUESTS_PER_HOUR) {
            log.warn("AI itinerary rate limit exceeded | userId={}", userId);
            throw new RateLimitExceededException("Itinerary generation limit reached. Try again after an hour.");
        }
        aiItineraryRateLimitCache.put(key, count == null ? 1 : count + 1);

        log.info("Incoming request | API=/api/v1/ai/itinerary | method=POST | userId={} | destination={}", userId, request.destination());
        GenerateItineraryResponse response = service.generate(request);
        log.info("Itinerary generated | userId={} | destination={} | days={} | status=SUCCESS", userId, request.destination(), request.numberOfDays());

        return ResponseEntity.ok(response);
    }
}
