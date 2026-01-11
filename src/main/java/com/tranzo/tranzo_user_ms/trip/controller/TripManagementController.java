package com.tranzo.tranzo_user_ms.trip.controller;

import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import com.tranzo.tranzo_user_ms.trip.dto.TripDto;
import com.tranzo.tranzo_user_ms.trip.dto.TripResponseDto;
import com.tranzo.tranzo_user_ms.trip.dto.TripViewDto;
import com.tranzo.tranzo_user_ms.trip.service.TripManagementService;
import com.tranzo.tranzo_user_ms.trip.validation.groups.DraftChecks;
import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import jakarta.security.auth.message.AuthException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/trips")
public class TripManagementController {
    TripManagementService tripManagementService;

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
        TripViewDto tripDto = tripManagementService.fetchTrip(tripId, userId);
        return ResponseEntity.ok(ResponseDto.success("Trip details have been fetched successfully", tripDto));
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
}
