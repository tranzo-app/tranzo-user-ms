package com.tranzo.tranzo_user_ms.trip.controller;

import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import com.tranzo.tranzo_user_ms.trip.dto.AnswerQnaRequestDto;
import com.tranzo.tranzo_user_ms.trip.dto.CreateQnaRequestDto;
import com.tranzo.tranzo_user_ms.trip.dto.TripDto;
import com.tranzo.tranzo_user_ms.trip.dto.TripQnaResponseDto;
import com.tranzo.tranzo_user_ms.trip.service.TripManagementService;
import com.tranzo.tranzo_user_ms.trip.validation.groups.DraftChecks;
import com.tranzo.tranzo_user_ms.user.dto.ResponseDto;
import jakarta.security.auth.message.AuthException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(name = "/trips")
public class TripManagementController {
    TripManagementService tripManagementService;

    @PostMapping("/")
    public ResponseEntity<ResponseDto<UUID>> createDraftTrip(@Validated(DraftChecks.class) @RequestBody TripDto tripDto) throws AuthException {
        String userId = SecurityUtils.getCurrentUserUuid();
        UUID tripId = tripManagementService.createDraftTrip(tripDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.success("Draft trip has been created successfully", tripId));
    }

    @PutMapping("/{tripId}")
    public ResponseEntity<ResponseDto<UUID>> updateDraftTrip(@Validated(DraftChecks.class) @RequestBody TripDto tripDto, @PathVariable UUID tripId) throws AuthException {
        String userId = SecurityUtils.getCurrentUserUuid();
        tripId = tripManagementService.updateDraftTrip(tripDto, tripId, userId);
        return ResponseEntity.ok(ResponseDto.success("Draft trip has been updated successfully", tripId));
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<ResponseDto<TripDto>> fetchTripDetails(@PathVariable UUID tripId) throws AuthException {
        String userId = SecurityUtils.getCurrentUserUuid();
        TripDto tripDto = tripManagementService.fetchTrip(tripId, userId);
        return ResponseEntity.ok(ResponseDto.success("Trip details have been fetched successfully", tripDto));
    }

    @DeleteMapping("/{tripId}")
    public ResponseEntity<ResponseDto<Void>> cancelTrip(@PathVariable UUID tripId) throws AuthException {
        String userId = SecurityUtils.getCurrentUserUuid();
        tripManagementService.cancelTrip(tripId, userId);
        return ResponseEntity.ok(ResponseDto.success("Trip successfully cancelled", null));
    }

    @PostMapping("/{tripId}/qna")
    public ResponseEntity<ResponseDto<Void>> addTripQnA(@RequestBody CreateQnaRequestDto createQnaRequestDto, @PathVariable String tripId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        tripManagementService.addTripQnA(userID, createQnaRequestDto, tripId);
        return ResponseEntity.ok(ResponseDto.success("Trip QnA added successfully", null));
    }

    @PostMapping("/{tripId}/qna/{qnaId}/answer")
    public ResponseEntity<ResponseDto<Void>> answerTripQnA(@PathVariable UUID tripId, @PathVariable UUID qnaId, @Valid AnswerQnaRequestDto answerQnaRequestDto) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        tripManagementService.answerTripQnA(userID, tripId, qnaId, answerQnaRequestDto);
        return ResponseEntity.ok(ResponseDto.success("Trip QnA answered successfully", null));
    }

    @GetMapping("/{tripId}/qna")
    public ResponseEntity<ResponseDto<List<TripQnaResponseDto>>> getTripQnA(
            @PathVariable UUID tripId
    ) {
        List<TripQnaResponseDto> response =
                tripManagementService.getTripQna(tripId, answerQnaRequestDto);

        return ResponseEntity.ok(
                ResponseDto.success("Trip QnA fetched successfully", response)
        );
    }



}
