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
public class TripTagDto {
    @Size(max = 100, message = "Tag name cannot exceed 100 characters")
    private String tagName;
}
