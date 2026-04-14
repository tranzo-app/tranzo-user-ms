package com.tranzo.tranzo_user_ms.splitwise.events;

import com.tranzo.tranzo_user_ms.commons.events.JoinRequestApprovedEvent;
import com.tranzo.tranzo_user_ms.commons.events.ParticipantJoinedTripEvent;
import com.tranzo.tranzo_user_ms.commons.events.TripPublishedEvent;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.GroupResponse;
import com.tranzo.tranzo_user_ms.splitwise.service.SplitwiseGroupService;
import com.tranzo.tranzo_user_ms.trip.model.TripEntity;
import com.tranzo.tranzo_user_ms.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Listens to trip-related events and keeps the Splitwise group in sync with trip membership:
 * - On trip publish: create a Splitwise group for the trip and add the host as admin.
 * - On participant join (auto-approve or host-approve): add the user to the trip's Splitwise group.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SplitwiseTripEventListener {

    private final TripRepository tripRepository;
    private final SplitwiseGroupService splitwiseGroupService;

    @EventListener
    @Transactional
    public void onTripPublished(TripPublishedEvent event) {
        UUID tripId = event.getTripId();
        UUID hostUserId = event.getHostUserId();
        log.info("Creating Splitwise group for published trip {}, host {}", tripId, hostUserId);

        TripEntity trip = tripRepository.findById(tripId).orElse(null);
        String tripTitle = trip != null ? trip.getTripTitle() : null;
        if (tripTitle == null) {
            tripTitle = "Trip " + tripId;
        }

        try {
            GroupResponse groupResponse = splitwiseGroupService.createGroupForTrip(tripId, tripTitle, hostUserId);
            if (trip != null && groupResponse != null && groupResponse.getId() != null) {
                trip.setSplitwiseGroupId(groupResponse.getId());
                tripRepository.save(trip);
                log.info("Stored splitwise_group_id {} on trip {}", groupResponse.getId(), tripId);
            }
        } catch (Exception e) {
            log.error("Failed to create Splitwise group for trip {}", tripId, e);
        }
    }

    @EventListener
    public void onParticipantJoinedTrip(ParticipantJoinedTripEvent event) {
        UUID tripId = event.getTripId();
        UUID userId = event.getUserId();
        log.info("Adding user {} to Splitwise group for trip {}", userId, tripId);

        try {
            splitwiseGroupService.addMemberToGroupByTripId(tripId, userId);
        } catch (Exception e) {
            log.error("Failed to add user {} to Splitwise group for trip {}", userId, tripId, e);
        }
    }

    @EventListener
    public void onJoinRequestApproved(JoinRequestApprovedEvent event) {
        UUID tripId = event.getTripId();
        UUID requestorUserId = event.getRequestorUserId();
        log.info("Adding approved user {} to Splitwise group for trip {}", requestorUserId, tripId);

        try {
            splitwiseGroupService.addMemberToGroupByTripId(tripId, requestorUserId);
        } catch (Exception e) {
            log.error("Failed to add user {} to Splitwise group for trip {}", requestorUserId, tripId, e);
        }
    }
}
