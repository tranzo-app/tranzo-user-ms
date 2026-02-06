package com.tranzo.tranzo_user_ms.chat.kafka;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class ChatTripPublishedEventDto {
    private String eventType;
    private UUID tripId;
    private UUID hostUserId;
}