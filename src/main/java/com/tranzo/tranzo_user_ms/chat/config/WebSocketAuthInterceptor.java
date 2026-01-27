package com.tranzo.tranzo_user_ms.chat.config;

import com.tranzo.tranzo_user_ms.commons.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    @Override
    @Nullable
    public Message<?> preSend(@Nullable Message<?> message, @Nullable MessageChannel channel) {
        if (message == null) {
            return null;
        }

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = extractToken(accessor);

            log.debug("WebSocket CONNECT - Token extraction result: {}", token != null ? "token found" : "token not found");

            if (token != null && jwtService.validateAccessToken(token)) {
                try {
                    UUID userUuid = jwtService.extractUserUuid(token);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userUuid,
                                    null,
                                    Collections.emptyList()
                            );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    accessor.setUser(authentication);
                    log.info("WebSocket user authenticated: {}", userUuid);
                } catch (Exception e) {
                    log.error("Error during WebSocket authentication", e);
                    return null;
                }
            } else {
                log.warn("WebSocket authentication failed: invalid or missing token");
                return null;
            }
        }

        return message;
    }

    private String extractToken(StompHeaderAccessor accessor) {
        // Try to extract from Authorization header
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.getFirst();
            if (authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        }

        // Try to extract from custom token header
        List<String> tokenHeaders = accessor.getNativeHeader("X-Auth-Token");
        if (tokenHeaders != null && !tokenHeaders.isEmpty()) {
            return tokenHeaders.getFirst();
        }

        // Try to extract from query parameters (if available)
        String simpConnectMessage = accessor.getFirstNativeHeader("simpConnectMessage");
        if (simpConnectMessage != null && simpConnectMessage.contains("token=")) {
            String[] parts = simpConnectMessage.split("token=");
            if (parts.length > 1) {
                return parts[1].split("[&\\s]")[0];
            }
        }



        return null;
    }
}

