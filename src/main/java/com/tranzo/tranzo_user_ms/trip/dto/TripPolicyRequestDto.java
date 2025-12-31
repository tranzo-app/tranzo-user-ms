package com.tranzo.tranzo_user_ms.trip.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TripPolicyRequestDto {

    @Size(max = 500)
    private String cancellationPolicy;

    @Size(max = 500)
    private String refundPolicy;
}
