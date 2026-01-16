package com.tranzo.tranzo_user_ms.trip.validation;


import com.tranzo.tranzo_user_ms.trip.enums.TripStatus;
import com.tranzo.tranzo_user_ms.trip.exception.TripPublishException;
import com.tranzo.tranzo_user_ms.trip.model.TripEntity;
import org.springframework.stereotype.Component;

import static com.tranzo.tranzo_user_ms.trip.enums.TripPublishErrorCode.*;

@Component
public class TripPublishEligibilityValidator {

    public void validate(TripEntity trip) {

        if (trip.getTripStatus() == TripStatus.PUBLISHED) {
            throw new TripPublishException(TRIP_ALREADY_PUBLISHED);
        }

        if (isBlank(trip.getTripTitle())) {
            throw new TripPublishException(TITLE_MISSING);
        }

        if (isBlank(trip.getTripDescription())) {
            throw new TripPublishException(DESCRIPTION_MISSING);
        }

        if (isBlank(trip.getTripDestination())) {
            throw new TripPublishException(DESTINATION_MISSING);
        }

        if (trip.getTripStartDate() == null) {
            throw new TripPublishException(START_DATE_MISSING);
        }

        if (trip.getTripEndDate() == null) {
            throw new TripPublishException(END_DATE_MISSING);
        }

        if (trip.getTripEndDate().isBefore(trip.getTripStartDate())) {
            throw new TripPublishException(INVALID_DATE_RANGE);
        }

        if (trip.getEstimatedBudget() == null) {
            throw new TripPublishException(ESTIMATED_BUDGET_MISSING);
        }

        if (trip.getEstimatedBudget() <= 0) {
            throw new TripPublishException(INVALID_ESTIMATED_BUDGET);
        }

        if (trip.getMaxParticipants() == null) {
            throw new TripPublishException(MAX_PARTICIPANTS_MISSING);
        }

        if (trip.getMaxParticipants() <= 0) {
            throw new TripPublishException(INVALID_MAX_PARTICIPANTS);
        }

        if (trip.getJoinPolicy() == null) {
            throw new TripPublishException(JOIN_POLICY_MISSING);
        }

        if (trip.getTripItineraries() == null || trip.getTripItineraries().isEmpty()) {
            throw new TripPublishException(ITINERARY_MISSING);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

