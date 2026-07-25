package com.tranzo.tranzo_user_ms.trip.dto;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripSearchRequest {
    @Valid
    private TripSearchFilters filters;
    
    @Valid
    private TripSearchCriteria search;
    
    private String sortBy;
}
