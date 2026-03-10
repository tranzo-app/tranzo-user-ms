package com.tranzo.tranzo_user_ms.user.controller;

import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import com.tranzo.tranzo_user_ms.user.dto.SubmitHostRatingRequest;
import com.tranzo.tranzo_user_ms.user.dto.SubmitMemberRatingsRequest;
import com.tranzo.tranzo_user_ms.user.dto.SubmitTripRatingRequest;
import com.tranzo.tranzo_user_ms.user.service.RatingService;
import jakarta.security.auth.message.AuthException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/trips/{tripId}/ratings")
@Slf4j
@RequiredArgsConstructor
public  class RatingController {

    private final RatingService ratingService;

    @PutMapping("/trip")
    public ResponseEntity<ResponseDto<Void>> submitTripRating(
            @PathVariable UUID tripId,
            @Valid @RequestBody SubmitTripRatingRequest request) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        ratingService.submitTripRating(tripId, userId, request);
        return ResponseEntity.ok(ResponseDto.success(200, "Trip rating submitted", null));
    }

    @PutMapping("/host")
    public ResponseEntity<ResponseDto<Void>> submitHostRating(
            @PathVariable UUID tripId,
            @Valid @RequestBody SubmitHostRatingRequest request) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        ratingService.submitHostRating(tripId, userId, request);
        return ResponseEntity.ok(ResponseDto.success(200, "Host rating submitted", null));
    }

    @PutMapping("/members")
    public ResponseEntity<ResponseDto<Void>> submitMemberRatings(
            @PathVariable UUID tripId,
            @Valid @RequestBody SubmitMemberRatingsRequest request) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        ratingService.submitMemberRatings(tripId, userId, request);
        return ResponseEntity.ok(ResponseDto.success(200, "Member ratings submitted", null));
    }
}