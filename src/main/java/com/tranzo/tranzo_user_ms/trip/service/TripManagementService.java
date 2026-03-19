package com.tranzo.tranzo_user_ms.trip.service;

import com.tranzo.tranzo_user_ms.commons.exception.*;
import com.tranzo.tranzo_user_ms.trip.dto.*;
import com.tranzo.tranzo_user_ms.trip.enums.*;
import com.tranzo.tranzo_user_ms.trip.exception.*;
import com.tranzo.tranzo_user_ms.trip.events.TripEventPublisher;
import com.tranzo.tranzo_user_ms.trip.events.TripPublishedEventPayloadDto;
import com.tranzo.tranzo_user_ms.trip.model.*;
import com.tranzo.tranzo_user_ms.trip.repository.*;
import com.tranzo.tranzo_user_ms.trip.specification.SpecificationBuilder;
import com.tranzo.tranzo_user_ms.trip.utility.PageableBuilder;
import com.tranzo.tranzo_user_ms.trip.utility.UserUtil;
import com.tranzo.tranzo_user_ms.user.client.UserProfileClient;
import com.tranzo.tranzo_user_ms.user.dto.UserNameDto;
import com.tranzo.tranzo_user_ms.user.service.TravelPalService;
import com.tranzo.tranzo_user_ms.trip.validation.TripPublishEligibilityValidator;
import com.tranzo.tranzo_user_ms.commons.events.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import jakarta.persistence.criteria.Predicate;
import java.util.stream.Collectors;

@Service
public class TripManagementService {
    private final TripRepository tripRepository;
    private final TagRepository tagRepository;
    private final TripItineraryRepository tripItineraryRepository;
    private final TripMemberRepository tripMemberRepository;
    private final TripQueryRepository tripQueryRepository;
    private final TripReportRepository tripReportRepository;
    private final TripPublishEligibilityValidator tripPublishEligibilityValidator;
    private final TripEventPublisher tripEventPublisher;
    private final UserUtil userUtil;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TravelPalService travelPalService;
    private final UserProfileClient userProfileClient;

    public TripManagementService(TripMemberRepository tripMemberRepository,
                                 TripRepository tripRepository,
                                 TagRepository tagRepository,
                                 TripItineraryRepository tripItineraryRepository,
                                 TripQueryRepository tripQueryRepository,
                                 TripReportRepository tripReportRepository,
                                 TripPublishEligibilityValidator tripPublishEligibilityValidator,
                                 TripEventPublisher tripEventPublisher,
                                 UserUtil userUtil,
                                 ApplicationEventPublisher applicationEventPublisher,
                                 TravelPalService travelPalService,
                                 UserProfileClient userProfileClient) {
        this.tripMemberRepository = tripMemberRepository;
        this.tripRepository = tripRepository;
        this.tagRepository = tagRepository;
        this.tripItineraryRepository = tripItineraryRepository;
        this.tripQueryRepository = tripQueryRepository;
        this.tripReportRepository = tripReportRepository;
        this.tripPublishEligibilityValidator = tripPublishEligibilityValidator;
        this.tripEventPublisher = tripEventPublisher;
        this.userUtil = userUtil;
        this.applicationEventPublisher = applicationEventPublisher;
        this.travelPalService = travelPalService;
        this.userProfileClient = userProfileClient;
    }


