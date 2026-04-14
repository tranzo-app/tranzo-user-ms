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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripWishlistService {
    private final TripRepository tripRepository;
    private final TripWishlistRepository tripWishlistRepository;

    @Transactional
    public TripWishlistResponseDto addTripToWishlist(TripWishlistRequestDto tripWishlistRequestDto, UUID userId)
    {
        log.info("Processing started | operation=addTripToWishlist | userId={} | tripId={}", userId, tripWishlistRequestDto.getTripId());
        
        try {
            UUID tripId = tripWishlistRequestDto.getTripId();
            TripEntity trip = tripRepository.findByTripIdAndTripStatus(tripId, TripStatus.PUBLISHED)
                    .orElseThrow(() -> new EntityNotFoundException("Trip not found"));
            // Call Rest endpoint here to check if the user exists

            boolean isWishlistExists = tripWishlistRepository.existsByUserIdAndTrip_TripId(userId, tripId);
            if (isWishlistExists)
            {
                log.warn("Duplicate wishlist attempt | operation=addTripToWishlist | userId={} | tripId={} | reason=ALREADY_EXISTS", userId, tripId);
                throw new ConflictException("Trip is already wishlisted by the user");
            }
            
            TripWishlistEntity wishlist = new TripWishlistEntity();
            wishlist.setUserId(userId);
            wishlist.setTrip(trip);
            TripWishlistEntity savedWishlist = tripWishlistRepository.save(wishlist);

            log.info("Processing completed | operation=addTripToWishlist | userId={} | tripId={} | status=SUCCESS", userId, tripId);
            return TripWishlistResponseDto.builder()
                    .tripId(trip.getTripId())
                    .tripTitle(trip.getTripTitle())
                    .destination(trip.getTripDestination())
                    .tripStatus(trip.getTripStatus())
                    .createdAt(savedWishlist.getCreatedAt())
                    .build();
        } catch (ConflictException | EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Operation failed | operation=addTripToWishlist | userId={} | tripId={} | reason={}", userId, tripWishlistRequestDto.getTripId(), e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void removeTripFromWishlist(UUID tripId, UUID userId) {
        log.info("Processing started | operation=removeTripFromWishlist | userId={} | tripId={}", userId, tripId);
        
        try {
            TripWishlistEntity wishlist = tripWishlistRepository.findByTrip_TripIdAndUserId(tripId, userId)
                    .orElseThrow(() -> new EntityNotFoundException("Trip is not in user's wishlist"));
            tripWishlistRepository.delete(wishlist);
            
            log.info("Processing completed | operation=removeTripFromWishlist | userId={} | tripId={} | status=SUCCESS", userId, tripId);
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Operation failed | operation=removeTripFromWishlist | userId={} | tripId={} | reason={}", userId, tripId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<TripWishlistResponseDto> fetchWishlist(UUID userId) {
        log.info("Processing started | operation=fetchWishlist | userId={}", userId);
        
        try {
            List<TripWishlistEntity> tripWishlistEntityList = tripWishlistRepository.findByUserIdOrderByCreatedAtDesc(userId);
            List<TripWishlistResponseDto> wishlist = tripWishlistEntityList.stream().map((wishlistItem) -> {
                TripWishlistResponseDto wishlistDto = TripWishlistResponseDto.builder()
                        .tripId(wishlistItem.getTrip().getTripId())
                        .tripTitle(wishlistItem.getTrip().getTripTitle())
                        .destination(wishlistItem.getTrip().getTripDestination())
                        .tripStatus(wishlistItem.getTrip().getTripStatus())
                        .createdAt(wishlistItem.getCreatedAt())
                        .build();
                return wishlistDto;
            }).toList();
            
            log.info("Processing completed | operation=fetchWishlist | userId={} | itemsCount={} | status=SUCCESS", userId, wishlist.size());
            return wishlist;
        } catch (Exception e) {
            log.error("Operation failed | operation=fetchWishlist | userId={} | reason={}", userId, e.getMessage(), e);
            throw e;
        }
    }
}
