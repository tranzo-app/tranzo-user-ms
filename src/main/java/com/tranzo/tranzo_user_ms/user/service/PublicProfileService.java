package com.tranzo.tranzo_user_ms.user.service;

import com.tranzo.tranzo_user_ms.commons.exception.UserProfileNotFoundException;
import com.tranzo.tranzo_user_ms.user.dto.PublicProfileResponseDto;
import com.tranzo.tranzo_user_ms.user.dto.ReviewItemDto;
import com.tranzo.tranzo_user_ms.user.model.HostRatingEntity;
import com.tranzo.tranzo_user_ms.user.model.MemberRatingEntity;
import com.tranzo.tranzo_user_ms.user.repository.HostRatingRepository;
import com.tranzo.tranzo_user_ms.user.repository.MemberRatingRepository;
import com.tranzo.tranzo_user_ms.user.model.UserProfileEntity;
import com.tranzo.tranzo_user_ms.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PublicProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserService userService;
    private final HostRatingRepository hostRatingRepository;
    private final MemberRatingRepository memberRatingRepository;

    @Transactional(readOnly = true)
    public PublicProfileResponseDto getPublicProfile(UUID userId, int page, int size) {
        UserProfileEntity profile = userProfileRepository.findAllUserProfileDetailByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("User profile not found for id: " + userId));

        List<ReviewItemDto> allReviews = new ArrayList<>();
        for (HostRatingEntity r : hostRatingRepository.findByHostUserIdOrderByCreatedAtDesc(userId)) {
            double avg = (r.getCoordinationRating() + r.getCommunicationRating() + r.getLeadershipRating()) / 3.0;
            allReviews.add(ReviewItemDto.builder()
                    .reviewText(r.getReviewText())
                    .source(ReviewItemDto.ReviewSource.HOST)
                    .averageRating(avg)
                    .createdAt(r.getCreatedAt())
                    .build());
        }
        for (MemberRatingEntity r : memberRatingRepository.findByRatedUserIdAndVisibleAtIsNotNullOrderByCreatedAtDesc(userId)) {
            allReviews.add(ReviewItemDto.builder()
                    .reviewText(r.getReviewText())
                    .vibeTag(r.getVibeTag())
                    .source(ReviewItemDto.ReviewSource.MEMBER)
                    .averageRating(r.getRatingScore() != null ? r.getRatingScore().doubleValue() : null)
                    .createdAt(r.getCreatedAt())
                    .build());
        }
        allReviews.sort(Comparator.comparing(ReviewItemDto::getCreatedAt).reversed());

        int total = allReviews.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        List<ReviewItemDto> pagedReviews = from < total ? allReviews.subList(from, to) : List.of();

        String profilePictureUrl = userService.resolveProfilePictureUrl(profile.getProfilePictureUrl());
        return PublicProfileResponseDto.builder()
                .firstName(profile.getFirstName())
                .middleName(profile.getMiddleName())
                .lastName(profile.getLastName())
                .profilePictureUrl(profilePictureUrl)
                .bio(profile.getBio())
                .trustScore(profile.getTrustScore() != null ? profile.getTrustScore() : BigDecimal.ZERO)
                .reviews(pagedReviews)
                .totalReviewCount(total)
                .build();
    }
}
