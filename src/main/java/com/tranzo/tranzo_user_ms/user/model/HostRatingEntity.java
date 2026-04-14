package com.tranzo.tranzo_user_ms.user.model;

import com.tranzo.tranzo_user_ms.trip.model.TripEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Rating of the host (coordination, communication, leadership).
 * One per rater per trip. Includes optional text review.
 */
@Entity
@Table(
        name = "host_rating",
        uniqueConstraints = @UniqueConstraint(columnNames = {"trip_id", "rater_user_id"}),
        indexes = {
                @Index(name = "idx_host_rating_trip_id", columnList = "trip_id"),
                @Index(name = "idx_host_rating_host_user_id", columnList = "host_user_id"),
                @Index(name = "idx_host_rating_rater_user_id", columnList = "rater_user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HostRatingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false, foreignKey = @ForeignKey(name = "fk_host_rating_trip"))
    private TripEntity trip;

    @Column(name = "host_user_id", nullable = false)
    private UUID hostUserId;

    @Column(name = "rater_user_id", nullable = false)
    private UUID raterUserId;

    @Column(name = "coordination_rating", nullable = false)
    private Integer coordinationRating; // 1-5

    @Column(name = "communication_rating", nullable = false)
    private Integer communicationRating; // 1-5

    @Column(name = "leadership_rating", nullable = false)
    private Integer leadershipRating; // 1-5

    @Column(name = "review_text", columnDefinition = "TEXT")
    private String reviewText;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
}
