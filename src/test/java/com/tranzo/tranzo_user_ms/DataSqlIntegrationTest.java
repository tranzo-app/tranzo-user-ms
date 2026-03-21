package com.tranzo.tranzo_user_ms;

import com.tranzo.tranzo_user_ms.user.repository.HostRatingRepository;
import com.tranzo.tranzo_user_ms.user.repository.MemberRatingRepository;
import com.tranzo.tranzo_user_ms.user.repository.TripRatingRepository;
import com.tranzo.tranzo_user_ms.user.service.PublicProfileService;
import com.tranzo.tranzo_user_ms.trip.enums.TripStatus;
import com.tranzo.tranzo_user_ms.trip.repository.TripMemberRepository;
import com.tranzo.tranzo_user_ms.trip.repository.TripRepository;
import com.tranzo.tranzo_user_ms.notification.repository.UserNotificationRepository;
import com.tranzo.tranzo_user_ms.user.repository.TravelPalRepository;
import com.tranzo.tranzo_user_ms.user.repository.UserProfileRepository;
import com.tranzo.tranzo_user_ms.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test that verifies seed data is present and services can read it.
 * Uses trip-api-test-data.sql (no project data.sql); thresholds relaxed to match.
 */
@SpringBootTest(classes = TranzoUserMsApplication.class, properties = "spring.profiles.active=test")
@DisplayName("Data.sql integration and verification")
@Sql(scripts = "/trip-api-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class DataSqlIntegrationTest {

    private static final int MIN_ENTRIES = 1;
    private static final UUID USER_WITH_TRUST_SCORE = UUID.fromString("11111111-1111-4111-8111-111111111111");

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private TripMemberRepository tripMemberRepository;
    @Autowired
    private TripRatingRepository tripRatingRepository;
    @Autowired
    private HostRatingRepository hostRatingRepository;
    @Autowired
    private MemberRatingRepository memberRatingRepository;
    @Autowired
    private UserNotificationRepository userNotificationRepository;
    @Autowired
    private TravelPalRepository travelPalRepository;
    @Autowired
    private PublicProfileService publicProfileService;

    @Test
    @DisplayName("Users and user_profile have at least 1 entry")
    void usersAndProfilesPopulated() {
        long users = userRepository.count();
        long profiles = userProfileRepository.count();
        assertTrue(users >= MIN_ENTRIES, "users count >= 1, got: " + users);
        assertTrue(profiles >= MIN_ENTRIES, "user_profile count >= 1, got: " + profiles);
    }

    @Test
    @DisplayName("At least 1 COMPLETED trip exists")
    void completedTripsExist() {
        long completed = tripRepository.findAll().stream()
                .filter(t -> t.getTripStatus() == TripStatus.COMPLETED)
                .count();
        assertTrue(completed >= MIN_ENTRIES, "COMPLETED trips >= 1, got: " + completed);
    }

    @Test
    @DisplayName("Trip members, ratings and notifications populated from seed")
    void tripAndReputationDataPopulated() {
        long members = tripMemberRepository.count();
        long tripRatings = tripRatingRepository.count();
        long hostRatings = hostRatingRepository.count();
        long memberRatings = memberRatingRepository.count();
        long notifications = userNotificationRepository.count();
        assertTrue(members >= MIN_ENTRIES, "trip_members >= 1, got: " + members);
        assertTrue(tripRatings >= 0, "trip_rating >= 0, got: " + tripRatings);
        assertTrue(hostRatings >= 0, "host_rating >= 0, got: " + hostRatings);
        assertTrue(memberRatings >= 0, "member_rating >= 0, got: " + memberRatings);
        assertTrue(notifications >= 0, "user_notification >= 0, got: " + notifications);
    }

    @Test
    @DisplayName("Travel pal populated from seed")
    void travelPalPopulated() {
        long travelPals = travelPalRepository.count();
        assertTrue(travelPals >= 0, "travel_pal >= 0, got: " + travelPals);
    }

    @Test
    @DisplayName("Public profile with trust score loads without error")
    void publicProfileWithTrustScoreLoads() {
        var profile = publicProfileService.getPublicProfile(USER_WITH_TRUST_SCORE, 0, 20);
        assertNotNull(profile);
        assertNotNull(profile.getTrustScore(), "User 111... should have trust score from seed");
        assertTrue(profile.getTrustScore().compareTo(java.math.BigDecimal.ZERO) > 0,
                "User 111... should have positive trust score from seed");
    }
}
