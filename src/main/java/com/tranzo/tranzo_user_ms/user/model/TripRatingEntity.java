package com.tranzo.tranzo_user_ms.user.model;

import com.tranzo.tranzo_user_ms.trip.model.TripEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Rating of the trip experience (destination, itinerary, overall).
 * One per user per trip (idempotency).
 */
@Entity
@Table(
        name = "trip_rating",
        uniqueConstraints = @UniqueConstraint(columnNames = {"trip_id", "rater_user_id"}),
        indexes = {
                @Index(name = "idx_trip_rating_trip_id", columnList = "trip_id"),
                @Index(name = "idx_trip_rating_rater_user_id", columnList = "rater_user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripRatingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false, foreignKey = @ForeignKey(name = "fk_trip_rating_trip"))
    private TripEntity trip;

    @Column(name = "rater_user_id", nullable = false)
    private UUID raterUserId;

    @Column(name = "destination_rating", nullable = false)
    private Integer destinationRating; // 1-5

    @Column(name = "itinerary_rating", nullable = false)
    private Integer itineraryRating; // 1-5

    @Column(name = "overall_rating", nullable = false)
    private Integer overallRating; // 1-5

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
}