    @Transactional
    public TripResponseDto createDraftTrip(TripDto tripDto, UUID userId)
    {
        // First check if the user exists and is active.

        TripEntity tripEntity = new TripEntity();
        tripEntity.setTripDescription(tripDto.getTripDescription());
        tripEntity.setTripTitle(tripDto.getTripTitle());
        tripEntity.setTripDestination(tripDto.getTripDestination());
        if (tripDto.getTripStartDate() != null &&
                tripDto.getTripEndDate() != null && tripDto.getTripStartDate().isAfter(tripDto.getTripEndDate()))
        {
            throw new TripValidationException(TripErrorCode.INVALID_DATE_RANGE);
        }
        tripEntity.setTripStartDate(tripDto.getTripStartDate());
        tripEntity.setTripEndDate(tripDto.getTripEndDate());
        tripEntity.setEstimatedBudget(tripDto.getEstimatedBudget());
        tripEntity.setMaxParticipants(tripDto.getMaxParticipants());
        tripEntity.setJoinPolicy(tripDto.getJoinPolicy());
        tripEntity.setVisibilityStatus(tripDto.getVisibilityStatus());
        tripEntity.setTripStatus(TripStatus.DRAFT);
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
        host.setUserId(userId);
        host.setTrip(tripEntity);
        tripEntity.getTripMembers().add(host);
        tripEntity.setCurrentParticipants(1);

        TripEntity newTrip = tripRepository.save(tripEntity);
        return TripResponseDto.builder()
                .tripId(newTrip.getTripId())
                .tripStatus(newTrip.getTripStatus())
                .build();
    }

