package com.tranzo.tranzo_user_ms.trip.dto;

import com.tranzo.tranzo_user_ms.trip.enums.ImageSource;
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
public class TripImageDto {
    private UUID imageId;
    private String imageUrl;
    private String destination;
    private ImageSource source;
    private Integer usageCount;
    private LocalDateTime createdAt;
}
