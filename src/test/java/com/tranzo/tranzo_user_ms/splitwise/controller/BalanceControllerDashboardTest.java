package com.tranzo.tranzo_user_ms.splitwise.controller;

import com.tranzo.tranzo_user_ms.ApiTestBase;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.UserDashboardResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Sql(scripts = "/splitwise-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class BalanceControllerDashboardTest extends ApiTestBase {

    @Test
    @DisplayName("GET /api/splitwise/balances/dashboard returns 200 when authenticated")
    @WithMockUser(username = USER_UUID_1)
    void getUserDashboard_authenticated_returns200() throws Exception {
        mvc.perform(get("/api/splitwise/balances/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.totalAmountUserOwes").exists())
                .andExpect(jsonPath("$.data.totalAmountOwedToUser").exists())
                .andExpect(jsonPath("$.data.totalOutstandingBalance").exists())
                .andExpect(jsonPath("$.data.travelPalSummary").exists())
                .andExpect(jsonPath("$.data.expenseSummary").exists());
    }

    @Test
    @DisplayName("GET /api/splitwise/balances/dashboard returns 401 when not authenticated")
    void getUserDashboard_unauthenticated_returns401() throws Exception {
        mvc.perform(get("/api/splitwise/balances/dashboard"))
                .andExpect(status().isUnauthorized());
    }
}
