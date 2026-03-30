package com.tranzo.tranzo_user_ms.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for user display name. Used by UserProfileClient contract.
 * In microservices, this will be the response shape from the User service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNameDto {
    private UUID userId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String profilePictureUrl;
}
