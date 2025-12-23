package com.tranzo.tranzo_user_ms.commons.config;

import com.tranzo.tranzo_user_ms.commons.exception.UnauthorizedException;
import com.tranzo.tranzo_user_ms.commons.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccessTokenFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/auth/otp/**")
                || request.getRequestURI().startsWith("/auth/session/**")
                || request.getRequestURI().startsWith("/user/register")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = extractAccessToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }
        jwtService.validateTokenOrThrow(token);
        if ("ACCESS".equals(jwtService.extractTokenType(token))) {
            throw new UnauthorizedException("Invalid token type for access");
        }
        UUID userUuid = UUID.fromString(jwtService.extractSubject(token));
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userUuid.toString(),
                        null,
                        Collections.emptyList()
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private String extractAccessToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("ACCESS_TOKEN".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
