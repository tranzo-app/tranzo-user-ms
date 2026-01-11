package com.tranzo.tranzo_user_ms.trip.service;

import com.tranzo.tranzo_user_ms.commons.exception.BadRequestException;
import com.tranzo.tranzo_user_ms.commons.exception.ConflictException;
import com.tranzo.tranzo_user_ms.commons.exception.EntityNotFoundException;
import com.tranzo.tranzo_user_ms.commons.exception.ForbiddenException;
import com.tranzo.tranzo_user_ms.trip.dto.RemoveParticipantRequestDto;
import com.tranzo.tranzo_user_ms.trip.dto.TripJoinRequestDto;
import com.tranzo.tranzo_user_ms.trip.dto.TripJoinRequestResponseDto;
import com.tranzo.tranzo_user_ms.trip.enums.*;
import com.tranzo.tranzo_user_ms.trip.exception.TripPublishException;
import com.tranzo.tranzo_user_ms.trip.model.TripEntity;
import com.tranzo.tranzo_user_ms.trip.model.TripJoinRequestEntity;
import com.tranzo.tranzo_user_ms.trip.model.TripMemberEntity;
import com.tranzo.tranzo_user_ms.trip.repository.TripJoinRequestRepository;
import com.tranzo.tranzo_user_ms.trip.repository.TripMemberRepository;
import com.tranzo.tranzo_user_ms.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.tranzo.tranzo_user_ms.trip.enums.TripPublishErrorCode.TRIP_NOT_FOUND;
import static com.tranzo.tranzo_user_ms.trip.enums.TripPublishErrorCode.TRIP_NOT_PUBLISHED;

@Service
@RequiredArgsConstructor
public class TripJoinRequestService {
    TripRepository tripRepository;
    TripMemberRepository tripMemberRepository;
    TripJoinRequestRepository tripJoinRequestRepository;

    @Transactional
    public TripJoinRequestResponseDto createJoinRequest(TripJoinRequestDto tripJoinRequestDto, UUID tripId, UUID userId)
    {
        // Trip validation
        TripEntity trip = tripRepository.findByIdForUpdate(tripId)
                .orElseThrow(() -> new TripPublishException(TRIP_NOT_FOUND));
        if (trip.getTripStatus() != TripStatus.PUBLISHED)
        {
            throw new TripPublishException(TRIP_NOT_PUBLISHED);
        }
        if (trip.getVisibilityStatus() != VisibilityStatus.PUBLIC) {
            throw new BadRequestException("Trip is not joinable");
        }

        // Trip member validation
        Optional<TripMemberEntity> existingTripMember = tripMemberRepository.findByTrip_TripIdAndUserIdAndStatus(tripId, userId, TripMemberStatus.ACTIVE);
        if (existingTripMember.isPresent()) {
            if (existingTripMember.get().getRole() == TripMemberRole.HOST) {
                throw new BadRequestException("Trip host cannot create join request");
            }
            throw new ConflictException("User is already a member of the trip");
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
            throw new ConflictException("Join request already exists");
        }

        // Create join request status
        JoinRequestStatus status =
                trip.getJoinPolicy() == JoinPolicy.OPEN
                        ? JoinRequestStatus.AUTO_APPROVED
                        : JoinRequestStatus.PENDING;

        // Capacity check (before persistence)
        if (status == JoinRequestStatus.AUTO_APPROVED &&
                trip.getCurrentParticipants() >= trip.getMaxParticipants()) {
            throw new BadRequestException("Trip is already full");
        }

        // Create join request
        TripJoinRequestEntity tripJoinRequestEntity = new TripJoinRequestEntity();
        tripJoinRequestEntity.setUserId(userId);
        tripJoinRequestEntity.setSource(tripJoinRequestDto.getRequestSource());
        tripJoinRequestEntity.setStatus(status);
        tripJoinRequestEntity.setTrip(trip);

        TripJoinRequestEntity savedRequest =
                tripJoinRequestRepository.save(tripJoinRequestEntity);

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
        }

        // Response
        return TripJoinRequestResponseDto.builder()
                .joinRequestId(savedRequest.getRequestId())
                .tripId(tripId)
                .requestorUserId(userId)
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
                .orElseThrow(() -> new EntityNotFoundException("Join request not found"));
        if (joinRequest.getStatus() != JoinRequestStatus.PENDING)
        {
            throw new ConflictException("Join request is not pending");
        }
        TripEntity trip = tripRepository.findByIdForUpdate(joinRequest.getTrip().getTripId())
                .orElseThrow(() -> new TripPublishException(TRIP_NOT_FOUND));
        boolean isHost = tripMemberRepository.existsByTrip_TripIdAndUserIdAndRoleAndStatus(trip.getTripId(), userId, TripMemberRole.HOST, TripMemberStatus.ACTIVE);
        if (!isHost)
        {
            throw new ForbiddenException("Only host can approve join requests");
        }
        if (trip.getCurrentParticipants() >= trip.getMaxParticipants())
        {
            throw new ConflictException("Trip is already full");
        }
        boolean alreadyMember = tripMemberRepository.existsByTrip_TripIdAndUserIdAndStatus(trip.getTripId(), joinRequest.getUserId(), TripMemberStatus.ACTIVE);
        if (alreadyMember)
        {
            throw new ConflictException("User is already a trip member");
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

        return TripJoinRequestResponseDto.builder()
                .joinRequestId(joinRequest.getRequestId())
                .tripId(trip.getTripId())
                .requestorUserId(joinRequest.getUserId())
                .status(joinRequest.getStatus())
                .requestedChannel(joinRequest.getSource())
                .createdAt(joinRequest.getCreatedAt())
                .updatedAt(joinRequest.getUpdatedAt())
                .build();
    }

