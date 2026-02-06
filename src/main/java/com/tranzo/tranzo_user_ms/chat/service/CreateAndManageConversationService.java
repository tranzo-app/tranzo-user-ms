package com.tranzo.tranzo_user_ms.chat.service;

import com.tranzo.tranzo_user_ms.chat.dto.*;
import com.tranzo.tranzo_user_ms.chat.enums.ConversationRole;
import com.tranzo.tranzo_user_ms.chat.enums.ConversationType;
import com.tranzo.tranzo_user_ms.chat.exception.ConversationNotFoundException;
import com.tranzo.tranzo_user_ms.chat.model.*;
import com.tranzo.tranzo_user_ms.chat.repository.*;
import com.tranzo.tranzo_user_ms.commons.exception.ForbiddenException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class CreateAndManageConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository conversationParticipantRepository;
    private final ConversationBlockRepository conversationBlockRepository;
    private final MessageRepository messageRepository;
    private final ConversationMuteRepository muteRepository;
    private final SimpMessagingTemplate messagingTemplate;

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

    public ConversationEntity createTripGroupChat(UUID hostUserId){
        ConversationEntity newConversation = ConversationEntity.createGroup(hostUserId);
        newConversation.addParticipant(hostUserId, ConversationRole.ADMIN_HOST);
        MessageEntity systemMessage = MessageEntity.systemMessage(
                newConversation,
                "Trip Hosted Successfully"
        );
        ConversationEntity conversationEntity = conversationRepository.save(newConversation);
        messageRepository.save(systemMessage);
        return conversationEntity;
    }

    public SendMessageResponseDto sendMessage(UUID conversationId, UUID senderId, SendMessageRequestDto request) {
        String content =  request.getContent();
        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        conversationParticipantRepository.findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(conversationId, senderId)
                .orElseThrow(() -> new ConversationNotFoundException("USER_NOT_PARTICIPANT"));

        if(conversation.getType().equals(ConversationType.ONE_ON_ONE))
        {
            // TODO : Do we need to check if the conversation has been blocked by opposite user too? Should we maintain a flag to mark conversation as blocked if anyone from the conversation does it?
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
        if (conversation.getType() == ConversationType.GROUP_CHAT) {
            messagingTemplate.convertAndSend(
                    "/topic/conversations/" + conversationId,
                    response
            );

        } else if (conversation.getType() == ConversationType.ONE_ON_ONE) {
            // Send only to the receiver
            List<ConversationParticipantEntity> participants =
                    conversationParticipantRepository
                            .findByConversation_ConversationIdAndLeftAtIsNull(conversationId);
            UUID receiverId = participants.stream()
                    .map(ConversationParticipantEntity::getUserId)
                    .filter(id -> !id.equals(senderId))
                    .findFirst()
                    .orElseThrow();
            log.info("Sending private message from {} to {}", senderId, receiverId);
            messagingTemplate.convertAndSendToUser(
                    receiverId.toString(),
                    "/queue/messages",
                    response
            );

        }
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

        // Remove travelPAL after blocking user i.e trigger unmatching from user service
    }

    public void unblockConversation(UUID conversationId, UUID userId) {

        ConversationEntity conversation =
                conversationRepository.findById(conversationId)
                        .orElseThrow(() -> new ConversationNotFoundException("CONVERSATION_NOT_FOUND"));

        if (conversation.getType() != ConversationType.ONE_ON_ONE) {
            throw new ForbiddenException("UNBLOCK_NOT_ALLOWED");
        }

        conversationParticipantRepository.findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(conversationId, userId)
                .orElseThrow(() -> new ConversationNotFoundException("User is not a participant of the conversation"));

        ConversationBlockEntity blockEntity =
                conversationBlockRepository.findByConversation_ConversationIdAndBlockedBy(
                        conversationId, userId)
                        .orElseThrow(() -> new ConversationNotFoundException("BLOCK_ENTRY_NOT_FOUND"));

        conversationBlockRepository.delete(blockEntity);
    }


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