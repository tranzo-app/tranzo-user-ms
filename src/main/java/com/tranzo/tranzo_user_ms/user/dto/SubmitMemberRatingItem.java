package com.tranzo.tranzo_user_ms.user.dto;

import com.tranzo.tranzo_user_ms.user.enums.VibeTag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitMemberRatingItem {

    @NotNull
    private UUID ratedUserId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer ratingScore;

    private VibeTag vibeTag;

    private String reviewText;
}
