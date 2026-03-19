package com.tranzo.tranzo_user_ms.chat.service;

import com.tranzo.tranzo_user_ms.chat.dto.*;
import com.tranzo.tranzo_user_ms.chat.enums.ConversationRole;
import com.tranzo.tranzo_user_ms.chat.enums.ChatErrorCode;
import com.tranzo.tranzo_user_ms.chat.enums.ConversationType;
import com.tranzo.tranzo_user_ms.chat.exception.ConversationNotFoundException;
import com.tranzo.tranzo_user_ms.chat.exception.UserLeftConversationException;
import com.tranzo.tranzo_user_ms.chat.exception.InvalidMessageException;
import com.tranzo.tranzo_user_ms.chat.exception.ConversationMutedException;
import com.tranzo.tranzo_user_ms.chat.model.*;
import com.tranzo.tranzo_user_ms.chat.repository.*;
import com.tranzo.tranzo_user_ms.commons.exception.ForbiddenException;
import com.tranzo.tranzo_user_ms.trip.repository.TripRepository;
import com.tranzo.tranzo_user_ms.user.client.UserProfileClient;
import com.tranzo.tranzo_user_ms.user.dto.UserNameDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for creating and managing conversations.
 * Handles one-to-one chats, group chats, and conversation operations like muting/blocking.
 */
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
    private final UserProfileClient userProfileClient;
    private final TripRepository tripRepository;

    /**
     * Creates a one-to-one conversation between two users.
     * If a conversation already exists, returns the existing conversation.
     *
     * @param userId  the current user initiating the conversation
     * @param request the other user's ID
     * @return CreateConversationResponseDto with conversation ID
     * @throws ConversationNotFoundException if other user not found
     * @throws IllegalArgumentException if user tries to create conversation with themselves
     */
    public CreateConversationResponseDto createOneToOneConversation(UUID userId, CreateConversationRequestDto request) {
        UUID otherUserId = request.getOtherUserId();
        log.debug("Creating one-to-one conversation between user {} and {}", userId, otherUserId);

        if (userId.equals(otherUserId)) {
            log.warn("User {} attempted to create conversation with themselves", userId);
            throw new ConversationNotFoundException(ChatErrorCode.SELF_CONVERSATION, "Cannot create a conversation with yourself");
        }

        // Fetch other user's profile information
        Map<UUID, UserNameDto> namesByUserId = userProfileClient.getNamesByUserIds(List.of(otherUserId));
        UserNameDto otherUserName = namesByUserId.get(otherUserId);
        if (otherUserName == null) {
            log.error("User profile not found for user ID: {}", otherUserId);
            throw new ConversationNotFoundException(ChatErrorCode.RECIPIENT_NOT_FOUND, "Recipient user not found");
        }

        String conversationName = buildUserName(otherUserName);

        // Check if conversation already exists
        Optional<ConversationEntity> existingConversation = conversationRepository.findOneToOneConversationBetweenUsers(userId, otherUserId);
        if (existingConversation.isPresent()) {
            ConversationEntity conversation = existingConversation.get();
            log.info("One-to-one conversation already exists between {} and {}", userId, otherUserId);
            return CreateConversationResponseDto.builder()
                    .conversationId(conversation.getConversationId())
                    .createdAt(conversation.getCreatedAt())
                    .existing(true)
                    .build();
        }

        // Create new one-to-one conversation
        ConversationEntity newConversation = ConversationEntity.createOneToOneChat(userId);
        newConversation.addParticipant(userId, ConversationRole.MEMBER);
        newConversation.addParticipant(otherUserId, ConversationRole.MEMBER);
        newConversation.setConversationName(conversationName);

        MessageEntity systemMessage = MessageEntity.systemMessage(
                newConversation,
                "You are now connected"
        );

        conversationRepository.save(newConversation);
        messageRepository.save(systemMessage);
        log.info("One-to-one conversation created successfully between {} and {}", userId, otherUserId);
        
        return CreateConversationResponseDto.builder()
                .conversationId(newConversation.getConversationId())
                .createdAt(newConversation.getCreatedAt())
                .existing(false)
                .build();
    }

    /**
     * Builds a formatted name from UserNameDto components.
     */
    private String buildUserName(UserNameDto userNameDto) {
        StringBuilder name = new StringBuilder();
        if (userNameDto.getFirstName() != null) {
            name.append(userNameDto.getFirstName());
        }
        if (userNameDto.getMiddleName() != null) {
            name.append(" ").append(userNameDto.getMiddleName());
        }
        if (userNameDto.getLastName() != null) {
            name.append(" ").append(userNameDto.getLastName());
        }
        return name.toString().trim();
    }

    /**
     * Creates a group chat for a trip.
     * The host user is added as ADMIN_HOST and a system message is posted.
     *
     * @param hostUserId the user creating the trip and chat
     * @param tripId     the trip ID associated with this conversation
     * @return the created ConversationEntity
     * @throws ConversationNotFoundException if trip not found
     */
    public ConversationEntity createTripGroupChat(UUID hostUserId, UUID tripId) {
        log.debug("Creating group chat for trip {} hosted by user {}", tripId, hostUserId);

        ConversationEntity newConversation = ConversationEntity.createGroup(hostUserId);
        String tripTitle = tripRepository.findTripNameByTripId(tripId);
        
        if (tripTitle == null || tripTitle.isBlank()) {
            log.error("Trip title not found for trip ID: {}", tripId);
            throw new ConversationNotFoundException(ChatErrorCode.TRIP_NOT_FOUND, "Trip not found");
        }

        newConversation.addParticipant(hostUserId, ConversationRole.ADMIN_HOST);
        newConversation.setConversationName(tripTitle);
        
        MessageEntity systemMessage = MessageEntity.systemMessage(
                newConversation,
                "Trip Hosted Successfully"
        );
        
        ConversationEntity conversationEntity = conversationRepository.save(newConversation);
        messageRepository.save(systemMessage);
        log.info("Group chat created successfully for trip {} with conversation ID {}", tripId, conversationEntity.getConversationId());
        
        return conversationEntity;
    }

    /**
     * Adds a user as a participant to an existing conversation (e.g., when they join a trip).
     * Idempotent: if the user is already a participant, no action is taken.
     *
     * @param conversationId the conversation ID
     * @param userId         the user to add
     * @throws ConversationNotFoundException if conversation not found
     */
    public void addParticipantToConversation(UUID conversationId, UUID userId) {
        log.debug("Adding user {} to conversation {}", userId, conversationId);

        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> {
                    log.error("Conversation not found with ID: {}", conversationId);
                    return new ConversationNotFoundException("CONVERSATION_NOT_FOUND");
                });

        boolean alreadyParticipant = conversationParticipantRepository
                .findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(conversationId, userId)
                .isPresent();

        if (!alreadyParticipant) {
            conversation.addParticipant(userId, ConversationRole.MEMBER);
            conversationRepository.save(conversation);
            log.info("User {} successfully added to conversation {}", userId, conversationId);
        } else {
            log.debug("User {} is already a participant in conversation {}", userId, conversationId);
        }
    }

    /**
     * Sends a message to a conversation.
     * For group chats, broadcasts to all participants.
     * For one-on-one chats, sends only to the receiver and checks blocking status.
     *
     * @param conversationId the conversation ID
     * @param senderId       the user sending the message
     * @param request        the message content
     * @return SendMessageResponseDto with message details
     * @throws ConversationNotFoundException if conversation or sender not found
     * @throws ForbiddenException if sender has blocked the conversation
     */
    public SendMessageResponseDto sendMessage(UUID conversationId, UUID senderId, SendMessageRequestDto request) {
        String content = request.getContent();
        log.debug("User {} sending message to conversation {}", senderId, conversationId);

        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> {
                    log.error("Conversation not found with ID: {}", conversationId);
                    return new ConversationNotFoundException("CONVERSATION_NOT_FOUND");
                });

        conversationParticipantRepository.findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(conversationId, senderId)
                .orElseThrow(() -> {
                    log.error("User {} is not a participant in conversation {}", senderId, conversationId);
                    return new UserLeftConversationException(ChatErrorCode.USER_NOT_IN_CONVERSATION, "User is not a participant in this conversation");
                });

        // Check if sender has blocked the conversation (for one-on-one chats)
        if (conversation.getType().equals(ConversationType.ONE_ON_ONE)) {
            // TODO : Do we need to check if the conversation has been blocked by opposite user too? Should we maintain a flag to mark conversation as blocked if anyone from the conversation does it?
            boolean isBlocked = conversationBlockRepository.existsByConversation_ConversationIdAndBlockedBy(conversationId, senderId);
            if (isBlocked) {
                log.warn("User {} attempted to send message to blocked conversation {}", senderId, conversationId);
                throw new ConversationMutedException(ChatErrorCode.USER_BLOCKED, "User has blocked this conversation");
            }
        }

        // Validate message content
        if (content == null || content.trim().isEmpty()) {
            throw new InvalidMessageException(ChatErrorCode.MESSAGE_EMPTY, "Message cannot be empty");
        }
        if (content.length() > 1000) {
            throw new InvalidMessageException(ChatErrorCode.MESSAGE_TOO_LONG, "Message exceeds maximum length");
        }

        // Save the message
        MessageEntity message = messageRepository.save(
                MessageEntity.userMessage(
                        conversation,
                        senderId,
                        content
                )
        );

        // Fetch sender's profile information
        Map<UUID, UserNameDto> namesByUserId = userProfileClient.getNamesByUserIds(List.of(senderId));
        UserNameDto senderName = namesByUserId.get(senderId);
        
        SendMessageResponseDto response = new SendMessageResponseDto(
                message.getMessageId(),
                conversationId,
                senderId,
                senderName != null ? senderName.getFirstName() : "Unknown",
                senderName != null ? senderName.getMiddleName() : null,
                senderName != null ? senderName.getLastName() : null,
                content,
                message.getCreatedAt()
        );

        // Send message to appropriate recipients based on conversation type
        if (conversation.getType() == ConversationType.GROUP_CHAT) {
            log.debug("Broadcasting message to group chat participants in conversation {}", conversationId);
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
                    .orElseThrow(() -> {
                        log.error("Could not find receiver in one-on-one conversation {}", conversationId);
                        return new ConversationNotFoundException(ChatErrorCode.RECIPIENT_NOT_FOUND, "Recipient user not found");
                    });
            log.debug("Sending private message from {} to {}", senderId, receiverId);
            messagingTemplate.convertAndSendToUser(
                    receiverId.toString(),
                    "/queue/messages",
                    response
            );
        }

        log.info("Message sent successfully by user {} to conversation {}", senderId, conversationId);
        return response;
    }

    /**
     * Marks a conversation as read for the specified user.
     *
     * @param conversationId the conversation ID
     * @param userId         the user marking the conversation as read
     * @throws ConversationNotFoundException if conversation or participant not found
     */
    public void markConversationAsRead(UUID conversationId, UUID userId) {
        log.debug("Marking conversation {} as read for user {}", conversationId, userId);

        conversationRepository.findById(conversationId)
                .orElseThrow(() -> {
                    log.error("Conversation not found with ID: {}", conversationId);
                    return new ConversationNotFoundException("CONVERSATION_NOT_FOUND");
                });

        ConversationParticipantEntity conversationParticipant = conversationParticipantRepository
                .findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(conversationId, userId)
                .orElseThrow(() -> {
                    log.error("User {} is not a participant in conversation {}", userId, conversationId);
                    return new UserLeftConversationException(ChatErrorCode.USER_NOT_IN_CONVERSATION, "User is not a participant in this conversation");
                });

        conversationParticipant.markAsRead();
        log.debug("Conversation {} marked as read for user {}", conversationId, userId);
    }


    /**
     * Blocks a one-on-one conversation for the specified user.
     * Only applicable to one-on-one conversations.
     * Idempotent: if already blocked, no action is taken.
     *
     * @param conversationId the conversation ID
     * @param blockingUserId the user blocking the conversation
     * @throws ConversationNotFoundException if conversation or participant not found
     * @throws ForbiddenException if conversation is not one-on-one
     */
    public void blockConversation(UUID conversationId, UUID blockingUserId) {
        log.debug("User {} attempting to block conversation {}", blockingUserId, conversationId);

        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> {
                    log.error("Conversation not found with ID: {}", conversationId);
                    return new ConversationNotFoundException("CONVERSATION_NOT_FOUND");
                });

        if (conversation.getType() != ConversationType.ONE_ON_ONE) {
            log.error("User {} attempted to block non-one-on-one conversation {}", blockingUserId, conversationId);
            throw new ConversationMutedException(ChatErrorCode.BLOCK_NOT_ALLOWED, "Blocking is not allowed for this conversation type");
        }

        conversationParticipantRepository.findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(conversationId, blockingUserId)
                .orElseThrow(() -> {
                    log.error("User {} is not a participant in conversation {}", blockingUserId, conversationId);
                    return new UserLeftConversationException(ChatErrorCode.USER_NOT_IN_CONVERSATION, "User is not a participant in this conversation");
                });

        if (conversationBlockRepository.existsByConversation_ConversationIdAndBlockedBy(
                conversationId, blockingUserId)) {
            log.debug("Conversation {} already blocked by user {}", conversationId, blockingUserId);
            return;
        }

        conversationBlockRepository.save(
                ConversationBlockEntity.create(conversation, blockingUserId)
        );
        log.info("Conversation {} successfully blocked by user {}", conversationId, blockingUserId);

        // TODO: Remove travel PAL after blocking user i.e trigger unmatching from user service
    }

    /**
     * Unblocks a previously blocked one-on-one conversation for the specified user.
     *
     * @param conversationId the conversation ID
     * @param userId         the user unblocking the conversation
     * @throws ConversationNotFoundException if conversation, participant, or block entry not found
     * @throws ForbiddenException if conversation is not one-on-one
     */
    public void unblockConversation(UUID conversationId, UUID userId) {
        log.debug("User {} attempting to unblock conversation {}", userId, conversationId);

        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> {
                    log.error("Conversation not found with ID: {}", conversationId);
                    return new ConversationNotFoundException("CONVERSATION_NOT_FOUND");
                });

        if (conversation.getType() != ConversationType.ONE_ON_ONE) {
            log.error("User {} attempted to unblock non-one-on-one conversation {}", userId, conversationId);
            throw new ConversationMutedException(ChatErrorCode.UNBLOCK_NOT_ALLOWED, "Unblocking is not allowed for this conversation type");
        }

        conversationParticipantRepository.findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(conversationId, userId)
                .orElseThrow(() -> {
                    log.error("User {} is not a participant in conversation {}", userId, conversationId);
                    return new UserLeftConversationException(ChatErrorCode.USER_NOT_IN_CONVERSATION, "User is not a participant in this conversation");
                });

        ConversationBlockEntity blockEntity = conversationBlockRepository.findByConversation_ConversationIdAndBlockedBy(
                conversationId, userId)
                .orElseThrow(() -> {
                    log.error("Block entry not found for conversation {} blocked by user {}", conversationId, userId);
                    return new ConversationNotFoundException(ChatErrorCode.USER_BLOCKED, "No block entry found for this user");
                });

        conversationBlockRepository.delete(blockEntity);
        log.info("Conversation {} successfully unblocked by user {}", conversationId, userId);
    }


    /**
     * Mutes notifications for a conversation for the specified user.
     * Idempotent: if already muted, no action is taken.
     *
     * @param conversationId the conversation ID
     * @param userId         the user muting the conversation
     * @throws ConversationNotFoundException if conversation or participant not found
     * @throws ForbiddenException if user is not a participant
     */
    public void muteConversation(UUID conversationId, UUID userId) {
        log.debug("User {} attempting to mute conversation {}", userId, conversationId);

        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> {
                    log.error("Conversation not found with ID: {}", conversationId);
                    return new ConversationNotFoundException("CONVERSATION_NOT_FOUND");
                });

        conversationParticipantRepository.findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(conversationId, userId)
                .orElseThrow(() -> {
                    log.error("User {} is not a participant in conversation {}", userId, conversationId);
                    return new UserLeftConversationException(ChatErrorCode.USER_NOT_IN_CONVERSATION, "User is not a participant in this conversation");
                });

        if (muteRepository.existsByConversation_ConversationIdAndUserId(conversationId, userId)) {
            log.debug("Conversation {} already muted by user {}", conversationId, userId);
            return;
        }

        muteRepository.save(ConversationMuteEntity.create(conversation, userId));
        log.info("Conversation {} successfully muted by user {}", conversationId, userId);
    }

    /**
     * Unmutes notifications for a conversation for the specified user.
     * Idempotent: if not muted, no action is taken.
     *
     * @param conversationId the conversation ID
     * @param userId         the user unmuting the conversation
     * @throws ConversationNotFoundException if conversation or participant not found
     * @throws ForbiddenException if user is not a participant
     */
    public void unmuteConversation(UUID conversationId, UUID userId) {
        log.debug("User {} attempting to unmute conversation {}", userId, conversationId);

        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> {
                    log.error("Conversation not found with ID: {}", conversationId);
                    return new ConversationNotFoundException("CONVERSATION_NOT_FOUND");
                });

        conversationParticipantRepository
                .findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(
                        conversationId, userId
                )
                .orElseThrow(() -> {
                    log.error("User {} is not a participant in conversation {}", userId, conversationId);
                    return new UserLeftConversationException(ChatErrorCode.USER_NOT_IN_CONVERSATION, "User is not a participant in this conversation");
                });

        muteRepository
                .findByConversation_ConversationIdAndUserId(
                        conversationId, userId
                )
                .ifPresentOrElse(
                        muteEntity -> {
                            muteRepository.delete(muteEntity);
                            log.info("Conversation {} successfully unmuted by user {}", conversationId, userId);
                        },
                        () -> log.debug("Conversation {} was not muted by user {}", conversationId, userId)
                );
    }
}
