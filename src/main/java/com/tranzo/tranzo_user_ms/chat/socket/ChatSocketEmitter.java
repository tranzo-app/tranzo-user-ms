package com.tranzo.tranzo_user_ms.chat.socket;

import com.tranzo.tranzo_user_ms.chat.dto.MessageResponseDto;
import com.tranzo.tranzo_user_ms.chat.dto.SendMessageResponseDto;
import com.tranzo.tranzo_user_ms.chat.model.MessageEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatSocketEmitter {

    private final SimpMessagingTemplate messagingTemplate;

    public void emitMessage(SendMessageResponseDto message) {

        messagingTemplate.convertAndSend(
                "/topic/conversations/" +
                        message.getConversationId(),
                new MessageResponseDto(
                        message.getMessageId(),
                        message.getConversationId(),
                        message.getSenderId(),
                        message.getContent(),
                        message.getCreatedAt()
                )
        );
    }
}

