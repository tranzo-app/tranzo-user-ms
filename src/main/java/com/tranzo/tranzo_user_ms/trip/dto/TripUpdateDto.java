package com.tranzo.tranzo_user_ms.trip.dto;

import com.tranzo.tranzo_user_ms.trip.enums.JoinPolicy;
import com.tranzo.tranzo_user_ms.trip.enums.VisibilityStatus;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripUpdateDto {
    @Size(max = 200, message = "Trip title must not exceed 200 characters")
    private String tripTitle;

    @Size(max = 1000, message = "Trip description must not exceed 1000 characters")
    private String tripDescription;

    @Size(max = 200, message = "Trip destination must not exceed 200 characters")
    private String tripDestination;

    private Double latitude;

    private Double longitude;

    private LocalDate tripStartDate;

    private LocalDate tripEndDate;

    @Positive(message = "Estimated budget must be a positive value")
    private Double estimatedBudget;

    @Positive(message = "Max participants must be greater than zero")
    private Integer maxParticipants;

    private Boolean isFull;

    private String tripFullReason;

    private JoinPolicy joinPolicy;

    private VisibilityStatus visibilityStatus;

    private TripPolicyDto tripPolicy;

    private TripMetaDataDto tripMetaData;

    private List<TripTagDto> tripTags = new ArrayList<>();

    private List<TripItineraryDto> tripItineraries = new ArrayList<>();

    private List<String> imageUrls;
}
