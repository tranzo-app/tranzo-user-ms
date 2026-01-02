package com.tranzo.tranzo_user_ms.trip.controller;

import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import com.tranzo.tranzo_user_ms.trip.dto.TripWishlistRequestDto;
import com.tranzo.tranzo_user_ms.trip.dto.TripWishlistResponseDto;
import com.tranzo.tranzo_user_ms.trip.service.TripWishlistService;
import com.tranzo.tranzo_user_ms.user.dto.ResponseDto;
import jakarta.security.auth.message.AuthException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(name = "/users/me/wishlist")
@RequiredArgsConstructor
public class TripWishlistController {
    TripWishlistService tripWishlistService;

    @PostMapping("/")
    public ResponseEntity<ResponseDto<TripWishlistResponseDto>> addToWishlist(@Valid @RequestBody TripWishlistRequestDto tripWishlistRequestDto) throws AuthException {
        String userId = SecurityUtils.getCurrentUserUuid();
        TripWishlistResponseDto response = tripWishlistService.addTripToWishlist(tripWishlistRequestDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.success("Trip has been added to the wishlist successfully", response));
    }

    @DeleteMapping("/{tripId}")
    public ResponseEntity<ResponseDto<TripWishlistResponseDto>> removeFromWishlist(@PathVariable UUID tripId) throws AuthException {
        String userId = SecurityUtils.getCurrentUserUuid();
        tripWishlistService.removeTripFromWishlist(tripId, userId);
        return ResponseEntity.ok(ResponseDto.success("Trip has been removed from the wishlist successfully", null));
    }

    @GetMapping("/")
    public ResponseEntity<ResponseDto<List<TripWishlistResponseDto>>> fetchWishlist() throws AuthException {
        String userId = SecurityUtils.getCurrentUserUuid();
        List<TripWishlistResponseDto> response = tripWishlistService.fetchWishlist(userId);
        return ResponseEntity.ok(ResponseDto.success("Wishlist fetched successfully", response));
    }
}
