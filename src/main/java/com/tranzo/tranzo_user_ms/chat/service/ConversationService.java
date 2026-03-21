package com.tranzo.tranzo_user_ms.chat.service;

import com.tranzo.tranzo_user_ms.chat.dto.ChatListItemDto;
import com.tranzo.tranzo_user_ms.chat.dto.MessageResponseDto;
import com.tranzo.tranzo_user_ms.chat.enums.ChatErrorCode;
import com.tranzo.tranzo_user_ms.chat.enums.ConversationType;
import com.tranzo.tranzo_user_ms.chat.exception.ConversationNotFoundException;
import com.tranzo.tranzo_user_ms.chat.model.ConversationParticipantEntity;
import com.tranzo.tranzo_user_ms.chat.model.MessageEntity;
import com.tranzo.tranzo_user_ms.chat.repository.ConversationParticipantRepository;
import com.tranzo.tranzo_user_ms.chat.repository.ConversationRepository;
import com.tranzo.tranzo_user_ms.chat.repository.MessageRepository;
import com.tranzo.tranzo_user_ms.commons.exception.BadRequestException;
import com.tranzo.tranzo_user_ms.user.client.UserProfileClient;
import com.tranzo.tranzo_user_ms.user.dto.UserNameDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for querying and retrieving conversation data.
 * Handles fetching user conversations and message history with pagination.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ConversationService {

    private static final int DEFAULT_LIMIT = 30;
    private static final int MAX_LIMIT = 100;

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository conversationParticipantRepository;
    private final MessageRepository messageRepository;
    private final UserProfileClient userProfileClient;

    /**
     * Retrieves all conversations for the current user.
     *
     * @param currentUserId the user ID
     * @return list of ChatListItemDto with conversation details
     */
    public List<ChatListItemDto> getMyConversations(UUID currentUserId) {
        log.debug("Fetching conversations for user {}", currentUserId);

        List<ChatListItemDto> conversations = conversationRepository.findChatListForUser(currentUserId);
        
        // Populate conversation names for one-on-one chats
        for (ChatListItemDto conversation : conversations) {
            if (conversation.getType().equals(ConversationType.ONE_ON_ONE)) {
                // This is a one-on-one conversation, fetch the other user's name
                List<ConversationParticipantEntity> participants =
                    conversationParticipantRepository.findByConversation_ConversationIdAndLeftAtIsNull(
                        conversation.getConversationId());
                
                // Find the other participant (not the current user)
                UUID otherUserId = participants.stream()
                    .map(ConversationParticipantEntity::getUserId)
                    .filter(userId -> !userId.equals(currentUserId))
                    .findFirst()
                    .orElse(null);
                
                if (otherUserId != null) {
                    Map<UUID, UserNameDto> namesByUserId = userProfileClient.getNamesByUserIds(List.of(otherUserId));
                    UserNameDto otherUser = namesByUserId.get(otherUserId);
                    
                    if (otherUser != null) {
                        String fullName = buildFullName(otherUser.getFirstName(), otherUser.getMiddleName(), otherUser.getLastName());
                        conversation.setConversationName(fullName);
                    } else {
                        conversation.setConversationName("Unknown User");
                    }
                } else {
                    conversation.setConversationName("Unknown");
                }
            }
        }
        
        log.info("Retrieved {} conversations for user {}", conversations.size(), currentUserId);
        return conversations;
    }
    
    private String buildFullName(String firstName, String middleName, String lastName) {
        StringBuilder name = new StringBuilder();
        if (firstName != null && !firstName.trim().isEmpty()) {
            name.append(firstName.trim());
        }
        if (middleName != null && !middleName.trim().isEmpty()) {
            if (!name.isEmpty()) name.append(" ");
            name.append(middleName.trim());
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            if (!name.isEmpty()) name.append(" ");
            name.append(lastName.trim());
        }
        return name.length() > 0 ? name.toString() : "Unknown";
    }

    /**
     * Fetches messages from a conversation with pagination support.
     * Messages can be fetched before a specific timestamp for pagination.
     * Automatically marks the conversation as read for the user.
     *
     * @param conversationId the conversation ID
     * @param currentUserId  the user fetching messages
     * @param before         optional timestamp to fetch messages before (for pagination)
     * @param limit          optional limit on number of messages (default: 30, max: 100)
     * @return list of MessageResponseDto with message details
     * @throws ConversationNotFoundException if conversation not found or user is not participant
     * @throws BadRequestException if limit is invalid
     */
    public List<MessageResponseDto> fetchMessages(UUID conversationId, UUID currentUserId, LocalDateTime before, Integer limit) {
        log.debug("Fetching messages for conversation {} by user {} with limit {} before {}", 
                  conversationId, currentUserId, limit, before);

        conversationRepository.findById(conversationId)
                .orElseThrow(() -> {
                    log.error("Conversation not found with ID: {}", conversationId);
                    return new ConversationNotFoundException(ChatErrorCode.CONVERSATION_NOT_FOUND, "Conversation not found");
                });

        var participant = conversationParticipantRepository
                .findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(conversationId, currentUserId)
                .orElseThrow(() -> {
                    log.error("User {} is not a participant in conversation {}", currentUserId, conversationId);
                    return new ConversationNotFoundException(ChatErrorCode.USER_NOT_IN_CONVERSATION, "User is not a participant in this conversation");
                });

        // Validate and set page size
        int pageSize = DEFAULT_LIMIT;
        if (limit != null) {
            if (limit <= 0 || limit > MAX_LIMIT) {
                log.warn("Invalid limit {} requested for conversation {}", limit, conversationId);
                throw new BadRequestException("Limit must be between 1 and " + MAX_LIMIT);
            }
            pageSize = limit;
        }

        Pageable pageable = PageRequest.of(0, pageSize);

        // Fetch messages with optional timestamp filtering
        List<MessageEntity> messages = (before == null)
                ? messageRepository.findMessages(conversationId, pageable)
                : messageRepository.findMessagesBefore(conversationId, before, pageable);

        // Mark conversation as read
        participant.markAsRead();
        conversationParticipantRepository.save(participant);
        log.debug("Conversation {} marked as read for user {}", conversationId, currentUserId);

        // Convert messages to DTOs with sender information
        List<MessageResponseDto> messageResponseDtos = messages.stream()
                .map(message -> convertToMessageResponseDto(message, conversationId, currentUserId))
                .toList();

        log.info("Retrieved {} messages for conversation {} for user {}", messageResponseDtos.size(), conversationId, currentUserId);
        return messageResponseDtos;
    }

    /**
     * Converts a MessageEntity to MessageResponseDto with sender's profile information.
     *
     * @param message        the message entity
     * @param conversationId the conversation ID
     * @param currentUserId  the current user ID
     * @return MessageResponseDto with enriched sender details
     */
    private MessageResponseDto convertToMessageResponseDto(MessageEntity message, UUID conversationId, UUID currentUserId) {
        UUID senderId = message.getSenderId();
        String firstName, middleName, lastName;

        if (senderId == null && (message.getContent().equals("You are now connected") || message.getContent().equals("Trip Hosted Successfully"))) {
            // System message
            firstName = "System";
            middleName = null;
            lastName = null;
        } else {
            // User message - fetch sender details
            Map<UUID, UserNameDto> namesByUserId = userProfileClient.getNamesByUserIds(List.of(senderId));
            UserNameDto senderName = namesByUserId.get(senderId);

            firstName = senderName != null ? senderName.getFirstName() : "Unknown";
            middleName = senderName != null ? senderName.getMiddleName() : null;
            lastName = senderName != null ? senderName.getLastName() : null;
        }

        // Determine if current user is the sender
        Boolean isSender = senderId != null && senderId.equals(currentUserId);

        return new MessageResponseDto(
                message.getMessageId(),
                conversationId,
                senderId,
                firstName,
                middleName,
                lastName,
                message.getContent(),
                isSender,
                message.getCreatedAt()
        );
    }
}
