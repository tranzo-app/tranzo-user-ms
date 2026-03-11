package com.tranzo.tranzo_user_ms.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitHostRatingRequest {

    @NotNull
    @Min(1)
    @Max(5)
    private Integer coordinationRating;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer communicationRating;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer leadershipRating;

    private String reviewText;
}
