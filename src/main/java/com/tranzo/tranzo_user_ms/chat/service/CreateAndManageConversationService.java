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
        log.info("Processing started | operation=createOneToOneConversation | userId={} | otherUserId={}", userId, otherUserId);

        try {
            if (userId.equals(otherUserId)) {
                log.warn("Invalid request | operation=createOneToOneConversation | userId={} | reason=SELF_CONVERSATION", userId);
                throw new ConversationNotFoundException(ChatErrorCode.SELF_CONVERSATION, "Cannot create a conversation with yourself");
            }

            // Fetch other user's profile information
            log.info("Calling external service | service=UserProfileClient | operation=getNamesByUserIds | userIds={}", List.of(otherUserId));
            Map<UUID, UserNameDto> namesByUserId = userProfileClient.getNamesByUserIds(List.of(otherUserId));
            UserNameDto otherUserName = namesByUserId.get(otherUserId);
            if (otherUserName == null) {
                log.error("User not found | operation=createOneToOneConversation | otherUserId={} | reason=NOT_FOUND", otherUserId);
                throw new ConversationNotFoundException(ChatErrorCode.RECIPIENT_NOT_FOUND, "Recipient user not found");
            }

            String conversationName = buildUserName(otherUserName);

            // Check if conversation already exists (with pessimistic lock to prevent race conditions)
            Optional<ConversationEntity> existingConversation = conversationRepository.findOneToOneConversationBetweenUsers(userId, otherUserId);
            if (existingConversation.isPresent()) {
                ConversationEntity conversation = existingConversation.get();
                log.info("Processing completed | operation=createOneToOneConversation | userId={} | otherUserId={} | conversationId={} | status=EXISTING", userId, otherUserId, conversation.getConversationId());
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
            
            log.info("Processing completed | operation=createOneToOneConversation | userId={} | otherUserId={} | conversationId={} | status=CREATED", userId, otherUserId, newConversation.getConversationId());
            return CreateConversationResponseDto.builder()
                    .conversationId(newConversation.getConversationId())
                    .createdAt(newConversation.getCreatedAt())
                    .existing(false)
                    .build();
        } catch (ConversationNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Operation failed | operation=createOneToOneConversation | userId={} | otherUserId={} | reason={}", userId, otherUserId, e.getMessage(), e);
            throw e;
        }
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
        log.info("Processing started | operation=createTripGroupChat | hostUserId={} | tripId={}", hostUserId, tripId);

        try {
            ConversationEntity newConversation = ConversationEntity.createGroup(hostUserId);
            String tripTitle = tripRepository.findTripNameByTripId(tripId);
            
            if (tripTitle == null || tripTitle.isBlank()) {
                log.error("Trip not found | operation=createTripGroupChat | tripId={} | reason=NOT_FOUND", tripId);
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
            
            log.info("Processing completed | operation=createTripGroupChat | hostUserId={} | tripId={} | conversationId={} | status=CREATED", hostUserId, tripId, conversationEntity.getConversationId());
            return conversationEntity;
        } catch (ConversationNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Operation failed | operation=createTripGroupChat | hostUserId={} | tripId={} | reason={}", hostUserId, tripId, e.getMessage(), e);
            throw e;
        }
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
        log.info("Processing started | operation=addParticipantToConversation | conversationId={} | userId={}", conversationId, userId);

        try {
            ConversationEntity conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> {
                        log.error("Conversation not found | operation=addParticipantToConversation | conversationId={} | reason=NOT_FOUND", conversationId);
                        return new ConversationNotFoundException("CONVERSATION_NOT_FOUND");
                    });

            boolean alreadyParticipant = conversationParticipantRepository
                    .findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(conversationId, userId)
                    .isPresent();

            if (!alreadyParticipant) {
                conversation.addParticipant(userId, ConversationRole.MEMBER);
                conversationRepository.save(conversation);
                log.info("Processing completed | operation=addParticipantToConversation | conversationId={} | userId={} | status=ADDED", conversationId, userId);
            } else {
                log.info("Processing completed | operation=addParticipantToConversation | conversationId={} | userId={} | status=ALREADY_PARTICIPANT", conversationId, userId);
            }
        } catch (ConversationNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Operation failed | operation=addParticipantToConversation | conversationId={} | userId={} | reason={}", conversationId, userId, e.getMessage(), e);
            throw e;
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
        log.info("Processing started | operation=sendMessage | conversationId={} | senderId={} | contentLength={}", conversationId, senderId, content != null ? content.length() : 0);

        try {
            ConversationEntity conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> {
                        log.error("Conversation not found | operation=sendMessage | conversationId={} | reason=NOT_FOUND", conversationId);
                        return new ConversationNotFoundException("CONVERSATION_NOT_FOUND");
                    });

            conversationParticipantRepository.findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(conversationId, senderId)
                    .orElseThrow(() -> {
                        log.error("Access denied | operation=sendMessage | conversationId={} | senderId={} | reason=NOT_PARTICIPANT", conversationId, senderId);
                        return new UserLeftConversationException(ChatErrorCode.USER_NOT_IN_CONVERSATION, "User is not a participant in this conversation");
                    });

            // Check if sender has blocked the conversation (for one-on-one chats)
            if (conversation.getType().equals(ConversationType.ONE_ON_ONE)) {
                boolean isBlocked = conversationBlockRepository.existsByConversation_ConversationIdAndBlockedBy(conversationId, senderId);
                if (isBlocked) {
                    log.warn("Access denied | operation=sendMessage | conversationId={} | senderId={} | reason=BLOCKED", conversationId, senderId);
                    throw new ConversationMutedException(ChatErrorCode.USER_BLOCKED, "User has blocked this conversation");
                }
            }

            // Validate message content
            if (content == null || content.trim().isEmpty()) {
                log.warn("Invalid request | operation=sendMessage | conversationId={} | senderId={} | reason=EMPTY_MESSAGE", conversationId, senderId);
                throw new InvalidMessageException(ChatErrorCode.MESSAGE_EMPTY, "Message cannot be empty");
            }
            if (content.length() > 1000) {
                log.warn("Invalid request | operation=sendMessage | conversationId={} | senderId={} | reason=MESSAGE_TOO_LONG", conversationId, senderId);
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
            log.info("Calling external service | service=UserProfileClient | operation=getNamesByUserIds | senderId={}", senderId);
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
                log.info("Calling external service | service=WebSocket | operation=broadcastToGroup | conversationId={}", conversationId);
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
                            log.error("Receiver not found | operation=sendMessage | conversationId={} | reason=NO_RECEIVER", conversationId);
                            return new ConversationNotFoundException(ChatErrorCode.RECIPIENT_NOT_FOUND, "Recipient user not found");
                        });
                
                log.info("Calling external service | service=WebSocket | operation=sendToUser | senderId={} | receiverId={}", senderId, receiverId);
                messagingTemplate.convertAndSendToUser(
                        receiverId.toString(),
                        "/queue/messages",
                        response
                );
            }

            log.info("Processing completed | operation=sendMessage | conversationId={} | senderId={} | messageId={} | status=SENT", conversationId, senderId, message.getMessageId());
            return response;
        } catch (ConversationNotFoundException | UserLeftConversationException | ConversationMutedException | InvalidMessageException e) {
            throw e;
        } catch (Exception e) {
            log.error("Operation failed | operation=sendMessage | conversationId={} | senderId={} | reason={}", conversationId, senderId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Marks a conversation as read for the specified user.
     *
     * @param conversationId the conversation ID
     * @param userId         the user marking the conversation as read
     * @throws ConversationNotFoundException if conversation or participant not found
     */
    public void markConversationAsRead(UUID conversationId, UUID userId) {
        log.info("Processing started | operation=markConversationAsRead | conversationId={} | userId={}", conversationId, userId);

        try {
            conversationRepository.findById(conversationId)
                    .orElseThrow(() -> {
                        log.error("Conversation not found | operation=markConversationAsRead | conversationId={} | reason=NOT_FOUND", conversationId);
                        return new ConversationNotFoundException("CONVERSATION_NOT_FOUND");
                    });

            ConversationParticipantEntity conversationParticipant = conversationParticipantRepository
                    .findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(conversationId, userId)
                    .orElseThrow(() -> {
                        log.error("Access denied | operation=markConversationAsRead | conversationId={} | userId={} | reason=NOT_PARTICIPANT", conversationId, userId);
                        return new UserLeftConversationException(ChatErrorCode.USER_NOT_IN_CONVERSATION, "User is not a participant in this conversation");
                    });

            conversationParticipant.markAsRead();
            log.info("Processing completed | operation=markConversationAsRead | conversationId={} | userId={} | status=SUCCESS", conversationId, userId);
        } catch (ConversationNotFoundException | UserLeftConversationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Operation failed | operation=markConversationAsRead | conversationId={} | userId={} | reason={}", conversationId, userId, e.getMessage(), e);
            throw e;
        }
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
        log.info("Processing started | operation=blockConversation | conversationId={} | userId={}", conversationId, blockingUserId);

        try {
            ConversationEntity conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> {
                        log.error("Conversation not found | operation=blockConversation | conversationId={} | reason=NOT_FOUND", conversationId);
                        return new ConversationNotFoundException("CONVERSATION_NOT_FOUND");
                    });

            if (conversation.getType() != ConversationType.ONE_ON_ONE) {
                log.warn("Invalid request | operation=blockConversation | conversationId={} | userId={} | reason=NOT_ONE_ON_ONE", conversationId, blockingUserId);
                throw new ConversationMutedException(ChatErrorCode.BLOCK_NOT_ALLOWED, "Blocking is not allowed for this conversation type");
            }

            conversationParticipantRepository.findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(conversationId, blockingUserId)
                    .orElseThrow(() -> {
                        log.error("Access denied | operation=blockConversation | conversationId={} | userId={} | reason=NOT_PARTICIPANT", conversationId, blockingUserId);
                        return new UserLeftConversationException(ChatErrorCode.USER_NOT_IN_CONVERSATION, "User is not a participant in this conversation");
                    });

            if (conversationBlockRepository.existsByConversation_ConversationIdAndBlockedBy(
                    conversationId, blockingUserId)) {
                log.info("Processing completed | operation=blockConversation | conversationId={} | userId={} | status=ALREADY_BLOCKED", conversationId, blockingUserId);
                return;
            }

            conversationBlockRepository.save(
                    ConversationBlockEntity.create(conversation, blockingUserId)
            );
            log.info("Processing completed | operation=blockConversation | conversationId={} | userId={} | status=BLOCKED", conversationId, blockingUserId);

            // TODO: Remove travel PAL after blocking user i.e trigger unmatching from user service
        } catch (ConversationNotFoundException | UserLeftConversationException | ConversationMutedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Operation failed | operation=blockConversation | conversationId={} | userId={} | reason={}", conversationId, blockingUserId, e.getMessage(), e);
            throw e;
        }
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
        log.info("Processing started | operation=unblockConversation | conversationId={} | userId={}", conversationId, userId);

        try {
            ConversationEntity conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> {
                        log.error("Conversation not found | operation=unblockConversation | conversationId={} | reason=NOT_FOUND", conversationId);
                        return new ConversationNotFoundException("CONVERSATION_NOT_FOUND");
                    });

            if (conversation.getType() != ConversationType.ONE_ON_ONE) {
                log.warn("Invalid request | operation=unblockConversation | conversationId={} | userId={} | reason=NOT_ONE_ON_ONE", conversationId, userId);
                throw new ConversationMutedException(ChatErrorCode.UNBLOCK_NOT_ALLOWED, "Unblocking is not allowed for this conversation type");
            }

            conversationParticipantRepository.findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(conversationId, userId)
                    .orElseThrow(() -> {
                        log.error("Access denied | operation=unblockConversation | conversationId={} | userId={} | reason=NOT_PARTICIPANT", conversationId, userId);
                        return new UserLeftConversationException(ChatErrorCode.USER_NOT_IN_CONVERSATION, "User is not a participant in this conversation");
                    });

            ConversationBlockEntity blockEntity = conversationBlockRepository.findByConversation_ConversationIdAndBlockedBy(
                    conversationId, userId)
                    .orElseThrow(() -> {
                        log.error("Block not found | operation=unblockConversation | conversationId={} | userId={} | reason=NOT_BLOCKED", conversationId, userId);
                        return new ConversationNotFoundException(ChatErrorCode.USER_BLOCKED, "No block entry found for this user");
                    });

            conversationBlockRepository.delete(blockEntity);
            log.info("Processing completed | operation=unblockConversation | conversationId={} | userId={} | status=UNBLOCKED", conversationId, userId);
        } catch (ConversationNotFoundException | UserLeftConversationException | ConversationMutedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Operation failed | operation=unblockConversation | conversationId={} | userId={} | reason={}", conversationId, userId, e.getMessage(), e);
            throw e;
        }
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
        log.info("Processing started | operation=muteConversation | conversationId={} | userId={}", conversationId, userId);

        try {
            ConversationEntity conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> {
                        log.error("Conversation not found | operation=muteConversation | conversationId={} | reason=NOT_FOUND", conversationId);
                        return new ConversationNotFoundException("CONVERSATION_NOT_FOUND");
                    });

            conversationParticipantRepository.findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(conversationId, userId)
                    .orElseThrow(() -> {
                        log.error("Access denied | operation=muteConversation | conversationId={} | userId={} | reason=NOT_PARTICIPANT", conversationId, userId);
                        return new UserLeftConversationException(ChatErrorCode.USER_NOT_IN_CONVERSATION, "User is not a participant in this conversation");
                    });

            if (muteRepository.existsByConversation_ConversationIdAndUserId(conversationId, userId)) {
                log.info("Processing completed | operation=muteConversation | conversationId={} | userId={} | status=ALREADY_MUTED", conversationId, userId);
                return;
            }

            muteRepository.save(ConversationMuteEntity.create(conversation, userId));
            log.info("Processing completed | operation=muteConversation | conversationId={} | userId={} | status=MUTED", conversationId, userId);
        } catch (ConversationNotFoundException | UserLeftConversationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Operation failed | operation=muteConversation | conversationId={} | userId={} | reason={}", conversationId, userId, e.getMessage(), e);
            throw e;
        }
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
        log.info("Processing started | operation=unmuteConversation | conversationId={} | userId={}", conversationId, userId);

        try {
            ConversationEntity conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> {
                        log.error("Conversation not found | operation=unmuteConversation | conversationId={} | reason=NOT_FOUND", conversationId);
                        return new ConversationNotFoundException("CONVERSATION_NOT_FOUND");
                    });

            conversationParticipantRepository
                    .findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(
                            conversationId, userId
                    )
                    .orElseThrow(() -> {
                        log.error("Access denied | operation=unmuteConversation | conversationId={} | userId={} | reason=NOT_PARTICIPANT", conversationId, userId);
                        return new UserLeftConversationException(ChatErrorCode.USER_NOT_IN_CONVERSATION, "User is not a participant in this conversation");
                    });

            muteRepository
                    .findByConversation_ConversationIdAndUserId(
                            conversationId, userId
                    )
                    .ifPresentOrElse(
                            muteEntity -> {
                                muteRepository.delete(muteEntity);
                                log.info("Processing completed | operation=unmuteConversation | conversationId={} | userId={} | status=UNMUTED", conversationId, userId);
                            },
                            () -> log.info("Processing completed | operation=unmuteConversation | conversationId={} | userId={} | status=NOT_MUTED", conversationId, userId)
                    );
        } catch (ConversationNotFoundException | UserLeftConversationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Operation failed | operation=unmuteConversation | conversationId={} | userId={} | reason={}", conversationId, userId, e.getMessage(), e);
            throw e;
        }
    }
}
