package com.tranzo.tranzo_user_ms.user.client;

import com.tranzo.tranzo_user_ms.user.dto.UserNameDto;
import com.tranzo.tranzo_user_ms.user.model.UserProfileEntity;
import com.tranzo.tranzo_user_ms.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserProfileClient {

    private final UserProfileRepository userProfileRepository;

    public Map<UUID, UserNameDto> getNamesByUserIds(List<UUID> userIds) {
        return userIds.stream()
                .collect(Collectors.toMap(
                        userId -> userId,
                        userId -> {
                            UserNameDto dto = new UserNameDto();
                            dto.setUserId(userId);
                            userProfileRepository.findAllUserProfileDetailByUserId(userId)
                                    .ifPresent(profile -> {
                                        dto.setFirstName(profile.getFirstName());
                                        dto.setMiddleName(profile.getMiddleName());
                                        dto.setLastName(profile.getLastName());
                                        dto.setProfilePictureUrl(profile.getProfilePictureUrl());
                                        dto.setBio(profile.getBio());
                                        dto.setDob(profile.getDob());
                                        dto.setLocation(profile.getLocation());
                                    });
                            return dto;
                        }
                ));
    }
}
