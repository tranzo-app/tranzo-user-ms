package com.tranzo.tranzo_user_ms.trip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemoveParticipantRequestDto {
    @NotBlank(message = "Removal reason can't be empty")
    @Size(min = 10)
    private String removalReason;
}