package com.tranzo.tranzo_user_ms.trip.controller;

import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import com.tranzo.tranzo_user_ms.trip.dto.*;
import com.tranzo.tranzo_user_ms.trip.dto.AnswerQnaRequestDto;
import com.tranzo.tranzo_user_ms.trip.dto.CreateQnaRequestDto;
import com.tranzo.tranzo_user_ms.trip.dto.TripDto;
import com.tranzo.tranzo_user_ms.trip.dto.TripResponseDto;
import com.tranzo.tranzo_user_ms.trip.dto.TripViewDto;
import com.tranzo.tranzo_user_ms.trip.service.TripInviteService;
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
    private final TripInviteService tripInviteService;

    @PostMapping("/")
    public ResponseEntity<ResponseDto<TripResponseDto>> createDraftTrip(@Validated(DraftChecks.class) @RequestBody TripDto tripDto) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/trips/ | method=POST | userId={}", userId);
        TripResponseDto tripResponse = tripManagementService.createDraftTrip(tripDto, userId);
        log.info("Draft trip created | userId={} | tripId={} | status=SUCCESS", userId, tripResponse.getTripId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.success(201,"Draft trip has been created successfully", tripResponse));
    }

    // Frontend should send the complete TripDto whether any field is empty or not. That's why it is PUT
    @PutMapping("/{tripId}")
    public ResponseEntity<ResponseDto<TripResponseDto>> updateDraftTrip(@Validated(DraftChecks.class) @RequestBody TripDto tripDto, @PathVariable UUID tripId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/trips/{} | method=PUT | userId={}", tripId, userId);
        TripResponseDto tripResponse = tripManagementService.updateDraftTrip(tripDto, tripId, userId);
        log.info("Draft trip updated | userId={} | tripId={} | status=SUCCESS", userId, tripId);
        return ResponseEntity.ok(ResponseDto.success("Draft trip has been updated successfully", tripResponse));
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<ResponseDto<TripViewDto>> fetchTripDetails(@PathVariable UUID tripId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/trips/{} | method=GET | userId={}", tripId, userId);
        TripViewDto tripDto = tripManagementService.fetchTrip(tripId, userId);
        log.info("Trip details fetched | userId={} | tripId={} | status=SUCCESS", userId, tripId);
        return ResponseEntity.ok(ResponseDto.success("Trip details have been fetched successfully", tripDto));
    }

    @GetMapping("/{tripId}/members")
    public ResponseEntity<ResponseDto<TripMembersListResponseDto>> getTripMembers(@PathVariable UUID tripId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/trips/{}/members | method=GET | userId={}", tripId, userId);
        TripMembersListResponseDto members = tripManagementService.getTripMembers(tripId, userId);
        log.info("Trip members fetched | userId={} | tripId={} | membersCount={} | status=SUCCESS", userId, tripId, members.getMembers().size());
        return ResponseEntity.ok(ResponseDto.success("Trip members fetched successfully", members));
    }

    @GetMapping("/mutual-with/{otherUserId}")
    public ResponseEntity<ResponseDto<List<TripViewDto>>> getMutualTrips(@PathVariable UUID otherUserId) throws AuthException {
        UUID currentUserId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/trips/mutual-with/{} | method=GET | userId={}", otherUserId, currentUserId);
        List<TripViewDto> trips = tripManagementService.getMutualTrips(currentUserId, otherUserId);
        log.info("Mutual trips retrieved | userId={} | otherUserId={} | tripsCount={} | status=SUCCESS", currentUserId, otherUserId, trips.size());
        return ResponseEntity.ok(ResponseDto.success("Mutual trips retrieved", trips));
    }

    @GetMapping("/user")
    public ResponseEntity<ResponseDto<List<TripViewDto>>> fetchTripDetailsForUser() throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/trips/user | method=GET | userId={}", userId);
        List<TripViewDto> tripDto = tripManagementService.fetchTripForUser(userId);
        log.info("User trips fetched | userId={} | tripsCount={} | status=SUCCESS", userId, tripDto.size());
        return ResponseEntity.ok(ResponseDto.success("Trip details have been fetched successfully for the user", tripDto));
    }

    // Handles both /trips and /trips/ - WebConfig.setUseTrailingSlashMatch(true) ensures this works
    @GetMapping
    public ResponseEntity<ResponseDto<List<TripViewDto>>> fetchAllTrips() {
        UUID userId = null;
        try {
            userId = SecurityUtils.getCurrentUserUuid();
        } catch (Exception e) {
            // User is not authenticated, proceed with null userId
        }
        log.info("Incoming request | API=/trips | method=GET | userId={}", userId);
        List<TripViewDto> tripDto = tripManagementService.fetchAllTrips(userId);
        log.info("All trips fetched | userId={} | tripsCount={} | status=SUCCESS", userId, tripDto.size());
        return ResponseEntity.ok(ResponseDto.success("All trip details have been fetched successfully", tripDto));
    }

    @DeleteMapping("/{tripId}")
    public ResponseEntity<ResponseDto<Void>> cancelTrip(@PathVariable UUID tripId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/trips/{} | method=DELETE | userId={}", tripId, userId);
        tripManagementService.cancelTrip(tripId, userId);
        log.info("Trip cancelled | userId={} | tripId={} | status=SUCCESS", userId, tripId);
        return ResponseEntity.ok(ResponseDto.success("Trip successfully cancelled", null));
    }

    @PostMapping("/{tripId}/publish")
    public ResponseEntity<ResponseDto<TripResponseDto>> publishDraftTrip(@PathVariable UUID tripId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/trips/{}/publish | method=POST | userId={}", tripId, userId);
        TripResponseDto tripResponse = tripManagementService.publishTrip(tripId, userId);
        log.info("Trip published | userId={} | tripId={} | status=SUCCESS", userId, tripId);
        return ResponseEntity.ok(ResponseDto.success("Trip is successfully published", tripResponse));
    }

    // Frontend should send the partial TripDto whether any field is empty or not. That's why it is PATCH
    @PatchMapping("/{tripId}")
    public ResponseEntity<ResponseDto<TripResponseDto>> updatePublishedTrip(@PathVariable UUID tripId, @Valid @RequestBody TripDto tripDto) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/trips/{} | method=PATCH | userId={}", tripId, userId);
        TripResponseDto tripResponse = tripManagementService.updateTrip(tripDto, tripId, userId);
        log.info("Published trip updated | userId={} | tripId={} | status=SUCCESS", userId, tripId);
        return ResponseEntity.ok(ResponseDto.success("Published trip has been updated successfully", tripResponse));
    }

    @PostMapping("/{tripId}/qna")
    public ResponseEntity<ResponseDto<Void>> addTripQnA(@RequestBody CreateQnaRequestDto createQnaRequestDto, @PathVariable UUID tripId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/trips/{}/qna | method=POST | userId={}", tripId, userId);
        tripManagementService.addTripQnA(userId, createQnaRequestDto, tripId);
        log.info("Trip Q&A added | userId={} | tripId={} | status=SUCCESS", userId, tripId);
        return ResponseEntity.ok(ResponseDto.success("Trip QnA added successfully", null));
    }


    @PostMapping("/{tripId}/qna/{qnaId}/answer")
    public ResponseEntity<ResponseDto<Void>> answerTripQnA(@PathVariable UUID tripId, @PathVariable UUID qnaId, @RequestBody AnswerQnaRequestDto answerQnaRequestDto) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/trips/{}/qna/{}/answer | method=POST | userId={}", tripId, qnaId, userId);
        tripManagementService.answerTripQnA(userId, tripId, qnaId, answerQnaRequestDto);
        log.info("Trip Q&A answered | userId={} | tripId={} | qnaId={} | status=SUCCESS", userId, tripId, qnaId);
        return ResponseEntity.ok(ResponseDto.success("Trip QnA answered successfully", null));
    }

    @GetMapping("/{tripId}/qna")
    public ResponseEntity<ResponseDto<List<TripQnaResponseDto>>> getTripQnA(
            @PathVariable UUID tripId
    ) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/trips/{}/qna | method=GET | userId={}", tripId, userId);
        List<TripQnaResponseDto> response =
                tripManagementService.getTripQna(tripId,userId);
        log.info("Trip Q&A fetched | userId={} | tripId={} | qnaCount={} | status=SUCCESS", userId, tripId, response.size());
        return ResponseEntity.ok(
                ResponseDto.success("Trip QnA fetched successfully", response)
        );
    }

    @PostMapping("/{tripId}/reports")
    public ResponseEntity<ResponseDto<Void>> reportTrip(@PathVariable UUID tripId, @RequestBody @Valid ReportTripRequestDto reportTripRequestDto) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/trips/{}/reports | method=POST | userId={}", tripId, userId);
        tripManagementService.reportTrip(userId, tripId,  reportTripRequestDto);
        log.info("Trip reported | userId={} | tripId={} | status=SUCCESS", userId, tripId);
        return ResponseEntity.ok(ResponseDto.success("Trip has been reported successfully", null));
    }
    @PostMapping("/{tripId}/participants/{participantUserId}/promote-cohost")
    public ResponseEntity<ResponseDto<Void>> promoteToCoHost(@PathVariable UUID tripId, @PathVariable UUID participantUserId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/trips/{}/participants/{}/promote-cohost | method=POST | userId={}", tripId, participantUserId, userId);
        tripManagementService.promoteToCoHost(userId, tripId, participantUserId);
        log.info("Participant promoted to co-host | userId={} | tripId={} | participantUserId={} | status=SUCCESS", userId, tripId, participantUserId);
        return ResponseEntity.ok(ResponseDto.success("Participant has been promoted to co-host successfully", null));
    }

    @PostMapping("/{tripId}/mark-full")
    public ResponseEntity<ResponseDto<Void>> markTripFull(@PathVariable UUID tripId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/trips/{}/mark-full | method=POST | userId={}", tripId, userId);
        tripManagementService.markTripFull(userId, tripId);
        log.info("Trip marked full | userId={} | tripId={} | status=SUCCESS", userId, tripId);
        return ResponseEntity.ok(ResponseDto.success("Trip has been marked full successfully", null));
    }

