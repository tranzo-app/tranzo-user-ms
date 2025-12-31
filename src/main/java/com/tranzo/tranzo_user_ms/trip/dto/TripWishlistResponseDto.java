package com.tranzo.tranzo_user_ms.trip.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tranzo.tranzo_user_ms.trip.enums.TripStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripWishlistResponseDto {
    private UUID tripId;
    private String tripTitle;
    private String destination;
    private TripStatus tripStatus;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
