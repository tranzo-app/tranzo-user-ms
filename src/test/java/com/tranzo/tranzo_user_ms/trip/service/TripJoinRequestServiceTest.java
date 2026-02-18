package com.tranzo.tranzo_user_ms.trip.service;

import com.tranzo.tranzo_user_ms.commons.exception.*;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TripJoinRequestService Unit Tests")
class TripJoinRequestServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripMemberRepository tripMemberRepository;

    @Mock
    private TripJoinRequestRepository tripJoinRequestRepository;

    @InjectMocks
    private TripJoinRequestService tripJoinRequestService;

    private UUID tripId;
    private UUID userId;
    private TripEntity tripEntity;
    private TripJoinRequestDto joinRequestDto;
    private TripJoinRequestEntity joinRequestEntity;

    @BeforeEach
    void setUp() {
        tripId = UUID.randomUUID();
        userId = UUID.randomUUID();
        tripEntity = createSampleTripEntity();
        joinRequestDto = createSampleJoinRequestDto();
        joinRequestEntity = createSampleJoinRequestEntity();
    }

    // ============== CREATE JOIN REQUEST TESTS ==============

    @Test
    @DisplayName("Should create auto-approved join request for OPEN policy")
    void testCreateJoinRequest_OpenPolicy_Success() {
        // Given
        tripEntity.setJoinPolicy(JoinPolicy.OPEN);
        tripEntity.setCurrentParticipants(5);
        tripEntity.setMaxParticipants(10);

        when(tripRepository.findByIdForUpdate(tripId)).thenReturn(Optional.of(tripEntity));
        when(tripMemberRepository.findByTrip_TripIdAndUserIdAndStatus(tripId, userId, TripMemberStatus.ACTIVE))
            .thenReturn(Optional.empty());
        when(tripJoinRequestRepository.existsByTrip_TripIdAndUserIdAndStatusIn(eq(tripId), eq(userId), anySet()))
            .thenReturn(false);
        when(tripJoinRequestRepository.save(any(TripJoinRequestEntity.class))).thenReturn(joinRequestEntity);
        when(tripMemberRepository.save(any(TripMemberEntity.class))).thenReturn(new TripMemberEntity());

        // When
        TripJoinRequestResponseDto response = tripJoinRequestService.createJoinRequest(joinRequestDto, tripId, userId);

        // Then
        assertNotNull(response);
        verify(tripJoinRequestRepository, times(1)).save(any(TripJoinRequestEntity.class));
        verify(tripMemberRepository, times(1)).save(any(TripMemberEntity.class));
    }

    @Test
    @DisplayName("Should create pending join request for APPROVAL_REQUIRED policy")
    void testCreateJoinRequest_ClosedPolicy_Success() {
        // Given
        tripEntity.setJoinPolicy(JoinPolicy.APPROVAL_REQUIRED);

        when(tripRepository.findByIdForUpdate(tripId)).thenReturn(Optional.of(tripEntity));
        when(tripMemberRepository.findByTrip_TripIdAndUserIdAndStatus(tripId, userId, TripMemberStatus.ACTIVE))
            .thenReturn(Optional.empty());
        when(tripJoinRequestRepository.existsByTrip_TripIdAndUserIdAndStatusIn(eq(tripId), eq(userId), anySet()))
            .thenReturn(false);
        when(tripJoinRequestRepository.save(any(TripJoinRequestEntity.class))).thenReturn(joinRequestEntity);

        // When
        TripJoinRequestResponseDto response = tripJoinRequestService.createJoinRequest(joinRequestDto, tripId, userId);

        // Then
        assertNotNull(response);
        verify(tripJoinRequestRepository, times(1)).save(any(TripJoinRequestEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when trip not found")
    void testCreateJoinRequest_TripNotFound() {
        // Given
        when(tripRepository.findByIdForUpdate(tripId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TripPublishException.class, () ->
            tripJoinRequestService.createJoinRequest(joinRequestDto, tripId, userId)
        );
    }

    @Test
    @DisplayName("Should throw exception when trip not published")
    void testCreateJoinRequest_TripNotPublished() {
        // Given
        tripEntity.setTripStatus(TripStatus.DRAFT);
        when(tripRepository.findByIdForUpdate(tripId)).thenReturn(Optional.of(tripEntity));

        // When & Then
        assertThrows(TripPublishException.class, () ->
            tripJoinRequestService.createJoinRequest(joinRequestDto, tripId, userId)
        );
    }

    @Test
    @DisplayName("Should throw exception when trip is private")
    void testCreateJoinRequest_PrivateTrip() {
        // Given
        tripEntity.setVisibilityStatus(VisibilityStatus.PRIVATE);
        when(tripRepository.findByIdForUpdate(tripId)).thenReturn(Optional.of(tripEntity));

        // When & Then
        assertThrows(BadRequestException.class, () ->
            tripJoinRequestService.createJoinRequest(joinRequestDto, tripId, userId)
        );
    }

    @Test
    @DisplayName("Should throw exception when host tries to join")
    void testCreateJoinRequest_HostCannotJoin() {
        // Given
        TripMemberEntity hostMember = new TripMemberEntity();
        hostMember.setRole(TripMemberRole.HOST);
        hostMember.setStatus(TripMemberStatus.ACTIVE);
        hostMember.setUserId(userId);

        when(tripRepository.findByIdForUpdate(tripId)).thenReturn(Optional.of(tripEntity));
        when(tripMemberRepository.findByTrip_TripIdAndUserIdAndStatus(tripId, userId, TripMemberStatus.ACTIVE))
            .thenReturn(Optional.of(hostMember));

        // When & Then
        assertThrows(BadRequestException.class, () ->
            tripJoinRequestService.createJoinRequest(joinRequestDto, tripId, userId)
        );
    }

    @Test
    @DisplayName("Should throw exception when user is already member")
    void testCreateJoinRequest_AlreadyMember() {
        // Given
        TripMemberEntity member = new TripMemberEntity();
        member.setRole(TripMemberRole.HOST);
        member.setStatus(TripMemberStatus.ACTIVE);
        member.setUserId(userId);

        when(tripRepository.findByIdForUpdate(tripId)).thenReturn(Optional.of(tripEntity));
        when(tripMemberRepository.findByTrip_TripIdAndUserIdAndStatus(tripId, userId, TripMemberStatus.ACTIVE))
            .thenReturn(Optional.of(member));

        // When & Then
        assertThrows(BadRequestException.class, () ->
            tripJoinRequestService.createJoinRequest(joinRequestDto, tripId, userId)
        );
    }

    @Test
    @DisplayName("Should throw exception when active join request already exists")
    void testCreateJoinRequest_ActiveRequestExists() {
        // Given
        when(tripRepository.findByIdForUpdate(tripId)).thenReturn(Optional.of(tripEntity));
        when(tripMemberRepository.findByTrip_TripIdAndUserIdAndStatus(tripId, userId, TripMemberStatus.ACTIVE))
            .thenReturn(Optional.empty());
        when(tripJoinRequestRepository.existsByTrip_TripIdAndUserIdAndStatusIn(eq(tripId), eq(userId), anySet()))
            .thenReturn(true);

        // When & Then
        assertThrows(ConflictException.class, () ->
            tripJoinRequestService.createJoinRequest(joinRequestDto, tripId, userId)
        );
    }

    @Test
    @DisplayName("Should throw exception when trip is full for auto-approve")
    void testCreateJoinRequest_TripFull() {
        // Given
        tripEntity.setJoinPolicy(JoinPolicy.OPEN);
        tripEntity.setCurrentParticipants(10);
        tripEntity.setMaxParticipants(10);

        when(tripRepository.findByIdForUpdate(tripId)).thenReturn(Optional.of(tripEntity));
        when(tripMemberRepository.findByTrip_TripIdAndUserIdAndStatus(tripId, userId, TripMemberStatus.ACTIVE))
            .thenReturn(Optional.empty());
        when(tripJoinRequestRepository.existsByTrip_TripIdAndUserIdAndStatusIn(eq(tripId), eq(userId), anySet()))
            .thenReturn(false);

        // When & Then
        assertThrows(BadRequestException.class, () ->
            tripJoinRequestService.createJoinRequest(joinRequestDto, tripId, userId)
        );
    }

    // ============== HELPER METHODS ==============

    private TripEntity createSampleTripEntity() {
        TripEntity trip = new TripEntity();
        trip.setTripId(tripId);
        trip.setTripTitle("Sample Trip");
        trip.setTripDescription("Sample Description");
        trip.setTripDestination("Paris");
        trip.setTripStartDate(LocalDate.of(2026, 6, 1));
        trip.setTripEndDate(LocalDate.of(2026, 6, 10));
        trip.setEstimatedBudget(5000.0);
        trip.setMaxParticipants(10);
        trip.setCurrentParticipants(0);
        trip.setIsFull(false);
        trip.setJoinPolicy(JoinPolicy.OPEN);
        trip.setVisibilityStatus(VisibilityStatus.PUBLIC);
        trip.setTripStatus(TripStatus.PUBLISHED);
        trip.setTripMembers(new HashSet<>());
        trip.setTripTags(new HashSet<>());
        trip.setTripItineraries(new HashSet<>());
        return trip;
    }

    private TripJoinRequestDto createSampleJoinRequestDto() {
        TripJoinRequestDto dto = new TripJoinRequestDto();
        dto.setRequestSource(JoinRequestSource.DIRECT);
        return dto;
    }

    private TripJoinRequestEntity createSampleJoinRequestEntity() {
        TripJoinRequestEntity entity = new TripJoinRequestEntity();
        entity.setUserId(userId);
        entity.setSource(JoinRequestSource.DIRECT);
        entity.setStatus(JoinRequestStatus.PENDING);
        entity.setTrip(tripEntity);
        return entity;
    }
}

