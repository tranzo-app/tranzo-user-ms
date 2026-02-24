package com.tranzo.tranzo_user_ms.trip.service;

import com.tranzo.tranzo_user_ms.commons.exception.*;
import com.tranzo.tranzo_user_ms.trip.dto.*;
import com.tranzo.tranzo_user_ms.trip.enums.*;
import com.tranzo.tranzo_user_ms.trip.exception.TripPublishException;
import com.tranzo.tranzo_user_ms.trip.events.TripEventPublisher;
import com.tranzo.tranzo_user_ms.trip.model.*;
import com.tranzo.tranzo_user_ms.trip.repository.*;
import com.tranzo.tranzo_user_ms.trip.utility.UserUtil;
import com.tranzo.tranzo_user_ms.trip.validation.TripPublishEligibilityValidator;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TripManagementService Unit Tests")
class TripManagementServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TripMemberRepository tripMemberRepository;

    @Mock
    private TripItineraryRepository tripItineraryRepository;

    @Mock
    private TripQueryRepository tripQueryRepository;

    @Mock
    private TripReportRepository tripReportRepository;

    @Mock
    private TripPublishEligibilityValidator tripPublishEligibilityValidator;

    @Mock
    private TripEventPublisher tripEventPublisher;

    @Mock
    private UserUtil userUtil;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private TripManagementService tripManagementService;

    private UUID userId;
    private UUID tripId;
    private TripEntity tripEntity;
    private TripDto tripDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        tripId = UUID.randomUUID();
        tripEntity = createSampleTripEntity();
        tripDto = createSampleTripDto();
    }

    // ============== CREATE DRAFT TRIP TESTS ==============

    @Test
    @DisplayName("Should create draft trip successfully")
    void testCreateDraftTrip_Success() {
        // Given
        when(tripRepository.save(any(TripEntity.class))).thenReturn(tripEntity);
        when(tagRepository.findByTagNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(tagRepository.save(any(TagEntity.class))).thenReturn(new TagEntity());

        // When
        TripResponseDto response = tripManagementService.createDraftTrip(tripDto, userId);

        // Then
        assertNotNull(response);
        assertEquals(tripEntity.getTripId(), response.getTripId());
        assertEquals(TripStatus.DRAFT, response.getTripStatus());
        verify(tripRepository, times(1)).save(any(TripEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when start date is after end date")
    void testCreateDraftTrip_InvalidDateRange() {
        // Given
        TripDto invalidDto = createSampleTripDto();
        invalidDto.setTripStartDate(LocalDate.of(2026, 3, 1));
        invalidDto.setTripEndDate(LocalDate.of(2026, 2, 1));

        // When & Then
        assertThrows(TripPublishException.class, () ->
            tripManagementService.createDraftTrip(invalidDto, userId)
        );
    }

    @Test
    @DisplayName("Should create draft trip with policy")
    void testCreateDraftTrip_WithPolicy() {
        // Given
        when(tripRepository.save(any(TripEntity.class))).thenReturn(tripEntity);
        when(tagRepository.findByTagNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(tagRepository.save(any(TagEntity.class))).thenReturn(new TagEntity());

        TripPolicyDto policyDto = new TripPolicyDto();
        policyDto.setCancellationPolicy("No refund after 7 days");
        policyDto.setRefundPolicy("Full refund if cancelled before 7 days");
        tripDto.setTripPolicy(policyDto);

        // When
        TripResponseDto response = tripManagementService.createDraftTrip(tripDto, userId);

        // Then
        assertNotNull(response);
        verify(tripRepository, times(1)).save(any(TripEntity.class));
    }

    @Test
    @DisplayName("Should create draft trip with metadata")
    void testCreateDraftTrip_WithMetadata() {
        // Given
        when(tripRepository.save(any(TripEntity.class))).thenReturn(tripEntity);
        when(tagRepository.findByTagNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(tagRepository.save(any(TagEntity.class))).thenReturn(new TagEntity());

        TripMetaDataDto metaDataDto = new TripMetaDataDto();
        metaDataDto.setTripSummary(Map.of("en", "Summer adventure"));
        metaDataDto.setWhatsIncluded(Map.of("en", "Accommodation, Food"));
        metaDataDto.setWhatsExcluded(Map.of("en", "Transport"));
        tripDto.setTripMetaData(metaDataDto);

        // When
        TripResponseDto response = tripManagementService.createDraftTrip(tripDto, userId);

        // Then
        assertNotNull(response);
        verify(tripRepository, times(1)).save(any(TripEntity.class));
    }

    @Test
    @DisplayName("Should create draft trip with itineraries")
    void testCreateDraftTrip_WithItineraries() {
        // Given
        when(tripRepository.save(any(TripEntity.class))).thenReturn(tripEntity);
        when(tagRepository.findByTagNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(tagRepository.save(any(TagEntity.class))).thenReturn(new TagEntity());

        TripItineraryDto itineraryDto = new TripItineraryDto();
        itineraryDto.setDayNumber(1);
        itineraryDto.setTitle("Day 1 - Arrival");
        itineraryDto.setDescription("Arrival and settling in");
        itineraryDto.setActivities(Map.of("en", "Sightseeing"));
        itineraryDto.setMeals(Map.of("en", "Breakfast, Dinner"));
        itineraryDto.setStay(Map.of("en", "Hotel ABC"));
        tripDto.setTripItineraries(new HashSet<>(Collections.singletonList(itineraryDto)));

        // When
        TripResponseDto response = tripManagementService.createDraftTrip(tripDto, userId);

        // Then
        assertNotNull(response);
        verify(tripRepository, times(1)).save(any(TripEntity.class));
    }

    @Test
    @DisplayName("Should create draft trip with existing tags")
    void testCreateDraftTrip_WithExistingTags() {
        // Given
        TagEntity existingTag = new TagEntity();
        existingTag.setTagName("Adventure");
        when(tripRepository.save(any(TripEntity.class))).thenReturn(tripEntity);
        when(tagRepository.findByTagNameIgnoreCase("Adventure")).thenReturn(Optional.of(existingTag));

        // When
        TripResponseDto response = tripManagementService.createDraftTrip(tripDto, userId);

        // Then
        assertNotNull(response);
        verify(tagRepository, never()).save(any(TagEntity.class));
    }

    // ============== UPDATE DRAFT TRIP TESTS ==============

    @Test
    @DisplayName("Should update draft trip successfully")
    void testUpdateDraftTrip_Success() {
        // Given
        TripEntity draftTrip = createSampleTripEntity();
        draftTrip.setTripStatus(TripStatus.DRAFT);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(draftTrip));
        when(tripRepository.save(any(TripEntity.class))).thenReturn(draftTrip);
        doNothing().when(userUtil).validateUserIsHost(tripId, userId);
        when(tagRepository.findByTagNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(tagRepository.save(any(TagEntity.class))).thenReturn(new TagEntity());

        // When
        TripResponseDto response = tripManagementService.updateDraftTrip(tripDto, tripId, userId);

        // Then
        assertNotNull(response);
        assertEquals(TripStatus.DRAFT, response.getTripStatus());
        verify(tripRepository, times(1)).save(any(TripEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-draft trip")
    void testUpdateDraftTrip_NonDraftTrip() {
        // Given
        TripEntity publishedTrip = createSampleTripEntity();
        publishedTrip.setTripStatus(TripStatus.PUBLISHED);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(publishedTrip));

        // When & Then
        assertThrows(ConflictException.class, () ->
            tripManagementService.updateDraftTrip(tripDto, tripId, userId)
        );
    }

    @Test
    @DisplayName("Should throw exception when trip not found for update")
    void testUpdateDraftTrip_TripNotFound() {
        // Given
        when(tripRepository.findById(tripId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TripPublishException.class, () ->
            tripManagementService.updateDraftTrip(tripDto, tripId, userId)
        );
    }

    @Test
    @DisplayName("Should throw exception when user is not host for update")
    void testUpdateDraftTrip_NotHost() {
        // Given
        TripEntity draftTrip = createSampleTripEntity();
        draftTrip.setTripStatus(TripStatus.DRAFT);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(draftTrip));
        doThrow(new ForbiddenException("Not host")).when(userUtil).validateUserIsHost(tripId, userId);

        // When & Then
        assertThrows(ForbiddenException.class, () ->
            tripManagementService.updateDraftTrip(tripDto, tripId, userId)
        );
    }

    // ============== FETCH TRIP TESTS ==============

    @Test
    @DisplayName("Should fetch trip details successfully")
    void testFetchTrip_Success() {
        // Given
        TripEntity publishedTrip = createSampleTripEntity();
        publishedTrip.setTripStatus(TripStatus.PUBLISHED);
        publishedTrip.setVisibilityStatus(VisibilityStatus.PUBLIC);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(publishedTrip));

        // When
        TripViewDto response = tripManagementService.fetchTrip(tripId, userId);

        // Then
        assertNotNull(response);
        verify(tripRepository, times(1)).findById(tripId);
    }

    @Test
    @DisplayName("Should throw exception when trip not found for fetch")
    void testFetchTrip_TripNotFound() {
        // Given
        when(tripRepository.findById(tripId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TripPublishException.class, () ->
            tripManagementService.fetchTrip(tripId, userId)
        );
    }

    @Test
    @DisplayName("Should throw exception when fetching cancelled trip")
    void testFetchTrip_CancelledTrip() {
        // Given
        TripEntity cancelledTrip = createSampleTripEntity();
        cancelledTrip.setTripStatus(TripStatus.CANCELLED);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(cancelledTrip));

        // When & Then
        assertThrows(ForbiddenException.class, () ->
            tripManagementService.fetchTrip(tripId, userId)
        );
    }

    @Test
    @DisplayName("Should throw exception when accessing private trip as non-member")
    void testFetchTrip_PrivateTripNonMember() {
        // Given
        TripEntity privateTrip = createSampleTripEntity();
        privateTrip.setTripStatus(TripStatus.PUBLISHED);
        privateTrip.setVisibilityStatus(VisibilityStatus.PRIVATE);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(privateTrip));
        when(tripMemberRepository.findByTrip_TripIdAndUserIdAndStatus(tripId, userId, TripMemberStatus.ACTIVE))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ForbiddenException.class, () ->
            tripManagementService.fetchTrip(tripId, userId)
        );
    }

    @Test
    @DisplayName("Should allow member to fetch private trip")
    void testFetchTrip_PrivateTripMember() {
        // Given
        TripEntity privateTrip = createSampleTripEntity();
        privateTrip.setTripStatus(TripStatus.PUBLISHED);
        privateTrip.setVisibilityStatus(VisibilityStatus.PRIVATE);
        TripMemberEntity member = new TripMemberEntity();
        member.setUserId(userId);
        member.setRole(TripMemberRole.MEMBER);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(privateTrip));
        when(tripMemberRepository.findByTrip_TripIdAndUserIdAndStatus(tripId, userId, TripMemberStatus.ACTIVE))
            .thenReturn(Optional.of(member));

        // When
        TripViewDto response = tripManagementService.fetchTrip(tripId, userId);

        // Then
        assertNotNull(response);
    }

    // ============== CANCEL TRIP TESTS ==============

    @Test
    @DisplayName("Should cancel draft trip successfully")
    void testCancelTrip_DraftTrip_Success() {
        // Given
        TripEntity draftTrip = createSampleTripEntity();
        draftTrip.setTripStatus(TripStatus.DRAFT);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(draftTrip));
        doNothing().when(userUtil).validateUserIsHost(tripId, userId);
        when(tripRepository.save(any(TripEntity.class))).thenReturn(draftTrip);

        // When
        tripManagementService.cancelTrip(tripId, userId);

        // Then
        assertEquals(TripStatus.CANCELLED, draftTrip.getTripStatus());
        verify(tripRepository, times(1)).save(any(TripEntity.class));
    }

    @Test
    @DisplayName("Should cancel published trip successfully")
    void testCancelTrip_PublishedTrip_Success() {
        // Given
        TripEntity publishedTrip = createSampleTripEntity();
        publishedTrip.setTripStatus(TripStatus.PUBLISHED);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(publishedTrip));
        doNothing().when(userUtil).validateUserIsHost(tripId, userId);
        when(tripRepository.save(any(TripEntity.class))).thenReturn(publishedTrip);
        when(tripMemberRepository.findByTrip_TripIdAndStatus(tripId, TripMemberStatus.ACTIVE))
            .thenReturn(Collections.emptyList());

        // When
        tripManagementService.cancelTrip(tripId, userId);

        // Then
        assertEquals(TripStatus.CANCELLED, publishedTrip.getTripStatus());
        verify(tripRepository, times(1)).save(any(TripEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when cancelling already cancelled trip")
    void testCancelTrip_AlreadyCancelled() {
        // Given
        TripEntity cancelledTrip = createSampleTripEntity();
        cancelledTrip.setTripStatus(TripStatus.CANCELLED);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(cancelledTrip));

        // When & Then
        assertThrows(ConflictException.class, () ->
            tripManagementService.cancelTrip(tripId, userId)
        );
    }

    @Test
    @DisplayName("Should throw exception when cancelling ongoing trip")
    void testCancelTrip_OngoingTrip() {
        // Given
        TripEntity ongoingTrip = createSampleTripEntity();
        ongoingTrip.setTripStatus(TripStatus.ONGOING);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(ongoingTrip));

        // When & Then
        assertThrows(ConflictException.class, () ->
            tripManagementService.cancelTrip(tripId, userId)
        );
    }

    // ============== PUBLISH TRIP TESTS ==============

    @Test
    @DisplayName("Should publish trip successfully")
    void testPublishTrip_Success() {
        // Given
        TripEntity draftTrip = createSampleTripEntity();
        draftTrip.setTripStatus(TripStatus.DRAFT);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(draftTrip));
        doNothing().when(userUtil).validateUserIsHost(tripId, userId);
        doNothing().when(tripPublishEligibilityValidator).validate(any(TripEntity.class));
        when(tripRepository.save(any(TripEntity.class))).thenReturn(draftTrip);

        // When
        TripResponseDto response = tripManagementService.publishTrip(tripId, userId);

        // Then
        assertNotNull(response);
        assertEquals(TripStatus.PUBLISHED, draftTrip.getTripStatus());
        verify(tripPublishEligibilityValidator, times(1)).validate(any(TripEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when publishing fails validation")
    void testPublishTrip_ValidationFails() {
        // Given
        TripEntity draftTrip = createSampleTripEntity();
        draftTrip.setTripStatus(TripStatus.DRAFT);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(draftTrip));
        doNothing().when(userUtil).validateUserIsHost(tripId, userId);
        doThrow(new TripPublishException(TripPublishErrorCode.TITLE_MISSING))
            .when(tripPublishEligibilityValidator).validate(any(TripEntity.class));

        // When & Then
        assertThrows(TripPublishException.class, () ->
            tripManagementService.publishTrip(tripId, userId)
        );
    }

    // ============== AUTO MARK ONGOING TESTS ==============

    @Test
    @DisplayName("Should auto mark trips as ongoing when start date reached")
    void testAutoMarkTripsAsOngoing_Success() {
        // Given
        LocalDate yesterday = LocalDate.now().minusDays(1);
        TripEntity publishedTrip = createSampleTripEntity();
        publishedTrip.setTripStatus(TripStatus.PUBLISHED);
        publishedTrip.setTripStartDate(yesterday);

        List<TripEntity> trips = Collections.singletonList(publishedTrip);
        when(tripRepository.findByTripStatusAndTripStartDateLessThanEqual(TripStatus.PUBLISHED, LocalDate.now()))
            .thenReturn(trips);

        // When
        tripManagementService.autoMarkTripsAsOngoing();

        // Then
        assertEquals(TripStatus.ONGOING, publishedTrip.getTripStatus());
    }

    @Test
    @DisplayName("Should not mark trips when no trips found for ongoing transition")
    void testAutoMarkTripsAsOngoing_NoTripsFound() {
        // Given
        when(tripRepository.findByTripStatusAndTripStartDateLessThanEqual(TripStatus.PUBLISHED, LocalDate.now()))
            .thenReturn(Collections.emptyList());

        // When
        tripManagementService.autoMarkTripsAsOngoing();

        // Then
        verify(tripRepository, times(1)).findByTripStatusAndTripStartDateLessThanEqual(TripStatus.PUBLISHED, LocalDate.now());
    }

    // ============== AUTO MARK COMPLETED TESTS ==============

    @Test
    @DisplayName("Should auto mark trips as completed when end date passed")
    void testAutoMarkTripsAsCompleted_Success() {
        // Given
        LocalDate yesterday = LocalDate.now().minusDays(1);
        TripEntity ongoingTrip = createSampleTripEntity();
        ongoingTrip.setTripStatus(TripStatus.ONGOING);
        ongoingTrip.setTripEndDate(yesterday);

        List<TripEntity> trips = Collections.singletonList(ongoingTrip);
        when(tripRepository.findByTripStatusAndTripEndDateBefore(TripStatus.ONGOING, LocalDate.now()))
            .thenReturn(trips);
        when(tripMemberRepository.findByTrip_TripIdAndStatus(eq(ongoingTrip.getTripId()), eq(TripMemberStatus.ACTIVE)))
            .thenReturn(Collections.emptyList());

        // When
        tripManagementService.autoMarkTripsAsCompleted();

        // Then
        assertEquals(TripStatus.COMPLETED, ongoingTrip.getTripStatus());
    }

    // ============== UPDATE TRIP (PUBLISHED) TESTS ==============

    @Test
    @DisplayName("Should update published trip successfully and publish TripDetailsChangedEvent")
    void testUpdateTrip_Published_Success() {
        TripEntity publishedTrip = createSampleTripEntity();
        publishedTrip.setTripStatus(TripStatus.PUBLISHED);
        TripMemberEntity member = new TripMemberEntity();
        member.setUserId(userId);
        List<TripMemberEntity> members = Collections.singletonList(member);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(publishedTrip));
        doNothing().when(userUtil).validateUserIsHost(tripId, userId);
        when(tripRepository.save(any(TripEntity.class))).thenReturn(publishedTrip);
        when(tripMemberRepository.findByTrip_TripIdAndStatus(tripId, TripMemberStatus.ACTIVE)).thenReturn(members);

        TripResponseDto response = tripManagementService.updateTrip(tripDto, tripId, userId);

        assertNotNull(response);
        verify(tripRepository).save(any(TripEntity.class));
        verify(applicationEventPublisher).publishEvent(any(com.tranzo.tranzo_user_ms.commons.events.TripDetailsChangedEvent.class));
    }

    @Test
    @DisplayName("Should throw when updateTrip for non-published trip")
    void testUpdateTrip_NotPublished() {
        TripEntity draftTrip = createSampleTripEntity();
        draftTrip.setTripStatus(TripStatus.DRAFT);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(draftTrip));

        assertThrows(ConflictException.class, () ->
            tripManagementService.updateTrip(tripDto, tripId, userId));
    }

    @Test
    @DisplayName("Should throw when updateTrip trip not found")
    void testUpdateTrip_TripNotFound() {
        when(tripRepository.findById(tripId)).thenReturn(Optional.empty());

        assertThrows(TripPublishException.class, () ->
            tripManagementService.updateTrip(tripDto, tripId, userId));
    }

    @Test
    @DisplayName("Should throw when updateTrip with invalid date range")
    void testUpdateTrip_InvalidDateRange() {
        TripEntity publishedTrip = createSampleTripEntity();
        publishedTrip.setTripStatus(TripStatus.PUBLISHED);
        tripDto.setTripStartDate(LocalDate.of(2026, 6, 10));
        tripDto.setTripEndDate(LocalDate.of(2026, 6, 1));

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(publishedTrip));
        doNothing().when(userUtil).validateUserIsHost(tripId, userId);

        assertThrows(TripPublishException.class, () ->
            tripManagementService.updateTrip(tripDto, tripId, userId));
    }

    // ============== PROMOTE TO CO-HOST TESTS ==============

    @Test
    @DisplayName("Should promote member to co-host and publish MemberPromotedToCoHostEvent")
    void testPromoteToCoHost_Success() {
        UUID participantUserId = UUID.randomUUID();
        TripEntity trip = createSampleTripEntity();
        trip.setTripStatus(TripStatus.PUBLISHED);
        TripMemberEntity hostMember = new TripMemberEntity();
        hostMember.setUserId(userId);
        hostMember.setRole(TripMemberRole.HOST);
        hostMember.setStatus(TripMemberStatus.ACTIVE);
        TripMemberEntity member = new TripMemberEntity();
        member.setUserId(participantUserId);
        member.setRole(TripMemberRole.MEMBER);
        member.setStatus(TripMemberStatus.ACTIVE);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        doNothing().when(userUtil).validateUserIsHost(tripId, userId);
        when(tripMemberRepository.findByTrip_TripIdAndUserIdAndStatus(tripId, participantUserId, TripMemberStatus.ACTIVE))
            .thenReturn(Optional.of(member));
        when(tripMemberRepository.save(any(TripMemberEntity.class))).thenReturn(member);
        when(tripMemberRepository.findByTrip_TripIdAndStatus(tripId, TripMemberStatus.ACTIVE))
            .thenReturn(Arrays.asList(hostMember, member));

        tripManagementService.promoteToCoHost(userId, tripId, participantUserId);

        assertEquals(TripMemberRole.CO_HOST, member.getRole());
        verify(applicationEventPublisher).publishEvent(any(com.tranzo.tranzo_user_ms.commons.events.MemberPromotedToCoHostEvent.class));
    }

    @Test
    @DisplayName("Should throw when host promotes self to co-host")
    void testPromoteToCoHost_HostPromotesSelf() {
        TripEntity trip = createSampleTripEntity();
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        doNothing().when(userUtil).validateUserIsHost(tripId, userId);

        assertThrows(BadRequestException.class, () ->
            tripManagementService.promoteToCoHost(userId, tripId, userId));
    }

    @Test
    @DisplayName("Should throw when participant already co-host")
    void testPromoteToCoHost_AlreadyCoHost() {
        UUID participantUserId = UUID.randomUUID();
        TripMemberEntity member = new TripMemberEntity();
        member.setUserId(participantUserId);
        member.setRole(TripMemberRole.CO_HOST);
        member.setStatus(TripMemberStatus.ACTIVE);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(createSampleTripEntity()));
        doNothing().when(userUtil).validateUserIsHost(tripId, userId);
        when(tripMemberRepository.findByTrip_TripIdAndUserIdAndStatus(tripId, participantUserId, TripMemberStatus.ACTIVE))
            .thenReturn(Optional.of(member));

        assertThrows(ConflictException.class, () ->
            tripManagementService.promoteToCoHost(userId, tripId, participantUserId));
    }

    @Test
    @DisplayName("Should throw when participant not found for promote")
    void testPromoteToCoHost_ParticipantNotFound() {
        UUID participantUserId = UUID.randomUUID();
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(createSampleTripEntity()));
        doNothing().when(userUtil).validateUserIsHost(tripId, userId);
        when(tripMemberRepository.findByTrip_TripIdAndUserIdAndStatus(tripId, participantUserId, TripMemberStatus.ACTIVE))
            .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
            tripManagementService.promoteToCoHost(userId, tripId, participantUserId));
    }

    // ============== MARK TRIP FULL TESTS ==============

    @Test
    @DisplayName("Should mark trip full and publish TripMarkedFullByHostEvent")
    void testMarkTripFull_Success() {
        TripEntity trip = createSampleTripEntity();
        trip.setIsFull(false);
        TripMemberEntity host = new TripMemberEntity();
        host.setUserId(userId);
        host.setRole(TripMemberRole.HOST);
        TripMemberEntity other = new TripMemberEntity();
        other.setUserId(UUID.randomUUID());
        other.setRole(TripMemberRole.MEMBER);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        doNothing().when(userUtil).validateUserIsHost(tripId, userId);
        when(tripRepository.save(any(TripEntity.class))).thenReturn(trip);
        when(tripMemberRepository.findByTrip_TripIdAndStatus(tripId, TripMemberStatus.ACTIVE))
            .thenReturn(Arrays.asList(host, other));

        tripManagementService.markTripFull(userId, tripId);

        assertTrue(trip.getIsFull());
        verify(applicationEventPublisher).publishEvent(any(com.tranzo.tranzo_user_ms.commons.events.TripMarkedFullByHostEvent.class));
    }

    @Test
    @DisplayName("Should throw when trip already marked full")
    void testMarkTripFull_AlreadyFull() {
        TripEntity trip = createSampleTripEntity();
        trip.setIsFull(true);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        doNothing().when(userUtil).validateUserIsHost(tripId, userId);

        assertThrows(ConflictException.class, () ->
            tripManagementService.markTripFull(userId, tripId));
    }

    @Test
    @DisplayName("Should throw when mark trip full and trip not found")
    void testMarkTripFull_TripNotFound() {
        when(tripRepository.findById(tripId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
            tripManagementService.markTripFull(userId, tripId));
    }

    // ============== ADD TRIP QnA TESTS ==============

    @Test
    @DisplayName("Should add trip QnA and publish TripQuestionAskedEvent")
    void testAddTripQnA_Success() {
        TripEntity trip = createSampleTripEntity();
        trip.setTripStatus(TripStatus.PUBLISHED);
        CreateQnaRequestDto qnaDto = new CreateQnaRequestDto();
        qnaDto.setQuestion("Where do we meet?");
        TripMemberEntity member = new TripMemberEntity();
        member.setUserId(userId);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        doNothing().when(userUtil).validateUserIsHost(tripId, userId);
        when(tripQueryRepository.save(any(TripQueryEntity.class))).thenReturn(new TripQueryEntity());
        when(tripMemberRepository.findByTrip_TripIdAndStatus(tripId, TripMemberStatus.ACTIVE))
            .thenReturn(Collections.singletonList(member));

        tripManagementService.addTripQnA(userId, qnaDto, tripId);

        verify(tripQueryRepository).save(any(TripQueryEntity.class));
        verify(applicationEventPublisher).publishEvent(any(com.tranzo.tranzo_user_ms.commons.events.TripQuestionAskedEvent.class));
    }

    @Test
    @DisplayName("Should throw when add QnA with empty question")
    void testAddTripQnA_EmptyQuestion() {
        TripEntity trip = createSampleTripEntity();
        trip.setTripStatus(TripStatus.PUBLISHED);
        CreateQnaRequestDto qnaDto = new CreateQnaRequestDto();
        qnaDto.setQuestion("   ");

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        doNothing().when(userUtil).validateUserIsHost(tripId, userId);

        assertThrows(BadRequestException.class, () ->
            tripManagementService.addTripQnA(userId, qnaDto, tripId));
    }

    @Test
    @DisplayName("Should throw when add QnA for non-published trip")
    void testAddTripQnA_NotPublished() {
        TripEntity trip = createSampleTripEntity();
        trip.setTripStatus(TripStatus.DRAFT);
        CreateQnaRequestDto qnaDto = new CreateQnaRequestDto();
        qnaDto.setQuestion("Where?");

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        doNothing().when(userUtil).validateUserIsHost(tripId, userId);

        assertThrows(ConflictException.class, () ->
            tripManagementService.addTripQnA(userId, qnaDto, tripId));
    }

    // ============== ANSWER TRIP QnA TESTS ==============

    @Test
    @DisplayName("Should answer trip QnA and publish TripQuestionAnsweredEvent")
    void testAnswerTripQnA_Success() {
        UUID qnaId = UUID.randomUUID();
        UUID askedByUserId = UUID.randomUUID();
        TripEntity trip = createSampleTripEntity();
        trip.setTripStatus(TripStatus.PUBLISHED);
        TripQueryEntity tripQuery = TripQueryEntity.builder()
            .queryId(qnaId)
            .askedBy(askedByUserId)
            .question("Where?")
            .answer(null)
            .trip(trip)
            .visibility(TripQueryVisibility.HOST_AND_CO_HOSTS)
            .build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripQueryRepository.findByQueryIdAndTrip_TripId(qnaId, tripId)).thenReturn(Optional.of(tripQuery));
        when(tripQueryRepository.save(any(TripQueryEntity.class))).thenReturn(tripQuery);

        AnswerQnaRequestDto answerDto = new AnswerQnaRequestDto();
        answerDto.setAnswer("At the airport");
        tripManagementService.answerTripQnA(userId, tripId, qnaId, answerDto);

        verify(tripQueryRepository).save(any(TripQueryEntity.class));
        verify(applicationEventPublisher).publishEvent(any(com.tranzo.tranzo_user_ms.commons.events.TripQuestionAnsweredEvent.class));
    }

    @Test
    @DisplayName("Should throw when answer QnA already answered")
    void testAnswerTripQnA_AlreadyAnswered() {
        UUID qnaId = UUID.randomUUID();
        TripEntity trip = createSampleTripEntity();
        TripQueryEntity tripQuery = TripQueryEntity.builder()
            .queryId(qnaId)
            .askedBy(userId)
            .question("Where?")
            .answer("Already answered")
            .trip(trip)
            .visibility(TripQueryVisibility.HOST_AND_CO_HOSTS)
            .build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripQueryRepository.findByQueryIdAndTrip_TripId(qnaId, tripId)).thenReturn(Optional.of(tripQuery));

        AnswerQnaRequestDto answerDto = new AnswerQnaRequestDto();
        answerDto.setAnswer("New answer");
        assertThrows(ConflictException.class, () ->
            tripManagementService.answerTripQnA(userId, tripId, qnaId, answerDto));
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
        trip.setTripStatus(TripStatus.DRAFT);
        trip.setTripItineraries(new HashSet<>());
        trip.setTripMembers(new HashSet<>());
        trip.setTripTags(new HashSet<>());
        return trip;
    }

    private TripDto createSampleTripDto() {
        TripDto dto = new TripDto();
        dto.setTripTitle("Sample Trip");
        dto.setTripDescription("Sample Description");
        dto.setTripDestination("Paris");
        dto.setTripStartDate(LocalDate.of(2026, 6, 1));
        dto.setTripEndDate(LocalDate.of(2026, 6, 10));
        dto.setEstimatedBudget(5000.0);
        dto.setMaxParticipants(10);
        dto.setJoinPolicy(JoinPolicy.OPEN);
        dto.setVisibilityStatus(VisibilityStatus.PUBLIC);
        dto.setTripTags(new HashSet<>(Collections.singletonList(new TripTagDto("Adventure"))));
        dto.setTripItineraries(new HashSet<>());
        return dto;
    }
}

