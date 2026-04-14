package com.tranzo.tranzo_user_ms.user.controller;

import com.tranzo.tranzo_user_ms.ApiTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TravelPalApiTest extends ApiTestBase {

    @Test
    @DisplayName("GET /travel-pal/my returns 200 when authenticated")
    @WithMockUser(username = USER_UUID_1)
    void getMyTravelPals_authenticated_returns200() throws Exception {
        mvc.perform(get("/travel-pal/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("GET /travel-pal/my returns 401 when not authenticated")
    void getMyTravelPals_unauthenticated_returns401() throws Exception {
        mvc.perform(get("/travel-pal/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /travel-pal/pending returns 200 when authenticated")
    @WithMockUser(username = USER_UUID_1)
    void getPending_authenticated_returns200() throws Exception {
        mvc.perform(get("/travel-pal/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("GET /travel-pal/pending returns 401 when not authenticated")
    void getPending_unauthenticated_returns401() throws Exception {
        mvc.perform(get("/travel-pal/pending"))
                .andExpect(status().isUnauthorized());
    }
}