//    @PostMapping("/{tripId}/invites/travel-pal/bulk")
//    public ResponseEntity<ResponseDto<Void>> inviteAllTravelPals(@PathVariable UUID tripId) throws AuthException {
//        UUID userId = SecurityUtils.getCurrentUserUuid();
//        tripInviteService.inviteAllTravelPals(tripId, userId);
//        return ResponseEntity.ok(ResponseDto.success("Travel pals invited successfully", null));
//    }

    @PostMapping("/{tripId}/invites/travel-pal")
    public ResponseEntity<ResponseDto<Void>> inviteTravelPals(@PathVariable UUID tripId, @RequestBody @Valid TravelPalInviteRequestDto inviteRequest) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/trips/{}/invites/travel-pal | method=POST | userId={} | palsCount={}", tripId, userId, inviteRequest.getTravelPalIds().size());
        tripInviteService.inviteMultipleTravelPals(tripId, userId, inviteRequest.getTravelPalIds());
        log.info("Travel pals invited | userId={} | tripId={} | palsCount={} | status=SUCCESS", userId, tripId, inviteRequest.getTravelPalIds().size());
        return ResponseEntity.ok(ResponseDto.success("Travel pals invited successfully", null));
    }

    @PostMapping("/{tripId}/broadcast")
    public ResponseEntity<ResponseDto<Void>> broadcastTrip(@PathVariable UUID tripId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/trips/{}/broadcast | method=POST | userId={}", tripId, userId);
        tripManagementService.broadcastTripToTravelPals(userId, tripId);
        log.info("Trip broadcast | userId={} | tripId={} | status=SUCCESS", userId, tripId);
        return ResponseEntity.ok(ResponseDto.success("Trip broadcast to travel pals successfully", null));
    }

//    @PostMapping("/search")
//    public ResponseEntity<ResponseDto<Page<TripViewDto>>> searchTrips(
//            @RequestBody SearchRequest request) throws AuthException {
//        UUID userId = SecurityUtils.getCurrentUserUuid();
//        List<String> globalFields = new ArrayList<>();
//        Page<TripViewDto> searchedTrips = tripManagementService.search(request, globalFields);
//        return ResponseEntity.ok(ResponseDto.success("Trip search is success", searchedTrips));
//    }
}
