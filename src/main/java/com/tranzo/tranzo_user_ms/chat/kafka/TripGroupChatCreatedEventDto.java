package com.tranzo.tranzo_user_ms.chat.kafka;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;


@Data
@Builder
public class TripGroupChatCreatedEventDto {
    private String eventType;       // e.g. "TRIP_GROUP_CHAT_CREATED"
    private UUID tripId;
    private UUID conversationId;
}
