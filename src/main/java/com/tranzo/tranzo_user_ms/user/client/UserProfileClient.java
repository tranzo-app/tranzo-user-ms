package com.tranzo.tranzo_user_ms.user.client;

import com.tranzo.tranzo_user_ms.user.dto.UserNameDto;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Client contract for fetching user display names by user IDs.
 * In monolith: implemented by {@link UserProfileClientLocalImpl} (in-process).
 * In microservices: implement with Feign calling the User service API.
 */
public interface UserProfileClient {

    /**
     * Fetches display names for the given user IDs.
     * In microservices, this will be a Feign call to the User service.
     *
     * @param userIds collection of user UUIDs (may be empty)
     * @return map of userId to UserNameDto; missing profiles are omitted from the map
     */
    Map<UUID, UserNameDto> getNamesByUserIds(Collection<UUID> userIds);
}
