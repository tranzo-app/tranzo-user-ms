package com.tranzo.tranzo_user_ms.trip.dto;

import com.tranzo.tranzo_user_ms.trip.enums.JoinPolicy;
import com.tranzo.tranzo_user_ms.trip.enums.VisibilityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripViewDto {
    private UUID tripId;
    private String tripTitle;
    private String tripDescription;
    private String tripDestination;
    private LocalDate tripStartDate;
    private LocalDate tripEndDate;
    private Double estimatedBudget;
    private Integer maxParticipants;
    private Boolean isFull;
    private String tripFullReason;
    private JoinPolicy joinPolicy;
    private VisibilityStatus visibilityStatus;
    private TripPolicyViewDto tripPolicy;
    private TripMetaDataViewDto tripMetaData;
    private Set<TripTagViewDto> tripTags = new HashSet<>();
    private Set<TripItineraryViewDto> tripItineraries = new HashSet<>();
}
