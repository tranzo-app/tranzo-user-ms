package com.tranzo.tranzo_user_ms.trip.model;

import com.tranzo.tranzo_user_ms.trip.enums.TripReportStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "trip_reports",
        indexes = {
                @Index(name = "idx_trip_reports_trip", columnList = "trip_id"),
                @Index(name = "idx_trip_reports_status", columnList = "status")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"trip_id", "reported_by"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class TripReportEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "report_id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID reportId;

<<<<<<< HEAD
    @Column(name = "trip_id", nullable = false, updatable = false, insertable = false)
    private UUID tripId;

=======
>>>>>>> c7a5e61799d77c0f6ab9fba4db1e45fa877221ea
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private TripEntity trip;

    @Column(name = "reported_by", nullable = false)
    private UUID reportedBy;

    @Column(name = "reason", nullable = false)
    private String reportReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TripReportStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
