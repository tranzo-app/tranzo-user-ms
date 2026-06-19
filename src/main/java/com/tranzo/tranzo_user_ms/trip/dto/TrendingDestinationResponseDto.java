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
public class TrendingDestinationResponseDto {
    private String destination;
    private long tripsCount;
    private String coverImageUrl;
    private List<FilterCriteria> filters;
}
