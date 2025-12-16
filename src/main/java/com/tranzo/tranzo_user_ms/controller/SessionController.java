package com.tranzo.tranzo_user_ms.controller;

import com.tranzo.tranzo_user_ms.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.dto.SessionRequestDto;
import com.tranzo.tranzo_user_ms.dto.SessionResponseDto;
import com.tranzo.tranzo_user_ms.service.SessionService;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PostMapping("/session")
    public ResponseEntity<ResponseDto<SessionResponseDto>> createSession(
            @Valid @RequestBody SessionRequestDto request,
            HttpServletResponse response
    ) {
        SessionResponseDto sessionResponse = sessionService.createSession(request, response);
        return ResponseEntity.ok(ResponseDto.success(200, "Session created successfully", sessionResponse));
    }

    @PostMapping("/session/refresh")
    public ResponseEntity<ResponseDto<SessionResponseDto>> refreshSession(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws AuthException {
        SessionResponseDto sessionResponse = sessionService.refreshSession(request, response);
        return ResponseEntity.ok(ResponseDto.success(200, "Session refreshed successfully", sessionResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseDto<SessionResponseDto>> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        sessionService.logout(request, response);
        return ResponseEntity.ok(ResponseDto.success(200, "Session refreshed successfully", null));
    }
}

