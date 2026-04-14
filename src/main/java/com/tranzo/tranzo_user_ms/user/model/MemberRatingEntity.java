package com.tranzo.tranzo_user_ms.user.model;

import com.tranzo.tranzo_user_ms.user.enums.VibeTag;
import com.tranzo.tranzo_user_ms.trip.model.TripEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Participant-to-participant rating. One per (rater, rated) per trip.
 * visible_at is set when rated user has also submitted (blind anti-bias).
 */
@Entity
@Table(
        name = "member_rating",
        uniqueConstraints = @UniqueConstraint(columnNames = {"trip_id", "rater_user_id", "rated_user_id"}),
        indexes = {
                @Index(name = "idx_member_rating_trip_id", columnList = "trip_id"),
                @Index(name = "idx_member_rating_rater_user_id", columnList = "rater_user_id"),
                @Index(name = "idx_member_rating_rated_user_id", columnList = "rated_user_id"),
                @Index(name = "idx_member_rating_visible_at", columnList = "visible_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberRatingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false, foreignKey = @ForeignKey(name = "fk_member_rating_trip"))
    private TripEntity trip;

    @Column(name = "rater_user_id", nullable = false)
    private UUID raterUserId;

    @Column(name = "rated_user_id", nullable = false)
    private UUID ratedUserId;

    @Column(name = "rating_score", nullable = false)
    private Integer ratingScore; // 1-5

    @Enumerated(EnumType.STRING)
    @Column(name = "vibe_tag", length = 30)
    private VibeTag vibeTag;

    @Column(name = "review_text", columnDefinition = "TEXT")
    private String reviewText;

    @Column(name = "visible_at")
    private LocalDateTime visibleAt; // null until rated user has also submitted (blind)

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
}
