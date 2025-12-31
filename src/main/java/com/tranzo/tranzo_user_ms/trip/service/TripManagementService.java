package com.tranzo.tranzo_user_ms.trip.service;

import com.tranzo.tranzo_user_ms.commons.exception.BadRequestException;
import com.tranzo.tranzo_user_ms.commons.exception.ConflictException;
import com.tranzo.tranzo_user_ms.commons.exception.EntityNotFoundException;
import com.tranzo.tranzo_user_ms.commons.exception.ForbiddenException;
import com.tranzo.tranzo_user_ms.trip.dto.*;
import com.tranzo.tranzo_user_ms.trip.enums.TripMemberRole;
import com.tranzo.tranzo_user_ms.trip.enums.TripMemberStatus;
import com.tranzo.tranzo_user_ms.trip.enums.TripStatus;
import com.tranzo.tranzo_user_ms.trip.enums.VisibilityStatus;
import com.tranzo.tranzo_user_ms.trip.model.*;
import com.tranzo.tranzo_user_ms.trip.repository.TagRepository;
import com.tranzo.tranzo_user_ms.trip.repository.TripItineraryRepository;
import com.tranzo.tranzo_user_ms.trip.repository.TripMemberRepository;
import com.tranzo.tranzo_user_ms.trip.repository.TripRepository;
import com.tranzo.tranzo_user_ms.trip.utility.UserUtil;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripManagementService {
    TripRepository tripRepository;
    TagRepository tagRepository;
    TripItineraryRepository tripItineraryRepository;
    TripMemberRepository tripMemberRepository;
    UserUtil userUtil;

    @Transactional
    public UUID createDraftTrip(TripDto tripDto, String userId)
    {
        // First check if the user exists and is active.

        TripEntity tripEntity = new TripEntity();
        tripEntity.setTripDescription(tripDto.getTripDescription());
        tripEntity.setTripTitle(tripDto.getTripTitle());
        tripEntity.setTripDestination(tripDto.getTripDestination());
        if (tripDto.getTripStartDate() != null &&
                tripDto.getTripEndDate() != null && tripDto.getTripStartDate().isAfter(tripDto.getTripEndDate()))
        {
            throw new BadRequestException("Trip start date cannot be after end date");
        }
        tripEntity.setTripStartDate(tripDto.getTripStartDate());
        tripEntity.setTripEndDate(tripDto.getTripEndDate());
        tripEntity.setEstimatedBudget(tripDto.getEstimatedBudget());
        tripEntity.setMaxParticipants(tripDto.getMaxParticipants());
        tripEntity.setJoinPolicy(tripDto.getJoinPolicy());
        tripEntity.setVisibilityStatus(tripDto.getVisibilityStatus());
        tripEntity.setTripStatus(TripStatus.DRAFT);
        tripEntity.setCurrentParticipants(1);
        tripEntity.setIsFull(false);

        // Trip Policy
        if (tripDto.getTripPolicy() != null)
        {
            TripPolicyEntity tripPolicyEntity = new TripPolicyEntity();
            tripPolicyEntity.setCancellationPolicy(tripDto.getTripPolicy().getCancellationPolicy());
            tripPolicyEntity.setRefundPolicy(tripDto.getTripPolicy().getRefundPolicy());
            tripPolicyEntity.setTrip(tripEntity);
            tripEntity.setTripPolicyEntity(tripPolicyEntity);
        }

        // Trip Metadata
        if (tripDto.getTripMetaData() != null)
        {
            TripMetaDataEntity tripMetaDataEntity = new TripMetaDataEntity();
            tripMetaDataEntity.setTripSummary(tripDto.getTripMetaData().getTripSummary());
            tripMetaDataEntity.setWhatsIncluded(tripDto.getTripMetaData().getWhatsIncluded());
            tripMetaDataEntity.setWhatsExcluded(tripDto.getTripMetaData().getWhatsExcluded());
            tripMetaDataEntity.setTrip(tripEntity);
            tripEntity.setTripMetaData(tripMetaDataEntity);
        }

        // Trip Tags
        Set<TagEntity> tripTags = new HashSet<>();
        for (TripTagDto tagDto : tripDto.getTripTags())
        {
            TagEntity tag = tagRepository.findByTagNameIgnoreCase(tagDto.getTagName())
                    .orElseGet(() -> {
                        TagEntity newTag = new TagEntity();
                        newTag.setTagName(tagDto.getTagName());
                        tagRepository.save(newTag);
                        return newTag;
                    });
            tripTags.add(tag);
        }
        tripEntity.setTripTags(tripTags);

        // Trip itinerary
        for (TripItineraryDto tripItineraryDto : tripDto.getTripItineraries())
        {
            TripItineraryEntity tripItineraryEntity = new TripItineraryEntity();
            setTripItineraryEntity(tripItineraryDto, tripEntity, tripItineraryEntity);
            tripEntity.getTripItineraries().add(tripItineraryEntity);
        }

        // Host
        TripMemberEntity host = new TripMemberEntity();
        host.setRole(TripMemberRole.HOST);
        host.setStatus(TripMemberStatus.ACTIVE);
        host.setUserId(UUID.fromString(userId));
        host.setTrip(tripEntity);
        tripEntity.getTripMembers().add(host);

        TripEntity newTrip = tripRepository.save(tripEntity);
        return newTrip.getTripId();
    }

    @Transactional
    public UUID updateDraftTrip(TripDto tripDto, UUID tripId, String userId)
    {
        TripEntity trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("Trip not found"));
        if (trip.getTripStatus() != TripStatus.DRAFT)
        {
            throw new ConflictException("Only trips with DRAFT status can be updated");
        }
        userUtil.validateUserIsHost(tripId, userId);
        updateDraftTripBasicInfo(trip, tripDto);
        updateDraftTripPolicy(trip, tripDto);
        updateDraftTripMetadata(trip, tripDto);
        updateDraftTripTags(trip, tripDto);
        updateDraftTripItinerary(trip, tripDto);
        TripEntity updateTrip = tripRepository.save(trip);
        return updateTrip.getTripId();
    }

    public TripDto fetchTrip(UUID tripId, String userId)
    {
        TripEntity trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("Trip details not found for the given trip id"));
        if (trip.getTripStatus() == TripStatus.CANCELLED) {
            throw new ForbiddenException("Cancelled trip is not accessible");
        }
        UUID userUuid = UUID.fromString(userId);
        if (trip.getVisibilityStatus() == VisibilityStatus.PRIVATE)
        {
            tripMemberRepository.findByTrip_TripIdAndUserIdAndStatus(tripId, userUuid, TripMemberStatus.ACTIVE)
                    .orElseThrow(() -> new ForbiddenException("User is not allowed to view this private trip"));
        }
        return mapTripEntityToDto(trip);
    }

    @Transactional
    public void cancelTrip(UUID tripId, String userId)
    {
        TripEntity trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("Trip not found"));
        if (!trip.getTripStatus().canManuallyTransitionTo(TripStatus.CANCELLED))
        {
            throw new ConflictException("Only trips in a valid status can be cancelled");
        }
        userUtil.validateUserIsHost(tripId, userId);
        trip.setTripStatus(TripStatus.CANCELLED);

        // Do we need cancellation timestamp and reason?
        tripRepository.save(trip);
    }

    @Transactional
    public void autoMarkTripsAsOngoing() {
        List<TripEntity> trips = tripRepository
                .findByTripStatusAndTripStartDateLessThanEqual(
                        TripStatus.PUBLISHED, LocalDate.now());

        trips.forEach(trip -> {
            if (trip.getTripStatus().canAutomaticallyTransitionTo(TripStatus.ONGOING)) {
                trip.setTripStatus(TripStatus.ONGOING);
            }
        });
    }

    @Transactional
    public void autoMarkTripsAsCompleted() {
        List<TripEntity> trips = tripRepository
                .findByTripStatusAndTripEndDateBefore(
                        TripStatus.ONGOING, LocalDate.now());

        trips.forEach(trip -> {
            if (trip.getTripStatus().canAutomaticallyTransitionTo(TripStatus.COMPLETED)) {
                trip.setTripStatus(TripStatus.COMPLETED);
            }
        });
    }

    private static void setTripItineraryEntity(TripItineraryDto tripItineraryDto, TripEntity tripEntity, TripItineraryEntity tripItineraryEntity) {
        tripItineraryEntity.setDayNumber(tripItineraryDto.getDayNumber());
        tripItineraryEntity.setTitle(tripItineraryDto.getTitle());
        tripItineraryEntity.setDescription(tripItineraryDto.getDescription());
        tripItineraryEntity.setMeals(tripItineraryDto.getMeals());
        tripItineraryEntity.setActivities(tripItineraryDto.getActivities());
        tripItineraryEntity.setStay(tripItineraryDto.getStay());
        tripItineraryEntity.setTrip(tripEntity);
    }

    private void updateDraftTripBasicInfo(TripEntity trip, TripDto tripDto)
    {
        trip.setTripDescription(tripDto.getTripDescription());
        trip.setTripTitle(tripDto.getTripTitle());
        trip.setTripDestination(tripDto.getTripDestination());
        if (tripDto.getTripStartDate() != null &&
                tripDto.getTripEndDate() != null && tripDto.getTripStartDate().isAfter(tripDto.getTripEndDate()))
        {
            throw new BadRequestException("Trip start date cannot be after end date");
        }
        trip.setTripStartDate(tripDto.getTripStartDate());
        trip.setTripEndDate(tripDto.getTripEndDate());
        trip.setEstimatedBudget(tripDto.getEstimatedBudget());
        trip.setMaxParticipants(tripDto.getMaxParticipants());
        trip.setJoinPolicy(tripDto.getJoinPolicy());
        trip.setVisibilityStatus(tripDto.getVisibilityStatus());
    }

    private void updateDraftTripPolicy(TripEntity trip, TripDto tripDto)
    {
        if (tripDto.getTripPolicy() != null)
        {
            TripPolicyEntity tripPolicyEntity = trip.getTripPolicyEntity();
            if (tripPolicyEntity == null)
            {
                tripPolicyEntity = new TripPolicyEntity();
                tripPolicyEntity.setTrip(trip);
                trip.setTripPolicyEntity(tripPolicyEntity);
            }
            tripPolicyEntity.setCancellationPolicy(tripDto.getTripPolicy().getCancellationPolicy());
            tripPolicyEntity.setRefundPolicy(tripDto.getTripPolicy().getRefundPolicy());
        }
    }

    private void updateDraftTripMetadata(TripEntity trip, TripDto tripDto)
    {
        if (tripDto.getTripMetaData() != null)
        {
            TripMetaDataEntity tripMetaDataEntity = trip.getTripMetaData();
            if (tripMetaDataEntity == null)
            {
                tripMetaDataEntity = new TripMetaDataEntity();
                tripMetaDataEntity.setTrip(trip);
                trip.setTripMetaData(tripMetaDataEntity);
            }
            tripMetaDataEntity.setTripSummary(tripDto.getTripMetaData().getTripSummary());
            tripMetaDataEntity.setWhatsIncluded(tripDto.getTripMetaData().getWhatsIncluded());
            tripMetaDataEntity.setWhatsExcluded(tripDto.getTripMetaData().getWhatsExcluded());
        }
    }

    private void updateDraftTripTags(TripEntity trip, TripDto tripDto)
    {
        if (tripDto.getTripTags() == null || tripDto.getTripTags().isEmpty()) {
            trip.getTripTags().clear();
            return;
        }
        Set<TagEntity> updatedTripTags = new HashSet<>();
        for (TripTagDto tagDto : tripDto.getTripTags())
        {
            TagEntity tag = tagRepository.findByTagNameIgnoreCase(tagDto.getTagName())
                    .orElseGet(() -> {
                        TagEntity newTag = new TagEntity();
                        newTag.setTagName(tagDto.getTagName());
                        return tagRepository.save(newTag);
                    });
            updatedTripTags.add(tag);
        }
        trip.getTripTags().clear();
        trip.getTripTags().addAll(updatedTripTags);
    }

    private void updateDraftTripItinerary(TripEntity trip, TripDto tripDto)
    {
        if (tripDto.getTripItineraries() == null || tripDto.getTripItineraries().isEmpty())
        {
            trip.getTripItineraries().clear();
            return;
        }
        Set<TripItineraryEntity> updatedTripItineraries = new HashSet<>();
        for (TripItineraryDto tripItineraryDto : tripDto.getTripItineraries())
        {
            TripItineraryEntity tripItinerary = tripItineraryRepository.findByTrip_TripIdAndDayNumber(trip.getTripId(), tripItineraryDto.getDayNumber())
                    .orElseGet(TripItineraryEntity::new);
            setTripItineraryEntity(tripItineraryDto, trip, tripItinerary);
            updatedTripItineraries.add(tripItinerary);
        }
        trip.getTripItineraries().clear();
        trip.getTripItineraries().addAll(updatedTripItineraries);
    }

    private TripDto mapTripEntityToDto(TripEntity trip)
    {
        return TripDto.builder()
                .tripDescription(trip.getTripDescription())
                .tripTitle(trip.getTripTitle())
                .tripDestination(trip.getTripDestination())
                .tripStartDate(trip.getTripStartDate())
                .tripEndDate(trip.getTripEndDate())
                .estimatedBudget(trip.getEstimatedBudget())
                .maxParticipants(trip.getMaxParticipants())
                .isFull(trip.getIsFull())
                .tripFullReason(trip.getTripFullReason())
                .joinPolicy(trip.getJoinPolicy())
                .visibilityStatus(trip.getVisibilityStatus())
                .tripPolicy(trip.getTripPolicyEntity() != null ? mapTripPolicyToDto(trip.getTripPolicyEntity()) : null)
                .tripMetaData(trip.getTripMetaData() != null ? mapTripMetaDataToDto(trip.getTripMetaData()) : null)
                .tripTags(trip.getTripTags() != null ? mapTripTagsToDto(trip.getTripTags()) :Set.of())
                .tripItineraries(trip.getTripItineraries() != null ? mapTripItinerariesToDto(trip.getTripItineraries()) : Set.of())
                .build();
    }

    private TripPolicyDto mapTripPolicyToDto(TripPolicyEntity tripPolicy)
    {
        return TripPolicyDto.builder()
                .refundPolicy(tripPolicy.getRefundPolicy())
                .cancellationPolicy(tripPolicy.getCancellationPolicy())
                .build();
    }

    private TripMetaDataDto mapTripMetaDataToDto(TripMetaDataEntity tripMetaData)
    {
        return TripMetaDataDto.builder()
                .tripSummary(tripMetaData.getTripSummary())
                .whatsIncluded(tripMetaData.getWhatsIncluded())
                .whatsExcluded(tripMetaData.getWhatsExcluded())
                .build();
    }

    private Set<TripTagDto> mapTripTagsToDto(Set<TagEntity> tripTags)
    {
        return tripTags.stream().map((tag) -> {
            TripTagDto tagDto = new TripTagDto();
            tagDto.setTagName(tag.getTagName());
            return tagDto;
        })
        .collect(Collectors.toSet());
    }

    private Set<TripItineraryDto> mapTripItinerariesToDto(Set<TripItineraryEntity> tripItineraries)
    {
        return tripItineraries.stream()
                .sorted(Comparator.comparingInt(TripItineraryEntity::getDayNumber))
                .map((tripItinerary) -> {
            TripItineraryDto tripItineraryDto = new TripItineraryDto();
            tripItineraryDto.setDayNumber(tripItinerary.getDayNumber());
            tripItineraryDto.setDescription(tripItinerary.getDescription());
            tripItineraryDto.setTitle(tripItinerary.getTitle());
            tripItineraryDto.setStay(tripItinerary.getStay());
            tripItineraryDto.setMeals(tripItinerary.getMeals());
            tripItineraryDto.setActivities(tripItinerary.getActivities());
            return tripItineraryDto;
        })
        .collect(Collectors.toSet());
    }
}
