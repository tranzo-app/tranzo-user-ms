package com.tranzo.tranzo_user_ms.trip.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripMetaDataViewDto {
    private UUID metadataId;
    private Map<String, Object> tripSummary;
    private Map<String, Object> whatsIncluded;
    private Map<String, Object> whatsExcluded;
}
