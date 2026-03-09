package com.tranzo.tranzo_user_ms.trip.service;

import com.tranzo.tranzo_user_ms.commons.exception.BadRequestException;
import com.tranzo.tranzo_user_ms.trip.enums.TripMemberRole;
import com.tranzo.tranzo_user_ms.trip.enums.TripMemberStatus;
import com.tranzo.tranzo_user_ms.trip.enums.TripStatus;
import com.tranzo.tranzo_user_ms.trip.enums.VisibilityStatus;
import com.tranzo.tranzo_user_ms.trip.model.TripEntity;
import com.tranzo.tranzo_user_ms.trip.repository.TripInviteRepository;
import com.tranzo.tranzo_user_ms.trip.repository.TripMemberRepository;
import com.tranzo.tranzo_user_ms.trip.repository.TripRepository;
import com.tranzo.tranzo_user_ms.user.service.TravelPalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TripInviteService Unit Tests")
class TripInviteServiceTest {

    @Mock
    private TripInviteRepository tripInviteRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripMemberRepository tripMemberRepository;

    @Mock
    private TravelPalService travelPalService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private TripInviteService tripInviteService;

    private UUID tripId;
    private UUID hostUserId;
    private UUID travelPalUserId;
    private TripEntity tripEntity;

    @BeforeEach
    void setUp() {
        tripId = UUID.randomUUID();
        hostUserId = UUID.randomUUID();
        travelPalUserId = UUID.randomUUID();
        tripEntity = createPublishedTrip();
    }

    @Test
    @DisplayName("Should throw when inviting yourself")
    void inviteTravelPal_selfInvite_throwsBadRequest() {
        assertThrows(BadRequestException.class, () ->
                tripInviteService.inviteTravelPal(tripId, hostUserId, hostUserId));

        verify(tripRepository, never()).findById(any());
        verify(travelPalService, never()).getMyTravelPals(any());
        verify(tripInviteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should invite travel pal successfully")
    void inviteTravelPal_success() {
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(tripEntity));
        when(tripMemberRepository.existsByTrip_TripIdAndUserIdAndRoleAndStatus(
                eq(tripId), eq(hostUserId), eq(TripMemberRole.HOST), eq(TripMemberStatus.ACTIVE)))
                .thenReturn(true);
        when(travelPalService.getMyTravelPals(hostUserId)).thenReturn(List.of(travelPalUserId));
        when(tripMemberRepository.existsByTrip_TripIdAndUserIdAndStatus(
                eq(tripId), eq(travelPalUserId), eq(TripMemberStatus.ACTIVE))).thenReturn(false);
        when(tripInviteRepository.existsByTrip_TripIdAndInvitedUserId(tripId, travelPalUserId)).thenReturn(false);
        when(tripInviteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        tripInviteService.inviteTravelPal(tripId, hostUserId, travelPalUserId);

        ArgumentCaptor<com.tranzo.tranzo_user_ms.commons.events.TripInviteCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(com.tranzo.tranzo_user_ms.commons.events.TripInviteCreatedEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(tripId, eventCaptor.getValue().getTripId());
        assertEquals(travelPalUserId, eventCaptor.getValue().getInvitedUserId());
        assertEquals(hostUserId, eventCaptor.getValue().getInvitedByUserId());
    }

    private TripEntity createPublishedTrip() {
        TripEntity trip = new TripEntity();
        trip.setTripId(tripId);
        trip.setTripStatus(TripStatus.PUBLISHED);
        trip.setTripTitle("Test Trip");
        trip.setVisibilityStatus(VisibilityStatus.PUBLIC);
        return trip;
    }
}
