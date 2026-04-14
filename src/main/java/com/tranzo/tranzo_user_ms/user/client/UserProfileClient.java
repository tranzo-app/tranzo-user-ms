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
public interface UserProfileClient {
    Map<UUID, UserNameDto> getNamesByUserIds(List<UUID> userIds);
}
