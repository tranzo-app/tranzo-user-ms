package com.tranzo.tranzo_user_ms.chat.socket;

import com.tranzo.tranzo_user_ms.chat.dto.SendMessageRequestDto;
import com.tranzo.tranzo_user_ms.chat.dto.SendMessageResponseDto;
import com.tranzo.tranzo_user_ms.chat.model.MessageEntity;
import com.tranzo.tranzo_user_ms.chat.service.CreateAndManageConversationService;
import com.tranzo.tranzo_user_ms.chat.socket.payload.SendMessageSocketPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChatSocketHandler {

    private final CreateAndManageConversationService createAndManageConversationService;
    private final ChatSocketEmitter socketEmitter;

    @MessageMapping("/send-message")
    public void handleSendMessage(
            @Payload SendMessageSocketPayload payload,
            Principal principal
    ) {

        UUID senderId = UUID.fromString(principal.getName());
        SendMessageResponseDto response =
                createAndManageConversationService.sendMessage(payload.getConversationId(),
                        senderId,
                        payload.getSendMessageRequestDto());

        socketEmitter.emitMessage(response);
    }
}
