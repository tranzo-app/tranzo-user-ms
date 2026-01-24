package com.tranzo.tranzo_user_ms.chat.service;

import com.tranzo.tranzo_user_ms.chat.dto.CreateConversationRequestDto;
import com.tranzo.tranzo_user_ms.chat.dto.CreateConversationResponseDto;
import com.tranzo.tranzo_user_ms.chat.dto.SendMessageRequestDto;
import com.tranzo.tranzo_user_ms.chat.dto.SendMessageResponseDto;
import com.tranzo.tranzo_user_ms.chat.enums.ConversationRole;
import com.tranzo.tranzo_user_ms.chat.enums.ConversationType;
import com.tranzo.tranzo_user_ms.chat.exception.ConversationNotFoundException;
import com.tranzo.tranzo_user_ms.chat.model.ConversationBlockEntity;
import com.tranzo.tranzo_user_ms.chat.model.ConversationEntity;
import com.tranzo.tranzo_user_ms.chat.model.ConversationParticipantEntity;
import com.tranzo.tranzo_user_ms.chat.model.MessageEntity;
import com.tranzo.tranzo_user_ms.chat.repository.ConversationBlockRepository;
import com.tranzo.tranzo_user_ms.chat.repository.ConversationParticipantRepository;
import com.tranzo.tranzo_user_ms.chat.repository.ConversationRepository;
import com.tranzo.tranzo_user_ms.chat.repository.MessageRepository;
import com.tranzo.tranzo_user_ms.commons.exception.ForbiddenException;
import com.tranzo.tranzo_user_ms.chat.exception.BaseException;
import lombok.AllArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
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
    private final ConversationBlockRepository blockRepository;

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

        MessageEntity systemMessage =
                MessageEntity.systemMessage(
                        newConversation,
                        "You are now connected"
                );

        conversationRepository.save(newConversation);
        return CreateConversationResponseDto.builder()
                .conversationId(newConversation.getConversationId())
                .createdAt(newConversation.getCreatedAt())
                .existing(false)
                .build();
    }

    public void markConversationAsRead(UUID conversationId, UUID userId) {
         conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        ConversationParticipantEntity ConversationParticipant =  conversationParticipantRepository.findByConversation_ConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User is not a participant of the conversation"));

        ConversationParticipant.markAsRead();
    }

//    @Transactional(readOnly = true)
//    public List<ChatListItemDto> getMyChatList(UUID currentUserId) {
//        return conversationRepository.findChatListByUserId(currentUserId);
//    }

    public SendMessageResponseDto sendMessage(UUID conversationId, UUID senderId, SendMessageRequestDto request) {
        String content =  request.getContent();
        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        conversationParticipantRepository.findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(conversationId, senderId)
                .orElseThrow(() -> new ConversationNotFoundException("USER_NOT_PARTICIPANT"));

       if(conversation.getType().equals(ConversationType.ONE_ON_ONE))
       {
           boolean isBlocked = conversationBlockRepository.existsByConversation_ConversationIdAndUserId(conversationId, senderId);
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
        return new SendMessageResponseDto(
                message.getMessageId(),
                conversationId,
                senderId,
                message.getContent(),
                message.getCreatedAt()
        );
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

        if (blockRepository.existsByConversation_ConversationIdAndUserId(
                conversationId, blockinguserid)) {
            return;
        }

        blockRepository.save(
                ConversationBlockEntity.create(conversation, blockinguserid)
        );
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
        blockRepository
                .findByConversation_ConversationIdAndUserId(
                        conversationId, userId
                )
                .ifPresent(blockRepository::delete);
    }

    public void muteConversation(){

    }
}
