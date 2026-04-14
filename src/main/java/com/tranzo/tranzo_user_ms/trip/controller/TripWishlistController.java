package com.tranzo.tranzo_user_ms.trip.controller;

import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import com.tranzo.tranzo_user_ms.trip.dto.TripWishlistRequestDto;
import com.tranzo.tranzo_user_ms.trip.dto.TripWishlistResponseDto;
import com.tranzo.tranzo_user_ms.trip.service.TripWishlistService;
import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import jakarta.security.auth.message.AuthException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users/me/wishlist")
@RequiredArgsConstructor
@Slf4j
public class TripWishlistController {
    private final TripWishlistService tripWishlistService;

    @PostMapping("/")
    public ResponseEntity<ResponseDto<TripWishlistResponseDto>> addToWishlist(@Valid @RequestBody TripWishlistRequestDto tripWishlistRequestDto) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/users/me/wishlist/ | method=POST | userId={}", userId);
        
        try {
            TripWishlistResponseDto response = tripWishlistService.addTripToWishlist(tripWishlistRequestDto, userId);
            
            log.info("Trip added to wishlist | userId={} | tripId={} | status=SUCCESS", userId, tripWishlistRequestDto.getTripId());
            return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.success(201, "Trip has been added to the wishlist successfully", response));
        } catch (Exception e) {
            log.error("Request failed | API=/users/me/wishlist/ | method=POST | userId={} | reason={}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/{tripId}")
    public ResponseEntity<ResponseDto<TripWishlistResponseDto>> removeFromWishlist(@PathVariable UUID tripId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/users/me/wishlist/{} | method=DELETE | userId={}", tripId, userId);
        
        try {
            tripWishlistService.removeTripFromWishlist(tripId, userId);
            
            log.info("Trip removed from wishlist | userId={} | tripId={} | status=SUCCESS", userId, tripId);
            return ResponseEntity.ok(ResponseDto.success("Trip has been removed from the wishlist successfully", null));
        } catch (Exception e) {
            log.error("Request failed | API=/users/me/wishlist/{} | method=DELETE | userId={} | reason={}", tripId, userId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<ResponseDto<List<TripWishlistResponseDto>>> fetchWishlist() throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/users/me/wishlist | method=GET | userId={}", userId);
        
        try {
            List<TripWishlistResponseDto> response = tripWishlistService.fetchWishlist(userId);
            
            log.info("Wishlist fetched | userId={} | tripsCount={} | status=SUCCESS", userId, response.size());
            return ResponseEntity.ok(ResponseDto.success("Wishlist fetched successfully", response));
        } catch (Exception e) {
            log.error("Request failed | API=/users/me/wishlist | method=GET | userId={} | reason={}", userId, e.getMessage(), e);
            throw e;
        }
    }
}
