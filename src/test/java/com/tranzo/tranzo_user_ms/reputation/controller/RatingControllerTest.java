package com.tranzo.tranzo_user_ms.reputation.controller;

import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import com.tranzo.tranzo_user_ms.user.controller.RatingController;
import com.tranzo.tranzo_user_ms.user.dto.SubmitHostRatingRequest;
import com.tranzo.tranzo_user_ms.user.dto.SubmitMemberRatingItem;
import com.tranzo.tranzo_user_ms.user.dto.SubmitMemberRatingsRequest;
import com.tranzo.tranzo_user_ms.user.dto.SubmitTripRatingRequest;
import com.tranzo.tranzo_user_ms.user.enums.VibeTag;
import com.tranzo.tranzo_user_ms.user.service.RatingService;
import com.tranzo.tranzo_user_ms.user.controller.AadharController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RatingController Unit Tests")
class RatingControllerTest {

    @Mock
    private RatingService ratingService;

    @InjectMocks
    private RatingController controller;

    private UUID userId;
    private UUID tripId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        tripId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should submit trip rating and return 200")
    void submitTripRating_Success() throws Exception {
        SubmitTripRatingRequest request = SubmitTripRatingRequest.builder()
                .destinationRating(5)
                .itineraryRating(5)
                .overallRating(5)
                .build();
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<?> res = controller.submitTripRating(tripId, request);

            assertEquals(HttpStatus.OK, res.getStatusCode());
            verify(ratingService).submitTripRating(eq(tripId), eq(userId), eq(request));
        }
    }

    @Test
    @DisplayName("Should submit host rating and return 200")
    void submitHostRating_Success() throws Exception {
        SubmitHostRatingRequest request = SubmitHostRatingRequest.builder()
                .coordinationRating(5)
                .communicationRating(4)
                .leadershipRating(5)
                .reviewText("Great")
                .build();
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<?> res = controller.submitHostRating(tripId, request);

            assertEquals(HttpStatus.OK, res.getStatusCode());
            verify(ratingService).submitHostRating(eq(tripId), eq(userId), eq(request));
        }
    }

    @Test
    @DisplayName("Should submit member ratings and return 200")
    void submitMemberRatings_Success() throws Exception {
        UUID ratedUserId = UUID.randomUUID();
        SubmitMemberRatingsRequest request = SubmitMemberRatingsRequest.builder()
                .ratings(List.of(
                        SubmitMemberRatingItem.builder()
                                .ratedUserId(ratedUserId)
                                .ratingScore(5)
                                .vibeTag(VibeTag.RELIABLE)
                                .build()))
                .build();
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<?> res = controller.submitMemberRatings(tripId, request);

            assertEquals(HttpStatus.OK, res.getStatusCode());
            verify(ratingService).submitMemberRatings(eq(tripId), eq(userId), eq(request));
        }
    }
}
