package com.tranzo.tranzo_user_ms.trip.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class TripGroupChatCreatedEventDto {
    private String eventType;       // e.g. "TRIP_GROUP_CHAT_CREATED"
    private UUID tripId;
    private UUID conversationId;
}