    public TripJoinRequestResponseDto rejectJoinRequest(UUID joinRequestId, UUID userId)
    {
        TripJoinRequestEntity joinRequest = tripJoinRequestRepository.findById(joinRequestId)
                .orElseThrow(() -> new EntityNotFoundException("Join request not found"));
        if (joinRequest.getStatus() != JoinRequestStatus.PENDING)
        {
            throw new ConflictException("Join request is not pending");
        }
        TripEntity trip = tripRepository.findById(joinRequest.getTrip().getTripId())
                .orElseThrow(() -> new TripPublishException(TRIP_NOT_FOUND));
        boolean isHost = tripMemberRepository.existsByTrip_TripIdAndUserIdAndRoleAndStatus(trip.getTripId(), userId, TripMemberRole.HOST, TripMemberStatus.ACTIVE);
        if (!isHost)
        {
            throw new ForbiddenException("Only host can reject join requests");
        }
        joinRequest.setStatus(JoinRequestStatus.REJECTED);
        joinRequest.setReviewedBy(userId);
        joinRequest.setReviewedAt(LocalDateTime.now());
        return TripJoinRequestResponseDto.builder()
                .joinRequestId(joinRequest.getRequestId())
                .tripId(trip.getTripId())
                .requestorUserId(joinRequest.getUserId())
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
                .orElseThrow(() -> new TripPublishException(TRIP_NOT_FOUND));
        if (trip.getTripStatus() != TripStatus.PUBLISHED) {
            throw new TripPublishException(TRIP_NOT_PUBLISHED) ;
        }
        boolean isHost = tripMemberRepository.existsByTrip_TripIdAndUserIdAndRoleAndStatus(tripId, userId, TripMemberRole.HOST, TripMemberStatus.ACTIVE);
        if (!isHost)
        {
            throw new ForbiddenException("Only host can fetch join requests for the trip");
        }
        List<TripJoinRequestEntity> joinRequests = (status == null) ? tripJoinRequestRepository.findByTrip_TripId(tripId) : tripJoinRequestRepository.findByTrip_TripIdAndStatus(tripId, status);
        List<TripJoinRequestResponseDto> joinRequestResponseDtoList = joinRequests.stream().map((joinRequest) -> {
            return TripJoinRequestResponseDto.builder()
                    .joinRequestId(joinRequest.getRequestId())
                    .tripId(tripId)
                    .requestorUserId(joinRequest.getUserId())
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
                .orElseThrow(() -> new EntityNotFoundException("Join request not found"));
        if (joinRequest.getStatus() != JoinRequestStatus.PENDING)
        {
            throw new ConflictException("Join request is not pending");
        }
        if (!joinRequest.getUserId().equals(userId))
        {
            throw new ForbiddenException("User can't cancel the join request of another user");
        }
        joinRequest.setStatus(JoinRequestStatus.CANCELLED);
    }

    @Transactional
    public void removeOrLeaveTrip(UUID tripId, UUID removalParticipantUserId, UUID userId, RemoveParticipantRequestDto removeParticipantRequestDto)
    {
        TripEntity trip = tripRepository.findByIdForUpdate(tripId)
                .orElseThrow(() -> new TripPublishException(TRIP_NOT_FOUND));
        if (trip.getTripStatus() != TripStatus.PUBLISHED)
        {
            throw new TripPublishException(TRIP_NOT_PUBLISHED);
        }
        TripMemberEntity tripMember = tripMemberRepository.findByTrip_TripIdAndUserIdAndStatus(tripId, removalParticipantUserId, TripMemberStatus.ACTIVE)
                .orElseThrow(() -> new EntityNotFoundException("User is not member of the trip"));
        boolean isTripHost = tripMemberRepository.existsByTrip_TripIdAndUserIdAndRoleAndStatus(tripId, userId, TripMemberRole.HOST, TripMemberStatus.ACTIVE);
        if (isTripHost)
        {
            if (userId.equals(removalParticipantUserId))
            {
                throw new ConflictException("Host can't leave his own trip");
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
                throw new ForbiddenException("User can't remove another user from the trip until the user is a host");
            }
        }
        tripMember.setExitedAt(LocalDateTime.now());
        tripMember.setRemovalReason(removeParticipantRequestDto.getRemovalReason());
        int updatedCount = Math.max(0, trip.getCurrentParticipants() - 1);
        trip.setCurrentParticipants(updatedCount);
        trip.setIsFull(updatedCount >= trip.getMaxParticipants());
    }
}
