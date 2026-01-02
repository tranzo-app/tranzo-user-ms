package com.tranzo.tranzo_user_ms.trip.controller;

import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import com.tranzo.tranzo_user_ms.trip.dto.TripDto;
import com.tranzo.tranzo_user_ms.trip.service.TripManagementService;
import com.tranzo.tranzo_user_ms.trip.validation.groups.DraftChecks;
import com.tranzo.tranzo_user_ms.user.dto.ResponseDto;
import jakarta.security.auth.message.AuthException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
}
