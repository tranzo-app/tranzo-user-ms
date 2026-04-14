package com.tranzo.tranzo_user_ms.media.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PresignedUrlResponseDto {
    private String url;
    /** Expiry duration in minutes. */
    private int expiryMinutes;
}
