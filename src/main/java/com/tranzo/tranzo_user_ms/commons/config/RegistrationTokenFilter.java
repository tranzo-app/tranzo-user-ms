package com.tranzo.tranzo_user_ms.commons.config;

import com.tranzo.tranzo_user_ms.commons.exception.UnauthorizedException;
import com.tranzo.tranzo_user_ms.commons.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RegistrationTokenFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!request.getRequestURI().equals("/user/register"))
        {
            filterChain.doFilter(request, response);
            return;
        }
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer "))
        {
            throw new UnauthorizedException("Registration token missing");
        }
        String token = authHeader.substring(7);
        jwtService.validateTokenOrThrow(token);
        if (!"REGISTRATION".equals(jwtService.extractTokenType(token)))
        {
            throw new UnauthorizedException("Invalid token type for registration");
        }
        String identifier = jwtService.extractSubject(token);
        request.setAttribute("registrationIdentifier", identifier);
        filterChain.doFilter(request, response);
    }
}
