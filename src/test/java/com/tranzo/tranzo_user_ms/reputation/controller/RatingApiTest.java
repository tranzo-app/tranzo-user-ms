package com.tranzo.tranzo_user_ms.reputation.controller;

import com.tranzo.tranzo_user_ms.ApiTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RatingApiTest extends ApiTestBase {

    private static final String TRIP_RATING_VALID = """
            {"destinationRating": 5, "itineraryRating": 5, "overallRating": 5}
            """;
    private static final String TRIP_RATING_INVALID = """
            {"destinationRating": 0, "itineraryRating": 5, "overallRating": 5}
            """;

    @Test
    @DisplayName("PUT /trips/{tripId}/ratings/trip with valid body returns 200 when authenticated")
    @WithMockUser(username = USER_UUID_2)
    void submitTripRating_valid_returns200() throws Exception {
        mvc.perform(put("/trips/{tripId}/ratings/trip", COMPLETED_TRIP_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TRIP_RATING_VALID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("PUT /trips/{tripId}/ratings/trip with invalid rating returns 400")
    @WithMockUser(username = USER_UUID_2)
    void submitTripRating_invalid_returns400() throws Exception {
        mvc.perform(put("/trips/{tripId}/ratings/trip", COMPLETED_TRIP_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TRIP_RATING_INVALID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("PUT /trips/{tripId}/ratings/trip without auth returns 401")
    void submitTripRating_unauthenticated_returns401() throws Exception {
        mvc.perform(put("/trips/{tripId}/ratings/trip", COMPLETED_TRIP_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TRIP_RATING_VALID))
                .andExpect(status().isUnauthorized());
    }
}
