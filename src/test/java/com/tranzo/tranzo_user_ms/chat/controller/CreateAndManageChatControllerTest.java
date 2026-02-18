package com.tranzo.tranzo_user_ms.chat.controller;

import com.tranzo.tranzo_user_ms.chat.dto.*;
import com.tranzo.tranzo_user_ms.chat.service.ConversationService;
import com.tranzo.tranzo_user_ms.chat.service.CreateAndManageConversationService;
import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateAndManageChatController Unit Tests")
class CreateAndManageChatControllerTest {

    @Mock
    private CreateAndManageConversationService createAndManageConversationService;

    @Mock
    private ConversationService conversationService;

    @InjectMocks
    private CreateAndManageChatController controller;

    private UUID userId;
    private UUID conversationId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        conversationId = UUID.randomUUID();
    }

    @Test
    @DisplayName("sendMessage returns 201 and payload")
    void testSendMessage() throws Exception {
        SendMessageRequestDto request = SendMessageRequestDto.builder().content("hello").build();
        SendMessageResponseDto resp = new SendMessageResponseDto(UUID.randomUUID(), conversationId, userId, "hello", LocalDateTime.now());

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);
            when(createAndManageConversationService.sendMessage(eq(conversationId), eq(userId), any(SendMessageRequestDto.class)))
                    .thenReturn(resp);

            ResponseEntity<ResponseDto<SendMessageResponseDto>> response = controller.sendMessage(conversationId, request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(resp.getMessageId(), response.getBody().getData().getMessageId());
        }
    }

    @Test
    @DisplayName("createConversation returns 200 and payload")
    void testCreateConversation() throws Exception {
        CreateConversationRequestDto req = new CreateConversationRequestDto();
        req.setOtherUserId(UUID.randomUUID());

        CreateConversationResponseDto resp = CreateConversationResponseDto.builder()
                .conversationId(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .existing(false)
                .build();

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);
            when(createAndManageConversationService.createOneToOneConversation(eq(userId), any(CreateConversationRequestDto.class)))
                    .thenReturn(resp);

            ResponseEntity<ResponseDto<CreateConversationResponseDto>> response = controller.createConversation(req);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(resp.getConversationId(), response.getBody().getData().getConversationId());
        }
    }

    @Test
    @DisplayName("getMyConversations returns list from service")
    void testGetMyConversations() throws Exception {
        ChatListItemDto item = new ChatListItemDto(UUID.randomUUID(), null, "hi", LocalDateTime.now(), false, 0L);
        List<ChatListItemDto> list = List.of(item);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);
            when(conversationService.getMyConversations(eq(userId))).thenReturn(list);

            ResponseEntity<ResponseDto<List<ChatListItemDto>>> response = controller.getMyConversations();
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getData().size());
        }
    }

    @Test
    @DisplayName("fetchMessages delegates to service and returns messages")
    void testFetchMessages() throws Exception {
        MessageResponseDto m = new MessageResponseDto(UUID.randomUUID(), conversationId, userId, "hi", LocalDateTime.now());
        List<MessageResponseDto> messages = List.of(m);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);
            when(conversationService.fetchMessages(eq(conversationId), eq(userId), any(), any())).thenReturn(messages);

            ResponseEntity<ResponseDto<List<MessageResponseDto>>> response = controller.fetchMessages(conversationId, null, null);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getData().size());
        }
    }

    @Test
    @DisplayName("markConversationAsRead calls service")
    void testMarkConversationAsRead() throws Exception {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);
            doNothing().when(createAndManageConversationService).markConversationAsRead(eq(conversationId), eq(userId));

            ResponseEntity<ResponseDto<Void>> response = controller.markConversationAsRead(conversationId);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(createAndManageConversationService, times(1)).markConversationAsRead(eq(conversationId), eq(userId));
        }
    }

    @Test
    @DisplayName("mute/unmute/block/unblock endpoints call service")
    void testOtherEndpoints() throws Exception {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            doNothing().when(createAndManageConversationService).muteConversation(eq(conversationId), eq(userId));
            ResponseEntity<ResponseDto<Void>> muteResp = controller.muteConversation(conversationId);
            assertEquals(HttpStatus.OK, muteResp.getStatusCode());
            verify(createAndManageConversationService).muteConversation(eq(conversationId), eq(userId));

            doNothing().when(createAndManageConversationService).unmuteConversation(eq(conversationId), eq(userId));
            ResponseEntity<ResponseDto<Void>> unmuteResp = controller.unmuteConversation(conversationId);
            assertEquals(HttpStatus.OK, unmuteResp.getStatusCode());
            verify(createAndManageConversationService).unmuteConversation(eq(conversationId), eq(userId));

            doNothing().when(createAndManageConversationService).blockConversation(eq(conversationId), eq(userId));
            ResponseEntity<ResponseDto<Void>> blockResp = controller.blockConversation(conversationId);
            assertEquals(HttpStatus.OK, blockResp.getStatusCode());
            verify(createAndManageConversationService).blockConversation(eq(conversationId), eq(userId));

            doNothing().when(createAndManageConversationService).unblockConversation(eq(conversationId), eq(userId));
            ResponseEntity<ResponseDto<Void>> unblockResp = controller.unblockConversation(conversationId);
            assertEquals(HttpStatus.OK, unblockResp.getStatusCode());
            verify(createAndManageConversationService).unblockConversation(eq(conversationId), eq(userId));
        }
    }
}
