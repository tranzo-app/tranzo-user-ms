package com.tranzo.tranzo_user_ms.trip.controller;

import com.tranzo.tranzo_user_ms.ApiTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Sql(scripts = "/trip-api-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class TripApiTest extends ApiTestBase {

    @Test
    @DisplayName("GET /trips/user returns 200 when authenticated")
    @WithMockUser(username = USER_UUID_1)
    void fetchTripForUser_authenticated_returns200() throws Exception {
        mvc.perform(get("/trips/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /trips/user returns 401 when not authenticated")
    void fetchTripForUser_unauthenticated_returns401() throws Exception {
        mvc.perform(get("/trips/user"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /trips returns 200 when authenticated")
    @WithMockUser(username = USER_UUID_1)
    void fetchAllTrips_authenticated_returns200() throws Exception {
        mvc.perform(get("/trips"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /trips returns 401 when not authenticated")
    void fetchAllTrips_unauthenticated_returns401() throws Exception {
        mvc.perform(get("/trips"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /trips/{tripId} returns 200 for existing trip when authenticated")
    @WithMockUser(username = USER_UUID_1)
    void fetchTrip_existing_returns200() throws Exception {
        mvc.perform(get("/trips/{tripId}", COMPLETED_TRIP_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.tripId").value(COMPLETED_TRIP_ID.toString()));
    }

    @Test
    @DisplayName("GET /trips/{tripId} returns 404 for non-existent trip when authenticated")
    @WithMockUser(username = USER_UUID_1)
    void fetchTrip_nonExistent_returns404() throws Exception {
        mvc.perform(get("/trips/{tripId}", NON_EXISTENT_UUID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));
    }
}
