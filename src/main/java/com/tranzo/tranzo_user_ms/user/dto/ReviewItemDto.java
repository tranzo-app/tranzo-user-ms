package com.tranzo.tranzo_user_ms.user.dto;

import com.tranzo.tranzo_user_ms.user.enums.VibeTag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewItemDto {

    public enum ReviewSource {
        HOST,
        MEMBER
    }

    private String reviewText;
    private VibeTag vibeTag;
    private ReviewSource source;
    private Double averageRating; // 1-5 for host (avg of 3 dims), or single score for member
    private LocalDateTime createdAt;
}
