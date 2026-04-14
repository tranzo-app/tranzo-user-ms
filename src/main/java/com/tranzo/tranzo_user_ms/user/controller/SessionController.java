package com.tranzo.tranzo_user_ms.user.controller;

import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.user.dto.SessionRequestDto;
import com.tranzo.tranzo_user_ms.user.dto.SessionResponseDto;
import com.tranzo.tranzo_user_ms.user.service.SessionService;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/session")
@RequiredArgsConstructor
@Slf4j
public class SessionController {

    private final SessionService sessionService;

    @PostMapping("/login")
    public ResponseEntity<ResponseDto<SessionResponseDto>> createSession(
            @Valid @RequestBody SessionRequestDto request,
            HttpServletResponse response
    ) {
        log.info("Incoming request | API=/auth/session/login | method=POST | identifier={}", 
                request.getEmailId() != null ? request.getEmailId() : request.getMobileNumber());
        SessionResponseDto sessionResponse = sessionService.createSession(request, response);
        log.info("Session created successfully | status=SUCCESS");
        return ResponseEntity.ok(ResponseDto.success(200, "Session created successfully", sessionResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseDto<SessionResponseDto>> refreshSession(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws AuthException {
        log.info("Incoming request | API=/auth/session/refresh | method=POST");
        SessionResponseDto sessionResponse = sessionService.refreshSession(request, response);
        log.info("Session refreshed successfully | status=SUCCESS");
        return ResponseEntity.ok(ResponseDto.success(200, "Session refreshed successfully", sessionResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseDto<SessionResponseDto>> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.info("Incoming request | API=/auth/session/logout | method=POST");
        sessionService.logout(request, response);
        log.info("Session logged out successfully | status=SUCCESS");
        return ResponseEntity.ok(ResponseDto.success(200, "Session logged out successfully", null));
    }
}

