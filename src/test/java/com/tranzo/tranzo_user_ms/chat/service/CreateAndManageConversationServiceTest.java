package com.tranzo.tranzo_user_ms.chat.service;

import com.tranzo.tranzo_user_ms.chat.dto.CreateConversationRequestDto;
import com.tranzo.tranzo_user_ms.chat.dto.CreateConversationResponseDto;
import com.tranzo.tranzo_user_ms.chat.dto.SendMessageRequestDto;
import com.tranzo.tranzo_user_ms.chat.dto.SendMessageResponseDto;
import com.tranzo.tranzo_user_ms.chat.enums.ConversationRole;
import com.tranzo.tranzo_user_ms.chat.exception.ConversationNotFoundException;
import com.tranzo.tranzo_user_ms.chat.model.*;
import com.tranzo.tranzo_user_ms.chat.repository.*;
import com.tranzo.tranzo_user_ms.commons.exception.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateAndManageConversationService Unit Tests")
class CreateAndManageConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ConversationParticipantRepository conversationParticipantRepository;

    @Mock
    private ConversationBlockRepository conversationBlockRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ConversationMuteRepository muteRepository;

    @InjectMocks
    private CreateAndManageConversationService service;

    private UUID userId;
    private UUID otherUserId;
    private UUID conversationId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
        conversationId = UUID.randomUUID();
    }

    @Test
    @DisplayName("createOneToOneConversation throws when user tries to create conversation with self")
    void testCreateOneToOneConversation_self_throws() {
        CreateConversationRequestDto req = new CreateConversationRequestDto();
        req.setOtherUserId(userId);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createOneToOneConversation(userId, req));
        assertTrue(ex.getMessage().contains("Cannot create a conversation with yourself"));
    }

    @Test
    @DisplayName("createOneToOneConversation returns existing when repository finds one")
    void testCreateOneToOneConversation_existing() {
        CreateConversationRequestDto req = new CreateConversationRequestDto();
        req.setOtherUserId(otherUserId);

        ConversationEntity conv = ConversationEntity.createOneToOneChat(userId);
        conv.addParticipant(userId, ConversationRole.MEMBER);
        conv.addParticipant(otherUserId, ConversationRole.MEMBER);

        when(conversationRepository.findOneToOneConversationBetweenUsers(userId, otherUserId))
                .thenReturn(Optional.of(conv));

        CreateConversationResponseDto resp = service.createOneToOneConversation(userId, req);

        assertNotNull(resp);
        assertTrue(resp.isExisting());
        assertEquals(conv.getConversationId(), resp.getConversationId());
    }

    @Test
    @DisplayName("createOneToOneConversation creates new conversation when none exists")
    void testCreateOneToOneConversation_new() {
        CreateConversationRequestDto req = new CreateConversationRequestDto();
        req.setOtherUserId(otherUserId);

        when(conversationRepository.findOneToOneConversationBetweenUsers(userId, otherUserId))
                .thenReturn(Optional.empty());

        when(conversationRepository.save(any(ConversationEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(messageRepository.save(any(MessageEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateConversationResponseDto resp = service.createOneToOneConversation(userId, req);

        assertNotNull(resp);
        assertFalse(resp.isExisting());
        assertNotNull(resp.getConversationId());
        verify(conversationRepository, times(1)).save(any(ConversationEntity.class));
        verify(messageRepository, times(1)).save(any(MessageEntity.class));
    }

    @Test
    @DisplayName("sendMessage throws when conversation not found")
    void testSendMessage_conversationNotFound() {
        when(conversationRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        SendMessageRequestDto req = SendMessageRequestDto.builder().content("hi").build();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.sendMessage(conversationId, userId, req));
        assertTrue(ex.getMessage().contains("Conversation not found"));
    }

    @Test
    @DisplayName("sendMessage throws when user not participant")
    void testSendMessage_userNotParticipant() {
        ConversationEntity conv = ConversationEntity.createOneToOneChat(userId);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conv));
        when(conversationParticipantRepository.findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(conversationId, userId))
                .thenReturn(Optional.empty());

        SendMessageRequestDto req = SendMessageRequestDto.builder().content("hello").build();
        assertThrows(ConversationNotFoundException.class,
                () -> service.sendMessage(conversationId, userId, req));
    }

    @Test
    @DisplayName("markConversationAsRead throws when participant not found")
    void testMarkConversationAsRead_noParticipant() {
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(ConversationEntity.createOneToOneChat(userId)));
        when(conversationParticipantRepository.findByConversation_ConversationIdAndUserId(conversationId, userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.markConversationAsRead(conversationId, userId));
    }

    @Test
    @DisplayName("markConversationAsRead marks participant as read")
    void testMarkConversationAsRead_success() {
        ConversationEntity conv = ConversationEntity.createOneToOneChat(userId);
        ConversationParticipantEntity participant = ConversationParticipantEntity.create(conv, userId, ConversationRole.MEMBER);

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conv));
        when(conversationParticipantRepository.findByConversation_ConversationIdAndUserId(conversationId, userId))
                .thenReturn(Optional.of(participant));

        service.markConversationAsRead(conversationId, userId);
        assertNotNull(participant.getLastReadAt());
    }

    @Test
    @DisplayName("blockConversation throws when conversation not found")
    void testBlockConversation_noConversation() {
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());
        assertThrows(ConversationNotFoundException.class, () -> service.blockConversation(conversationId, userId));
    }

    @Test
    @DisplayName("blockConversation throws when not one-on-one")
    void testBlockConversation_notOneOnOne() {
        ConversationEntity conv = ConversationEntity.createGroup(userId);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conv));

        assertThrows(ForbiddenException.class, () -> service.blockConversation(conversationId, userId));
    }

    @Test
    @DisplayName("blockConversation saves block when valid")
    void testBlockConversation_success() {
        ConversationEntity conv = ConversationEntity.createOneToOneChat(userId);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conv));
        when(conversationParticipantRepository.findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(conversationId, userId))
                .thenReturn(Optional.of(ConversationParticipantEntity.create(conv, userId, ConversationRole.MEMBER)));

        when(conversationBlockRepository.existsByConversation_ConversationIdAndBlockedBy(conversationId, userId)).thenReturn(false);
        when(conversationBlockRepository.save(any(ConversationBlockEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.blockConversation(conversationId, userId);
        verify(conversationBlockRepository, times(1)).save(any(ConversationBlockEntity.class));
    }

    @Test
    @DisplayName("blockConversation returns early when already blocked")
    void testBlockConversation_alreadyBlocked() {
        ConversationEntity conv = ConversationEntity.createOneToOneChat(userId);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conv));
        when(conversationParticipantRepository.findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(conversationId, userId))
                .thenReturn(Optional.of(ConversationParticipantEntity.create(conv, userId, ConversationRole.MEMBER)));

        when(conversationBlockRepository.existsByConversation_ConversationIdAndBlockedBy(conversationId, userId)).thenReturn(true);

        service.blockConversation(conversationId, userId);
        verify(conversationBlockRepository, never()).save(any(ConversationBlockEntity.class));
    }

    @Test
    @DisplayName("blockConversation throws when participant is missing")
    void testBlockConversation_participantMissing() {
        ConversationEntity conv = ConversationEntity.createOneToOneChat(userId);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conv));
        when(conversationParticipantRepository.findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(conversationId, userId))
                .thenReturn(Optional.empty());

        assertThrows(ConversationNotFoundException.class, () -> service.blockConversation(conversationId, userId));
    }

    @Test
    @DisplayName("unblockConversation throws when block entry not found")
    void testUnblockConversation_blockMissing() {
        ConversationEntity conv = ConversationEntity.createOneToOneChat(userId);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conv));
        when(conversationParticipantRepository.findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(conversationId, userId))
                .thenReturn(Optional.of(ConversationParticipantEntity.create(conv, userId, ConversationRole.MEMBER)));

        when(conversationBlockRepository.findByConversation_ConversationIdAndBlockedBy(conversationId, userId))
                .thenReturn(Optional.empty());

        assertThrows(ConversationNotFoundException.class, () -> service.unblockConversation(conversationId, userId));
    }

    @Test
    @DisplayName("unblockConversation deletes block when present")
    void testUnblockConversation_success() {
        ConversationEntity conv = ConversationEntity.createOneToOneChat(userId);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conv));
        when(conversationParticipantRepository.findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(conversationId, userId))
                .thenReturn(Optional.of(ConversationParticipantEntity.create(conv, userId, ConversationRole.MEMBER)));

        ConversationBlockEntity block = ConversationBlockEntity.create(conv, userId);
        when(conversationBlockRepository.findByConversation_ConversationIdAndBlockedBy(conversationId, userId))
                .thenReturn(Optional.of(block));

        doNothing().when(conversationBlockRepository).delete(block);
        service.unblockConversation(conversationId, userId);
        verify(conversationBlockRepository, times(1)).delete(block);
    }

    @Test
    @DisplayName("unblockConversation throws when conversation is not one-on-one")
    void testUnblockConversation_notOneOnOne() {
        ConversationEntity conv = ConversationEntity.createGroup(userId);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conv));

        assertThrows(ForbiddenException.class, () -> service.unblockConversation(conversationId, userId));
    }

    @Test
    @DisplayName("muteConversation saves mute when not already muted")
    void testMuteConversation_success() {
        ConversationEntity conv = ConversationEntity.createOneToOneChat(userId);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conv));
        when(conversationParticipantRepository.findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(conversationId, userId))
                .thenReturn(Optional.of(ConversationParticipantEntity.create(conv, userId, ConversationRole.MEMBER)));

        when(muteRepository.existsByConversation_ConversationIdAndUserId(conversationId, userId)).thenReturn(false);
        when(muteRepository.save(any(ConversationMuteEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.muteConversation(conversationId, userId);
        verify(muteRepository, times(1)).save(any(ConversationMuteEntity.class));
    }

    @Test
    @DisplayName("muteConversation returns early when already muted")
    void testMuteConversation_alreadyMuted() {
        ConversationEntity conv = ConversationEntity.createOneToOneChat(userId);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conv));
        when(conversationParticipantRepository.findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(conversationId, userId))
                .thenReturn(Optional.of(ConversationParticipantEntity.create(conv, userId, ConversationRole.MEMBER)));

        when(muteRepository.existsByConversation_ConversationIdAndUserId(conversationId, userId)).thenReturn(true);
        service.muteConversation(conversationId, userId);
        verify(muteRepository, never()).save(any(ConversationMuteEntity.class));
    }

    @Test
    @DisplayName("unmuteConversation deletes mute when present")
    void testUnmuteConversation_success() {
        ConversationEntity conv = ConversationEntity.createOneToOneChat(userId);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conv));
        when(conversationParticipantRepository.findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(conversationId, userId))
                .thenReturn(Optional.of(ConversationParticipantEntity.create(conv, userId, ConversationRole.MEMBER)));

        ConversationMuteEntity mute = ConversationMuteEntity.create(conv, userId);
        when(muteRepository.findByConversation_ConversationIdAndUserId(conversationId, userId)).thenReturn(Optional.of(mute));

        doNothing().when(muteRepository).delete(mute);
        service.unmuteConversation(conversationId, userId);
        verify(muteRepository, times(1)).delete(mute);
    }

    @Test
    @DisplayName("unmuteConversation is no-op when mute not present")
    void testUnmuteConversation_noMutePresent() {
        ConversationEntity conv = ConversationEntity.createOneToOneChat(userId);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conv));
        when(conversationParticipantRepository.findByConversation_ConversationIdAndUserIdAndLeftAtIsNull(conversationId, userId))
                .thenReturn(Optional.of(ConversationParticipantEntity.create(conv, userId, ConversationRole.MEMBER)));

        when(muteRepository.findByConversation_ConversationIdAndUserId(conversationId, userId)).thenReturn(Optional.empty());
        service.unmuteConversation(conversationId, userId);
        verify(muteRepository, never()).delete(any(ConversationMuteEntity.class));
    }

    @Test
    @DisplayName("markConversationAsRead throws when conversation not found")
    void testMarkConversationAsRead_conversationNotFound() {
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.markConversationAsRead(conversationId, userId));
    }

}
