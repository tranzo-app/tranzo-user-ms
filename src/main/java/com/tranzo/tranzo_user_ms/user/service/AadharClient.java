package com.tranzo.tranzo_user_ms.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.tranzo.tranzo_user_ms.user.configuration.AadharProperties;
import com.tranzo.tranzo_user_ms.user.dto.AadharAuthResponse;
import com.tranzo.tranzo_user_ms.user.dto.AadharOtpBaseResponse;
import com.tranzo.tranzo_user_ms.user.dto.AadharOtpRequest;
import com.tranzo.tranzo_user_ms.user.dto.AadharOtpSuccessResponse;
import com.tranzo.tranzo_user_ms.user.exception.AadharIntegrationException;
import com.tranzo.tranzo_user_ms.user.exception.AadharServiceUnavailableException;
import com.tranzo.tranzo_user_ms.user.exception.AadharUnauthorizedException;
import com.tranzo.tranzo_user_ms.user.exception.AadharValidationException;
import com.tranzo.tranzo_user_ms.user.model.AadharOtpVerifyBaseResponse;
import com.tranzo.tranzo_user_ms.user.model.AadharOtpVerifyRequest;
import com.tranzo.tranzo_user_ms.user.model.AadharOtpVerifySuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AadharClient {
    private final WebClient aadharWebClient;
    private final ObjectMapper objectMapper;
    private final AadharProperties properties;
    private final Cache<String, String> aadharTokenCache;
    private static final String CACHE_NAME = "aadhaarTokenCache";
    private static final String CACHE_KEY = "aadhaarAccessToken";

    @CachePut(value = CACHE_NAME, key = "'" + CACHE_KEY + "'")
    public String fetchNewAccessToken() {
        AadharAuthResponse authResponse = aadharWebClient.post()
                .uri("/authenticate")
                .header("x-api-key", properties.getXApiKey())
                .header("x-api-secret", properties.getXApiSecret())
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .map(body -> new RuntimeException("Aadhar otp get access token auth failed: " + body))
                )
                .bodyToMono(AadharAuthResponse.class)
                .block();
        if (authResponse == null || authResponse.getData() == null) {
            throw new RuntimeException("Failed to fetch Aadhaar access token");
        }
        return authResponse.getData().getAccessToken();
    }

    public AadharOtpSuccessResponse sendOtp(AadharOtpRequest request) {
        try {
            String token = getAccessToken();
            return callOtpApi(request, token);
        } catch (AadharUnauthorizedException ex) {
            // Token expired → evict and retry once
            evictAccessToken();
            String newToken = getAccessToken();
            return callOtpApi(request, newToken);
        }
    }

    @Cacheable(value = CACHE_NAME, key = "'" + CACHE_KEY + "'")
    public String getAccessToken() {
        return fetchNewAccessToken();
    }

    @CacheEvict(value = CACHE_NAME, key = "'" + CACHE_KEY + "'")
    public void evictAccessToken() {

    }

    private AadharOtpSuccessResponse callOtpApi(AadharOtpRequest request, String token) {
        return aadharWebClient.post()
                .uri("/kyc/aadhaar/okyc/otp")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .bodyValue(request)
                .retrieve()

                // 401 → token expired
                .onStatus(
                        status -> status.value() == 401,
                        response -> Mono.error(
                                new AadharUnauthorizedException("Access token expired")
                        )
                )

                // 422 → validation error
                .onStatus(
                        status -> status.value() == 422,
                        response -> response.bodyToMono(AadharOtpBaseResponse.class)
                                .map(error ->
                                        new AadharValidationException(error.getMessage()))
                )

                // 503 → service unavailable
                .onStatus(
                        status -> status.value() == 503,
                        response -> response.bodyToMono(AadharOtpBaseResponse.class)
                                .map(error ->
                                        new AadharServiceUnavailableException(error.getMessage()))
                )

                // other errors
                .onStatus(
                        HttpStatusCode::isError,
                        response -> response.bodyToMono(AadharOtpBaseResponse.class)
                                .map(error ->
                                        new AadharIntegrationException(error.getMessage()))
                )

                .bodyToMono(AadharOtpSuccessResponse.class)
                .block();
    }

    private AadharOtpSuccessResponse handleResponse(int status, String body) {
        try {
            if (status == 200) {
                return objectMapper.readValue(body, AadharOtpSuccessResponse.class);
            }
            AadharOtpBaseResponse error =
                    objectMapper.readValue(body, AadharOtpBaseResponse.class);
            if (status == 422) {
                throw new AadharValidationException(error.getMessage());
            }
            if (status == 503) {
                throw new AadharServiceUnavailableException(error.getMessage());
            }
            throw new AadharIntegrationException(error.getMessage());
        } catch (IOException e) {
            throw new AadharIntegrationException("Failed to parse Aadhaar response");
        }
    }

    public AadharOtpVerifySuccessResponse verifyOtp(
            AadharOtpVerifyRequest request
    ) {
        String token = getAccessToken();
        return aadharWebClient.post()
                .uri("/kyc/aadhaar/okyc/otp/verify")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .bodyValue(request)
                .exchangeToMono(response ->
                        response.bodyToMono(String.class)
                                .map(body -> handleVerifyResponse(
                                        response.statusCode().value(),
                                        body
                                ))
                )
                .block();
    }

    private AadharOtpVerifySuccessResponse handleVerifyResponse(
            int status,
            String body
    ) {
        try {
            if (status == 200) {
                JsonNode root = objectMapper.readTree(body);
                JsonNode dataNode = root.get("data");
                // Case 1: OTP Expired
                if (dataNode != null && dataNode.has("message")
                        && !"Aadhaar Card Exists".equalsIgnoreCase(
                        dataNode.get("message").asText())) {
                    throw new AadharValidationException("OTP expired");
                }
                // Case 2: VALID
                return objectMapper.readValue(
                        body,
                        AadharOtpVerifySuccessResponse.class
                );
            }
            AadharOtpVerifyBaseResponse error =
                    objectMapper.readValue(
                            body,
                            AadharOtpVerifyBaseResponse.class
                    );
            if (status == 422) {
                throw new AadharValidationException(error.getMessage());
            }
            if (status == 503) {
                throw new AadharServiceUnavailableException(error.getMessage());
            }
            throw new AadharIntegrationException(error.getMessage());
        } catch (IOException e) {
            throw new AadharIntegrationException("Failed to parse verify response");
        }
    }
}