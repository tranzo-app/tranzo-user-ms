package com.tranzo.tranzo_user_ms.user.service;

import com.tranzo.tranzo_user_ms.user.enums.TravelPalStatus;
import com.tranzo.tranzo_user_ms.user.model.TravelPalEntity;
import com.tranzo.tranzo_user_ms.user.repository.TravelPalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TravelPalService {
    private final TravelPalRepository repository;

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
                    if (existing.getStatus() == TravelPalStatus.ACCEPTED)
                    {
                        throw new IllegalStateException("Connection already exists");
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

    /* ================= INCOMING REQUESTS ================= */

    public List<TravelPalEntity> getIncomingPendingRequests(UUID userId) {
        return repository.findIncomingPending(userId);
    }
}

record UserPair(UUID low, UUID high) {}