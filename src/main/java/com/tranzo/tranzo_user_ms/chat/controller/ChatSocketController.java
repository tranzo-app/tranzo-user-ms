package com.tranzo.tranzo_user_ms.chat.controller;

import com.tranzo.tranzo_user_ms.chat.dto.ChatMessageRequest;
import com.tranzo.tranzo_user_ms.chat.dto.SendMessageRequestDto;
import com.tranzo.tranzo_user_ms.chat.dto.SendMessageResponseDto;
import com.tranzo.tranzo_user_ms.chat.service.CreateAndManageConversationService;
import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatSocketController {
    private final CreateAndManageConversationService conversationService;

    @MessageMapping("/chat.send")
    public void sendMessageOverSocket(ChatMessageRequest request, Principal principal) throws AuthException {
        UUID userId = UUID.fromString(principal.getName());
        log.info("WebSocket user: {}", principal.getName());
        SendMessageResponseDto response =
                conversationService.sendMessage(
                        request.getConversationId(),
                        userId,
                        new SendMessageRequestDto(request.getContent())
                );
    }
}
