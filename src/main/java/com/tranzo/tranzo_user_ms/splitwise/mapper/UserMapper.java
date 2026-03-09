package com.tranzo.tranzo_user_ms.splitwise.mapper;

import com.tranzo.tranzo_user_ms.splitwise.dto.response.UserResponse;
import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import com.tranzo.tranzo_user_ms.user.model.UserProfileEntity;

/**
 * Utility class for mapping between UsersEntity and UserResponse.
 */
public class UserMapper {

    /**
     * Converts UsersEntity to UserResponse.
     * If userProfile is available, uses the name from profile, otherwise uses email.
     */
    public static UserResponse toResponse(UsersEntity user, UserProfileEntity userProfile) {
        if (user == null) {
            return null;
        }

        String name = user.getEmail(); // fallback to email
        if (userProfile != null) {
            StringBuilder fullName = new StringBuilder();
            if (userProfile.getFirstName() != null) {
                fullName.append(userProfile.getFirstName());
            }
            if (userProfile.getMiddleName() != null && !userProfile.getMiddleName().trim().isEmpty()) {
                fullName.append(" ").append(userProfile.getMiddleName());
            }
            if (userProfile.getLastName() != null) {
                fullName.append(" ").append(userProfile.getLastName());
            }
            name = fullName.toString().trim();
            if (name.isEmpty()) {
                name = user.getEmail(); // fallback if profile name is empty
            }
        }

        return UserResponse.builder()
                .userUuid(user.getUserUuid())
                .name(name)
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Converts UsersEntity to UserResponse (without profile).
     * Uses email as name fallback.
     */
    public static UserResponse toResponse(UsersEntity user) {
        return toResponse(user, null);
    }
}