    @Transactional
    public TripResponseDto updateDraftTrip(TripDto tripDto, UUID tripId, UUID userId)
    {
        TripEntity trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException());
        if (trip.getTripStatus() != TripStatus.DRAFT)
        {
            throw new TripValidationException(TripErrorCode.INVALID_TRIP_STATUS_TRANSITION, "Only trips with DRAFT status can be updated");
        }
        userUtil.validateUserIsHost(tripId, userId);
        updateDraftTripBasicInfo(trip, tripDto);
        updateDraftTripPolicy(trip, tripDto);
        updateDraftTripMetadata(trip, tripDto);
        updateDraftTripTags(trip, tripDto);
        updateDraftTripItinerary(trip, tripDto);
        TripEntity updateTrip = tripRepository.save(trip);
        return TripResponseDto.builder()
                .tripId(updateTrip.getTripId())
                .tripStatus(updateTrip.getTripStatus())
                .build();
    }

    public TripViewDto fetchTrip(UUID tripId, UUID userId)
    {
        TripEntity trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException());
        boolean isTripHost = tripMemberRepository.existsByTrip_TripIdAndUserIdAndRoleAndStatus(tripId, userId, TripMemberRole.HOST, TripMemberStatus.ACTIVE);
        if (trip.getTripStatus() == TripStatus.CANCELLED && !isTripHost) {
            throw new TripAccessDeniedException("Cancelled trip is not accessible for anyone except the host of the trip");
        }
        if (trip.getVisibilityStatus() == VisibilityStatus.PRIVATE)
        {
            tripMemberRepository.findByTrip_TripIdAndUserIdAndStatus(tripId, userId, TripMemberStatus.ACTIVE)
                    .orElseThrow(() -> new TripAccessDeniedException("User is not allowed to view this private trip as the user is not the member of the trip"));
        }
        return mapTripEntityToDto(trip, isTripHost);
    }

    public TripMembersListResponseDto getTripMembers(UUID tripId, UUID userId) {
        TripEntity trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException());
        boolean isTripHost = tripMemberRepository.existsByTrip_TripIdAndUserIdAndRoleAndStatus(tripId, userId, TripMemberRole.HOST, TripMemberStatus.ACTIVE);
        if (trip.getTripStatus() == TripStatus.CANCELLED && !isTripHost) {
            throw new TripAccessDeniedException("Cancelled trip is not accessible for anyone except the host of the trip");
        }
        if (trip.getVisibilityStatus() == VisibilityStatus.PRIVATE) {
            tripMemberRepository.findByTrip_TripIdAndUserIdAndStatus(tripId, userId, TripMemberStatus.ACTIVE)
                    .orElseThrow(() -> new TripAccessDeniedException("User is not allowed to view this private trip as the user is not the member of the trip"));
        }
        List<TripMemberEntity> activeMembers = tripMemberRepository.findByTrip_TripIdAndStatus(tripId, TripMemberStatus.ACTIVE);
        UUID hostUserId = activeMembers.stream()
                .filter(m -> m.getRole() == TripMemberRole.HOST)
                .map(TripMemberEntity::getUserId)
                .findFirst()
                .orElse(null);
        List<UUID> coHostUserIds = activeMembers.stream()
                .filter(m -> m.getRole() == TripMemberRole.CO_HOST)
                .map(TripMemberEntity::getUserId)
                .toList();
        List<UUID> memberUserIds = activeMembers.stream().map(TripMemberEntity::getUserId).toList();
        Map<UUID, UserNameDto> namesByUserId = userProfileClient.getNamesByUserIds(memberUserIds);
        List<TripMemberResponseDto> memberDtos = activeMembers.stream()
                .map(m -> {
                    UserNameDto names = namesByUserId.get(m.getUserId());
                    return TripMemberResponseDto.builder()
                            .membershipId(m.getMembershipId())
                            .userId(m.getUserId())
                            .role(m.getRole())
                            .joinedAt(m.getJoinedAt())
                            .firstName(names != null ? names.getFirstName() : null)
                            .middleName(names != null ? names.getMiddleName() : null)
                            .lastName(names != null ? names.getLastName() : null)
                            .build();
                })
                .toList();
        return TripMembersListResponseDto.builder()
                .tripId(tripId)
                .hostUserId(hostUserId)
                .coHostUserIds(coHostUserIds)
                .members(memberDtos)
                .totalJoined(activeMembers.size())
                .build();
    }

    public List<TripViewDto> getMutualCompletedTrips(UUID currentUserId, UUID otherUserId) {
        if (currentUserId.equals(otherUserId)) {
            return List.of();
        }
        List<TripEntity> trips = tripRepository.findMutualCompletedTrips(
                currentUserId, otherUserId, TripStatus.COMPLETED);
        return trips.stream()
                .map(trip -> {
                    boolean isTripHost = tripMemberRepository.existsByTrip_TripIdAndUserIdAndRoleAndStatus(trip.getTripId(), currentUserId, TripMemberRole.HOST, TripMemberStatus.ACTIVE);
                    return mapTripEntityToDto(trip, isTripHost);
                })
                .toList();
    }

    public List<TripViewDto> fetchTripForUser(final UUID userId)
    {
        List<TripStatus> statuses = List.of(TripStatus.PUBLISHED, TripStatus.ONGOING, TripStatus.COMPLETED);
        List<TripEntity> trips = tripMemberRepository.findTripsByUserIdAndStatusIn(userId, statuses);
        return trips.stream()
                .map(trip -> {
                    boolean isTripHost = tripMemberRepository.existsByTrip_TripIdAndUserIdAndRoleAndStatus(trip.getTripId(), userId, TripMemberRole.HOST, TripMemberStatus.ACTIVE);
                    return mapTripEntityToDto(trip, isTripHost);
                })
                .toList();
    }

    public List<TripViewDto> fetchAllTrips(UUID userId)
    {
        List<TripEntity> trips = tripRepository.findAllTrips(TripStatus.COMPLETED);
        return trips.stream()
                .map(trip -> {
                    Boolean isTripHost = null;
                    if (userId != null)
                    {
                        isTripHost = tripMemberRepository.existsByTrip_TripIdAndUserIdAndRoleAndStatus(trip.getTripId(), userId, TripMemberRole.HOST, TripMemberStatus.ACTIVE);
                    }
                    return mapTripEntityToDto(trip, isTripHost);
                })
                .toList();
    }

    @Transactional
    public void cancelTrip(UUID tripId, UUID userId)
    {
        TripEntity trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException());
        if (!trip.getTripStatus().canManuallyTransitionTo(TripStatus.CANCELLED))
        {
            throw new TripValidationException(TripErrorCode.INVALID_TRIP_STATUS_TRANSITION, "Only trips in a valid status can be cancelled");
        }
        userUtil.validateUserIsHost(tripId, userId);
        trip.setTripStatus(TripStatus.CANCELLED);
        tripRepository.save(trip);

        List<UUID> memberUserIds = tripMemberRepository.findByTrip_TripIdAndStatus(tripId, TripMemberStatus.ACTIVE)
                .stream()
                .map(TripMemberEntity::getUserId)
                .toList();
        applicationEventPublisher.publishEvent(new TripCancelledEvent(tripId, trip.getTripTitle(), memberUserIds));
    }

    @Transactional
    public TripResponseDto publishTrip(UUID tripId, UUID userId)
    {
        TripEntity trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException());
        userUtil.validateUserIsHost(tripId, userId);
        tripPublishEligibilityValidator.validate(trip);
        trip.setTripStatus(TripStatus.PUBLISHED);
        if (trip.getVisibilityStatus() == null)
        {
            trip.setVisibilityStatus(VisibilityStatus.PUBLIC);
        }
        TripEntity updateTrip = tripRepository.save(trip);
        TripPublishedEventPayloadDto eventPayloadDto = TripPublishedEventPayloadDto.builder()
                .eventType("TRIP_PUBLISHED")
                .tripId(tripId)
                .userId(userId)
                .build();

        tripEventPublisher.publishTripPublished(eventPayloadDto);

        return TripResponseDto.builder()
                .tripId(updateTrip.getTripId())
                .tripStatus(updateTrip.getTripStatus())
                .build();
    }

    @Transactional
    public TripResponseDto updateTrip(TripDto tripDto, UUID tripId, UUID userId)
    {
        TripEntity trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException());
        if (trip.getTripStatus() != TripStatus.PUBLISHED && trip.getTripStatus() != TripStatus.ONGOING) {
            throw new TripValidationException(TripErrorCode.INVALID_TRIP_STATUS_TRANSITION, "Only published or ongoing trips can be updated");
        }
        userUtil.validateUserIsHost(tripId, userId);
        updatePublishedTripBasicInfo(trip, tripDto);
        TripEntity updateTrip = tripRepository.save(trip);

        List<UUID> memberUserIds = tripMemberRepository.findByTrip_TripIdAndStatus(tripId, TripMemberStatus.ACTIVE)
                .stream()
                .map(TripMemberEntity::getUserId)
                .toList();
        if (!memberUserIds.isEmpty()) {
            applicationEventPublisher.publishEvent(
                    new TripDetailsChangedEvent(tripId, trip.getTripTitle(), memberUserIds));
        }

        return TripResponseDto.builder()
                .tripId(updateTrip.getTripId())
                .tripStatus(updateTrip.getTripStatus())
                .build();
    }

    private void updatePublishedTripBasicInfo(TripEntity trip, TripDto tripDto) {
        if (tripDto.getTripTitle() != null) trip.setTripTitle(tripDto.getTripTitle());
        if (tripDto.getTripDescription() != null) trip.setTripDescription(tripDto.getTripDescription());
        if (tripDto.getTripDestination() != null) trip.setTripDestination(tripDto.getTripDestination());
        if (tripDto.getTripStartDate() != null) trip.setTripStartDate(tripDto.getTripStartDate());
        if (tripDto.getTripEndDate() != null) {
            if (tripDto.getTripStartDate() != null && tripDto.getTripStartDate().isAfter(tripDto.getTripEndDate())) {
                throw new TripValidationException(TripErrorCode.INVALID_DATE_RANGE);
            }
            trip.setTripEndDate(tripDto.getTripEndDate());
        }
        if (tripDto.getEstimatedBudget() != null) trip.setEstimatedBudget(tripDto.getEstimatedBudget());
        if (tripDto.getMaxParticipants() != null) trip.setMaxParticipants(tripDto.getMaxParticipants());
        if (tripDto.getJoinPolicy() != null) trip.setJoinPolicy(tripDto.getJoinPolicy());
        if (tripDto.getVisibilityStatus() != null) trip.setVisibilityStatus(tripDto.getVisibilityStatus());
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
                List<UUID> memberUserIds = tripMemberRepository
                        .findByTrip_TripIdAndStatus(trip.getTripId(), TripMemberStatus.ACTIVE)
                        .stream()
                        .map(TripMemberEntity::getUserId)
                        .toList();
                applicationEventPublisher.publishEvent(
                        new TripCompletedEvent(trip.getTripId(), trip.getTripTitle(), memberUserIds));
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
            throw new TripValidationException(TripErrorCode.INVALID_DATE_RANGE);
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

    private TripViewDto mapTripEntityToDto(TripEntity trip, Boolean isTripHost)
    {
        int activeMemberCount = tripMemberRepository.countByTrip_TripIdAndStatus(
                trip.getTripId(), TripMemberStatus.ACTIVE);
        return TripViewDto.builder()
                .tripId(trip.getTripId())
                .tripDescription(trip.getTripDescription())
                .tripTitle(trip.getTripTitle())
                .tripDestination(trip.getTripDestination())
                .tripStartDate(trip.getTripStartDate())
                .tripEndDate(trip.getTripEndDate())
                .estimatedBudget(trip.getEstimatedBudget())
                .maxParticipants(trip.getMaxParticipants())
                .currentParticipants(activeMemberCount)
                .isFull(trip.getIsFull())
                .splitWiseGroupId(trip.getSplitwiseGroupId())
                .isTripHost(isTripHost)
                .tripStatus(trip.getTripStatus())
                .tripFullReason(trip.getTripFullReason())
                .joinPolicy(trip.getJoinPolicy())
                .visibilityStatus(trip.getVisibilityStatus())
                .tripPolicy(trip.getTripPolicyEntity() != null ? mapTripPolicyToDto(trip.getTripPolicyEntity()) : null)
                .tripMetaData(trip.getTripMetaData() != null ? mapTripMetaDataToDto(trip.getTripMetaData()) : null)
                .tripTags(trip.getTripTags() != null ? mapTripTagsToDto(trip.getTripTags()) :Set.of())
                .tripItineraries(trip.getTripItineraries() != null ? mapTripItinerariesToDto(trip.getTripItineraries()) : Set.of())
                .build();
    }

    private TripPolicyViewDto mapTripPolicyToDto(TripPolicyEntity tripPolicy)
    {
        return TripPolicyViewDto.builder()
                .policyId(tripPolicy.getTripPolicyId())
                .refundPolicy(tripPolicy.getRefundPolicy())
                .cancellationPolicy(tripPolicy.getCancellationPolicy())
                .build();
    }

    private TripMetaDataViewDto mapTripMetaDataToDto(TripMetaDataEntity tripMetaData)
    {
        return TripMetaDataViewDto.builder()
                .metadataId(tripMetaData.getTripMetaDataId())
                .tripSummary(tripMetaData.getTripSummary())
                .whatsIncluded(tripMetaData.getWhatsIncluded())
                .whatsExcluded(tripMetaData.getWhatsExcluded())
                .build();
    }

    private Set<TripTagViewDto> mapTripTagsToDto(Set<TagEntity> tripTags)
    {
        return tripTags.stream().map((tag) -> {
            TripTagViewDto tagDto = new TripTagViewDto();
            tagDto.setTagId(tag.getTagId());
            tagDto.setTagName(tag.getTagName());
            return tagDto;
        })
        .collect(Collectors.toSet());
    }

    private Set<TripItineraryViewDto> mapTripItinerariesToDto(Set<TripItineraryEntity> tripItineraries)
    {
        return tripItineraries.stream()
                .sorted(Comparator.comparingInt(TripItineraryEntity::getDayNumber))
                .map((tripItinerary) -> {
            TripItineraryViewDto tripItineraryDto = new TripItineraryViewDto();
            tripItineraryDto.setItineraryId(tripItinerary.getItineraryId());
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


    public void addTripQnA(UUID userID, CreateQnaRequestDto createQnaRequestDto, UUID tripId){
            TripEntity trip = tripRepository.findById(tripId)
                    .orElseThrow(() -> new TripNotFoundException());

            userUtil.validateUserIsHost(tripId, userID);

            if(createQnaRequestDto.getQuestion() == null || createQnaRequestDto.getQuestion().trim().isEmpty()){
                throw new TripQnaException(TripErrorCode.QUESTION_EMPTY);
            }

            if(trip.getTripStatus().equals(TripStatus.PUBLISHED)){
                TripQueryEntity tripQueryEntity = TripQueryEntity.builder()
                        .question(createQnaRequestDto.getQuestion())
                        .answer(null)
                        .askedBy(userID)
                        .trip(trip)
                        .visibility(TripQueryVisibility.HOST_AND_CO_HOSTS)
                        .build();

                tripQueryRepository.save(tripQueryEntity);

                List<UUID> memberUserIds = tripMemberRepository.findByTrip_TripIdAndStatus(tripId, TripMemberStatus.ACTIVE)
                        .stream()
                        .map(TripMemberEntity::getUserId)
                        .toList();
                if (!memberUserIds.isEmpty()) {
                    applicationEventPublisher.publishEvent(
                            new TripQuestionAskedEvent(tripId, trip.getTripTitle(), userID, memberUserIds));
                }
            }
            else {
                throw new TripQnaException(TripErrorCode.INVALID_QNA_STATUS, "QnA can be added only to published trips");
            }
    }

    public void answerTripQnA(UUID userID, UUID tripId, UUID qnaId,AnswerQnaRequestDto answerQnaRequestDto){
            TripEntity trip = tripRepository.findById(tripId)
                    .orElseThrow(()-> new TripNotFoundException());

            // giving flexibilty  to answer open question in ongoing and completed state

            if(trip.getTripStatus() == TripStatus.CANCELLED){
                throw new TripQnaException(TripErrorCode.INVALID_QNA_STATUS, "QnA cannot be answered for cancelled or Completed trips");
            }

           // userUtil.validateUserIsHost(tripId, userID);

            TripQueryEntity tripQuery = tripQueryRepository.findByQueryIdAndTrip_TripId(qnaId,tripId)
                    .orElseThrow(()-> new TripQnaException(TripErrorCode.QNA_NOT_FOUND));

            // are we keeping edit answer as seperate api ? if not then we have to remove this validation
            if(tripQuery.getAnswer() != null){
                throw new TripQnaException(TripErrorCode.QNA_ALREADY_ANSWERED);
            }
            tripQuery.setAnswer(answerQnaRequestDto.getAnswer());
            tripQuery.setAnsweredAt(LocalDateTime.now());
            tripQuery.setAnsweredBy(userID);
            tripQueryRepository.save(tripQuery);

            applicationEventPublisher.publishEvent(
                    new TripQuestionAnsweredEvent(tripId, trip.getTripTitle(), tripQuery.getAskedBy()));
    }

    // what about question answer visibility : joined as well as not joined users
    public List<TripQnaResponseDto> getTripQna(UUID tripId,UUID userId){
        TripEntity trip = tripRepository.findById(tripId)
                .orElseThrow(()-> new TripNotFoundException());

        if(trip.getVisibilityStatus()== VisibilityStatus.PRIVATE &&
                !tripMemberRepository.existsByTrip_TripIdAndUserIdAndStatus(tripId,userId,TripMemberStatus.ACTIVE)){
            throw new TripAccessDeniedException("User is not allowed to fetch QnA of this trip");
        }

        List<TripQueryEntity> tripQueries = tripQueryRepository.findByTrip_TripIdOrderByCreatedAtDesc(tripId);

        Set<UUID> userIds = new HashSet<>();
        for (TripQueryEntity q : tripQueries) {
            userIds.add(q.getAskedBy());
            if (q.getAnsweredBy() != null) userIds.add(q.getAnsweredBy());
        }
        Map<UUID, UserNameDto> namesByUserId = userProfileClient.getNamesByUserIds(userIds);

        return tripQueries.stream()
                .map(q -> mapToTripQueryResponseDto(q, namesByUserId))
                .toList();
    }

    private TripQnaResponseDto mapToTripQueryResponseDto(TripQueryEntity tripQueryEntity, Map<UUID, UserNameDto> namesByUserId){
        UserNameDto askedByNames = namesByUserId != null ? namesByUserId.get(tripQueryEntity.getAskedBy()) : null;
        UserNameDto answeredByNames = tripQueryEntity.getAnsweredBy() != null && namesByUserId != null
                ? namesByUserId.get(tripQueryEntity.getAnsweredBy()) : null;
        return TripQnaResponseDto.builder()
                .qnaId(tripQueryEntity.getQueryId())
                .tripId(tripQueryEntity.getTrip().getTripId())
                .authorUserId(tripQueryEntity.getAskedBy())
                .askedByFirstName(askedByNames != null ? askedByNames.getFirstName() : null)
                .askedByMiddleName(askedByNames != null ? askedByNames.getMiddleName() : null)
                .askedByLastName(askedByNames != null ? askedByNames.getLastName() : null)
                .question(tripQueryEntity.getQuestion())
                .answer(tripQueryEntity.getAnswer())
                .answeredBy(tripQueryEntity.getAnsweredBy())
                .answeredByFirstName(answeredByNames != null ? answeredByNames.getFirstName() : null)
                .answeredByMiddleName(answeredByNames != null ? answeredByNames.getMiddleName() : null)
                .answeredByLastName(answeredByNames != null ? answeredByNames.getLastName() : null)
                .answeredAt(tripQueryEntity.getAnsweredAt())
                .createdAt(tripQueryEntity.getCreatedAt())
                .build();
    }

    public void reportTrip(UUID reportingUserId,UUID tripId, ReportTripRequestDto reportTripRequestDto) {
             TripEntity trip = tripRepository.findById(tripId)
                    .orElseThrow(() -> new TripNotFoundException());

             if(trip.getVisibilityStatus()== VisibilityStatus.PRIVATE &&
                !tripMemberRepository.existsByTrip_TripIdAndUserIdAndStatus(tripId,reportingUserId,TripMemberStatus.ACTIVE)){
                throw new TripAccessDeniedException("User is not allowed to report this private trip");
             }

             if(tripReportRepository.existsByReportedByAndTrip_TripId(reportingUserId,tripId)) {
                throw new TripValidationException(TripErrorCode.TRIP_ALREADY_REPORTED);
             }

             if(reportTripRequestDto.getReportReason()==null || reportTripRequestDto.getReportReason().trim().isEmpty()){
                throw new TripValidationException(TripErrorCode.REPORT_REASON_EMPTY);
             }

            TripReportEntity tripReportEntity = TripReportEntity.builder()
                    .trip(trip)
                    .reportedBy(reportingUserId)
                    .reportReason(reportTripRequestDto.getReportReason())
                    .build();
            tripReportEntity.setStatus(TripReportStatus.OPEN);
            tripReportRepository.save(tripReportEntity);
    }
    @Transactional
    public void promoteToCoHost(UUID userId, UUID tripId, UUID participantUserId) {
        TripEntity trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException());

        userUtil.validateUserIsHost(tripId, userId);
        // Do I need to make seprate exception for this one
        if(userId.equals(participantUserId)){
            throw new TripMemberException(TripErrorCode.HOST_CANNOT_BE_CO_HOST);
        }

        TripMemberEntity participant = tripMemberRepository
                .findByTrip_TripIdAndUserIdAndStatus(tripId, participantUserId, TripMemberStatus.ACTIVE)
                .orElseThrow(() -> new TripMemberException(TripErrorCode.PARTICIPANT_NOT_FOUND));

        if (participant.getRole() == TripMemberRole.CO_HOST) {
            throw new TripMemberException(TripErrorCode.USER_ALREADY_CO_HOST);
        }

        if (participant.getRole() != TripMemberRole.MEMBER) {
            throw new TripMemberException(TripErrorCode.ONLY_MEMBER_CAN_BE_PROMOTED);
        }

        participant.setRole(TripMemberRole.CO_HOST);
        tripMemberRepository.save(participant);

        List<UUID> allMemberUserIds = tripMemberRepository.findByTrip_TripIdAndStatus(tripId, TripMemberStatus.ACTIVE)
                .stream()
                .map(TripMemberEntity::getUserId)
                .toList();
        if (!allMemberUserIds.isEmpty()) {
            applicationEventPublisher.publishEvent(
                    new MemberPromotedToCoHostEvent(trip.getTripId(), trip.getTripTitle(), participantUserId, allMemberUserIds));
        }
    }

    @Transactional
    public void markTripFull(UUID userId, UUID tripId) {
        TripEntity trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException());
        userUtil.validateUserIsHost(tripId, userId);
        if (Boolean.TRUE.equals(trip.getIsFull())) {
            throw new TripValidationException(TripErrorCode.TRIP_ALREADY_MARKED_FULL);
        }
        trip.setIsFull(true);
        tripRepository.save(trip);

        UUID hostUserId = tripMemberRepository.findByTrip_TripIdAndStatus(tripId, TripMemberStatus.ACTIVE)
                .stream()
                .filter(m -> m.getRole() == TripMemberRole.HOST)
                .map(TripMemberEntity::getUserId)
                .findFirst()
                .orElse(null);
        List<UUID> membersExcludingHost = tripMemberRepository.findByTrip_TripIdAndStatus(tripId, TripMemberStatus.ACTIVE)
                .stream()
                .map(TripMemberEntity::getUserId)
                .filter(id -> !id.equals(hostUserId))
                .toList();
        if (!membersExcludingHost.isEmpty()) {
            applicationEventPublisher.publishEvent(
                    new TripMarkedFullByHostEvent(tripId, trip.getTripTitle(), membersExcludingHost));
        }
    }

    @Transactional
    public void broadcastTripToTravelPals(UUID userId, UUID tripId) {
        TripEntity trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException());
        userUtil.validateUserIsHost(tripId, userId);
        if (trip.getTripStatus() != TripStatus.PUBLISHED) {
            throw new TripValidationException(TripErrorCode.TRIP_NOT_BROADCASTABLE, "Can only broadcast published trips");
        }

        List<UUID> travelPals = travelPalService.getMyTravelPals(userId);
        List<UUID> broadcastToUserIds = travelPals.stream()
                .filter(palId -> !tripMemberRepository.existsByTrip_TripIdAndUserIdAndStatus(tripId, palId, TripMemberStatus.ACTIVE))
                .toList();

        if (!broadcastToUserIds.isEmpty()) {
            applicationEventPublisher.publishEvent(
                    new TripBroadcastEvent(tripId, trip.getTripTitle(), userId, broadcastToUserIds));
        }
    }

    public Page<TripViewDto> search(
            SearchRequest request,
            List<String> globalFields
    ) {
        SpecificationBuilder<TripEntity> builder = new SpecificationBuilder<>();
        Specification<TripEntity> spec = builder.build(request.getFilters());
        if (request.getGlobalSearch() != null && globalFields != null && !globalFields.isEmpty()) {
            Specification<TripEntity> globalSpec =
                    buildGlobalSearch(request.getGlobalSearch(), globalFields);
            spec = spec.and(globalSpec);
        }
        Pageable pageable = PageableBuilder.build(request);
        Page<TripEntity> trips = tripRepository.findAll(spec, pageable);
        return trips.map(trip -> {
            return mapTripEntityToDto(trip, null);
        });
    }

    private Specification<TripEntity> buildGlobalSearch(
            String keyword,
            List<String> fields
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (String field : fields) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get(field).as(String.class)),
                                "%" + keyword.toLowerCase() + "%"
                        )
                );
            }
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
