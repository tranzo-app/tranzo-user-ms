package com.tranzo.tranzo_user_ms.trip.controller;

import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import com.tranzo.tranzo_user_ms.trip.dto.RemoveParticipantRequestDto;
import com.tranzo.tranzo_user_ms.trip.dto.TripJoinRequestDto;
import com.tranzo.tranzo_user_ms.trip.dto.TripJoinRequestResponseDto;
import com.tranzo.tranzo_user_ms.trip.enums.JoinRequestStatus;
import com.tranzo.tranzo_user_ms.trip.service.TripJoinRequestService;
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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TripJoinRequestController Unit Tests")
class TripJoinRequestControllerTest {

    @Mock
    private TripJoinRequestService tripJoinRequestService;

    @InjectMocks
    private TripJoinRequestController tripJoinRequestController;

    private UUID userId;
    private UUID tripId;
    private UUID joinRequestId;
    private UUID participantUserId;
    private TripJoinRequestDto joinRequestDto;
    private TripJoinRequestResponseDto joinRequestResponseDto;
    private RemoveParticipantRequestDto removeParticipantRequestDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        tripId = UUID.randomUUID();
        joinRequestId = UUID.randomUUID();
        participantUserId = UUID.randomUUID();
        joinRequestDto = createSampleJoinRequestDto();
        joinRequestResponseDto = createSampleJoinRequestResponseDto();
        removeParticipantRequestDto = new RemoveParticipantRequestDto();
    }

    // ============== CREATE JOIN REQUEST TESTS ==============

    @Test
    @DisplayName("Should create join request successfully")
    void testCreateJoinRequest_Success() throws Exception {
        // Given
        when(tripJoinRequestService.createJoinRequest(any(TripJoinRequestDto.class), any(UUID.class), any(UUID.class)))
            .thenReturn(joinRequestResponseDto);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            // When
            ResponseEntity<ResponseDto<TripJoinRequestResponseDto>> response =
                tripJoinRequestController.createJoinRequest(joinRequestDto, tripId);

            // Then
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().getData());
            verify(tripJoinRequestService, times(1)).createJoinRequest(eq(joinRequestDto), eq(tripId), eq(userId));
        }
    }

    @Test
    @DisplayName("Should return correct response on join request creation")
    void testCreateJoinRequest_ResponseValidation() throws Exception {
        // Given
        when(tripJoinRequestService.createJoinRequest(any(TripJoinRequestDto.class), any(UUID.class), any(UUID.class)))
            .thenReturn(joinRequestResponseDto);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            // When
            ResponseEntity<ResponseDto<TripJoinRequestResponseDto>> response =
                tripJoinRequestController.createJoinRequest(joinRequestDto, tripId);

            // Then
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            ResponseDto<TripJoinRequestResponseDto> body = response.getBody();
            assertNotNull(body);
            assertNotNull(body.getData());
        }
    }

    // ============== APPROVE JOIN REQUEST TESTS ==============

    @Test
    @DisplayName("Should approve join request successfully")
    void testApproveJoinRequest_Success() throws Exception {
        // Given
        when(tripJoinRequestService.approveJoinRequest(any(UUID.class), any(UUID.class)))
            .thenReturn(joinRequestResponseDto);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            // When
            ResponseEntity<ResponseDto<TripJoinRequestResponseDto>> response =
                tripJoinRequestController.approveJoinRequest(joinRequestId);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(tripJoinRequestService, times(1)).approveJoinRequest(eq(joinRequestId), eq(userId));
        }
    }

    // ============== REJECT JOIN REQUEST TESTS ==============

    @Test
    @DisplayName("Should reject join request successfully")
    void testRejectJoinRequest_Success() throws Exception {
        // Given
        when(tripJoinRequestService.rejectJoinRequest(any(UUID.class), any(UUID.class)))
            .thenReturn(joinRequestResponseDto);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            // When
            ResponseEntity<ResponseDto<TripJoinRequestResponseDto>> response =
                tripJoinRequestController.rejectJoinRequest(joinRequestId);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(tripJoinRequestService, times(1)).rejectJoinRequest(eq(joinRequestId), eq(userId));
        }
    }

    // ============== FETCH JOIN REQUESTS TESTS ==============

    @Test
    @DisplayName("Should fetch join requests successfully")
    void testFetchJoinRequests_Success() throws Exception {
        // Given
        List<TripJoinRequestResponseDto> joinRequests = Collections.singletonList(joinRequestResponseDto);
        when(tripJoinRequestService.getJoinRequestsForTrip(any(UUID.class), any(UUID.class), any()))
            .thenReturn(joinRequests);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            // When
            ResponseEntity<ResponseDto<List<TripJoinRequestResponseDto>>> response =
                tripJoinRequestController.fetchJoinRequests(tripId, null);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().getData());
            assertEquals(1, response.getBody().getData().size());
            verify(tripJoinRequestService, times(1)).getJoinRequestsForTrip(eq(tripId), eq(userId), isNull());
        }
    }

    @Test
    @DisplayName("Should fetch join requests with status filter")
    void testFetchJoinRequests_WithStatus() throws Exception {
        // Given
        List<TripJoinRequestResponseDto> joinRequests = Collections.singletonList(joinRequestResponseDto);
        when(tripJoinRequestService.getJoinRequestsForTrip(any(UUID.class), any(UUID.class), any(JoinRequestStatus.class)))
            .thenReturn(joinRequests);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            // When
            ResponseEntity<ResponseDto<List<TripJoinRequestResponseDto>>> response =
                tripJoinRequestController.fetchJoinRequests(tripId, JoinRequestStatus.PENDING);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(tripJoinRequestService, times(1)).getJoinRequestsForTrip(eq(tripId), eq(userId), eq(JoinRequestStatus.PENDING));
        }
    }

    @Test
    @DisplayName("Should return empty list when no join requests found")
    void testFetchJoinRequests_EmptyList() throws Exception {
        // Given
        when(tripJoinRequestService.getJoinRequestsForTrip(any(UUID.class), any(UUID.class), any()))
            .thenReturn(Collections.emptyList());

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            // When
            ResponseEntity<ResponseDto<List<TripJoinRequestResponseDto>>> response =
                tripJoinRequestController.fetchJoinRequests(tripId, null);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().getData().isEmpty());
        }
    }

    // ============== CANCEL JOIN REQUEST TESTS ==============

    @Test
    @DisplayName("Should cancel join request successfully")
    void testCancelJoinRequest_Success() throws Exception {
        // Given
        doNothing().when(tripJoinRequestService).cancelJoinRequestsForTrip(any(UUID.class), any(UUID.class));

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            // When
            ResponseEntity<ResponseDto<Void>> response =
                tripJoinRequestController.cancelJoinRequest(joinRequestId);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(tripJoinRequestService, times(1)).cancelJoinRequestsForTrip(eq(joinRequestId), eq(userId));
        }
    }

    // ============== REMOVE OR LEAVE TRIP TESTS ==============

    @Test
    @DisplayName("Should remove participant from trip successfully")
    void testRemoveOrLeaveTrip_Success() throws Exception {
        // Given
        doNothing().when(tripJoinRequestService).removeOrLeaveTrip(any(UUID.class), any(UUID.class), any(UUID.class), any(RemoveParticipantRequestDto.class));

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            // When
            ResponseEntity<ResponseDto<Void>> response =
                tripJoinRequestController.removeOrLeaveTrip(tripId, participantUserId, removeParticipantRequestDto);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(tripJoinRequestService, times(1)).removeOrLeaveTrip(eq(tripId), eq(participantUserId), eq(userId), eq(removeParticipantRequestDto));
        }
    }

    // ============== HELPER METHODS ==============

    private TripJoinRequestDto createSampleJoinRequestDto() {
        TripJoinRequestDto dto = new TripJoinRequestDto();
        return dto;
    }

    private TripJoinRequestResponseDto createSampleJoinRequestResponseDto() {
        TripJoinRequestResponseDto dto = new TripJoinRequestResponseDto();
        return dto;
    }
}

