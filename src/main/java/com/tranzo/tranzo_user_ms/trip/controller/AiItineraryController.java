package com.tranzo.tranzo_user_ms.trip.controller;

import com.tranzo.tranzo_user_ms.trip.dto.GenerateItineraryRequest;
import com.tranzo.tranzo_user_ms.trip.dto.GenerateItineraryResponse;
import com.tranzo.tranzo_user_ms.trip.service.AiItineraryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiItineraryController {

    private final AiItineraryService service;

    @PostMapping("/itinerary")
    public ResponseEntity<GenerateItineraryResponse>
    generateItinerary(
            @Valid
            @RequestBody GenerateItineraryRequest request) {

        return ResponseEntity.ok(
                service.generate(request));
    }
}
