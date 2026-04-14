package com.tranzo.tranzo_user_ms.notification.controller;

import com.tranzo.tranzo_user_ms.ApiTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class NotificationApiTest extends ApiTestBase {

    @Test
    @DisplayName("GET /notifications returns 200 when authenticated")
    @WithMockUser(username = USER_UUID_1)
    void getNotifications_authenticated_returns200() throws Exception {
        mvc.perform(get("/notifications").param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("GET /notifications returns 401 when not authenticated")
    void getNotifications_unauthenticated_returns401() throws Exception {
        mvc.perform(get("/notifications").param("page", "0").param("size", "20"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /notifications/unread-count returns 200 when authenticated")
    @WithMockUser(username = USER_UUID_1)
    void getUnreadCount_authenticated_returns200() throws Exception {
        mvc.perform(get("/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("GET /notifications/unread-count returns 401 when not authenticated")
    void getUnreadCount_unauthenticated_returns401() throws Exception {
        mvc.perform(get("/notifications/unread-count"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /notifications/{id}/read returns 404 when notification not found")
    @WithMockUser(username = USER_UUID_1)
    void markAsRead_authenticated_returns404() throws Exception {
        mvc.perform(patch("/notifications/{notificationId}/read", NOTIFICATION_ID_USER1))
                .andExpect(status().isNotFound());
    }
}
