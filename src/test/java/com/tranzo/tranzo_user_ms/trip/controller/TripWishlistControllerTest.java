package com.tranzo.tranzo_user_ms.trip.controller;

import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import com.tranzo.tranzo_user_ms.trip.dto.TripWishlistRequestDto;
import com.tranzo.tranzo_user_ms.trip.dto.TripWishlistResponseDto;
import com.tranzo.tranzo_user_ms.trip.enums.TripStatus;
import com.tranzo.tranzo_user_ms.trip.service.TripWishlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TripWishlistController Unit Tests")
class TripWishlistControllerTest {

    @Mock
    private TripWishlistService tripWishlistService;

    @InjectMocks
    private TripWishlistController tripWishlistController;

    private UUID userId;
    private UUID tripId;
    private TripWishlistRequestDto wishlistRequestDto;
    private TripWishlistResponseDto wishlistResponseDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        tripId = UUID.randomUUID();
        wishlistRequestDto = createSampleWishlistRequestDto();
        wishlistResponseDto = createSampleWishlistResponseDto();
    }

    // ============== ADD TO WISHLIST TESTS ==============

    @Test
    @DisplayName("Should add trip to wishlist successfully")
    void testAddToWishlist_Success() throws Exception {
        // Given
        when(tripWishlistService.addTripToWishlist(any(TripWishlistRequestDto.class), any(UUID.class)))
            .thenReturn(wishlistResponseDto);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            // When
            ResponseEntity<ResponseDto<TripWishlistResponseDto>> response =
                tripWishlistController.addToWishlist(wishlistRequestDto);

            // Then
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().getData());
            verify(tripWishlistService, times(1)).addTripToWishlist(eq(wishlistRequestDto), eq(userId));
        }
    }

    @Test
    @DisplayName("Should return 201 CREATED when adding to wishlist")
    void testAddToWishlist_HttpStatus() throws Exception {
        // Given
        when(tripWishlistService.addTripToWishlist(any(TripWishlistRequestDto.class), any(UUID.class)))
            .thenReturn(wishlistResponseDto);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            // When
            ResponseEntity<ResponseDto<TripWishlistResponseDto>> response =
                tripWishlistController.addToWishlist(wishlistRequestDto);

            // Then
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            ResponseDto<TripWishlistResponseDto> body = response.getBody();
            assertNotNull(body);
            assertNotNull(body.getData());
            assertEquals(tripId, body.getData().getTripId());
        }
    }

    @Test
    @DisplayName("Should pass correct trip ID to service when adding to wishlist")
    void testAddToWishlist_ParameterValidation() throws Exception {
        // Given
        when(tripWishlistService.addTripToWishlist(any(TripWishlistRequestDto.class), any(UUID.class)))
            .thenReturn(wishlistResponseDto);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            // When
            tripWishlistController.addToWishlist(wishlistRequestDto);

            // Then
            verify(tripWishlistService, times(1)).addTripToWishlist(
                eq(wishlistRequestDto),
                eq(userId)
            );
        }
    }

    // ============== REMOVE FROM WISHLIST TESTS ==============

    @Test
    @DisplayName("Should remove trip from wishlist successfully")
    void testRemoveFromWishlist_Success() throws Exception {
        // Given
        doNothing().when(tripWishlistService).removeTripFromWishlist(any(UUID.class), any(UUID.class));

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            // When
            ResponseEntity<ResponseDto<TripWishlistResponseDto>> response =
                tripWishlistController.removeFromWishlist(tripId);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(tripWishlistService, times(1)).removeTripFromWishlist(eq(tripId), eq(userId));
        }
    }

    @Test
    @DisplayName("Should return 200 OK when removing from wishlist")
    void testRemoveFromWishlist_HttpStatus() throws Exception {
        // Given
        doNothing().when(tripWishlistService).removeTripFromWishlist(any(UUID.class), any(UUID.class));

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            // When
            ResponseEntity<ResponseDto<TripWishlistResponseDto>> response =
                tripWishlistController.removeFromWishlist(tripId);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }
    }

    @Test
    @DisplayName("Should pass correct trip ID to service when removing from wishlist")
    void testRemoveFromWishlist_ParameterValidation() throws Exception {
        // Given
        doNothing().when(tripWishlistService).removeTripFromWishlist(any(UUID.class), any(UUID.class));

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            // When
            tripWishlistController.removeFromWishlist(tripId);

            // Then
            verify(tripWishlistService, times(1)).removeTripFromWishlist(
                eq(tripId),
                eq(userId)
            );
        }
    }

    // ============== FETCH WISHLIST TESTS ==============

    @Test
    @DisplayName("Should fetch wishlist successfully")
    void testFetchWishlist_Success() throws Exception {
        // Given
        List<TripWishlistResponseDto> wishlist = Collections.singletonList(wishlistResponseDto);
        when(tripWishlistService.fetchWishlist(any(UUID.class)))
            .thenReturn(wishlist);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            // When
            ResponseEntity<ResponseDto<List<TripWishlistResponseDto>>> response =
                tripWishlistController.fetchWishlist();

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().getData());
            assertEquals(1, response.getBody().getData().size());
            verify(tripWishlistService, times(1)).fetchWishlist(eq(userId));
        }
    }

    @Test
    @DisplayName("Should return 200 OK when fetching wishlist")
    void testFetchWishlist_HttpStatus() throws Exception {
        // Given
        List<TripWishlistResponseDto> wishlist = Collections.singletonList(wishlistResponseDto);
        when(tripWishlistService.fetchWishlist(any(UUID.class)))
            .thenReturn(wishlist);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            // When
            ResponseEntity<ResponseDto<List<TripWishlistResponseDto>>> response =
                tripWishlistController.fetchWishlist();

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }

    @Test
    @DisplayName("Should return empty list when wishlist is empty")
    void testFetchWishlist_EmptyList() throws Exception {
        // Given
        when(tripWishlistService.fetchWishlist(any(UUID.class)))
            .thenReturn(Collections.emptyList());

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            // When
            ResponseEntity<ResponseDto<List<TripWishlistResponseDto>>> response =
                tripWishlistController.fetchWishlist();

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ResponseDto<List<TripWishlistResponseDto>> body = response.getBody();
            assertNotNull(body);
            assertTrue(body.getData().isEmpty());
        }
    }

    @Test
    @DisplayName("Should return multiple wishlist items successfully")
    void testFetchWishlist_MultipleItems() throws Exception {
        // Given
        TripWishlistResponseDto wishlist2 = createSampleWishlistResponseDto();
        wishlist2.setTripId(UUID.randomUUID());
        List<TripWishlistResponseDto> wishlist = List.of(wishlistResponseDto, wishlist2);
        when(tripWishlistService.fetchWishlist(any(UUID.class)))
            .thenReturn(wishlist);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            // When
            ResponseEntity<ResponseDto<List<TripWishlistResponseDto>>> response =
                tripWishlistController.fetchWishlist();

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ResponseDto<List<TripWishlistResponseDto>> body = response.getBody();
            assertNotNull(body);
            assertEquals(2, body.getData().size());
        }
    }

    @Test
    @DisplayName("Should pass correct user ID to service when fetching wishlist")
    void testFetchWishlist_ParameterValidation() throws Exception {
        // Given
        List<TripWishlistResponseDto> wishlist = Collections.singletonList(wishlistResponseDto);
        when(tripWishlistService.fetchWishlist(any(UUID.class)))
            .thenReturn(wishlist);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            // When
            tripWishlistController.fetchWishlist();

            // Then
            verify(tripWishlistService, times(1)).fetchWishlist(eq(userId));
        }
    }

    // ============== HELPER METHODS ==============

    private TripWishlistRequestDto createSampleWishlistRequestDto() {
        TripWishlistRequestDto dto = new TripWishlistRequestDto();
        dto.setTripId(tripId);
        return dto;
    }

    private TripWishlistResponseDto createSampleWishlistResponseDto() {
        TripWishlistResponseDto dto = new TripWishlistResponseDto();
        dto.setTripId(tripId);
        dto.setTripTitle("Sample Trip");
        dto.setDestination("Paris");
        dto.setTripStatus(TripStatus.PUBLISHED);
        dto.setCreatedAt(LocalDateTime.now());
        return dto;
    }
}

