package com.tranzo.tranzo_user_ms.reputation.controller;

import com.tranzo.tranzo_user_ms.ApiTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PublicProfileApiTest extends ApiTestBase {

    @Test
    @DisplayName("GET /public/profile/{userId} returns 200 with trustScore and data when authenticated")
    @WithMockUser(username = USER_UUID_1)
    void getPublicProfile_authenticated_returns200AndBody() throws Exception {
        mvc.perform(get("/public/profile/{userId}", USER_UUID_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.trustScore").exists())
                .andExpect(jsonPath("$.data.reviews").isArray());
    }

    @Test
    @DisplayName("GET /public/profile/{userId} returns 401 when not authenticated")
    void getPublicProfile_unauthenticated_returns401() throws Exception {
        mvc.perform(get("/public/profile/{userId}", USER_UUID_1))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.statusMessage").exists());
    }

    @Test
    @DisplayName("GET /public/profile/{userId} returns 404 for unknown userId when authenticated")
    @WithMockUser(username = USER_UUID_1)
    void getPublicProfile_unknownUser_returns404() throws Exception {
        mvc.perform(get("/public/profile/{userId}", NON_EXISTENT_UUID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.status").value("ERROR"));
    }
}
