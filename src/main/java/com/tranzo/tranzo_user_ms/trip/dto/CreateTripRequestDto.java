package com.tranzo.tranzo_user_ms.trip.dto;

import com.tranzo.tranzo_user_ms.trip.enums.JoinPolicy;
import com.tranzo.tranzo_user_ms.trip.enums.VisibilityStatus;
import com.tranzo.tranzo_user_ms.trip.utility.TripDateRangeValid;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TripDateRangeValid
public class CreateTripRequestDto {

    @NotBlank
    @Size(max = 255)
    private String tripTitle;

    @NotBlank
    @Size(max = 500)
    private String tripDescription;

    @NotBlank
    private String tripDestination;

    @NotNull
    @FutureOrPresent
    private LocalDate tripStartDate;

    @NotNull
    private LocalDate tripEndDate;

    @NotNull
    @Positive
    private Double estimatedBudget;

    @NotNull
    @Min(1)
    private Integer maxParticipants;

    @NotNull
    private JoinPolicy joinPolicy;

    @NotNull
    private VisibilityStatus visibilityStatus;

    @Valid
    private TripPolicyRequestDto tripPolicy;

/*
    @Valid
    private TripMetaDataRequestDto tripMetaData;*/

    @Valid
    @NotEmpty
    private List<TripItineraryRequestDto> itineraries;

    private Set<String> tags;
}
