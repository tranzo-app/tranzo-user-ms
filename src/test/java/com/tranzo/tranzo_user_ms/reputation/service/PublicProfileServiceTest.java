package com.tranzo.tranzo_user_ms.reputation.service;

import com.tranzo.tranzo_user_ms.commons.exception.UserProfileNotFoundException;
import com.tranzo.tranzo_user_ms.user.dto.PublicProfileResponseDto;
import com.tranzo.tranzo_user_ms.user.dto.ReviewItemDto;
import com.tranzo.tranzo_user_ms.user.model.HostRatingEntity;
import com.tranzo.tranzo_user_ms.user.model.MemberRatingEntity;
import com.tranzo.tranzo_user_ms.user.repository.HostRatingRepository;
import com.tranzo.tranzo_user_ms.user.repository.MemberRatingRepository;
import com.tranzo.tranzo_user_ms.user.model.UserProfileEntity;
import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import com.tranzo.tranzo_user_ms.user.repository.UserProfileRepository;
import com.tranzo.tranzo_user_ms.user.service.PublicProfileService;
import com.tranzo.tranzo_user_ms.user.service.UserService;
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
@DisplayName("PublicProfileService Unit Tests")
class PublicProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private HostRatingRepository hostRatingRepository;

    @Mock
    private MemberRatingRepository memberRatingRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private PublicProfileService publicProfileService;

    private UUID userId;
    private UserProfileEntity profile;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        profile = new UserProfileEntity();
        profile.setUserProfileUuid(UUID.randomUUID());
        profile.setUser(new UsersEntity());
        profile.setFirstName("Jane");
        profile.setLastName("Doe");
        profile.setProfilePictureUrl("https://example.com/photo.jpg");
        profile.setBio("Travel lover");
        profile.setTrustScore(new BigDecimal("4.50"));
    }

    @Test
    @DisplayName("Should throw when profile not found")
    void getPublicProfile_NotFound() {
        when(userProfileRepository.findAllUserProfileDetailByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(UserProfileNotFoundException.class, () ->
                publicProfileService.getPublicProfile(userId, 0, 20));
    }

    @Test
    @DisplayName("Should return profile with trust score and empty reviews")
    void getPublicProfile_NoReviews() {
        when(userProfileRepository.findAllUserProfileDetailByUserId(userId)).thenReturn(Optional.of(profile));
        when(hostRatingRepository.findByHostUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of());
        when(memberRatingRepository.findByRatedUserIdAndVisibleAtIsNotNullOrderByCreatedAtDesc(userId))
                .thenReturn(List.of());
        when(userService.resolveProfilePictureUrl(any())).thenAnswer(inv -> inv.getArgument(0));

        PublicProfileResponseDto result = publicProfileService.getPublicProfile(userId, 0, 20);

        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals(new BigDecimal("4.50"), result.getTrustScore());
        assertTrue(result.getReviews().isEmpty());
        assertEquals(0, result.getTotalReviewCount());
    }

    @Test
    @DisplayName("Should return profile with host and member reviews")
    void getPublicProfile_WithReviews() {
        when(userProfileRepository.findAllUserProfileDetailByUserId(userId)).thenReturn(Optional.of(profile));
        when(userService.resolveProfilePictureUrl(any())).thenAnswer(inv -> inv.getArgument(0));
        HostRatingEntity hostRating = new HostRatingEntity();
        hostRating.setCoordinationRating(5);
        hostRating.setCommunicationRating(4);
        hostRating.setLeadershipRating(5);
        hostRating.setReviewText("Great host");
        hostRating.setCreatedAt(LocalDateTime.now());
        when(hostRatingRepository.findByHostUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(hostRating));
        MemberRatingEntity memberRating = new MemberRatingEntity();
        memberRating.setRatingScore(5);
        memberRating.setReviewText("Fun to travel with");
        memberRating.setVisibleAt(LocalDateTime.now());
        memberRating.setCreatedAt(LocalDateTime.now());
        when(memberRatingRepository.findByRatedUserIdAndVisibleAtIsNotNullOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(memberRating));

        PublicProfileResponseDto result = publicProfileService.getPublicProfile(userId, 0, 20);

        assertNotNull(result);
        assertEquals(2, result.getReviews().size());
        assertEquals(2, result.getTotalReviewCount());
        assertTrue(result.getReviews().stream().anyMatch(r -> r.getSource() == ReviewItemDto.ReviewSource.HOST));
        assertTrue(result.getReviews().stream().anyMatch(r -> r.getSource() == ReviewItemDto.ReviewSource.MEMBER));
    }

    @Test
    @DisplayName("Should paginate reviews")
    void getPublicProfile_Pagination() {
        when(userProfileRepository.findAllUserProfileDetailByUserId(userId)).thenReturn(Optional.of(profile));
        when(userService.resolveProfilePictureUrl(any())).thenAnswer(inv -> inv.getArgument(0));
        HostRatingEntity r1 = new HostRatingEntity();
        r1.setCoordinationRating(4);
        r1.setCommunicationRating(4);
        r1.setLeadershipRating(4);
        r1.setCreatedAt(LocalDateTime.now().minusDays(2));
        HostRatingEntity r2 = new HostRatingEntity();
        r2.setCoordinationRating(5);
        r2.setCommunicationRating(5);
        r2.setLeadershipRating(5);
        r2.setCreatedAt(LocalDateTime.now().minusDays(1));
        when(hostRatingRepository.findByHostUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(r1, r2));
        when(memberRatingRepository.findByRatedUserIdAndVisibleAtIsNotNullOrderByCreatedAtDesc(userId))
                .thenReturn(List.of());

        PublicProfileResponseDto page0 = publicProfileService.getPublicProfile(userId, 0, 1);
        PublicProfileResponseDto page1 = publicProfileService.getPublicProfile(userId, 1, 1);

        assertEquals(1, page0.getReviews().size());
        assertEquals(1, page1.getReviews().size());
        assertEquals(2, page0.getTotalReviewCount());
        assertEquals(2, page1.getTotalReviewCount());
    }
}
