package com.tranzo.tranzo_user_ms.trip.service;

import com.tranzo.tranzo_user_ms.commons.exception.*;
import com.tranzo.tranzo_user_ms.trip.dto.*;
import com.tranzo.tranzo_user_ms.trip.enums.*;
import com.tranzo.tranzo_user_ms.trip.exception.TripPublishException;
import com.tranzo.tranzo_user_ms.trip.events.TripEventPublisher;
import com.tranzo.tranzo_user_ms.trip.events.TripPublishedEventPayloadDto;
import com.tranzo.tranzo_user_ms.trip.model.*;
import com.tranzo.tranzo_user_ms.trip.repository.*;
import com.tranzo.tranzo_user_ms.trip.utility.UserUtil;
import com.tranzo.tranzo_user_ms.trip.validation.TripPublishEligibilityValidator;
import com.tranzo.tranzo_user_ms.commons.events.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.tranzo.tranzo_user_ms.trip.enums.TripPublishErrorCode.*;

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

    public TripManagementService(TripMemberRepository tripMemberRepository,
                                 TripRepository tripRepository,
                                 TagRepository tagRepository,
                                 TripItineraryRepository tripItineraryRepository,
                                 TripQueryRepository tripQueryRepository,
                                 TripReportRepository tripReportRepository,
                                 TripPublishEligibilityValidator tripPublishEligibilityValidator,
                                 TripEventPublisher tripEventPublisher,
                                 UserUtil userUtil,
                                 ApplicationEventPublisher applicationEventPublisher) {
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
            throw new TripPublishException(INVALID_DATE_RANGE);
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
                .orElseThrow(() -> new TripPublishException(TRIP_NOT_FOUND));
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
        return TripResponseDto.builder()
                .tripId(updateTrip.getTripId())
                .tripStatus(updateTrip.getTripStatus())
                .build();
    }

    public TripViewDto fetchTrip(UUID tripId, UUID userId)
    {
        TripEntity trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripPublishException(TRIP_NOT_FOUND));
        boolean isTripHost = tripMemberRepository.existsByTrip_TripIdAndUserIdAndRoleAndStatus(tripId, userId, TripMemberRole.HOST, TripMemberStatus.ACTIVE);
        if (trip.getTripStatus() == TripStatus.CANCELLED && !isTripHost) {
            throw new ForbiddenException("Cancelled trip is not accessible for anyone except the host of the trip");
        }
        if (trip.getVisibilityStatus() == VisibilityStatus.PRIVATE)
        {
            tripMemberRepository.findByTrip_TripIdAndUserIdAndStatus(tripId, userId, TripMemberStatus.ACTIVE)
                    .orElseThrow(() -> new ForbiddenException("User is not allowed to view this private trip as the user is not the member of the trip"));
        }
        return mapTripEntityToDto(trip);
    }

    public List<TripViewDto> getMutualCompletedTrips(UUID currentUserId, UUID otherUserId) {
        if (currentUserId.equals(otherUserId)) {
            return List.of();
        }
        List<TripEntity> trips = tripRepository.findMutualCompletedTrips(
                currentUserId, otherUserId, TripStatus.COMPLETED);
        return trips.stream()
                .map(this::mapTripEntityToDto)
                .toList();
    }

    @Transactional
    public void cancelTrip(UUID tripId, UUID userId)
    {
        TripEntity trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripPublishException(TRIP_NOT_FOUND));
        if (!trip.getTripStatus().canManuallyTransitionTo(TripStatus.CANCELLED))
        {
            throw new ConflictException("Only trips in a valid status can be cancelled");
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
                .orElseThrow(() -> new TripPublishException(TRIP_NOT_FOUND));
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
                .orElseThrow(() -> new TripPublishException(TRIP_NOT_FOUND));
        if (trip.getTripStatus() != TripStatus.PUBLISHED && trip.getTripStatus() != TripStatus.ONGOING) {
            throw new ConflictException("Only published or ongoing trips can be updated");
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
                throw new TripPublishException(INVALID_DATE_RANGE);
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
            throw new TripPublishException(INVALID_DATE_RANGE);
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

    private TripViewDto mapTripEntityToDto(TripEntity trip)
    {
        return TripViewDto.builder()
                .tripId(trip.getTripId())
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
                    .orElseThrow(() -> new EntityNotFoundException("Trip not found"));

            userUtil.validateUserIsHost(tripId, userID);

            if(createQnaRequestDto.getQuestion() == null || createQnaRequestDto.getQuestion().trim().isEmpty()){
                throw new BadRequestException("Question cannot be empty");
            }

            if(trip.getTripStatus().equals(TripStatus.PUBLISHED)){
                TripQueryEntity tripQueryEntity = TripQueryEntity.builder()
                        .question(createQnaRequestDto.getQuestion())
                        .answer(null)
                        .askedBy(userID)
                        .trip(trip)
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
                throw new ConflictException("QnA can be added only to published trips");
            }
    }

    public void answerTripQnA(UUID userID, UUID tripId, UUID qnaId,AnswerQnaRequestDto answerQnaRequestDto){
            TripEntity trip = tripRepository.findById(tripId)
                    .orElseThrow(()-> new EntityNotFoundException("Trip not found"));

            // giving flexibilty  to answer open question in ongoing and completed state

            if(trip.getTripStatus() == TripStatus.CANCELLED){
                throw new ConflictException("QnA cannot be answered for cancelled or Completed trips");
            }

           // userUtil.validateUserIsHost(tripId, userID);

            TripQueryEntity tripQuery = tripQueryRepository.findByQueryIdAndTrip_TripId(qnaId,tripId)
                    .orElseThrow(()-> new EntityNotFoundException("QnA not found for the given trip"));

            // are we keeping edit answer as seperate api ? if not then we have to remove this validation
            if(tripQuery.getAnswer() != null){
                throw new ConflictException("QnA has already been answered");
            }
            tripQuery.setAnswer(answerQnaRequestDto.getAnswer());
            tripQuery.setAnsweredAt(LocalDateTime.now());
            tripQueryRepository.save(tripQuery);

            applicationEventPublisher.publishEvent(
                    new TripQuestionAnsweredEvent(tripId, trip.getTripTitle(), tripQuery.getAskedBy()));
    }

    // what about question answer visibility : joined as well as not joined users
    public List<TripQnaResponseDto> getTripQna(UUID tripId,UUID userId){
        TripEntity trip = tripRepository.findById(tripId)
                .orElseThrow(()-> new EntityNotFoundException("Trip not found"));

        if(trip.getVisibilityStatus()== VisibilityStatus.PRIVATE &&
                !tripMemberRepository.existsByTrip_TripIdAndUserIdAndStatus(tripId,userId,TripMemberStatus.ACTIVE)){
            throw new ForbiddenException("User is not allowed to fetch QnA of this trip");
        }

        List<TripQueryEntity> tripQueries = tripQueryRepository.findByTrip_TripIdOrderByCreatedAtDesc(tripId);

        return tripQueries.stream()
                .map(this::mapToTripQueryResponseDto)
                .toList();
    }

    private TripQnaResponseDto mapToTripQueryResponseDto(TripQueryEntity tripQueryEntity){
        return TripQnaResponseDto.builder()
                .qnaId(tripQueryEntity.getQueryId())
                .tripId(tripQueryEntity.getTrip().getTripId())
                .authorUserId(tripQueryEntity.getAskedBy())
                .question(tripQueryEntity.getQuestion())
                .answer(tripQueryEntity.getAnswer())
                .answeredAt(tripQueryEntity.getAnsweredAt())
                .createdAt(tripQueryEntity.getCreatedAt())
                .build();
    }

    public void reportTrip(UUID reportingUserId,UUID tripId, ReportTripRequestDto reportTripRequestDto) {
             TripEntity trip = tripRepository.findById(tripId)
                    .orElseThrow(() -> new EntityNotFoundException("Trip not found"));

             if(trip.getVisibilityStatus()== VisibilityStatus.PRIVATE &&
                !tripMemberRepository.existsByTrip_TripIdAndUserIdAndStatus(tripId,reportingUserId,TripMemberStatus.ACTIVE)){
                throw new ForbiddenException("User is not allowed to report this private trip");
             }

             if(tripReportRepository.existsByReportedByAndTrip_TripId(reportingUserId,tripId)) {
                throw new ConflictException("User has already reported this trip");
             }

             if(reportTripRequestDto.getReportReason()==null || reportTripRequestDto.getReportReason().trim().isEmpty()){
                throw new BadRequestException("Report reason cannot be empty");
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
                .orElseThrow(() -> new EntityNotFoundException("Trip not found"));

        userUtil.validateUserIsHost(tripId, userId);
        // Do I need to make seprate exception for this one
        if(userId.equals(participantUserId)){
            throw new BadRequestException("Host can make itself as cohost");
        }

        TripMemberEntity participant = tripMemberRepository
                .findByTrip_TripIdAndUserIdAndStatus(tripId, participantUserId, TripMemberStatus.ACTIVE)
                .orElseThrow(() -> new EntityNotFoundException("Participant not found or not active in the trip"));

        if (participant.getRole() == TripMemberRole.CO_HOST) {
            throw new ConflictException("Participant is already a CO-HOST");
        }

        if (participant.getRole() != TripMemberRole.MEMBER) {
            throw new ConflictException("Only participants with MEMBER role can be promoted to CO-HOST");
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
                .orElseThrow(() -> new EntityNotFoundException("Trip not found"));
        userUtil.validateUserIsHost(tripId, userId);
        if (Boolean.TRUE.equals(trip.getIsFull())) {
            throw new ConflictException("Trip is already marked full");
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

}
