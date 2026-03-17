package com.tranzo.tranzo_user_ms.splitwise.dto.response;

import com.tranzo.tranzo_user_ms.splitwise.entity.Activity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for activity response data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResponse {

    private UUID id;
    private GroupResponse group;
    private UserResponse user;
    private Activity.ActivityType activityType;
    private String description;
    /** Related entity ID (expense id, settlement id, group id as number, or user UUID as string). */
    private UUID relatedId;
    private String relatedType;
    private String oldValue;
    private String newValue;
    private LocalDateTime createdAt;
}
