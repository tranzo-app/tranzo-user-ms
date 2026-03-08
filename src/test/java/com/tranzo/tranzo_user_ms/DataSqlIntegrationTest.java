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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test that runs with data.sql loaded (H2 + defer-datasource-initialization).
 * Verifies that seed data is present and that services can read it without errors.
 */
@SpringBootTest
@DisplayName("Data.sql integration and verification")
class DataSqlIntegrationTest {

    private static final int MIN_ENTRIES = 10;
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
    @DisplayName("Users and user_profile have at least 10 entries")
    void usersAndProfilesPopulated() {
        long users = userRepository.count();
        long profiles = userProfileRepository.count();
        assertTrue(users >= MIN_ENTRIES, "users count >= 10, got: " + users);
        assertTrue(profiles >= MIN_ENTRIES, "user_profile count >= 10, got: " + profiles);
    }

    @Test
    @DisplayName("At least 3 COMPLETED trips exist")
    void completedTripsExist() {
        long completed = tripRepository.findAll().stream()
                .filter(t -> t.getTripStatus() == TripStatus.COMPLETED)
                .count();
        assertTrue(completed >= 3, "COMPLETED trips >= 3, got: " + completed);
    }

    @Test
    @DisplayName("Trip members, ratings and notifications have at least 10 entries")
    void tripAndReputationDataPopulated() {
        long members = tripMemberRepository.count();
        long tripRatings = tripRatingRepository.count();
        long hostRatings = hostRatingRepository.count();
        long memberRatings = memberRatingRepository.count();
        long notifications = userNotificationRepository.count();
        assertTrue(members >= MIN_ENTRIES, "trip_members >= 10, got: " + members);
        assertTrue(tripRatings >= MIN_ENTRIES, "trip_rating >= 10, got: " + tripRatings);
        assertTrue(hostRatings >= MIN_ENTRIES, "host_rating >= 10, got: " + hostRatings);
        assertTrue(memberRatings >= MIN_ENTRIES, "member_rating >= 10, got: " + memberRatings);
        assertTrue(notifications >= MIN_ENTRIES, "user_notification >= 10, got: " + notifications);
    }

    @Test
    @DisplayName("Travel pal has at least 10 entries")
    void travelPalPopulated() {
        long travelPals = travelPalRepository.count();
        assertTrue(travelPals >= MIN_ENTRIES, "travel_pal >= 10, got: " + travelPals);
    }

    @Test
    @DisplayName("Public profile with trust score loads without error")
    void publicProfileWithTrustScoreLoads() {
        var profile = publicProfileService.getPublicProfile(USER_WITH_TRUST_SCORE, 0, 20);
        assertNotNull(profile);
        assertNotNull(profile.getTrustScore());
        assertTrue(profile.getTrustScore().compareTo(java.math.BigDecimal.ZERO) > 0,
                "User 111... should have positive trust score from data.sql");
    }
}
