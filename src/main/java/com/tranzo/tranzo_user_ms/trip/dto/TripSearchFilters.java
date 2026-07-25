package com.tranzo.tranzo_user_ms.trip.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripSearchFilters {
    private BudgetRange estimatedBudget;
    private List<String> categories;
    private List<String> locations;
    private List<DurationRange> durations;
}
