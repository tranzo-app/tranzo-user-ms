package com.tranzo.tranzo_user_ms.trip.service;

import com.tranzo.tranzo_user_ms.trip.dto.GenerateItineraryRequest;
import com.tranzo.tranzo_user_ms.trip.dto.GenerateItineraryResponse;

public interface AiItineraryService {
    GenerateItineraryResponse generate(
            GenerateItineraryRequest request);
}
