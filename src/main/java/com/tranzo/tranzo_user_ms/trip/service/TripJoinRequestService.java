package com.tranzo.tranzo_user_ms.trip.service;

import com.tranzo.tranzo_user_ms.trip.dto.RemoveParticipantRequestDto;
import com.tranzo.tranzo_user_ms.trip.dto.TripJoinRequestDto;
import com.tranzo.tranzo_user_ms.trip.dto.TripJoinRequestResponseDto;
import com.tranzo.tranzo_user_ms.trip.enums.*;
import com.tranzo.tranzo_user_ms.trip.exception.*;
import com.tranzo.tranzo_user_ms.trip.events.TripEventPublisher;
import com.tranzo.tranzo_user_ms.trip.events.TripPublishedEventPayloadDto;
import com.tranzo.tranzo_user_ms.trip.model.TripEntity;
import com.tranzo.tranzo_user_ms.trip.model.TripJoinRequestEntity;
import com.tranzo.tranzo_user_ms.trip.model.TripMemberEntity;
import com.tranzo.tranzo_user_ms.commons.events.*;
import com.tranzo.tranzo_user_ms.trip.repository.TripJoinRequestRepository;
import com.tranzo.tranzo_user_ms.trip.repository.TripMemberRepository;
import com.tranzo.tranzo_user_ms.trip.repository.TripRepository;
import com.tranzo.tranzo_user_ms.user.client.UserProfileClient;
import com.tranzo.tranzo_user_ms.user.dto.UserNameDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripJoinRequestService {
    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final TripJoinRequestRepository tripJoinRequestRepository;
    private final TripEventPublisher tripEventPublisher;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final UserProfileClient userProfileClient;

    @Transactional
    public TripJoinRequestResponseDto createJoinRequest(TripJoinRequestDto tripJoinRequestDto, UUID tripId, UUID userId)
    {
        // Trip validation
        TripEntity trip = tripRepository.findByIdForUpdate(tripId)
                .orElseThrow(() -> new TripNotFoundException());
        if (trip.getTripStatus() != TripStatus.PUBLISHED)
        {
            throw new TripValidationException(TripErrorCode.TRIP_NOT_PUBLISHED);
        }
        if (trip.getVisibilityStatus() != VisibilityStatus.PUBLIC) {
            throw new TripValidationException(TripErrorCode.TRIP_NOT_JOINABLE);
        }

        // Trip member validation
        Optional<TripMemberEntity> existingTripMember = tripMemberRepository.findByTrip_TripIdAndUserIdAndStatus(tripId, userId, TripMemberStatus.ACTIVE);
        if (existingTripMember.isPresent()) {
            if (existingTripMember.get().getRole() == TripMemberRole.HOST) {
                throw new TripJoinRequestException(TripErrorCode.HOST_CANNOT_CREATE_JOIN_REQUEST);
            }
            throw new TripJoinRequestException(TripErrorCode.USER_ALREADY_TRIP_MEMBER);
        }

        // Active join request check
        Set<JoinRequestStatus> ACTIVE_JOIN_REQUEST_STATUSES = Set.of(
                JoinRequestStatus.PENDING, JoinRequestStatus.APPROVED, JoinRequestStatus.AUTO_APPROVED
        );

        boolean activeRequestExists =
                tripJoinRequestRepository.existsByTrip_TripIdAndUserIdAndStatusIn(
                        tripId,
                        userId,
                        ACTIVE_JOIN_REQUEST_STATUSES
                );
        if (activeRequestExists) {
            throw new TripJoinRequestException(TripErrorCode.JOIN_REQUEST_ALREADY_EXISTS);
        }

        // Create join request status
        JoinRequestStatus status =
                trip.getJoinPolicy() == JoinPolicy.OPEN
                        ? JoinRequestStatus.AUTO_APPROVED
                        : JoinRequestStatus.PENDING;

        // Capacity check (before persistence)
        if (status == JoinRequestStatus.AUTO_APPROVED &&
                trip.getCurrentParticipants() >= trip.getMaxParticipants()) {
            throw new TripValidationException(TripErrorCode.TRIP_FULL);
        }

        // Create join request
        TripJoinRequestEntity tripJoinRequestEntity = new TripJoinRequestEntity();
        tripJoinRequestEntity.setUserId(userId);
        tripJoinRequestEntity.setSource(tripJoinRequestDto.getRequestSource());
        tripJoinRequestEntity.setStatus(status);
        tripJoinRequestEntity.setTrip(trip);

        TripJoinRequestEntity savedRequest =
                tripJoinRequestRepository.save(tripJoinRequestEntity);

        if (status == JoinRequestStatus.PENDING) {
            UUID hostUserId = tripMemberRepository.findByTrip_TripIdAndStatus(tripId, TripMemberStatus.ACTIVE)
                    .stream()
                    .filter(m -> m.getRole() == TripMemberRole.HOST)
                    .map(TripMemberEntity::getUserId)
                    .findFirst()
                    .orElse(null);
            if (hostUserId != null) {
                applicationEventPublisher.publishEvent(
                        new JoinRequestCreatedEvent(tripId, trip.getTripTitle(), userId, hostUserId));
            }
        }

        // Auto-approve → create member
        if (status == JoinRequestStatus.AUTO_APPROVED) {
            TripMemberEntity member = new TripMemberEntity();
            member.setTrip(trip);
            member.setUserId(userId);
            member.setRole(TripMemberRole.MEMBER);
            member.setStatus(TripMemberStatus.ACTIVE);
            tripMemberRepository.save(member);
            int updatedCount = trip.getCurrentParticipants() + 1;
            trip.setCurrentParticipants(updatedCount);
            trip.setIsFull(updatedCount >= trip.getMaxParticipants());
            tripRepository.save(trip);
            // Spring event: add participant to trip's group chat
            TripPublishedEventPayloadDto eventPayloadDto = TripPublishedEventPayloadDto.builder()
                    .eventType("PARTICIPANT_JOINED")
                    .tripId(tripId)
                    .userId(userId)
                    .conversationId(trip.getConversationID())
                    .build();

            tripEventPublisher.participantJoined(eventPayloadDto);
            List<UUID> otherMemberUserIds = tripMemberRepository.findByTrip_TripIdAndStatus(tripId, TripMemberStatus.ACTIVE)
                    .stream()
                    .map(TripMemberEntity::getUserId)
                    .filter(id -> !id.equals(userId))
                    .toList();
            if (!otherMemberUserIds.isEmpty()) {
                applicationEventPublisher.publishEvent(
                        new MemberJoinedTripEvent(tripId, trip.getTripTitle(), userId, otherMemberUserIds));
            }
            if (Boolean.TRUE.equals(trip.getIsFull())) {
                List<UUID> allMemberUserIds = tripMemberRepository.findByTrip_TripIdAndStatus(tripId, TripMemberStatus.ACTIVE)
                        .stream()
                        .map(TripMemberEntity::getUserId)
                        .toList();
                if (!allMemberUserIds.isEmpty()) {
                    applicationEventPublisher.publishEvent(
                            new TripFullCapacityReachedEvent(tripId, trip.getTripTitle(), allMemberUserIds));
                }
            }
        }

        // Response with requestor name
        UserNameDto names = userProfileClient.getNamesByUserIds(List.of(userId)).get(userId);
        return TripJoinRequestResponseDto.builder()
                .joinRequestId(savedRequest.getRequestId())
                .tripId(tripId)
                .requestorUserId(userId)
                .firstName(names != null ? names.getFirstName() : null)
                .middleName(names != null ? names.getMiddleName() : null)
                .lastName(names != null ? names.getLastName() : null)
                .status(savedRequest.getStatus())
                .requestedChannel(savedRequest.getSource())
                .createdAt(savedRequest.getCreatedAt())
                .updatedAt(savedRequest.getUpdatedAt())
                .build();
    }

    @Transactional
    public TripJoinRequestResponseDto approveJoinRequest(UUID joinRequestId, UUID userId)
    {
        TripJoinRequestEntity joinRequest = tripJoinRequestRepository.findById(joinRequestId)
                .orElseThrow(() -> new TripJoinRequestException(TripErrorCode.JOIN_REQUEST_NOT_FOUND));
        if (joinRequest.getStatus() != JoinRequestStatus.PENDING)
        {
            throw new TripJoinRequestException(TripErrorCode.JOIN_REQUEST_NOT_PENDING);
        }
        TripEntity trip = tripRepository.findByIdForUpdate(joinRequest.getTrip().getTripId())
                .orElseThrow(() -> new TripNotFoundException());
        boolean isHost = tripMemberRepository.existsByTrip_TripIdAndUserIdAndRoleAndStatus(trip.getTripId(), userId, TripMemberRole.HOST, TripMemberStatus.ACTIVE);
        if (!isHost)
        {
            throw new TripAccessDeniedException("Only host can approve join requests");
        }
        if (trip.getCurrentParticipants() >= trip.getMaxParticipants())
        {
            throw new TripValidationException(TripErrorCode.TRIP_FULL);
        }
        boolean alreadyMember = tripMemberRepository.existsByTrip_TripIdAndUserIdAndStatus(trip.getTripId(), joinRequest.getUserId(), TripMemberStatus.ACTIVE);
        if (alreadyMember)
        {
            throw new TripJoinRequestException(TripErrorCode.USER_ALREADY_TRIP_MEMBER);
        }
        joinRequest.setStatus(JoinRequestStatus.APPROVED);
        joinRequest.setReviewedBy(userId);
        joinRequest.setReviewedAt(LocalDateTime.now());

        TripMemberEntity tripMember = new TripMemberEntity();
        tripMember.setTrip(trip);
        tripMember.setUserId(joinRequest.getUserId());
        tripMember.setRole(TripMemberRole.MEMBER);
        tripMember.setStatus(TripMemberStatus.ACTIVE);
        tripMemberRepository.save(tripMember);

        int updatedCount = trip.getCurrentParticipants() + 1;
        trip.setCurrentParticipants(updatedCount);
        trip.setIsFull(updatedCount >= trip.getMaxParticipants());

        applicationEventPublisher.publishEvent(
                new JoinRequestApprovedEvent(trip.getTripId(), trip.getTripTitle(), joinRequest.getUserId()));
        List<UUID> otherMemberUserIds = tripMemberRepository.findByTrip_TripIdAndStatus(trip.getTripId(), TripMemberStatus.ACTIVE)
                .stream()
                .map(TripMemberEntity::getUserId)
                .filter(id -> !id.equals(joinRequest.getUserId()))
                .toList();
        if (!otherMemberUserIds.isEmpty()) {
            applicationEventPublisher.publishEvent(
                    new MemberJoinedTripEvent(trip.getTripId(), trip.getTripTitle(), joinRequest.getUserId(), otherMemberUserIds));
        }
        if (Boolean.TRUE.equals(trip.getIsFull())) {
            List<UUID> allMemberUserIds = tripMemberRepository.findByTrip_TripIdAndStatus(trip.getTripId(), TripMemberStatus.ACTIVE)
                    .stream()
                    .map(TripMemberEntity::getUserId)
                    .toList();
            if (!allMemberUserIds.isEmpty()) {
                applicationEventPublisher.publishEvent(
                        new TripFullCapacityReachedEvent(trip.getTripId(), trip.getTripTitle(), allMemberUserIds));
            }
        }

        UserNameDto names = userProfileClient.getNamesByUserIds(List.of(joinRequest.getUserId())).get(joinRequest.getUserId());
        return TripJoinRequestResponseDto.builder()
                .joinRequestId(joinRequest.getRequestId())
                .tripId(trip.getTripId())
                .requestorUserId(joinRequest.getUserId())
                .firstName(names != null ? names.getFirstName() : null)
                .middleName(names != null ? names.getMiddleName() : null)
                .lastName(names != null ? names.getLastName() : null)
                .status(joinRequest.getStatus())
                .requestedChannel(joinRequest.getSource())
                .createdAt(joinRequest.getCreatedAt())
                .updatedAt(joinRequest.getUpdatedAt())
                .build();
    }

    @Transactional
    public TripJoinRequestResponseDto rejectJoinRequest(UUID joinRequestId, UUID userId)
    {
        TripJoinRequestEntity joinRequest = tripJoinRequestRepository.findById(joinRequestId)
                .orElseThrow(() -> new TripJoinRequestException(TripErrorCode.JOIN_REQUEST_NOT_FOUND));
        if (joinRequest.getStatus() != JoinRequestStatus.PENDING)
        {
            throw new TripJoinRequestException(TripErrorCode.JOIN_REQUEST_NOT_PENDING);
        }
        TripEntity trip = tripRepository.findById(joinRequest.getTrip().getTripId())
                .orElseThrow(() -> new TripNotFoundException());
        boolean isHost = tripMemberRepository.existsByTrip_TripIdAndUserIdAndRoleAndStatus(trip.getTripId(), userId, TripMemberRole.HOST, TripMemberStatus.ACTIVE);
        log.info("Host user id : {}", userId);
        if (!isHost)
        {
            throw new TripAccessDeniedException("Only host can reject join requests");
        }
        joinRequest.setStatus(JoinRequestStatus.REJECTED);
        joinRequest.setReviewedBy(userId);
        joinRequest.setReviewedAt(LocalDateTime.now());
        tripJoinRequestRepository.save(joinRequest);
        applicationEventPublisher.publishEvent(
                new JoinRequestRejectedEvent(trip.getTripId(), trip.getTripTitle(), joinRequest.getUserId()));
        UserNameDto names = userProfileClient.getNamesByUserIds(List.of(joinRequest.getUserId())).get(joinRequest.getUserId());
        return TripJoinRequestResponseDto.builder()
                .joinRequestId(joinRequest.getRequestId())
                .tripId(trip.getTripId())
                .requestorUserId(joinRequest.getUserId())
                .firstName(names != null ? names.getFirstName() : null)
                .middleName(names != null ? names.getMiddleName() : null)
                .lastName(names != null ? names.getLastName() : null)
                .status(joinRequest.getStatus())
                .requestedChannel(joinRequest.getSource())
                .createdAt(joinRequest.getCreatedAt())
                .updatedAt(joinRequest.getUpdatedAt())
                .build();
    }

    // Will we get status as an array in query parameter?
    public List<TripJoinRequestResponseDto> getJoinRequestsForTrip(UUID tripId, UUID userId, JoinRequestStatus status)
    {
        TripEntity trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException());
        if (trip.getTripStatus() != TripStatus.PUBLISHED) {
            throw new TripValidationException(TripErrorCode.TRIP_NOT_PUBLISHED);
        }
        boolean isHost = tripMemberRepository.existsByTrip_TripIdAndUserIdAndRoleAndStatus(tripId, userId, TripMemberRole.HOST, TripMemberStatus.ACTIVE);
        if (!isHost)
        {
            throw new TripAccessDeniedException("Only host can fetch join requests for the trip");
        }
        List<TripJoinRequestEntity> joinRequests = (status == null) ? tripJoinRequestRepository.findByTrip_TripId(tripId) : tripJoinRequestRepository.findByTrip_TripIdAndStatus(tripId, status);
        List<UUID> requestorUserIds = joinRequests.stream().map(TripJoinRequestEntity::getUserId).toList();
        Map<UUID, UserNameDto> namesByUserId = userProfileClient.getNamesByUserIds(requestorUserIds);
        List<TripJoinRequestResponseDto> joinRequestResponseDtoList = joinRequests.stream().map((joinRequest) -> {
            UserNameDto names = namesByUserId.get(joinRequest.getUserId());
            return TripJoinRequestResponseDto.builder()
                    .joinRequestId(joinRequest.getRequestId())
                    .tripId(tripId)
                    .requestorUserId(joinRequest.getUserId())
                    .firstName(names != null ? names.getFirstName() : null)
                    .middleName(names != null ? names.getMiddleName() : null)
                    .lastName(names != null ? names.getLastName() : null)
                    .profilePictureUrl(names != null ? names.getProfilePictureUrl() : null)
                    .status(joinRequest.getStatus())
                    .requestedChannel(joinRequest.getSource())
                    .createdAt(joinRequest.getCreatedAt())
                    .updatedAt(joinRequest.getUpdatedAt())
                    .build();
        }).toList();
        return joinRequestResponseDtoList;
    }

    // We will show only pending requests for a user
    @Transactional
    public void cancelJoinRequestsForTrip(UUID joinRequestId, UUID userId)
    {
        TripJoinRequestEntity joinRequest = tripJoinRequestRepository.findById(joinRequestId)
                .orElseThrow(() -> new TripJoinRequestException(TripErrorCode.JOIN_REQUEST_NOT_FOUND));
        if (joinRequest.getStatus() != JoinRequestStatus.PENDING)
        {
            throw new TripJoinRequestException(TripErrorCode.JOIN_REQUEST_NOT_PENDING);
        }
        if (!joinRequest.getUserId().equals(userId))
        {
            throw new TripJoinRequestException(TripErrorCode.INVALID_JOIN_REQUEST_CANCEL, "User can't cancel the join request of another user");
        }
        joinRequest.setStatus(JoinRequestStatus.CANCELLED);
    }

    @Transactional
    public void removeOrLeaveTrip(UUID tripId, UUID removalParticipantUserId, UUID userId, RemoveParticipantRequestDto removeParticipantRequestDto)
    {
        TripEntity trip = tripRepository.findByIdForUpdate(tripId)
                .orElseThrow(() -> new TripNotFoundException());
        if (trip.getTripStatus() != TripStatus.PUBLISHED)
        {
            throw new TripValidationException(TripErrorCode.TRIP_NOT_PUBLISHED);
        }
        TripMemberEntity tripMember = tripMemberRepository.findByTrip_TripIdAndUserIdAndStatus(tripId, removalParticipantUserId, TripMemberStatus.ACTIVE)
                .orElseThrow(() -> new TripMemberException(TripErrorCode.USER_NOT_TRIP_MEMBER));
        List<UUID> otherMemberUserIds = tripMemberRepository.findByTrip_TripIdAndStatus(tripId, TripMemberStatus.ACTIVE)
                .stream()
                .map(TripMemberEntity::getUserId)
                .filter(id -> !id.equals(removalParticipantUserId))
                .toList();
        boolean isTripHost = tripMemberRepository.existsByTrip_TripIdAndUserIdAndRoleAndStatus(tripId, userId, TripMemberRole.HOST, TripMemberStatus.ACTIVE);
        boolean removedByHost = isTripHost && !userId.equals(removalParticipantUserId);
        if (isTripHost)
        {
            if (userId.equals(removalParticipantUserId))
            {
                throw new TripMemberException(TripErrorCode.HOST_CANNOT_LEAVE_TRIP);
            }
            tripMember.setStatus(TripMemberStatus.REMOVED);
            tripMember.setExitedBy(userId);
        }
        else
        {
            if (userId.equals(removalParticipantUserId))
            {
                tripMember.setStatus(TripMemberStatus.LEFT);
                tripMember.setExitedBy(removalParticipantUserId);
            }
            else
            {
                throw new TripAccessDeniedException("User can't remove another user from the trip until the user is a host");
            }
        }
        tripMember.setExitedAt(LocalDateTime.now());
        tripMember.setRemovalReason(removeParticipantRequestDto.getRemovalReason());
        tripMemberRepository.save(tripMember);
        int updatedCount = Math.max(0, trip.getCurrentParticipants() - 1);
        trip.setCurrentParticipants(updatedCount);
        trip.setIsFull(updatedCount >= trip.getMaxParticipants());
        tripRepository.save(trip);
        if (!otherMemberUserIds.isEmpty()) {
            applicationEventPublisher.publishEvent(
                    new MemberLeftOrRemovedTripEvent(tripId, trip.getTripTitle(), removalParticipantUserId, otherMemberUserIds, removedByHost));
        }
    }
}
