package com.tranzo.tranzo_user_ms.trip.service;

import com.fasterxml.jackson.databind.ser.impl.UnknownSerializer;
import com.tranzo.tranzo_user_ms.commons.exception.ConflictException;
import com.tranzo.tranzo_user_ms.commons.exception.EntityNotFoundException;
import com.tranzo.tranzo_user_ms.commons.exception.InvalidUserIdException;
import com.tranzo.tranzo_user_ms.trip.dto.TripWishlistRequestDto;
import com.tranzo.tranzo_user_ms.trip.dto.TripWishlistResponseDto;
import com.tranzo.tranzo_user_ms.trip.enums.TripStatus;
import com.tranzo.tranzo_user_ms.trip.model.TripEntity;
import com.tranzo.tranzo_user_ms.trip.model.TripWishlistEntity;
import com.tranzo.tranzo_user_ms.trip.repository.TripRepository;
import com.tranzo.tranzo_user_ms.trip.repository.TripWishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TripWishlistService {
    TripRepository tripRepository;
    TripWishlistRepository tripWishlistRepository;

    @Transactional
    public TripWishlistResponseDto addTripToWishlist(TripWishlistRequestDto tripWishlistRequestDto, UUID userId)
    {
        UUID tripId = tripWishlistRequestDto.getTripId();
        TripEntity trip = tripRepository.findByTripIdAndTripStatus(tripId, TripStatus.PUBLISHED)
                .orElseThrow(() -> new EntityNotFoundException("Trip not found"));
        // Call Rest endpoint here to check if the user exists

        boolean isWishlistExists = tripWishlistRepository.existsByUserIdAndTrip_TripId(userId, tripId);
        if (isWishlistExists)
        {
            throw new ConflictException("Trip is already wishlisted by the user");
        }
        TripWishlistEntity wishlist = new TripWishlistEntity();
        wishlist.setUserId(userId);
        wishlist.setTrip(trip);
        TripWishlistEntity savedWishlist = tripWishlistRepository.save(wishlist);

        return TripWishlistResponseDto.builder()
                .tripId(trip.getTripId())
                .tripTitle(trip.getTripTitle())
                .destination(trip.getTripDestination())
                .tripStatus(trip.getTripStatus())
                .createdAt(savedWishlist.getCreatedAt())
                .build();
    }

    @Transactional
    public void removeTripFromWishlist(UUID tripId, UUID userId) {
        TripWishlistEntity wishlist = tripWishlistRepository.findByTrip_TripIdAndUserId(tripId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Trip is not in user's wishlist"));
        tripWishlistRepository.delete(wishlist);
    }

    @Transactional(readOnly = true)
    public List<TripWishlistResponseDto> fetchWishlist(UUID userId) {
        List<TripWishlistEntity> tripWishlistEntityList = tripWishlistRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return tripWishlistEntityList.stream().map((wishlist) -> {
            TripWishlistResponseDto wishlistDto = TripWishlistResponseDto.builder()
                    .tripId(wishlist.getTrip().getTripId())
                    .tripTitle(wishlist.getTrip().getTripTitle())
                    .destination(wishlist.getTrip().getTripDestination())
                    .tripStatus(wishlist.getTrip().getTripStatus())
                    .createdAt(wishlist.getCreatedAt())
                    .build();
            return wishlistDto;
        }).toList();
    }
}
