package com.tranzo.tranzo_user_ms.trip.dto;

import com.tranzo.tranzo_user_ms.trip.utility.NotEmptyIfPresent;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TripItineraryRequestDto {

    @NotNull
    @Min(1)
    private Integer dayNumber;

    @Size(max = 255)
    private String title;

    private String description;

    /*@NotEmptyIfPresent
    private Map<String, Object> activities;*/

    @NotEmptyIfPresent
    private Map<String, Object> meals;

    @NotEmptyIfPresent
    private Map<String, Object> stay;
}
