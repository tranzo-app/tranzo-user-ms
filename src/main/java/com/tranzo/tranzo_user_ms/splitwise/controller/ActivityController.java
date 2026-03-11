package com.tranzo.tranzo_user_ms.splitwise.controller;

import com.tranzo.tranzo_user_ms.splitwise.dto.response.ActivityResponse;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.GroupResponse;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.UserResponse;
import com.tranzo.tranzo_user_ms.splitwise.entity.Activity;
import com.tranzo.tranzo_user_ms.splitwise.service.ActivityService;
import com.tranzo.tranzo_user_ms.user.repository.UserRepository;
import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for managing activities.
 */
@Slf4j
@RestController
@RequestMapping("/api/splitwise/activities")
@RequiredArgsConstructor
@Validated
public class ActivityController {

    private final ActivityService activityService;
    private final UserRepository userRepository;

    /**
     * Gets activities for a specific group.
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<ActivityResponse>> getGroupActivities(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        
        log.debug("Received request to get activities for group: {} with limit {} and offset {}", 
                 groupId, limit, offset);
        
        List<Activity> activities = activityService.getGroupActivities(groupId, limit, offset);
        List<ActivityResponse> response = activities.stream()
                .map(this::convertToActivityResponse)
                .collect(Collectors.toList());
        
        log.debug("Retrieved {} activities for group {}", response.size(), groupId);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets activities for the current user.
     */
    @GetMapping("/my-activities")
    public ResponseEntity<List<ActivityResponse>> getUserActivities(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        
        log.debug("Received request to get activities for current user with limit {} and offset {}", 
                 limit, offset);
        
        UUID currentUserId;
        try {
            currentUserId = SecurityUtils.getCurrentUserUuid();
        } catch (AuthException e) {
            log.error("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(401).build();
        }
        List<Activity> activities = activityService.getUserActivities(currentUserId, limit, offset);
        List<ActivityResponse> response = activities.stream()
                .map(this::convertToActivityResponse)
                .collect(Collectors.toList());
        
        log.debug("Retrieved {} activities for user {}", response.size(), currentUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * Converts Activity entity to ActivityResponse DTO.
     */
    private ActivityResponse convertToActivityResponse(Activity activity) {
        return ActivityResponse.builder()
                .id(activity.getId())
                .group(activity.getGroup() != null ? 
                        GroupResponse.builder()
                                .id(activity.getGroup().getId())
                                .name(activity.getGroup().getName())
                                .build() : null)
                .user(getUserResponse(activity.getUserId()))
                .activityType(activity.getActivityType())
                .description(activity.getDescription())
                .relatedId(activity.getRelatedId())
                .relatedType(activity.getRelatedType())
                .oldValue(activity.getOldValue())
                .newValue(activity.getNewValue())
                .createdAt(activity.getCreatedAt())
                .build();
    }

    /**
     * Gets UserResponse from user UUID.
     */
    private UserResponse getUserResponse(UUID userId) {
        if (userId == null) return null;
        
        UsersEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;
        
        return UserResponse.builder()
                .userUuid(user.getUserUuid())
                .name(user.getUserProfileEntity() != null ?
                        user.getUserProfileEntity().getFirstName() + " " + 
                        user.getUserProfileEntity().getLastName() : "")
                .email(user.getEmail())
                .build();
    }
}
