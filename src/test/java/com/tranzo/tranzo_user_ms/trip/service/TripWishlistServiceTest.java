package com.tranzo.tranzo_user_ms.trip.service;

import com.tranzo.tranzo_user_ms.commons.exception.ConflictException;
import com.tranzo.tranzo_user_ms.commons.exception.EntityNotFoundException;
import com.tranzo.tranzo_user_ms.trip.dto.TripWishlistRequestDto;
import com.tranzo.tranzo_user_ms.trip.dto.TripWishlistResponseDto;
import com.tranzo.tranzo_user_ms.trip.enums.TripStatus;
import com.tranzo.tranzo_user_ms.trip.enums.VisibilityStatus;
import com.tranzo.tranzo_user_ms.trip.model.TripEntity;
import com.tranzo.tranzo_user_ms.trip.model.TripWishlistEntity;
import com.tranzo.tranzo_user_ms.trip.repository.TripRepository;
import com.tranzo.tranzo_user_ms.trip.repository.TripWishlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TripWishlistService Unit Tests")
class TripWishlistServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripWishlistRepository tripWishlistRepository;

    @InjectMocks
    private TripWishlistService tripWishlistService;

    private UUID tripId;
    private UUID userId;
    private TripEntity tripEntity;
    private TripWishlistRequestDto wishlistRequestDto;
    private TripWishlistEntity wishlistEntity;

    @BeforeEach
    void setUp() {
        tripId = UUID.randomUUID();
        userId = UUID.randomUUID();
        tripEntity = createSampleTripEntity();
        wishlistRequestDto = createSampleWishlistRequestDto();
        wishlistEntity = createSampleWishlistEntity();
    }

    // ============== ADD TO WISHLIST TESTS ==============

    @Test
    @DisplayName("Should add trip to wishlist successfully")
    void testAddTripToWishlist_Success() {
        // Given
        when(tripRepository.findByTripIdAndTripStatus(tripId, TripStatus.PUBLISHED))
            .thenReturn(Optional.of(tripEntity));
        when(tripWishlistRepository.existsByUserIdAndTrip_TripId(userId, tripId))
            .thenReturn(false);
        when(tripWishlistRepository.save(any(TripWishlistEntity.class)))
            .thenReturn(wishlistEntity);

        // When
        TripWishlistResponseDto response = tripWishlistService.addTripToWishlist(wishlistRequestDto, userId);

        // Then
        assertNotNull(response);
        assertEquals(tripEntity.getTripId(), response.getTripId());
        assertEquals(tripEntity.getTripTitle(), response.getTripTitle());
        assertEquals(tripEntity.getTripDestination(), response.getDestination());
        verify(tripWishlistRepository, times(1)).save(any(TripWishlistEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when trip not found")
    void testAddTripToWishlist_TripNotFound() {
        // Given
        when(tripRepository.findByTripIdAndTripStatus(tripId, TripStatus.PUBLISHED))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () ->
            tripWishlistService.addTripToWishlist(wishlistRequestDto, userId)
        );
    }

    @Test
    @DisplayName("Should throw exception when trip already wishlisted")
    void testAddTripToWishlist_AlreadyWishlisted() {
        // Given
        when(tripRepository.findByTripIdAndTripStatus(tripId, TripStatus.PUBLISHED))
            .thenReturn(Optional.of(tripEntity));
        when(tripWishlistRepository.existsByUserIdAndTrip_TripId(userId, tripId))
            .thenReturn(true);

        // When & Then
        assertThrows(ConflictException.class, () ->
            tripWishlistService.addTripToWishlist(wishlistRequestDto, userId)
        );
    }

    @Test
    @DisplayName("Should return response with correct trip details")
    void testAddTripToWishlist_ResponseValidation() {
        // Given
        when(tripRepository.findByTripIdAndTripStatus(tripId, TripStatus.PUBLISHED))
            .thenReturn(Optional.of(tripEntity));
        when(tripWishlistRepository.existsByUserIdAndTrip_TripId(userId, tripId))
            .thenReturn(false);
        when(tripWishlistRepository.save(any(TripWishlistEntity.class)))
            .thenReturn(wishlistEntity);

        // When
        TripWishlistResponseDto response = tripWishlistService.addTripToWishlist(wishlistRequestDto, userId);

        // Then
        assertNotNull(response);
        assertEquals(TripStatus.PUBLISHED, response.getTripStatus());
        assertNotNull(response.getCreatedAt());
    }

    // ============== REMOVE FROM WISHLIST TESTS ==============

    @Test
    @DisplayName("Should remove trip from wishlist successfully")
    void testRemoveTripFromWishlist_Success() {
        // Given
        when(tripWishlistRepository.findByTrip_TripIdAndUserId(tripId, userId))
            .thenReturn(Optional.of(wishlistEntity));
        doNothing().when(tripWishlistRepository).delete(any(TripWishlistEntity.class));

        // When
        tripWishlistService.removeTripFromWishlist(tripId, userId);

        // Then
        verify(tripWishlistRepository, times(1)).delete(any(TripWishlistEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when wishlist not found for removal")
    void testRemoveTripFromWishlist_NotFound() {
        // Given
        when(tripWishlistRepository.findByTrip_TripIdAndUserId(tripId, userId))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () ->
            tripWishlistService.removeTripFromWishlist(tripId, userId)
        );
    }

    @Test
    @DisplayName("Should verify deletion is called exactly once")
    void testRemoveTripFromWishlist_VerifyDeletion() {
        // Given
        when(tripWishlistRepository.findByTrip_TripIdAndUserId(tripId, userId))
            .thenReturn(Optional.of(wishlistEntity));

        // When
        tripWishlistService.removeTripFromWishlist(tripId, userId);

        // Then
        verify(tripWishlistRepository, times(1)).delete(wishlistEntity);
        verify(tripWishlistRepository, never()).save(any());
    }

    // ============== FETCH WISHLIST TESTS ==============

    @Test
    @DisplayName("Should fetch user's wishlist successfully")
    void testFetchWishlist_Success() {
        // Given
        List<TripWishlistEntity> wishlistEntities = Collections.singletonList(wishlistEntity);
        when(tripWishlistRepository.findByUserIdOrderByCreatedAtDesc(userId))
            .thenReturn(wishlistEntities);

        // When
        List<TripWishlistResponseDto> response = tripWishlistService.fetchWishlist(userId);

        // Then
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals(1, response.size());
        assertEquals(tripEntity.getTripId(), response.getFirst().getTripId());
    }

    @Test
    @DisplayName("Should return empty list when wishlist is empty")
    void testFetchWishlist_EmptyWishlist() {
        // Given
        when(tripWishlistRepository.findByUserIdOrderByCreatedAtDesc(userId))
            .thenReturn(Collections.emptyList());

        // When
        List<TripWishlistResponseDto> response = tripWishlistService.fetchWishlist(userId);

        // Then
        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    @DisplayName("Should fetch wishlist in correct order (newest first)")
    void testFetchWishlist_VerifyOrder() {
        // Given
        TripWishlistEntity wishlist1 = createSampleWishlistEntity();
        wishlist1.setCreatedAt(LocalDateTime.now());

        TripEntity trip2 = createSampleTripEntity();
        trip2.setTripId(UUID.randomUUID());
        trip2.setTripTitle("Trip 2");

        TripWishlistEntity wishlist2 = new TripWishlistEntity();
        wishlist2.setTrip(trip2);
        wishlist2.setUserId(userId);
        wishlist2.setCreatedAt(LocalDateTime.now().plusHours(1));

        List<TripWishlistEntity> wishlistEntities = List.of(wishlist2, wishlist1);
        when(tripWishlistRepository.findByUserIdOrderByCreatedAtDesc(userId))
            .thenReturn(wishlistEntities);

        // When
        List<TripWishlistResponseDto> response = tripWishlistService.fetchWishlist(userId);

        // Then
        assertEquals(2, response.size());
        // Verify order - newest first
        assertTrue(response.get(0).getCreatedAt().isAfter(response.get(1).getCreatedAt()));
    }

    @Test
    @DisplayName("Should fetch wishlist with correct trip details for multiple items")
    void testFetchWishlist_MultipleItems() {
        // Given
        TripEntity trip2 = createSampleTripEntity();
        trip2.setTripId(UUID.randomUUID());
        trip2.setTripTitle("Trip 2");
        trip2.setTripDestination("London");

        TripWishlistEntity wishlist2 = new TripWishlistEntity();
        wishlist2.setTrip(trip2);
        wishlist2.setUserId(userId);

        List<TripWishlistEntity> wishlistEntities = List.of(wishlistEntity, wishlist2);
        when(tripWishlistRepository.findByUserIdOrderByCreatedAtDesc(userId))
            .thenReturn(wishlistEntities);

        // When
        List<TripWishlistResponseDto> response = tripWishlistService.fetchWishlist(userId);

        // Then
        assertEquals(2, response.size());
        assertEquals(tripEntity.getTripTitle(), response.getFirst().getTripTitle());
        assertEquals(trip2.getTripTitle(), response.get(1).getTripTitle());
    }

    @Test
    @DisplayName("Should map all fields correctly in response")
    void testFetchWishlist_FieldMapping() {
        // Given
        List<TripWishlistEntity> wishlistEntities = Collections.singletonList(wishlistEntity);
        when(tripWishlistRepository.findByUserIdOrderByCreatedAtDesc(userId))
            .thenReturn(wishlistEntities);

        // When
        List<TripWishlistResponseDto> response = tripWishlistService.fetchWishlist(userId);

        // Then
        assertNotNull(response);
        TripWishlistResponseDto dto = response.getFirst();
        assertEquals(tripEntity.getTripId(), dto.getTripId());
        assertEquals(tripEntity.getTripTitle(), dto.getTripTitle());
        assertEquals(tripEntity.getTripDestination(), dto.getDestination());
        assertEquals(tripEntity.getTripStatus(), dto.getTripStatus());
        assertNotNull(dto.getCreatedAt());
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
        trip.setVisibilityStatus(VisibilityStatus.PUBLIC);
        trip.setTripStatus(TripStatus.PUBLISHED);
        trip.setCreatedAt(LocalDateTime.now());
        trip.setUpdatedAt(LocalDateTime.now());
        trip.setTripMembers(new HashSet<>());
        trip.setTripTags(new HashSet<>());
        trip.setTripItineraries(new HashSet<>());
        return trip;
    }

    private TripWishlistRequestDto createSampleWishlistRequestDto() {
        TripWishlistRequestDto dto = new TripWishlistRequestDto();
        dto.setTripId(tripId);
        return dto;
    }

    private TripWishlistEntity createSampleWishlistEntity() {
        TripWishlistEntity entity = new TripWishlistEntity();
        entity.setTrip(tripEntity);
        entity.setUserId(userId);
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }
}

