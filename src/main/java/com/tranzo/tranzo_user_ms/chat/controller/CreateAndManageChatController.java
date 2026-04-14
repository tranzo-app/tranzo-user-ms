package com.tranzo.tranzo_user_ms.chat.controller;

import com.tranzo.tranzo_user_ms.chat.dto.*;
import com.tranzo.tranzo_user_ms.chat.service.ConversationService;
import com.tranzo.tranzo_user_ms.chat.service.CreateAndManageConversationService;
import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.security.auth.message.AuthException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Chat Management
 * Handles conversation creation, messaging, muting, blocking
 */
@RestController
@RequestMapping("/conversations")
@Tag(name = "Chat Management", description = "Chat conversation and messaging operations")
@Slf4j
@RequiredArgsConstructor
public class CreateAndManageChatController {

    private final CreateAndManageConversationService createAndManageConversationService;
    private final ConversationService conversationService;

    /**
     * Send message to a conversation
     *
     * @param conversationId the conversation to send message to
     * @param request        message content
     * @return SendMessageResponseDto with message details
     */
    @PostMapping("/{conversationId}/send-message")
    @Operation(summary = "Send message", description = "Send a message to a conversation")
    public ResponseEntity<ResponseDto<SendMessageResponseDto>> sendMessage(
            @PathVariable UUID conversationId,
            @Valid @RequestBody SendMessageRequestDto request
    ) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/conversations/{}/send-message | method=POST | userId={}", conversationId, userId);

        SendMessageResponseDto sendMessageResponseDto = createAndManageConversationService.sendMessage(
                conversationId,
                userId,
                request
        );

