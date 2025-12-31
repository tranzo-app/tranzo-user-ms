package com.tranzo.tranzo_user_ms.trip.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripMetaDataDto {
    private Map<String, Object> tripSummary;
    private Map<String, Object> whatsIncluded;
    private Map<String, Object> whatsExcluded;
}
