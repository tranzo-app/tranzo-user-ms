package com.tranzo.tranzo_user_ms.trip.events;

import lombok.*;

import java.util.UUID;

@Builder
@AllArgsConstructor
@Setter
@Getter
@Data
public class TripPublishedEventPayloadDto {

    private String eventType;
    private UUID tripId;
    private UUID userId;
    /** Optional; used when eventType is PARTICIPANT_JOINED so chat can add user to trip's conversation. */
    private UUID conversationId;
}
