package com.tranzo.tranzo_user_ms.trip.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class TripQnaResponseDto {

    private UUID qnaId;
    private UUID tripId;

    private UUID authorUserId;
    private String question;

    private String answer;
    private UUID answeredBy;
    private LocalDateTime answeredAt;

    private LocalDateTime createdAt;
}

