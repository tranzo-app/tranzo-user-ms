package com.tranzo.tranzo_user_ms.user.controller;

import com.tranzo.tranzo_user_ms.ApiTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthApiTest extends ApiTestBase {

    private static final String OTP_REQUEST_VALID_PHONE = """
            {"countryCode": "+91", "mobileNumber": "9000000001"}
            """;
    private static final String OTP_REQUEST_INVALID = """
            {"countryCode": "+91", "mobileNumber": "9000000001", "emailId": "both@mail.com"}
            """;
    private static final String SESSION_LOGIN_VALID = """
            {"mobileNumber": "9000000001"}
            """;
    private static final String SESSION_LOGIN_NOT_FOUND = """
            {"mobileNumber": "9999999999"}
            """;
    private static final String SESSION_LOGIN_INVALID_EMAIL = """
            {"emailId": "not-an-email"}
            """;

    @Test
    @DisplayName("POST /auth/otp/request with valid body returns 200")
    void otpRequest_valid_returns200() throws Exception {
        mvc.perform(post("/auth/otp/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OTP_REQUEST_VALID_PHONE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.statusMessage").value("OTP sent successfully"));
    }

    @Test
    @DisplayName("POST /auth/otp/request with invalid body (both phone and email) returns 400")
    void otpRequest_invalid_returns400() throws Exception {
        mvc.perform(post("/auth/otp/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OTP_REQUEST_INVALID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    @DisplayName("POST /auth/session/login with valid mobile returns 200")
    void sessionLogin_valid_returns200() throws Exception {
        mvc.perform(post("/auth/session/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SESSION_LOGIN_VALID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.statusMessage").value("Session created successfully"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.authenticated").value(true));
    }

    @Test
    @DisplayName("POST /auth/session/login with non-existent user returns 404")
    void sessionLogin_userNotFound_returns404() throws Exception {
        mvc.perform(post("/auth/session/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SESSION_LOGIN_NOT_FOUND))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    @DisplayName("POST /auth/session/login with invalid email format returns 400")
    void sessionLogin_invalidEmail_returns400() throws Exception {
        mvc.perform(post("/auth/session/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SESSION_LOGIN_INVALID_EMAIL))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }
}
