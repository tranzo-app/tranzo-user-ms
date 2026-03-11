package com.tranzo.tranzo_user_ms.trip.model;

import com.tranzo.tranzo_user_ms.trip.enums.JoinRequestSource;
import com.tranzo.tranzo_user_ms.trip.enums.JoinRequestStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "trip_join_requests",
        // Removed unique constraint because if a request gets REJECTED/CANCELLED user won't be able to create new request
        indexes = {
                @Index(name = "idx_join_requests_trip_status", columnList = "trip_id, status"),
                @Index(name = "idx_join_requests_trip_user", columnList = "trip_id, user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TripJoinRequestEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "request_id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private TripEntity trip;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private JoinRequestSource source;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private JoinRequestStatus status;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