        log.info("Message sent | conversationId={} | userId={} | messageId={} | status=SUCCESS", 
                conversationId, userId, sendMessageResponseDto.getMessageId());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseDto.success(201, "Message sent successfully", sendMessageResponseDto)
        );
    }

    /**
     * Create one-to-one conversation
     *
     * @param request other user ID
     * @return CreateConversationResponseDto with conversation details
     */
    @PostMapping("/one-to-one")
    @Operation(summary = "Create one-to-one conversation", description = "Create a new one-to-one conversation with another user")
    public ResponseEntity<ResponseDto<CreateConversationResponseDto>> createConversation(
            @Valid @RequestBody CreateConversationRequestDto request
    ) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/conversations/one-to-one | method=POST | userId={} | otherUserId={}", userId, request.getOtherUserId());

        CreateConversationResponseDto response = createAndManageConversationService.createOneToOneConversation(
                userId,
                request
        );

        log.info("Conversation created | userId={} | conversationId={} | status=SUCCESS", userId, response.getConversationId());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseDto.success(201, "Conversation created successfully", response)
        );
    }

    /**
     * Get all conversations for current user
     *
     * @return list of ChatListItemDto with chat list
     */
    @GetMapping("/chat-list")
    @Operation(summary = "Get chat list", description = "Fetch all conversations for the current user")
    public ResponseEntity<ResponseDto<List<ChatListItemDto>>> getMyConversations() throws AuthException {
        UUID currentUserId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/conversations/chat-list | method=GET | userId={}", currentUserId);

        List<ChatListItemDto> chatList = conversationService.getMyConversations(currentUserId);

        log.info("Chat list retrieved | userId={} | conversationsCount={} | status=SUCCESS", currentUserId, chatList.size());
        return ResponseEntity.ok(
                ResponseDto.success("Chat list fetched successfully", chatList)
        );
    }

    /**
     * Fetch messages from a conversation
     *
     * @param conversationId the conversation ID
     * @param before         optional timestamp to fetch messages before
     * @param limit          optional limit on number of messages
     * @return list of MessageResponseDto
     */
    @GetMapping("/{conversationId}/messages")
    @Operation(summary = "Fetch messages", description = "Fetch messages from a conversation with pagination")
    public ResponseEntity<ResponseDto<List<MessageResponseDto>>> fetchMessages(
            @PathVariable UUID conversationId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime before,
            @RequestParam(required = false) Integer limit
    ) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/conversations/{}/messages | method=GET | userId={} | before={} | limit={}", 
                conversationId, userId, before, limit);

        List<MessageResponseDto> messages = conversationService.fetchMessages(
                conversationId,
                userId,
                before,
                limit
        );

        log.info("Messages fetched | conversationId={} | userId={} | messagesCount={} | status=SUCCESS", 
                conversationId, userId, messages.size());
        return ResponseEntity.ok(
                ResponseDto.success("Messages fetched successfully", messages)
        );
    }

    /**
     * Mark conversation as read
     *
     * @param conversationId the conversation to mark as read
     * @return ResponseDto with success message
     */
    @PatchMapping("/{conversationId}/read")
    @Operation(summary = "Mark as read", description = "Mark a conversation as read")
    public ResponseEntity<ResponseDto<Void>> markConversationAsRead(
            @PathVariable UUID conversationId
    ) throws AuthException {
        UUID currentUserId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/conversations/{}/read | method=PATCH | userId={}", conversationId, currentUserId);

        createAndManageConversationService.markConversationAsRead(conversationId, currentUserId);

        log.info("Conversation marked as read | conversationId={} | userId={} | status=SUCCESS", conversationId, currentUserId);
        return ResponseEntity.ok(
                ResponseDto.success("Conversation marked as read", null)
        );
    }

    /**
     * Mute a conversation
     *
     * @param conversationId the conversation to mute
     * @return ResponseDto with success message
     */
    @PostMapping("/{conversationId}/mute")
    @Operation(summary = "Mute conversation", description = "Mute notifications for a conversation")
    public ResponseEntity<ResponseDto<Void>> muteConversation(
            @PathVariable UUID conversationId
    ) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/conversations/{}/mute | method=POST | userId={}", conversationId, userId);

        createAndManageConversationService.muteConversation(conversationId, userId);

        log.info("Conversation muted | conversationId={} | userId={} | status=SUCCESS", conversationId, userId);
        return ResponseEntity.ok(
                ResponseDto.success("Conversation muted successfully", null)
        );
    }

    /**
     * Unmute a conversation
     *
     * @param conversationId the conversation to unmute
     * @return ResponseDto with success message
     */
    @PostMapping("/{conversationId}/unmute")
    @Operation(summary = "Unmute conversation", description = "Unmute notifications for a conversation")
    public ResponseEntity<ResponseDto<Void>> unmuteConversation(
            @PathVariable UUID conversationId
    ) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/conversations/{}/unmute | method=POST | userId={}", conversationId, userId);

        createAndManageConversationService.unmuteConversation(conversationId, userId);

        log.info("Conversation unmuted | conversationId={} | userId={} | status=SUCCESS", conversationId, userId);
        return ResponseEntity.ok(
                ResponseDto.success("Conversation unmuted successfully", null)
        );
    }

    /**
     * Block a conversation
     *
     * @param conversationId the conversation to block
     * @return ResponseDto with success message
     */
    @PostMapping("/{conversationId}/block")
    @Operation(summary = "Block conversation", description = "Block a user conversation")
    public ResponseEntity<ResponseDto<Void>> blockConversation(
            @PathVariable UUID conversationId
    ) throws AuthException {
        UUID currentUserId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/conversations/{}/block | method=POST | userId={}", conversationId, currentUserId);

        createAndManageConversationService.blockConversation(conversationId, currentUserId);

        log.info("Conversation blocked | conversationId={} | userId={} | status=SUCCESS", conversationId, currentUserId);
        return ResponseEntity.ok(
                ResponseDto.success("Conversation blocked successfully", null)
        );
    }

    /**
     * Unblock a conversation
     *
     * @param conversationId the conversation to unblock
     * @return ResponseDto with success message
     */
    @PostMapping("/{conversationId}/unblock")
    @Operation(summary = "Unblock conversation", description = "Unblock a previously blocked user conversation")
    public ResponseEntity<ResponseDto<Void>> unblockConversation(
            @PathVariable UUID conversationId
    ) throws AuthException {
        UUID currentUserId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/conversations/{}/unblock | method=POST | userId={}", conversationId, currentUserId);

        createAndManageConversationService.unblockConversation(conversationId, currentUserId);

        log.info("Conversation unblocked | conversationId={} | userId={} | status=SUCCESS", conversationId, currentUserId);
        return ResponseEntity.ok(
                ResponseDto.success("Conversation unblocked successfully", null)
        );
    }
}
