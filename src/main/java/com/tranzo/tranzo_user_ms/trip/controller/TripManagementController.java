package com.tranzo.tranzo_user_ms.trip.controller;

import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import com.tranzo.tranzo_user_ms.trip.dto.*;
import com.tranzo.tranzo_user_ms.trip.dto.AnswerQnaRequestDto;
import com.tranzo.tranzo_user_ms.trip.dto.CreateQnaRequestDto;
import com.tranzo.tranzo_user_ms.trip.dto.TripDto;
import com.tranzo.tranzo_user_ms.trip.dto.TripResponseDto;
import com.tranzo.tranzo_user_ms.trip.dto.TripViewDto;
import com.tranzo.tranzo_user_ms.trip.service.TripManagementService;
import com.tranzo.tranzo_user_ms.trip.validation.groups.DraftChecks;
import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import jakarta.security.auth.message.AuthException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/trips")
@Slf4j
@RequiredArgsConstructor
public class TripManagementController {
    private final TripManagementService tripManagementService;

    @PostMapping("/")
    public ResponseEntity<ResponseDto<TripResponseDto>> createDraftTrip(@Validated(DraftChecks.class) @RequestBody TripDto tripDto) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        TripResponseDto tripResponse = tripManagementService.createDraftTrip(tripDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.success(201,"Draft trip has been created successfully", tripResponse));
    }

    // Frontend should send the complete TripDto whether any field is empty or not. That's why it is PUT
    @PutMapping("/{tripId}")
    public ResponseEntity<ResponseDto<TripResponseDto>> updateDraftTrip(@Validated(DraftChecks.class) @RequestBody TripDto tripDto, @PathVariable UUID tripId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        TripResponseDto tripResponse = tripManagementService.updateDraftTrip(tripDto, tripId, userId);
        return ResponseEntity.ok(ResponseDto.success("Draft trip has been updated successfully", tripResponse));
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<ResponseDto<TripViewDto>> fetchTripDetails(@PathVariable UUID tripId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Trip id : {} ", tripId);
        log.info("User id : {} ", userId);
        TripViewDto tripDto = tripManagementService.fetchTrip(tripId, userId);
        return ResponseEntity.ok(ResponseDto.success("Trip details have been fetched successfully", tripDto));
    }

    @GetMapping("/user")
    public ResponseEntity<ResponseDto<List<TripViewDto>>> fetchTripDetailsForUser() throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("User id : {} ", userId);
        List<TripViewDto> tripDto = tripManagementService.fetchTripForUser(userId);
        return ResponseEntity.ok(ResponseDto.success("Trip details have been fetched successfully for the user", tripDto));
    }

    @GetMapping("")
    public ResponseEntity<ResponseDto<List<TripViewDto>>> fetchAllTrips() throws AuthException {
        List<TripViewDto> tripDto = tripManagementService.fetchAllTrips();
        return ResponseEntity.ok(ResponseDto.success("All trip details have been fetched successfully", tripDto));
    }

    @DeleteMapping("/{tripId}")
    public ResponseEntity<ResponseDto<Void>> cancelTrip(@PathVariable UUID tripId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        tripManagementService.cancelTrip(tripId, userId);
        return ResponseEntity.ok(ResponseDto.success("Trip successfully cancelled", null));
    }

    @PostMapping("/{tripId}/publish")
    public ResponseEntity<ResponseDto<TripResponseDto>> publishDraftTrip(@PathVariable UUID tripId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        TripResponseDto tripResponse = tripManagementService.publishTrip(tripId, userId);
        return ResponseEntity.ok(ResponseDto.success("Trip is successfully published", tripResponse));
    }

    // Frontend should send the partial TripDto whether any field is empty or not. That's why it is PATCH
    @PatchMapping("/{tripId}")
    public ResponseEntity<ResponseDto<TripResponseDto>> updatePublishedTrip(@PathVariable UUID tripId, @Valid @RequestBody TripDto tripDto) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        TripResponseDto tripResponse = tripManagementService.updateTrip(tripDto, tripId, userId);
        return ResponseEntity.ok(ResponseDto.success("Published trip has been updated successfully", tripResponse));
    }

    @PostMapping("/{tripId}/qna")
    public ResponseEntity<ResponseDto<Void>> addTripQnA(@RequestBody CreateQnaRequestDto createQnaRequestDto, @PathVariable UUID tripId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        tripManagementService.addTripQnA(userId, createQnaRequestDto, tripId);
        return ResponseEntity.ok(ResponseDto.success("Trip QnA added successfully", null));
    }


    @PostMapping("/{tripId}/qna/{qnaId}/answer")
    public ResponseEntity<ResponseDto<Void>> answerTripQnA(@PathVariable UUID tripId, @PathVariable UUID qnaId, @RequestBody AnswerQnaRequestDto answerQnaRequestDto) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        tripManagementService.answerTripQnA(userId, tripId, qnaId, answerQnaRequestDto);
        return ResponseEntity.ok(ResponseDto.success("Trip QnA answered successfully", null));
    }

    @GetMapping("/{tripId}/qna")
    public ResponseEntity<ResponseDto<List<TripQnaResponseDto>>> getTripQnA(
            @PathVariable UUID tripId
    ) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        List<TripQnaResponseDto> response =
                tripManagementService.getTripQna(tripId,userId);

        return ResponseEntity.ok(
                ResponseDto.success("Trip QnA fetched successfully", response)
        );
    }

    @PostMapping("/{tripId}/reports")
    public ResponseEntity<ResponseDto<Void>> reportTrip(@PathVariable UUID tripId, @RequestBody @Valid ReportTripRequestDto reportTripRequestDto) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        tripManagementService.reportTrip(userId, tripId,  reportTripRequestDto);
        return ResponseEntity.ok(ResponseDto.success("Trip has been reported successfully", null));
    }
    @PostMapping("/{tripId}/participants/{participantUserId}/promote-cohost")
    public ResponseEntity<ResponseDto<Void>> promoteToCoHost(@PathVariable UUID tripId, @PathVariable UUID participantUserId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Promote to co-host called for tripId: {} by userId: {} for participantUserId: {}", tripId, userId, participantUserId);
        tripManagementService.promoteToCoHost(userId, tripId, participantUserId);
        return ResponseEntity.ok(ResponseDto.success("Participant has been promoted to co-host successfully", null));
    }

    @PostMapping("/{tripId}/mark-full")
    public ResponseEntity<ResponseDto<Void>> markTripFull(@PathVariable UUID tripId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        tripManagementService.markTripFull(userId, tripId);
        return ResponseEntity.ok(ResponseDto.success("Trip has been marked full successfully", null));
    }
}
