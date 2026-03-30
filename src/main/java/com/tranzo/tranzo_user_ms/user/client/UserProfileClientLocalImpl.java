package com.tranzo.tranzo_user_ms.user.client;

import com.tranzo.tranzo_user_ms.user.dto.UserNameDto;
import com.tranzo.tranzo_user_ms.user.model.UserProfileEntity;
import com.tranzo.tranzo_user_ms.user.repository.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class UserProfileClientLocalImpl implements UserProfileClient {

    private final UserProfileRepository userProfileRepository;

    public UserProfileClientLocalImpl(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
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
                                .profilePictureUrl(p.getProfilePictureUrl())
                                .build(),
                        (a, b) -> a
                ));
    }
}
