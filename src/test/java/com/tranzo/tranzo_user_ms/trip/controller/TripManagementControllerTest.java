package com.tranzo.tranzo_user_ms.trip.controller;

import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import com.tranzo.tranzo_user_ms.trip.dto.*;
import com.tranzo.tranzo_user_ms.trip.enums.JoinPolicy;
import com.tranzo.tranzo_user_ms.trip.enums.TripStatus;
import com.tranzo.tranzo_user_ms.trip.enums.VisibilityStatus;
import com.tranzo.tranzo_user_ms.trip.service.TripInviteService;
import com.tranzo.tranzo_user_ms.trip.service.TripManagementService;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TripManagementController Unit Tests")
class TripManagementControllerTest {

    @Mock
    private TripManagementService tripManagementService;

    @Mock
    private TripInviteService tripInviteService;

    @InjectMocks
    private TripManagementController tripManagementController;

    private UUID userId;
    private UUID tripId;
    private TripDto tripDto;
    private TripResponseDto tripResponseDto;
    private TripViewDto tripViewDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        tripId = UUID.randomUUID();
        tripDto = createSampleTripDto();
        tripResponseDto = createSampleTripResponseDto();
        tripViewDto = createSampleTripViewDto();
    }

    // ============== CREATE DRAFT TRIP TESTS ==============

    @Test
    @DisplayName("Should create draft trip and return 201 status")
    void testCreateDraftTrip_Success() throws Exception {
        when(tripManagementService.createDraftTrip(any(TripDto.class), any(UUID.class)))
            .thenReturn(tripResponseDto);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<ResponseDto<TripResponseDto>> response =
                tripManagementController.createDraftTrip(tripDto);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().getData());
            verify(tripManagementService, times(1)).createDraftTrip(eq(tripDto), eq(userId));
        }
    }

    @Test
    @DisplayName("Should return response with correct trip ID")
    void testCreateDraftTrip_ResponseValidation() throws Exception {
        when(tripManagementService.createDraftTrip(any(TripDto.class), any(UUID.class)))
            .thenReturn(tripResponseDto);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<ResponseDto<TripResponseDto>> response =
                tripManagementController.createDraftTrip(tripDto);

            assertNotNull(response.getBody());
            ResponseDto<TripResponseDto> body = response.getBody();
            assertNotNull(body.getData());
            assertEquals(tripId, body.getData().getTripId());
            assertEquals(TripStatus.DRAFT, body.getData().getTripStatus());
        }
    }

    // ============== UPDATE DRAFT TRIP TESTS ==============

    @Test
    @DisplayName("Should update draft trip successfully")
    void testUpdateDraftTrip_Success() throws Exception {
        when(tripManagementService.updateDraftTrip(any(TripDto.class), any(UUID.class), any(UUID.class)))
            .thenReturn(tripResponseDto);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<ResponseDto<TripResponseDto>> response =
                tripManagementController.updateDraftTrip(tripDto, tripId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().getData());
            verify(tripManagementService, times(1)).updateDraftTrip(eq(tripDto), eq(tripId), eq(userId));
        }
    }

    @Test
    @DisplayName("Should pass correct parameters to service for update")
    void testUpdateDraftTrip_ParameterValidation() throws Exception {
        when(tripManagementService.updateDraftTrip(any(TripDto.class), any(UUID.class), any(UUID.class)))
            .thenReturn(tripResponseDto);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            tripManagementController.updateDraftTrip(tripDto, tripId);

            verify(tripManagementService, times(1)).updateDraftTrip(
                eq(tripDto),
                eq(tripId),
                eq(userId)
            );
        }
    }

    // ============== FETCH TRIP DETAILS TESTS ==============

    @Test
    @DisplayName("Should fetch trip details successfully")
    void testFetchTripDetails_Success() throws Exception {
        when(tripManagementService.fetchTrip(any(UUID.class), any(UUID.class)))
            .thenReturn(tripViewDto);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<ResponseDto<TripViewDto>> response =
                tripManagementController.fetchTripDetails(tripId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().getData());
            verify(tripManagementService, times(1)).fetchTrip(eq(tripId), eq(userId));
        }
    }

    // ============== CANCEL TRIP TESTS ==============

    @Test
    @DisplayName("Should cancel trip successfully")
    void testCancelTrip_Success() throws Exception {
        doNothing().when(tripManagementService).cancelTrip(any(UUID.class), any(UUID.class));

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<ResponseDto<Void>> response =
                tripManagementController.cancelTrip(tripId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            ResponseDto<Void> body = response.getBody();
            assertNotNull(body);
            verify(tripManagementService, times(1)).cancelTrip(eq(tripId), eq(userId));
        }
    }

    // ============== PUBLISH TRIP TESTS ==============

    @Test
    @DisplayName("Should publish trip successfully")
    void testPublishTrip_Success() throws Exception {
        tripResponseDto.setTripStatus(TripStatus.PUBLISHED);
        when(tripManagementService.publishTrip(any(UUID.class), any(UUID.class)))
            .thenReturn(tripResponseDto);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<ResponseDto<TripResponseDto>> response =
                tripManagementController.publishDraftTrip(tripId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().getData());
            assertEquals(TripStatus.PUBLISHED, response.getBody().getData().getTripStatus());
            verify(tripManagementService, times(1)).publishTrip(eq(tripId), eq(userId));
        }
    }

    // ============== UPDATE PUBLISHED TRIP TESTS ==============

    @Test
    @DisplayName("Should update published trip successfully")
    void testUpdatePublishedTrip_Success() throws Exception {
        when(tripManagementService.updateTrip(any(TripDto.class), any(UUID.class), any(UUID.class)))
            .thenReturn(tripResponseDto);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<ResponseDto<TripResponseDto>> response =
                tripManagementController.updatePublishedTrip(tripId, tripDto);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(tripManagementService, times(1)).updateTrip(eq(tripDto), eq(tripId), eq(userId));
        }
    }

    // ============== QNA TESTS ==============

    @Test
    @DisplayName("Should add trip QnA successfully")
    void testAddTripQnA_Success() throws Exception {
        CreateQnaRequestDto qnaDto = new CreateQnaRequestDto();
        doNothing().when(tripManagementService).addTripQnA(any(UUID.class), any(CreateQnaRequestDto.class), any(UUID.class));

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<ResponseDto<Void>> response =
                tripManagementController.addTripQnA(qnaDto, tripId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(tripManagementService, times(1)).addTripQnA(eq(userId), eq(qnaDto), eq(tripId));
        }
    }

    @Test
    @DisplayName("Should answer trip QnA successfully")
    void testAnswerTripQnA_Success() throws Exception {
        UUID qnaId = UUID.randomUUID();
        AnswerQnaRequestDto answerDto = new AnswerQnaRequestDto();
        doNothing().when(tripManagementService).answerTripQnA(any(UUID.class), any(UUID.class), any(UUID.class), any(AnswerQnaRequestDto.class));

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<ResponseDto<Void>> response =
                tripManagementController.answerTripQnA(tripId, qnaId, answerDto);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(tripManagementService, times(1)).answerTripQnA(eq(userId), eq(tripId), eq(qnaId), eq(answerDto));
        }
    }

    @Test
    @DisplayName("Should get trip QnA successfully")
    void testGetTripQnA_Success() throws Exception {
        TripQnaResponseDto qnaDto = TripQnaResponseDto.builder()
            .qnaId(UUID.randomUUID())
            .tripId(tripId)
            .authorUserId(userId)
            .question("What is the meeting point?")
            .answer("At the airport")
            .answeredBy(UUID.randomUUID())
            .answeredAt(null)
            .createdAt(java.time.LocalDateTime.now())
            .build();

        List<TripQnaResponseDto> qnaList = Collections.singletonList(qnaDto);
        when(tripManagementService.getTripQna(any(UUID.class), any(UUID.class)))
            .thenReturn(qnaList);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<ResponseDto<List<TripQnaResponseDto>>> response =
                tripManagementController.getTripQnA(tripId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().getData());
            assertEquals(1, response.getBody().getData().size());
            verify(tripManagementService, times(1)).getTripQna(eq(tripId), eq(userId));
        }
    }

    // ============== REPORT TRIP TESTS ==============

    @Test
    @DisplayName("Should report trip successfully")
    void testReportTrip_Success() throws Exception {
        ReportTripRequestDto reportDto = new ReportTripRequestDto();
        doNothing().when(tripManagementService).reportTrip(any(UUID.class), any(UUID.class), any(ReportTripRequestDto.class));

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<ResponseDto<Void>> response =
                tripManagementController.reportTrip(tripId, reportDto);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(tripManagementService, times(1)).reportTrip(eq(userId), eq(tripId), eq(reportDto));
        }
    }

    // ============== PROMOTE TO CO-HOST TESTS ==============

    @Test
    @DisplayName("Should promote participant to co-host successfully")
    void testPromoteToCoHost_Success() throws Exception {
        UUID participantUserId = UUID.randomUUID();
        doNothing().when(tripManagementService).promoteToCoHost(any(UUID.class), any(UUID.class), any(UUID.class));

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<ResponseDto<Void>> response =
                tripManagementController.promoteToCoHost(tripId, participantUserId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(tripManagementService, times(1)).promoteToCoHost(eq(userId), eq(tripId), eq(participantUserId));
        }
    }

    @Test
    @DisplayName("Should mark trip full successfully")
    void testMarkTripFull_Success() throws Exception {
        doNothing().when(tripManagementService).markTripFull(any(UUID.class), any(UUID.class));

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<ResponseDto<Void>> response =
                tripManagementController.markTripFull(tripId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(tripManagementService, times(1)).markTripFull(eq(userId), eq(tripId));
        }
    }

    // ============== MUTUAL TRIPS TESTS ==============

    @Test
    @DisplayName("Should return mutual trips successfully")
    void testGetMutualTrips_Success() throws Exception {
        UUID otherUserId = UUID.randomUUID();
        TripViewDto tripDto = TripViewDto.builder()
            .tripId(tripId)
            .tripTitle("Shared Trip")
            .tripDestination("Paris")
            .build();
        List<TripViewDto> mutualTrips = List.of(tripDto);
        when(tripManagementService.getMutualCompletedTrips(eq(userId), eq(otherUserId)))
            .thenReturn(mutualTrips);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<ResponseDto<List<TripViewDto>>> response =
                tripManagementController.getMutualTrips(otherUserId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().getData());
            assertEquals(1, response.getBody().getData().size());
            assertEquals("Shared Trip", response.getBody().getData().get(0).getTripTitle());
            verify(tripManagementService).getMutualCompletedTrips(userId, otherUserId);
        }
    }

    @Test
    @DisplayName("Should return empty list when no mutual trips")
    void testGetMutualTrips_EmptyList() throws Exception {
        UUID otherUserId = UUID.randomUUID();
        when(tripManagementService.getMutualCompletedTrips(eq(userId), eq(otherUserId)))
            .thenReturn(List.of());

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<ResponseDto<List<TripViewDto>>> response =
                tripManagementController.getMutualTrips(otherUserId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().getData());
            assertTrue(response.getBody().getData().isEmpty());
        }
    }

    @Test
    @DisplayName("Should pass current user and other user to service")
    void testGetMutualTrips_PassesCorrectParams() throws Exception {
        UUID otherUserId = UUID.randomUUID();
        when(tripManagementService.getMutualCompletedTrips(any(UUID.class), any(UUID.class)))
            .thenReturn(List.of());

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            tripManagementController.getMutualTrips(otherUserId);

            verify(tripManagementService).getMutualCompletedTrips(eq(userId), eq(otherUserId));
        }
    }

    // ============== HELPER METHODS ==============

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

    private TripResponseDto createSampleTripResponseDto() {
        TripResponseDto dto = new TripResponseDto();
        dto.setTripId(tripId);
        dto.setTripStatus(TripStatus.DRAFT);
        return dto;
    }

    private TripViewDto createSampleTripViewDto() {
        TripViewDto dto = new TripViewDto();
        dto.setTripId(tripId);
        dto.setTripTitle("Sample Trip");
        dto.setTripDestination("Paris");
        return dto;
    }
}

