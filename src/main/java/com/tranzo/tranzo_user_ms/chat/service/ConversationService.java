package com.tranzo.tranzo_user_ms.chat.service;

import com.tranzo.tranzo_user_ms.chat.dto.ChatListItemDto;
import com.tranzo.tranzo_user_ms.chat.dto.MessageResponseDto;
import com.tranzo.tranzo_user_ms.chat.exception.ConversationNotFoundException;
import com.tranzo.tranzo_user_ms.chat.model.MessageEntity;
import com.tranzo.tranzo_user_ms.chat.repository.ConversationParticipantRepository;
import com.tranzo.tranzo_user_ms.chat.repository.ConversationRepository;
import com.tranzo.tranzo_user_ms.chat.repository.MessageRepository;
import com.tranzo.tranzo_user_ms.commons.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConversationService {

    private static final int DEFAULT_LIMIT = 30;
    private static final int MAX_LIMIT = 100;

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository conversationParticipantRepository;
    private final MessageRepository messageRepository;

    public List<ChatListItemDto> getMyConversations(UUID currentUserId) {
        return conversationRepository.findChatListForUser(currentUserId);
    }

    public List<MessageResponseDto> fetchMessages( UUID conversationId, UUID currentUserId, LocalDateTime before, Integer limit){
        conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        conversationParticipantRepository.findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(conversationId, currentUserId)
                .orElseThrow(() -> new ConversationNotFoundException("User is not a participant of the conversation"));

        int pageSize = DEFAULT_LIMIT;

        if (limit != null) {
            if (limit <= 0 || limit > MAX_LIMIT) {
                throw new BadRequestException("Limit must be between 1 and " + MAX_LIMIT);
            }
            pageSize = limit;
        }
        Pageable pageable = PageRequest.of(0, pageSize);

        List<MessageEntity> messages =
                messageRepository.findMessages(
                        conversationId,
                        before,
                        pageable
                );

        return messages.stream()
                .map(message ->
                        new MessageResponseDto(
                                message.getMessageId(),
                                conversationId,
                                message.getSenderId(),
                                message.getContent(),
                                message.getCreatedAt()
                        )
                )
                .toList();
    }
}