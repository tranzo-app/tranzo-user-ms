package com.tranzo.tranzo_user_ms.trip.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Map;



// think on thse lines of trip summary, whats included, whats excluded, essentials etc.

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TripMetaDataRequestDto {

    @Size(max = 10)
    private Map<String, Object> tripSummary;

    /*@Size(max = 20)
    private Map<String, Object> whatsIncluded;

    @Size(max = 20)
    private Map<String, Object> whatsExcluded;*/

    @Size(max = 20)
    private Map<String, Object> essentials;
}
