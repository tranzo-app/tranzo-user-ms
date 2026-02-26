package com.tranzo.tranzo_user_ms.trip.service;

import com.tranzo.tranzo_user_ms.commons.events.TripInviteCreatedEvent;
import com.tranzo.tranzo_user_ms.commons.exception.BadRequestException;
import com.tranzo.tranzo_user_ms.commons.exception.ConflictException;
import com.tranzo.tranzo_user_ms.commons.exception.EntityNotFoundException;
import com.tranzo.tranzo_user_ms.commons.exception.ForbiddenException;
import com.tranzo.tranzo_user_ms.trip.enums.InviteSource;
import com.tranzo.tranzo_user_ms.trip.enums.InviteStatus;
import com.tranzo.tranzo_user_ms.trip.enums.InviteType;
import com.tranzo.tranzo_user_ms.trip.enums.TripMemberRole;
import com.tranzo.tranzo_user_ms.trip.enums.TripMemberStatus;
import com.tranzo.tranzo_user_ms.trip.enums.TripStatus;
import com.tranzo.tranzo_user_ms.trip.model.TripEntity;
import com.tranzo.tranzo_user_ms.trip.model.TripInviteEntity;
import com.tranzo.tranzo_user_ms.trip.repository.TripInviteRepository;
import com.tranzo.tranzo_user_ms.trip.repository.TripMemberRepository;
import com.tranzo.tranzo_user_ms.trip.repository.TripRepository;
import com.tranzo.tranzo_user_ms.user.service.TravelPalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripInviteService {

    private final TripInviteRepository tripInviteRepository;
    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final TravelPalService travelPalService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void inviteTravelPal(UUID tripId, UUID hostOrCoHostUserId, UUID travelPalUserId) {
        if (hostOrCoHostUserId.equals(travelPalUserId)) {
            throw new BadRequestException("Cannot invite yourself to the trip");
        }

        TripEntity trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("Trip not found"));
        validateTripPublished(trip);

        ensureUserIsHostOrCoHost(tripId, hostOrCoHostUserId);

        List<UUID> travelPals = travelPalService.getMyTravelPals(hostOrCoHostUserId);
        if (!travelPals.contains(travelPalUserId)) {
            throw new BadRequestException("User is not your travel pal");
        }

        if (tripMemberRepository.existsByTrip_TripIdAndUserIdAndStatus(tripId, travelPalUserId, TripMemberStatus.ACTIVE)) {
            throw new ConflictException("User is already a member of the trip");
        }

        if (tripInviteRepository.existsByTrip_TripIdAndInvitedUserId(tripId, travelPalUserId)) {
            throw new ConflictException("User has already been invited to this trip");
        }

        createAndPublishInvite(trip, hostOrCoHostUserId, travelPalUserId);
    }

    @Transactional
    public void inviteAllTravelPals(UUID tripId, UUID hostOrCoHostUserId) {
        TripEntity trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("Trip not found"));
        validateTripPublished(trip);

        ensureUserIsHostOrCoHost(tripId, hostOrCoHostUserId);

        List<UUID> travelPals = travelPalService.getMyTravelPals(hostOrCoHostUserId).stream()
                .filter(palId -> !palId.equals(hostOrCoHostUserId))
                .toList();
        int invited = 0;
        for (UUID palId : travelPals) {
            if (tripMemberRepository.existsByTrip_TripIdAndUserIdAndStatus(tripId, palId, TripMemberStatus.ACTIVE)) {
                continue;
            }
            if (tripInviteRepository.existsByTrip_TripIdAndInvitedUserId(tripId, palId)) {
                continue;
            }
            createAndPublishInvite(trip, hostOrCoHostUserId, palId);
            invited++;
        }
        log.info("Invited {} travel pals to trip {}", invited, tripId);
    }

    private void validateTripPublished(TripEntity trip) {
        if (trip.getTripStatus() != TripStatus.PUBLISHED) {
            throw new BadRequestException("Can only invite to published trips");
        }
    }

    private void ensureUserIsHostOrCoHost(UUID tripId, UUID userId) {
        boolean isHost = tripMemberRepository.existsByTrip_TripIdAndUserIdAndRoleAndStatus(
                tripId, userId, TripMemberRole.HOST, TripMemberStatus.ACTIVE);
        boolean isCoHost = tripMemberRepository.existsByTrip_TripIdAndUserIdAndRoleAndStatus(
                tripId, userId, TripMemberRole.CO_HOST, TripMemberStatus.ACTIVE);
        if (!isHost && !isCoHost) {
            throw new ForbiddenException("Only host or co-host can invite travel pals");
        }
    }

    private void createAndPublishInvite(TripEntity trip, UUID invitedBy, UUID invitedUserId) {
        TripInviteEntity invite = new TripInviteEntity();
        invite.setTrip(trip);
        invite.setInvitedBy(invitedBy);
        invite.setInviteType(InviteType.IN_APPLICATION);
        invite.setInviteSource(InviteSource.TRAVEL_PAL);
        invite.setInvitedUserId(invitedUserId);
        invite.setStatus(InviteStatus.PENDING);
        tripInviteRepository.save(invite);

        applicationEventPublisher.publishEvent(new TripInviteCreatedEvent(
                trip.getTripId(),
                trip.getTripTitle(),
                invitedUserId,
                invitedBy
        ));
    }
}
