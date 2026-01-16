package com.tranzo.tranzo_user_ms.trip.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripPolicyDto {
    @Size(max = 1000, message = "Cancellation policy cannot exceed 1000 characters")
    private String cancellationPolicy;

    @Size(max = 1000, message = "Refund policy cannot exceed 1000 characters")
    private String refundPolicy;
}
