package com.tranzo.tranzo_user_ms.commons.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tranzo.tranzo_user_ms.user.dto.ResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        ResponseDto<Void> apiResponse = ResponseDto.<Void>builder()
                .statusCode(HttpServletResponse.SC_UNAUTHORIZED)
                .status("UNAUTHORIZED")
                .statusMessage("Authentication required or token invalid")
                .data(null)
                .build();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(
                objectMapper.writeValueAsString(apiResponse)
        );
    }
}
