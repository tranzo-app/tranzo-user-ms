package com.tranzo.tranzo_user_ms.chat.service;

import com.tranzo.tranzo_user_ms.chat.dto.*;
import com.tranzo.tranzo_user_ms.chat.enums.ConversationRole;
import com.tranzo.tranzo_user_ms.chat.enums.ConversationType;
import com.tranzo.tranzo_user_ms.chat.exception.ConversationNotFoundException;
import com.tranzo.tranzo_user_ms.chat.model.*;
import com.tranzo.tranzo_user_ms.chat.repository.*;
import com.tranzo.tranzo_user_ms.commons.exception.ForbiddenException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
public class CreateAndManageConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository conversationParticipantRepository;
    private final ConversationBlockRepository conversationBlockRepository;
    private final MessageRepository messageRepository;
    private final ConversationMuteRepository muteRepository;

    public CreateConversationResponseDto createOneToOneConversation(UUID userId, CreateConversationRequestDto request) {
        UUID otherUserId = request.getOtherUserId();

        if(userId.equals(otherUserId)) {
            throw new IllegalArgumentException("Cannot create a conversation with yourself");
        }

        Optional<ConversationEntity> existingConversation = conversationRepository.findOneToOneConversationBetweenUsers(userId, otherUserId);
        if (existingConversation.isPresent()) {
            ConversationEntity conversation = existingConversation.get();
            return CreateConversationResponseDto.builder()
                    .conversationId(conversation.getConversationId())
                    .createdAt(conversation.getCreatedAt())
                    .existing(true)
                    .build();
        }

        ConversationEntity newConversation =  ConversationEntity.createOneToOneChat(userId);

        newConversation.addParticipant(userId, ConversationRole.MEMBER);
        newConversation.addParticipant(otherUserId, ConversationRole.MEMBER);

        MessageEntity systemMessage = MessageEntity.systemMessage(
                        newConversation,
                        "You are now connected"
                );

        conversationRepository.save(newConversation);
        messageRepository.save(systemMessage);
        return CreateConversationResponseDto.builder()
                .conversationId(newConversation.getConversationId())
                .createdAt(newConversation.getCreatedAt())
                .existing(false)
                .build();
    }

    public SendMessageResponseDto sendMessage(UUID conversationId, UUID senderId, SendMessageRequestDto request) {
        String content =  request.getContent();
        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        conversationParticipantRepository.findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(conversationId, senderId)
                .orElseThrow(() -> new ConversationNotFoundException("USER_NOT_PARTICIPANT"));

        if(conversation.getType().equals(ConversationType.ONE_ON_ONE))
        {
            boolean isBlocked = conversationBlockRepository.existsByConversation_ConversationIdAndBlockedBy(conversationId, senderId);
            if(isBlocked) {
                throw new ForbiddenException("USER_BLOCKED");
            }
        }

        MessageEntity message = messageRepository.save(
                MessageEntity.userMessage(
                        conversation,
                        senderId,
                        content
                )
        );
        SendMessageResponseDto response = new SendMessageResponseDto(
                message.getMessageId(),
                conversationId,
                senderId,
                content,
                message.getCreatedAt()
        );
        return response;
    }

    public void markConversationAsRead(UUID conversationId, UUID userId) {
        conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        ConversationParticipantEntity conversationParticipant = conversationParticipantRepository.findByConversation_ConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User is not a participant of the conversation"));

        conversationParticipant.markAsRead();
    }


    public void blockConversation(UUID conversationId, UUID blockinguserid) {

        ConversationEntity conversation =
                conversationRepository.findById(conversationId)
                        .orElseThrow(() -> new ConversationNotFoundException("CONVERSATION_NOT_FOUND"));

        if (conversation.getType() != ConversationType.ONE_ON_ONE) {
            throw new ForbiddenException("BLOCK_NOT_ALLOWED");
        }

        conversationParticipantRepository.findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(conversationId, blockinguserid)
                .orElseThrow(() -> new ConversationNotFoundException("USER_NOT_PARTICIPANT"));

        if (conversationBlockRepository.existsByConversation_ConversationIdAndBlockedBy(
                conversationId, blockinguserid)) {
            return;
        }

        conversationBlockRepository.save(
                ConversationBlockEntity.create(conversation, blockinguserid)
        );
    }

    /*public void unblockConversation(UUID conversationId, UUID userId) {

        ConversationEntity conversation =
                conversationRepository.findById(conversationId)
                        .orElseThrow(() -> new ConversationNotFoundException("CONVERSATION_NOT_FOUND"));

        if (conversation.getType() != ConversationType.ONE_ON_ONE) {
            throw new ForbiddenException("UNBLOCK_NOT_ALLOWED");
        }

        conversationParticipantRepository.findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(conversationId, userId)
                .orElseThrow(() -> new ConversationNotFoundException("User is not a participant of the conversation"));
        ConversationBlockRepository
                .findByConversation_ConversationIdAndBlockedBy(
                        conversationId, userId
                )
                .ifPresent(blockRepository::delete);
    }
*/


    public void muteConversation(UUID conversationId, UUID userId) {

        ConversationEntity conversation = conversationRepository.findById(conversationId)
                        .orElseThrow(() ->
                                new ConversationNotFoundException("CONVERSATION_NOT_FOUND")
                        );

        conversationParticipantRepository.findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(conversationId, userId)
                .orElseThrow(() ->
                        new ForbiddenException("USER_NOT_PARTICIPANT")
                );

        if (muteRepository.existsByConversation_ConversationIdAndUserId(conversationId, userId)) {
            return;
        }
        muteRepository.save(ConversationMuteEntity.create(conversation, userId));
    }

    public void unmuteConversation(UUID conversationId, UUID userId) {

        ConversationEntity conversation = conversationRepository.findById(conversationId)
                        .orElseThrow(() ->
                                new ConversationNotFoundException("CONVERSATION_NOT_FOUND")
                        );

        conversationParticipantRepository
                .findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(
                        conversationId, userId
                )
                .orElseThrow(() ->
                        new ForbiddenException("USER_NOT_PARTICIPANT")
                );

        muteRepository
                .findByConversation_ConversationIdAndUserId(
                        conversationId, userId
                )
                .ifPresent(muteRepository::delete);
    }
}