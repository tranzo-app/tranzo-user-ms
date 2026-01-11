package com.tranzo.tranzo_user_ms.trip.controller;

import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
<<<<<<< HEAD
=======
import com.tranzo.tranzo_user_ms.trip.dto.RemoveParticipantRequestDto;
>>>>>>> c7a5e61799d77c0f6ab9fba4db1e45fa877221ea
import com.tranzo.tranzo_user_ms.trip.dto.TripJoinRequestDto;
import com.tranzo.tranzo_user_ms.trip.dto.TripJoinRequestResponseDto;
import com.tranzo.tranzo_user_ms.trip.enums.JoinRequestStatus;
import com.tranzo.tranzo_user_ms.trip.service.TripJoinRequestService;
import jakarta.security.auth.message.AuthException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class TripJoinRequestController {
    TripJoinRequestService tripJoinRequestService;

<<<<<<< HEAD
    @PostMapping("/trips/{tripId}")
=======
    @PostMapping("/trips/{tripId}/join-requests")
>>>>>>> c7a5e61799d77c0f6ab9fba4db1e45fa877221ea
    public ResponseEntity<ResponseDto<TripJoinRequestResponseDto>> createJoinRequest(@Valid @RequestBody TripJoinRequestDto tripJoinRequestDto, @PathVariable UUID tripId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        TripJoinRequestResponseDto tripJoinRequestResponse = tripJoinRequestService.createJoinRequest(tripJoinRequestDto, tripId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body((ResponseDto.success(201, "Trip join request created successfully", tripJoinRequestResponse)));
    }

    @PostMapping("/join-requests/{id}/approve")
    public ResponseEntity<ResponseDto<TripJoinRequestResponseDto>> approveJoinRequest(@PathVariable UUID id) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        TripJoinRequestResponseDto tripJoinRequestResponse = tripJoinRequestService.approveJoinRequest(id, userId);
<<<<<<< HEAD
        return ResponseEntity.ok(ResponseDto.success(201, "Trip join request approved successfully", tripJoinRequestResponse));
=======
        return ResponseEntity.ok(ResponseDto.success("Trip join request approved successfully", tripJoinRequestResponse));
>>>>>>> c7a5e61799d77c0f6ab9fba4db1e45fa877221ea
    }

    @PostMapping("/join-requests/{id}/reject")
    public ResponseEntity<ResponseDto<TripJoinRequestResponseDto>> rejectJoinRequest(@PathVariable UUID id) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        TripJoinRequestResponseDto tripJoinRequestResponse = tripJoinRequestService.rejectJoinRequest(id, userId);
<<<<<<< HEAD
        return ResponseEntity.ok(ResponseDto.success(201, "Trip join request rejected successfully", tripJoinRequestResponse));
=======
        return ResponseEntity.ok(ResponseDto.success("Trip join request rejected successfully", tripJoinRequestResponse));
>>>>>>> c7a5e61799d77c0f6ab9fba4db1e45fa877221ea
    }

    @GetMapping("/trips/{tripId}/join-requests")
    public ResponseEntity<ResponseDto<List<TripJoinRequestResponseDto>>> fetchJoinRequests(@PathVariable UUID tripId, @RequestParam(required = false) JoinRequestStatus status) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        List<TripJoinRequestResponseDto> tripJoinRequestList = tripJoinRequestService.getJoinRequestsForTrip(tripId, userId, status);
        return ResponseEntity.ok(ResponseDto.success("Trip join requests fetched successfully", tripJoinRequestList));
    }

<<<<<<< HEAD
//    @DeleteMapping("/join-requests/{id}")
//    public ResponseEntity<ResponseDto<Void>> cancelJoinRequest(@PathVariable UUID id) throws AuthException {
//        UUID userId = SecurityUtils.getCurrentUserUuid();
//        tripJoinRequestService.cancelJoinRequestsForTrip(id, userId);
//        return ResponseEntity.ok(ResponseDto.success("Trip join requests fetched successfully", tripJoinRequestList));
//    }
=======
    @DeleteMapping("/join-requests/{id}/cancel")
    public ResponseEntity<ResponseDto<Void>> cancelJoinRequest(@PathVariable UUID id) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        tripJoinRequestService.cancelJoinRequestsForTrip(id, userId);
        return ResponseEntity.ok(ResponseDto.success("Trip join request cancelled successfully", null));
    }

    @DeleteMapping("/trips/{tripId}/participants/{participantUserId}")
    public ResponseEntity<ResponseDto<Void>> removeOrLeaveTrip(@PathVariable UUID tripId, @PathVariable UUID participantUserId, @Valid @RequestBody RemoveParticipantRequestDto removeParticipantRequestDto) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        tripJoinRequestService.removeOrLeaveTrip(tripId, participantUserId, userId, removeParticipantRequestDto);
        return ResponseEntity.ok(ResponseDto.success("Left from the trip successfully", null));
    }
>>>>>>> c7a5e61799d77c0f6ab9fba4db1e45fa877221ea
}