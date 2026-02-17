package com.tranzo.tranzo_user_ms.trip.kafka;

import lombok.*;

import java.time.LocalDate;
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

}
