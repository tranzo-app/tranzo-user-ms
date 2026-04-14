package com.tranzo.tranzo_user_ms.trip.validation;


import com.tranzo.tranzo_user_ms.trip.enums.TripStatus;
import com.tranzo.tranzo_user_ms.trip.exception.TripValidationException;
import com.tranzo.tranzo_user_ms.trip.model.TripEntity;
import org.springframework.stereotype.Component;

import static com.tranzo.tranzo_user_ms.trip.enums.TripErrorCode.*;

@Component
public class TripPublishEligibilityValidator {

    public void validate(TripEntity trip) {

        if (trip.getTripStatus() == TripStatus.PUBLISHED) {
            throw new TripValidationException(TRIP_ALREADY_PUBLISHED);
        }

        if (isBlank(trip.getTripTitle())) {
            throw new TripValidationException(TITLE_MISSING);
        }

        if (isBlank(trip.getTripDescription())) {
            throw new TripValidationException(DESCRIPTION_MISSING);
        }

        if (isBlank(trip.getTripDestination())) {
            throw new TripValidationException(DESTINATION_MISSING);
        }

        if (trip.getTripStartDate() == null) {
            throw new TripValidationException(START_DATE_MISSING);
        }

        if (trip.getTripEndDate() == null) {
            throw new TripValidationException(END_DATE_MISSING);
        }

        if (trip.getTripEndDate().isBefore(trip.getTripStartDate())) {
            throw new TripValidationException(INVALID_DATE_RANGE);
        }

        if (trip.getEstimatedBudget() == null) {
            throw new TripValidationException(ESTIMATED_BUDGET_MISSING);
        }

        if (trip.getEstimatedBudget() <= 0) {
            throw new TripValidationException(INVALID_ESTIMATED_BUDGET);
        }

        if (trip.getMaxParticipants() == null) {
            throw new TripValidationException(MAX_PARTICIPANTS_MISSING);
        }

        if (trip.getMaxParticipants() <= 0) {
            throw new TripValidationException(INVALID_MAX_PARTICIPANTS);
        }

        if (trip.getJoinPolicy() == null) {
            throw new TripValidationException(JOIN_POLICY_MISSING);
        }

        if (trip.getTripItineraries() == null || trip.getTripItineraries().isEmpty()) {
            throw new TripValidationException(ITINERARY_MISSING);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

