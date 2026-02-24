package com.tranzo.tranzo_user_ms.trip.validation;

import com.tranzo.tranzo_user_ms.trip.enums.JoinPolicy;
import com.tranzo.tranzo_user_ms.trip.enums.TripPublishErrorCode;
import com.tranzo.tranzo_user_ms.trip.enums.TripStatus;
import com.tranzo.tranzo_user_ms.trip.enums.VisibilityStatus;
import com.tranzo.tranzo_user_ms.trip.exception.TripPublishException;
import com.tranzo.tranzo_user_ms.trip.model.TripEntity;
import com.tranzo.tranzo_user_ms.trip.model.TripItineraryEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TripPublishEligibilityValidator Unit Tests")
class TripPublishEligibilityValidatorTest {

    @InjectMocks
    private TripPublishEligibilityValidator validator;

    private TripEntity validTrip;
    private UUID tripId;

    @BeforeEach
    void setUp() {
        tripId = UUID.randomUUID();
        validTrip = createValidTrip();
    }

    // ============== ALREADY PUBLISHED TESTS ==============

    @Test
    @DisplayName("Should throw exception when trip is already published")
    void testValidate_AlreadyPublished() {
        // Given
        validTrip.setTripStatus(TripStatus.PUBLISHED);

        // When & Then
        TripPublishException exception = assertThrows(TripPublishException.class,
            () -> validator.validate(validTrip));
        assertEquals(TripPublishErrorCode.TRIP_ALREADY_PUBLISHED, exception.getErrorCode());
    }

    // ============== TITLE VALIDATION TESTS ==============

