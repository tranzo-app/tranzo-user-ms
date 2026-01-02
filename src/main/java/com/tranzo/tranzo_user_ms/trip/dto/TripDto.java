package com.tranzo.tranzo_user_ms.trip.dto;

import com.tranzo.tranzo_user_ms.trip.enums.JoinPolicy;
import com.tranzo.tranzo_user_ms.trip.enums.VisibilityStatus;
import com.tranzo.tranzo_user_ms.trip.validation.groups.PublishChecks;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripDto {
    @NotBlank(groups = PublishChecks.class,
            message = "Trip title is required for publishing")
    private String tripDescription;

    @NotBlank(groups = PublishChecks.class,
            message = "Trip description is required for publishing")
    private String tripTitle;

    @NotBlank(groups = PublishChecks.class,
            message = "Trip destination is required for publishing")
    private String tripDestination;

    @NotNull(groups = PublishChecks.class,
            message = "Trip start date is required for publishing")
    private LocalDate tripStartDate;

    @NotNull(groups = PublishChecks.class,
            message = "Trip end date is required for publishing")
    private LocalDate tripEndDate;

    @NotNull(groups = PublishChecks.class,
            message = "Estimated budget is required for publishing")
    @Positive(groups = PublishChecks.class,
            message = "Estimated budget must be a positive value")
    private Double estimatedBudget;

    @NotNull(groups = PublishChecks.class,
            message = "Max participants is required for publishing")
    @Positive(groups = PublishChecks.class,
            message = "Max participants must be greater than zero")
    private Integer maxParticipants;

    private Boolean isFull;

    private String tripFullReason;

    @NotNull(groups = PublishChecks.class,
            message = "Join policy is required for publishing")
    private JoinPolicy joinPolicy;

    @NotNull(groups = PublishChecks.class,
            message = "Visibility status is required for publishing")
    private VisibilityStatus visibilityStatus;

    @Valid
    @NotNull(groups = PublishChecks.class,
            message = "Trip policy is required for publishing")
    private TripPolicyDto tripPolicy;

    @Valid
    private TripMetaDataDto tripMetaData;

    @Valid
    private Set<TripTagDto> tripTags = new HashSet<>();

    @Valid
    @NotEmpty(groups = PublishChecks.class,
            message = "At least one itinerary is required for publishing")
    private Set<TripItineraryDto> tripItineraries = new HashSet<>();
}
