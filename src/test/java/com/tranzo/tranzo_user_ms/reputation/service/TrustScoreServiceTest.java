package com.tranzo.tranzo_user_ms.reputation.service;

import com.tranzo.tranzo_user_ms.user.model.HostRatingEntity;
import com.tranzo.tranzo_user_ms.user.model.MemberRatingEntity;
import com.tranzo.tranzo_user_ms.user.repository.HostRatingRepository;
import com.tranzo.tranzo_user_ms.user.repository.MemberRatingRepository;
import com.tranzo.tranzo_user_ms.user.model.UserProfileEntity;
import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import com.tranzo.tranzo_user_ms.user.repository.UserProfileRepository;
import com.tranzo.tranzo_user_ms.user.service.TrustScoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrustScoreService Unit Tests")
class TrustScoreServiceTest {

    @Mock
    private HostRatingRepository hostRatingRepository;

    @Mock
    private MemberRatingRepository memberRatingRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private TrustScoreService trustScoreService;

    private UUID userId;
    private UserProfileEntity profile;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        profile = new UserProfileEntity();
        profile.setUserProfileUuid(UUID.randomUUID());
        profile.setUser(new UsersEntity());
        profile.setFirstName("Test");
        profile.setLastName("User");
    }

    @Test
    @DisplayName("Should skip update when profile not found")
    void updateTrustScore_NoProfile() {
        when(userProfileRepository.findAllUserProfileDetailByUserId(userId)).thenReturn(Optional.empty());

        trustScoreService.updateTrustScore(userId);

        verify(userProfileRepository).findAllUserProfileDetailByUserId(userId);
        verify(hostRatingRepository, never()).findByHostUserIdOrderByCreatedAtDesc(any());
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should set trust score from host and member ratings")
    void updateTrustScore_WithRatings() {
        when(userProfileRepository.findAllUserProfileDetailByUserId(userId)).thenReturn(Optional.of(profile));
        HostRatingEntity hostRating = new HostRatingEntity();
        hostRating.setCoordinationRating(4);
        hostRating.setCommunicationRating(5);
        hostRating.setLeadershipRating(4);
        when(hostRatingRepository.findByHostUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(hostRating));
        MemberRatingEntity memberRating = new MemberRatingEntity();
        memberRating.setRatingScore(5);
        memberRating.setVisibleAt(LocalDateTime.now());
        when(memberRatingRepository.findByRatedUserIdAndVisibleAtIsNotNullOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(memberRating));

        trustScoreService.updateTrustScore(userId);

        verify(userProfileRepository).save(profile);
        assertNotNull(profile.getTrustScore());
        assertTrue(profile.getTrustScore().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(profile.getTrustScoreUpdatedAt());
    }

    @Test
    @DisplayName("Should set zero trust score when no ratings")
    void updateTrustScore_NoRatings() {
        when(userProfileRepository.findAllUserProfileDetailByUserId(userId)).thenReturn(Optional.of(profile));
        when(hostRatingRepository.findByHostUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of());
        when(memberRatingRepository.findByRatedUserIdAndVisibleAtIsNotNullOrderByCreatedAtDesc(userId))
                .thenReturn(List.of());

        trustScoreService.updateTrustScore(userId);

        verify(userProfileRepository).save(profile);
        assertEquals(BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP), profile.getTrustScore());
    }
}
