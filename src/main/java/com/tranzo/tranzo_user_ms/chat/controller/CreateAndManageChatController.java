package com.tranzo.tranzo_user_ms.chat.controller;

import com.tranzo.tranzo_user_ms.chat.dto.*;
import com.tranzo.tranzo_user_ms.chat.model.MessageEntity;
import com.tranzo.tranzo_user_ms.chat.service.ConversationService;
import com.tranzo.tranzo_user_ms.chat.service.CreateAndManageConversationService;
import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
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


@RestController
@RequestMapping("/conversations")
@Slf4j
@RequiredArgsConstructor
public class CreateAndManageChatController {

    private final CreateAndManageConversationService createAndManageConversationService;
    private final ConversationService conversationService;

    @PostMapping("/{conversationId}/send-message")
    public ResponseEntity<ResponseDto<SendMessageResponseDto>> sendMessage(@PathVariable UUID conversationId, @Valid @RequestBody SendMessageRequestDto request)throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        SendMessageResponseDto sendMessageResponseDto = createAndManageConversationService.sendMessage(conversationId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.success("Message sent", sendMessageResponseDto));
    }

    @PostMapping("/one-to-one")
    public ResponseEntity<ResponseDto<CreateConversationResponseDto>> createConversation(@Valid @RequestBody CreateConversationRequestDto request)throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Create conversation request by userId={} with otherUserId={}", userId, request.getOtherUserId());
        CreateConversationResponseDto response = createAndManageConversationService.createOneToOneConversation(userId, request);
        return ResponseEntity.ok(ResponseDto.success("Conversation created successfully", response));
    }

    @GetMapping("/chat-list")
    public ResponseEntity<ResponseDto<List<ChatListItemDto>>> getMyConversations() throws AuthException {

        UUID currentUserId = SecurityUtils.getCurrentUserUuid();
        List<ChatListItemDto> chatList = conversationService.getMyConversations(currentUserId);

        return ResponseEntity.ok(
                ResponseDto.success("Chat list fetched successfully", chatList)
        );
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<ResponseDto<List<MessageResponseDto>>> fetchMessages(
            @PathVariable UUID conversationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime before,
            @RequestParam(required = false) Integer limit
    )throws AuthException{
        UUID userId = SecurityUtils.getCurrentUserUuid();
        List<MessageResponseDto> messages = conversationService.fetchMessages(conversationId, userId, before, limit);
        return ResponseEntity.ok(ResponseDto.success("Messages fetched successfully", messages));
    }


    @PatchMapping("/{conversationId}/read")
    public ResponseEntity<ResponseDto<Void>> markConversationAsRead(@PathVariable UUID conversationId) throws AuthException{
        UUID currentUserId = SecurityUtils.getCurrentUserUuid();
        log.info("Mark conversation as read: conversationId={}, userId={}", conversationId, currentUserId);
        createAndManageConversationService.markConversationAsRead(conversationId, currentUserId);
        return ResponseEntity.ok(ResponseDto.success("Conversation marked as read", null));
    }

    @PostMapping("/{conversationId}/mute")
    public ResponseEntity<ResponseDto<Void>> muteConversation(
            @PathVariable UUID conversationId
    ) throws AuthException{

        UUID userId = SecurityUtils.getCurrentUserUuid();
        createAndManageConversationService.muteConversation(conversationId, userId);

        return ResponseEntity.ok(
                ResponseDto.success("Conversation muted successfully", null)
        );
    }

    @PostMapping("/{conversationId}/unmute")
    public ResponseEntity<ResponseDto<Void>> unmuteConversation(
            @PathVariable UUID conversationId
    )throws AuthException {

        UUID userId = SecurityUtils.getCurrentUserUuid();
        createAndManageConversationService.unmuteConversation(conversationId, userId);

        return ResponseEntity.ok(
                ResponseDto.success("Conversation unmuted successfully", null)
        );
    }

    @PostMapping("/{conversationId}/block")
    public ResponseEntity<ResponseDto<Void>> blockConversation(
            @PathVariable UUID conversationId
    ) throws AuthException {

        UUID currentUserId = SecurityUtils.getCurrentUserUuid();

        createAndManageConversationService.blockConversation(
                conversationId,
                currentUserId
        );

        return ResponseEntity.ok(
                ResponseDto.success("Conversation blocked successfully", null)
        );
    }



    @PostMapping("/{conversationId}/unblock")
    public ResponseEntity<ResponseDto<Void>> unblockConversation(
            @PathVariable UUID conversationId
    ) throws AuthException {

        UUID currentUserId = SecurityUtils.getCurrentUserUuid();

        createAndManageConversationService.unblockConversation(
                conversationId,
                currentUserId
        );

        return ResponseEntity.ok(
                ResponseDto.success("Conversation unblocked successfully", null)
        );
    }

}
