package com.tranzo.tranzo_user_ms.chat.socket;

import com.tranzo.tranzo_user_ms.chat.dto.MessageResponseDto;
import com.tranzo.tranzo_user_ms.chat.dto.SendMessageResponseDto;
import com.tranzo.tranzo_user_ms.user.client.UserProfileClient;
import com.tranzo.tranzo_user_ms.user.dto.UserNameDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChatSocketEmitter {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserProfileClient userProfileClient;

    public void emitMessage(SendMessageResponseDto message) {
        Map<UUID, UserNameDto> namesByUserId = userProfileClient.getNamesByUserIds(List.of(message.getSenderId()));
        messagingTemplate.convertAndSend(
                "/topic/conversations/" +
                        message.getConversationId(),
                new MessageResponseDto(
                        message.getMessageId(),
                        message.getConversationId(),
                        message.getSenderId(),
                        namesByUserId.get(message.getSenderId()).getFirstName(),
                        namesByUserId.get(message.getSenderId()).getMiddleName(),
                        namesByUserId.get(message.getSenderId()).getLastName(),
                        message.getContent(),
                        true,
                        message.getCreatedAt()
                )
        );
    }
}

