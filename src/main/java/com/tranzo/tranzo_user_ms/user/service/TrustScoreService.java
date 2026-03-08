package com.tranzo.tranzo_user_ms.user.service;

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
import java.time.LocalDateTime;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

/**
 * Computes and persists user trust score from HostRating and MemberRating (visible only).
 * Formula: 0.5 * hostScore + 0.5 * memberScore, scale 0-5.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TrustScoreService {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
    private static final BigDecimal HALF = new BigDecimal("0.5");

    private final HostRatingRepository hostRatingRepository;
    private final MemberRatingRepository memberRatingRepository;
    private final UserProfileRepository userProfileRepository;

    @Transactional
    public void updateTrustScore(UUID userId) {
        UserProfileEntity profile = userProfileRepository.findAllUserProfileDetailByUserId(userId).orElse(null);
        if (profile == null) {
            log.debug("No profile for user {}, skipping trust score update", userId);
            return;
        }
        BigDecimal hostScore = computeHostScore(userId);
        BigDecimal memberScore = computeMemberScore(userId);
        BigDecimal trustScore = HALF.multiply(hostScore).add(HALF.multiply(memberScore)).setScale(SCALE, ROUNDING);
        profile.setTrustScore(trustScore);
        profile.setTrustScoreUpdatedAt(LocalDateTime.now());
        userProfileRepository.save(profile);
        log.debug("Updated trust score for user {} to {}", userId, trustScore);
    }

    private BigDecimal computeHostScore(UUID userId) {
        List<HostRatingEntity> list = hostRatingRepository.findByHostUserIdOrderByCreatedAtDesc(userId);
        if (list.isEmpty()) {
            return BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        }
        BigDecimal sum = list.stream()
                .map(r -> new BigDecimal(r.getCoordinationRating() + r.getCommunicationRating() + r.getLeadershipRating())
                        .divide(new BigDecimal(3), SCALE, ROUNDING))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(new BigDecimal(list.size()), SCALE, ROUNDING);
    }

    private BigDecimal computeMemberScore(UUID userId) {
        List<MemberRatingEntity> list = memberRatingRepository.findByRatedUserIdAndVisibleAtIsNotNullOrderByCreatedAtDesc(userId);
        if (list.isEmpty()) {
            return BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        }
        BigDecimal sum = list.stream()
                .map(r -> BigDecimal.valueOf(r.getRatingScore()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(new BigDecimal(list.size()), SCALE, ROUNDING);
    }
}
