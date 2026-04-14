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
public class SubmitTripRatingRequest {

    @NotNull
    @Min(1)
    @Max(5)
    private Integer destinationRating;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer itineraryRating;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer overallRating;
}
