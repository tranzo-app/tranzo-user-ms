package com.tranzo.tranzo_user_ms.trip.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "trip_wishlists",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"trip_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_wishlist_user_created", columnList = "user_id, created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TripWishlistEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(name = "trip_wishlist_id", nullable = false, updatable = false)
    private UUID tripWishlistId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private TripEntity trip;

    // Logical foreign key
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