    @Test
    @DisplayName("Should throw exception when title is missing")
    void testValidate_TitleMissing() {
        // Given
        validTrip.setTripTitle(null);

        // When & Then
        TripPublishException exception = assertThrows(TripPublishException.class,
            () -> validator.validate(validTrip));
        assertEquals(TripPublishErrorCode.TITLE_MISSING, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw exception when title is blank")
    void testValidate_TitleBlank() {
        // Given
        validTrip.setTripTitle("   ");

        // When & Then
        TripPublishException exception = assertThrows(TripPublishException.class,
            () -> validator.validate(validTrip));
        assertEquals(TripPublishErrorCode.TITLE_MISSING, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw exception when title is empty string")
    void testValidate_TitleEmpty() {
        // Given
        validTrip.setTripTitle("");

        // When & Then
        TripPublishException exception = assertThrows(TripPublishException.class,
            () -> validator.validate(validTrip));
        assertEquals(TripPublishErrorCode.TITLE_MISSING, exception.getErrorCode());
    }

    // ============== DESCRIPTION VALIDATION TESTS ==============

    @Test
    @DisplayName("Should throw exception when description is missing")
    void testValidate_DescriptionMissing() {
        // Given
        validTrip.setTripDescription(null);

        // When & Then
        TripPublishException exception = assertThrows(TripPublishException.class,
            () -> validator.validate(validTrip));
        assertEquals(TripPublishErrorCode.DESCRIPTION_MISSING, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw exception when description is blank")
    void testValidate_DescriptionBlank() {
        // Given
        validTrip.setTripDescription("   ");

        // When & Then
        TripPublishException exception = assertThrows(TripPublishException.class,
            () -> validator.validate(validTrip));
        assertEquals(TripPublishErrorCode.DESCRIPTION_MISSING, exception.getErrorCode());
    }

    // ============== DESTINATION VALIDATION TESTS ==============

    @Test
    @DisplayName("Should throw exception when destination is missing")
    void testValidate_DestinationMissing() {
        // Given
        validTrip.setTripDestination(null);

        // When & Then
        TripPublishException exception = assertThrows(TripPublishException.class,
            () -> validator.validate(validTrip));
        assertEquals(TripPublishErrorCode.DESTINATION_MISSING, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw exception when destination is blank")
    void testValidate_DestinationBlank() {
        // Given
        validTrip.setTripDestination("   ");

        // When & Then
        TripPublishException exception = assertThrows(TripPublishException.class,
            () -> validator.validate(validTrip));
        assertEquals(TripPublishErrorCode.DESTINATION_MISSING, exception.getErrorCode());
    }

    // ============== DATE VALIDATION TESTS ==============

    @Test
    @DisplayName("Should throw exception when start date is missing")
    void testValidate_StartDateMissing() {
        // Given
        validTrip.setTripStartDate(null);

        // When & Then
        TripPublishException exception = assertThrows(TripPublishException.class,
            () -> validator.validate(validTrip));
        assertEquals(TripPublishErrorCode.START_DATE_MISSING, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw exception when end date is missing")
    void testValidate_EndDateMissing() {
        // Given
        validTrip.setTripEndDate(null);

        // When & Then
        TripPublishException exception = assertThrows(TripPublishException.class,
            () -> validator.validate(validTrip));
        assertEquals(TripPublishErrorCode.END_DATE_MISSING, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw exception when end date is before start date")
    void testValidate_InvalidDateRange_EndBeforeStart() {
        // Given
        validTrip.setTripStartDate(LocalDate.of(2026, 6, 10));
        validTrip.setTripEndDate(LocalDate.of(2026, 6, 1));

        // When & Then
        TripPublishException exception = assertThrows(TripPublishException.class,
            () -> validator.validate(validTrip));
        assertEquals(TripPublishErrorCode.INVALID_DATE_RANGE, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should allow end date same as start date")
    void testValidate_SameDateRange() {
        // Given
        LocalDate sameDate = LocalDate.of(2026, 6, 5);
        validTrip.setTripStartDate(sameDate);
        validTrip.setTripEndDate(sameDate);

        // When & Then
        assertDoesNotThrow(() -> validator.validate(validTrip));
    }

    @Test
    @DisplayName("Should allow valid date range (end after start)")
    void testValidate_ValidDateRange() {
        // Given
        validTrip.setTripStartDate(LocalDate.of(2026, 6, 1));
        validTrip.setTripEndDate(LocalDate.of(2026, 6, 10));

        // When & Then - should not throw exception for date validation
        // (other fields are valid)
        assertDoesNotThrow(() -> validator.validate(validTrip));
    }

    // ============== BUDGET VALIDATION TESTS ==============

    @Test
    @DisplayName("Should throw exception when estimated budget is missing")
    void testValidate_BudgetMissing() {
        // Given
        validTrip.setEstimatedBudget(null);

        // When & Then
        TripPublishException exception = assertThrows(TripPublishException.class,
            () -> validator.validate(validTrip));
        assertEquals(TripPublishErrorCode.ESTIMATED_BUDGET_MISSING, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw exception when estimated budget is zero")
    void testValidate_BudgetZero() {
        // Given
        validTrip.setEstimatedBudget(0.0);

        // When & Then
        TripPublishException exception = assertThrows(TripPublishException.class,
            () -> validator.validate(validTrip));
        assertEquals(TripPublishErrorCode.INVALID_ESTIMATED_BUDGET, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw exception when estimated budget is negative")
    void testValidate_BudgetNegative() {
        // Given
        validTrip.setEstimatedBudget(-100.0);

        // When & Then
        TripPublishException exception = assertThrows(TripPublishException.class,
            () -> validator.validate(validTrip));
        assertEquals(TripPublishErrorCode.INVALID_ESTIMATED_BUDGET, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should allow positive budget")
    void testValidate_ValidBudget() {
        // Given
        validTrip.setEstimatedBudget(5000.0);

        // When & Then - should not throw exception for budget validation
        assertDoesNotThrow(() -> validator.validate(validTrip));
    }

    // ============== MAX PARTICIPANTS VALIDATION TESTS ==============

    @Test
    @DisplayName("Should throw exception when max participants is missing")
    void testValidate_MaxParticipantsMissing() {
        // Given
        validTrip.setMaxParticipants(null);

        // When & Then
        TripPublishException exception = assertThrows(TripPublishException.class,
            () -> validator.validate(validTrip));
        assertEquals(TripPublishErrorCode.MAX_PARTICIPANTS_MISSING, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw exception when max participants is zero")
    void testValidate_MaxParticipantsZero() {
        // Given
        validTrip.setMaxParticipants(0);

        // When & Then
        TripPublishException exception = assertThrows(TripPublishException.class,
            () -> validator.validate(validTrip));
        assertEquals(TripPublishErrorCode.INVALID_MAX_PARTICIPANTS, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw exception when max participants is negative")
    void testValidate_MaxParticipantsNegative() {
        // Given
        validTrip.setMaxParticipants(-5);

        // When & Then
        TripPublishException exception = assertThrows(TripPublishException.class,
            () -> validator.validate(validTrip));
        assertEquals(TripPublishErrorCode.INVALID_MAX_PARTICIPANTS, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should allow positive max participants")
    void testValidate_ValidMaxParticipants() {
        // Given
        validTrip.setMaxParticipants(10);

        // When & Then - should not throw exception for participants validation
        assertDoesNotThrow(() -> validator.validate(validTrip));
    }

    // ============== JOIN POLICY VALIDATION TESTS ==============

    @Test
    @DisplayName("Should throw exception when join policy is missing")
    void testValidate_JoinPolicyMissing() {
        // Given
        validTrip.setJoinPolicy(null);

        // When & Then
        TripPublishException exception = assertThrows(TripPublishException.class,
            () -> validator.validate(validTrip));
        assertEquals(TripPublishErrorCode.JOIN_POLICY_MISSING, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should allow any valid join policy")
    void testValidate_ValidJoinPolicy() {
        // Given
        validTrip.setJoinPolicy(JoinPolicy.OPEN);

        // When & Then - should not throw exception for policy validation
        assertDoesNotThrow(() -> validator.validate(validTrip));
    }

    // ============== ITINERARY VALIDATION TESTS ==============

    @Test
    @DisplayName("Should throw exception when itineraries are missing")
    void testValidate_ItinenrariesMissing() {
        // Given
        validTrip.setTripItineraries(null);

        // When & Then
        TripPublishException exception = assertThrows(TripPublishException.class,
            () -> validator.validate(validTrip));
        assertEquals(TripPublishErrorCode.ITINERARY_MISSING, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw exception when itineraries are empty")
    void testValidate_ItinenrariesEmpty() {
        // Given
        validTrip.setTripItineraries(new HashSet<>());

        // When & Then
        TripPublishException exception = assertThrows(TripPublishException.class,
            () -> validator.validate(validTrip));
        assertEquals(TripPublishErrorCode.ITINERARY_MISSING, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should allow at least one itinerary")
    void testValidate_ValidItinerary() {
        // Given
        Set<TripItineraryEntity> itineraries = new HashSet<>();
        TripItineraryEntity itinerary = new TripItineraryEntity();
        itinerary.setItineraryId(UUID.randomUUID());
        itineraries.add(itinerary);
        validTrip.setTripItineraries(itineraries);

        // When & Then - should not throw exception for itinerary validation
        assertDoesNotThrow(() -> validator.validate(validTrip));
    }

    // ============== COMPLETE VALIDATION TESTS ==============

    @Test
    @DisplayName("Should validate successfully with all required fields present")
    void testValidate_AllFieldsValid() {
        // When & Then
        assertDoesNotThrow(() -> validator.validate(validTrip));
    }

    @Test
    @DisplayName("Should validate successfully with minimum requirements")
    void testValidate_MinimumRequirements() {
        // Given
        TripEntity minimalTrip = new TripEntity();
        minimalTrip.setTripStatus(TripStatus.DRAFT);
        minimalTrip.setTripTitle("Minimal Trip");
        minimalTrip.setTripDescription("A description");
        minimalTrip.setTripDestination("Destination");
        minimalTrip.setTripStartDate(LocalDate.now());
        minimalTrip.setTripEndDate(LocalDate.now());
        minimalTrip.setEstimatedBudget(1.0);
        minimalTrip.setMaxParticipants(1);
        minimalTrip.setJoinPolicy(JoinPolicy.OPEN);

        Set<TripItineraryEntity> itineraries = new HashSet<>();
        itineraries.add(new TripItineraryEntity());
        minimalTrip.setTripItineraries(itineraries);

        // When & Then
        assertDoesNotThrow(() -> validator.validate(minimalTrip));
    }

    @Test
    @DisplayName("Should validate successfully with large budget and many participants")
    void testValidate_LargeValuesValid() {
        // Given
        validTrip.setEstimatedBudget(1000000.0);
        validTrip.setMaxParticipants(1000);

        // When & Then
        assertDoesNotThrow(() -> validator.validate(validTrip));
    }

    // ============== EDGE CASE TESTS ==============

    @Test
    @DisplayName("Should validate multiple itineraries")
    void testValidate_MultipleItineraries() {
        // Given
        Set<TripItineraryEntity> itineraries = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            TripItineraryEntity itinerary = new TripItineraryEntity();
            itinerary.setItineraryId(UUID.randomUUID());
            itineraries.add(itinerary);
        }
        validTrip.setTripItineraries(itineraries);

        // When & Then
        assertDoesNotThrow(() -> validator.validate(validTrip));
    }

    @Test
    @DisplayName("Should validate with title containing special characters")
    void testValidate_TitleWithSpecialCharacters() {
        // Given
        validTrip.setTripTitle("Trip to Paris & Rome (2026)!");

        // When & Then
        assertDoesNotThrow(() -> validator.validate(validTrip));
    }

    @Test
    @DisplayName("Should validate with very long description")
    void testValidate_LongDescription() {
        // Given
        String longDescription = "A".repeat(1000);
        validTrip.setTripDescription(longDescription);

        // When & Then
        assertDoesNotThrow(() -> validator.validate(validTrip));
    }

    @Test
    @DisplayName("Should fail on first validation error (title) before checking other fields")
    void testValidate_EarlyFailureOnTitle() {
        // Given
        validTrip.setTripTitle(null);
        validTrip.setTripDescription(null); // Also invalid
        validTrip.setTripDestination(null); // Also invalid

        // When & Then - Should fail on title, not description
        TripPublishException exception = assertThrows(TripPublishException.class,
            () -> validator.validate(validTrip));
        assertEquals(TripPublishErrorCode.TITLE_MISSING, exception.getErrorCode());
    }

    // ============== HELPER METHODS ==============

    private TripEntity createValidTrip() {
        TripEntity trip = new TripEntity();
        trip.setTripId(tripId);
        trip.setTripStatus(TripStatus.DRAFT);
        trip.setTripTitle("Valid Trip Title");
        trip.setTripDescription("Valid trip description");
        trip.setTripDestination("Valid Destination");
        trip.setTripStartDate(LocalDate.of(2026, 6, 1));
        trip.setTripEndDate(LocalDate.of(2026, 6, 10));
        trip.setEstimatedBudget(5000.0);
        trip.setMaxParticipants(10);
        trip.setJoinPolicy(JoinPolicy.OPEN);
        trip.setVisibilityStatus(VisibilityStatus.PUBLIC);

        Set<TripItineraryEntity> itineraries = new HashSet<>();
        TripItineraryEntity itinerary = new TripItineraryEntity();
        itinerary.setItineraryId(UUID.randomUUID());
        itineraries.add(itinerary);
        trip.setTripItineraries(itineraries);

        return trip;
    }
}

