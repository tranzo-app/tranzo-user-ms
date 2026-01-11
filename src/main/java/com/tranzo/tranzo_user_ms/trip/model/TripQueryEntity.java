package com.tranzo.tranzo_user_ms.trip.model;

import com.tranzo.tranzo_user_ms.trip.enums.TripQueryVisibility;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "trip_queries",
        indexes = {
                @Index(name = "idx_queries_trip", columnList = "trip_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TripQueryEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "query_id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID queryId;

    @Column(name = "trip_id", nullable = false)
    private UUID tripId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false, insertable = false, updatable = false)
    private TripEntity trip;

    @Column(name = "asked_by", nullable = false)
    private UUID askedBy;

    @Column(name = "question", nullable = false)
    private String question;

    @Column(name = "answer")
    private String answer;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 30)
    private TripQueryVisibility visibility;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;
}
