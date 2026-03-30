package com.tranzo.tranzo_user_ms.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for suggested travel pal response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestedTravelPalDto {
    private UUID userId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String bio;
    private String location;
    private LocalDate dob;
    private String profilePictureUrl;
}
