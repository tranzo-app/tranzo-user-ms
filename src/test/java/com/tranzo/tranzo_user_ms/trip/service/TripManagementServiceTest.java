package com.tranzo.tranzo_user_ms.trip.service;

import com.tranzo.tranzo_user_ms.commons.exception.*;
import com.tranzo.tranzo_user_ms.trip.dto.*;
import com.tranzo.tranzo_user_ms.trip.enums.*;
import com.tranzo.tranzo_user_ms.trip.exception.TripPublishException;
import com.tranzo.tranzo_user_ms.trip.model.*;
import com.tranzo.tranzo_user_ms.trip.repository.*;
import com.tranzo.tranzo_user_ms.trip.utility.UserUtil;
import com.tranzo.tranzo_user_ms.trip.validation.TripPublishEligibilityValidator;
import org.junit.jupiter.api.BeforeEach;
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
    private TripPublishEligibilityValidator tripPublishEligibilityValidator;

    @Mock
    private UserUtil userUtil;

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

        // When
        tripManagementService.autoMarkTripsAsCompleted();

        // Then
        assertEquals(TripStatus.COMPLETED, ongoingTrip.getTripStatus());
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

