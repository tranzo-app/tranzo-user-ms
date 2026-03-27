package com.tranzo.tranzo_user_ms.chat.controller;

import com.tranzo.tranzo_user_ms.chat.dto.ChatMessageRequest;
import com.tranzo.tranzo_user_ms.chat.dto.SendMessageRequestDto;
import com.tranzo.tranzo_user_ms.chat.dto.SendMessageResponseDto;
import com.tranzo.tranzo_user_ms.chat.service.CreateAndManageConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

/**
 * WebSocket Controller for Real-Time Chat
 * Handles WebSocket connections for live messaging
 */
@Controller
@Tag(name = "Chat WebSocket", description = "Real-time chat WebSocket operations")
@RequiredArgsConstructor
@Slf4j
public class ChatSocketController {

    private final CreateAndManageConversationService conversationService;

    /**
     * Send message over WebSocket
     * Maps to: /app/chat.send
     *
     * @param request  ChatMessageRequest with conversationId and content
     * @param principal current user principal
     * @throws AuthException if user is not authenticated
     */
    @MessageMapping("/chat.send")
    @Operation(summary = "Send message via WebSocket", description = "Send a real-time message via WebSocket connection")
    public void sendMessageOverSocket(ChatMessageRequest request, Principal principal) throws AuthException {
        UUID userId = UUID.fromString(principal.getName());
        UUID conversationId = request.getConversationId();

        log.info("Incoming request | API=/app/chat.send | method=WEBSOCKET | conversationId={} | userId={}", conversationId, userId);

        try {
            SendMessageResponseDto response = conversationService.sendMessage(
                    conversationId,
                    userId,
                    new SendMessageRequestDto(request.getContent())
            );

            log.info("Message sent via WebSocket | conversationId={} | userId={} | messageId={} | status=SUCCESS", 
                    conversationId, userId, response.getMessageId());
        } catch (Exception e) {
            log.error("WebSocket message failed | operation=sendMessage | conversationId={} | userId={} | reason={}", 
                    conversationId, userId, e.getMessage(), e);
            throw new RuntimeException("Failed to send message", e);
        }
    }
}
