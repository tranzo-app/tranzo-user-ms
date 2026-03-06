package com.tranzo.tranzo_user_ms.splitwise.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for user response data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID userUuid;
    private String name;
    private String email;
    private String mobileNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
