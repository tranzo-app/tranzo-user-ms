package com.tranzo.tranzo_user_ms.user.service;

import com.tranzo.tranzo_user_ms.user.dto.SuggestedTravelPalDto;
import com.tranzo.tranzo_user_ms.user.enums.AccountStatus;
import com.tranzo.tranzo_user_ms.user.enums.TravelPalStatus;
import com.tranzo.tranzo_user_ms.user.model.TravelPalEntity;
import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import com.tranzo.tranzo_user_ms.user.repository.TravelPalRepository;
import com.tranzo.tranzo_user_ms.user.repository.UserProfileRepository;
import com.tranzo.tranzo_user_ms.user.repository.UserRepository;
import com.tranzo.tranzo_user_ms.user.client.UserProfileClient;
import com.tranzo.tranzo_user_ms.user.dto.UserNameDto;
import com.tranzo.tranzo_user_ms.commons.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class TravelPalService {
    private final TravelPalRepository repository;
    private final UserRepository userRepository;
    private final UserProfileClient userProfileClient;

    /* ================= NORMALIZE ================= */

    private UserPair normalize(UUID userA, UUID userB) {
        if (userA.compareTo(userB) < 0) {
            return new UserPair(userA, userB);
        } else {
            return new UserPair(userB, userA);
        }
    }

    /* ================= SEND REQUEST ================= */

    public void sendRequest(UUID requesterId, UUID receiverId) {
        if (requesterId.equals(receiverId)) {
            throw new IllegalArgumentException("Cannot add yourself");
        }
        UserPair pair = normalize(requesterId, receiverId);
        repository.findByUserLowIdAndUserHighId(pair.low(), pair.high())
                .ifPresent(existing -> {
                    if (existing.getStatus() == TravelPalStatus.ACCEPTED) {
                        throw new IllegalStateException("Connection already exists");
                    } else if (existing.getStatus() == TravelPalStatus.PENDING) {
                        throw new ConflictException("Travel pal request already pending");
                    }
                });
        TravelPalEntity entity = new TravelPalEntity();
        entity.setUserLowId(pair.low());
        entity.setUserHighId(pair.high());
        entity.setRequestedBy(requesterId);
        entity.setStatus(TravelPalStatus.PENDING);
        repository.save(entity);
    }

    /* ================= ACCEPT ================= */

    public void acceptRequest(UUID currentUserId, UUID otherUserId) {
        UserPair pair = normalize(currentUserId, otherUserId);
        TravelPalEntity entity = repository
                .findByUserLowIdAndUserHighId(pair.low(), pair.high())
                .orElseThrow(() -> new RuntimeException("Request not found"));
        if (entity.getStatus() != TravelPalStatus.PENDING) {
            throw new IllegalStateException("Invalid request state");
        }
        if (entity.getRequestedBy().equals(currentUserId)) {
            throw new IllegalStateException("Cannot accept your own request");
        }
        entity.setStatus(TravelPalStatus.ACCEPTED);
    }

    /* ================= REJECT ================= */

    public void rejectRequest(UUID currentUserId, UUID otherUserId) {
        UserPair pair = normalize(currentUserId, otherUserId);
        TravelPalEntity entity = repository
                .findByUserLowIdAndUserHighId(pair.low(), pair.high())
                .orElseThrow(() -> new RuntimeException("Request not found"));
        if (entity.getStatus() != TravelPalStatus.PENDING) {
            throw new IllegalStateException("Invalid request state");
        }
        if (entity.getRequestedBy().equals(currentUserId)) {
            throw new IllegalStateException("Cannot reject your own request");
        }
        entity.setStatus(TravelPalStatus.REJECTED);
    }

    /* ================= REMOVE ================= */

    public void removeTravelPal(UUID userA, UUID userB) {
        UserPair pair = normalize(userA, userB);
        TravelPalEntity entity = repository
                .findByUserLowIdAndUserHighId(pair.low(), pair.high())
                .orElseThrow(() -> new RuntimeException("Not connected"));
        repository.delete(entity);
    }

    /* ================= GET MY PALS ================= */

    public List<UUID> getMyTravelPals(UUID userId) {
        return repository.findAcceptedByUser(userId)
                .stream()
                .map(entity -> entity.getUserLowId().equals(userId)
                        ? entity.getUserHighId()
                        : entity.getUserLowId())
                .toList();
    }

    public List<SuggestedTravelPalDto> getMyTravelPalsWithDetails(UUID userId) {
        // Get existing travel pal IDs
        List<UUID> palIds = getMyTravelPals(userId);
        
        if (palIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Get user names using UserProfileClient
        Map<UUID, UserNameDto> userNames = userProfileClient.getNamesByUserIds(palIds);
        
        // Convert to DTOs using available data from client
        return palIds.stream()
                .filter(userNames::containsKey) // Only include users found in client response
                .map(palId -> {
                    UserNameDto userName = userNames.get(palId);
                    return SuggestedTravelPalDto.builder()
                            .userId(userName.getUserId())
                            .firstName(userName.getFirstName())
                            .middleName(userName.getMiddleName())
                            .lastName(userName.getLastName())
                            .bio(userName.getBio()) // Not available from UserProfileClient
                            .location(userName.getLocation()) // Not available from UserProfileClient
                            .dob(userName.getDob()) // Not available from UserProfileClient
                            .profilePictureUrl(userName.getProfilePictureUrl())
                            .build();
                })
                .toList();
    }

    /* ================= INCOMING REQUESTS ================= */

    public List<TravelPalEntity> getIncomingPendingRequests(UUID userId) {
        return repository.findIncomingPending(userId);
    }

    public List<SuggestedTravelPalDto> getSuggestedTravelPals(UUID currentUserId) {
        // Get existing travel pals
        List<UUID> existingPals = getMyTravelPals(currentUserId);
        
        // Get incoming pending requests
        List<UUID> incomingPending = repository.findIncomingPending(currentUserId)
                .stream()
                .map(entity -> entity.getUserLowId().equals(currentUserId)
                        ? entity.getUserHighId()
                        : entity.getUserLowId())
                .toList();
        
        // Get outgoing pending requests
        List<UUID> outgoingPending = repository.findOutgoingPending(currentUserId)
                .stream()
                .map(entity -> entity.getUserLowId().equals(currentUserId)
                        ? entity.getUserHighId()
                        : entity.getUserLowId())
                .toList();
        
        // Combine all excluded user IDs
        Set<UUID> excludedUserIds = new HashSet<>();
        excludedUserIds.add(currentUserId);
        excludedUserIds.addAll(existingPals);
        excludedUserIds.addAll(incomingPending);
        excludedUserIds.addAll(outgoingPending);
        // Get user IDs of all active users except excluded ones
        List<UUID> suggestedUserIds = userRepository.findAll().stream()
                .filter(user -> user.getAccountStatus() == AccountStatus.ACTIVE)
                .filter(user -> !excludedUserIds.contains(user.getUserUuid()))
                .map(UsersEntity::getUserUuid)
                .toList();

        // Get user details using UserProfileClient
        Map<UUID, UserNameDto> userDetails = userProfileClient.getNamesByUserIds(suggestedUserIds);

        // Convert to DTOs using available data from client
        return suggestedUserIds.stream()
                .filter(userDetails::containsKey) // Only include users found in client response
                .map(userId -> {
                    UserNameDto userName = userDetails.get(userId);
                    return SuggestedTravelPalDto.builder()
                            .userId(userName.getUserId())
                            .firstName(userName.getFirstName())
                            .middleName(userName.getMiddleName())
                            .lastName(userName.getLastName())
                            .bio(userName.getBio())
                            .dob(userName.getDob())
                            .location(userName.getLocation())
                            .profilePictureUrl(userName.getProfilePictureUrl())
                            .build();
                })
                .toList();
    }
}

record UserPair(UUID low, UUID high) {}