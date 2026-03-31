package com.tranzo.tranzo_user_ms.user.client;

import com.tranzo.tranzo_user_ms.media.service.S3MediaService;
import com.tranzo.tranzo_user_ms.user.dto.UserNameDto;
import com.tranzo.tranzo_user_ms.user.model.UserProfileEntity;
import com.tranzo.tranzo_user_ms.user.repository.UserProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class UserProfileClientLocalImpl implements UserProfileClient {

    private final UserProfileRepository userProfileRepository;
    private final S3MediaService s3MediaService;

    public UserProfileClientLocalImpl(UserProfileRepository userProfileRepository, S3MediaService s3MediaService) {
        this.userProfileRepository = userProfileRepository;
        this.s3MediaService = s3MediaService;
    }

    @Override
    public Map<UUID, UserNameDto> getNamesByUserIds(Collection<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<UserProfileEntity> profiles = userProfileRepository.findByUser_UserUuidIn(userIds);
        return profiles.stream()
                .collect(Collectors.toMap(
                        p -> p.getUser().getUserUuid(),
                        p -> UserNameDto.builder()
                                .userId(p.getUser().getUserUuid())
                                .firstName(p.getFirstName())
                                .middleName(p.getMiddleName())
                                .lastName(p.getLastName())
                                .dob(p.getDob())
                                .bio(p.getBio())
                                .location(p.getLocation())
                                .profilePictureUrl(p.getProfilePictureUrl() != null ? resolveProfilePictureUrl(p.getProfilePictureUrl()) : null)
                                .build(),
                        (a, b) -> a
                ));
    }

    private String resolveProfilePictureUrl(String urlOrKey) {
        if (urlOrKey == null || urlOrKey.isBlank()) {
            return urlOrKey;
        }
        if (!urlOrKey.startsWith("uploads/")) {
            return urlOrKey;
        }
        try {
            return s3MediaService.getPresignedUrl(urlOrKey, null).getUrl();
        } catch (Exception e) {
            log.debug("Could not resolve S3 key to presigned URL (S3 may not be configured): {}", e.getMessage());
            return urlOrKey;
        }
    }
}
