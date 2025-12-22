package com.tranzo.tranzo_user_ms.trip.model;

import com.tranzo.tranzo_user_ms.trip.enums.TripMemberRole;
import com.tranzo.tranzo_user_ms.trip.enums.TripMemberStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "trip_members",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"trip_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_members_trip_status", columnList = "trip_id, status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TripMemberEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "membership_id", nullable = false, updatable = false)
    private UUID membershipId;

    @Column(name = "trip_id", nullable = false)
    private UUID tripId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private TripMemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TripMemberStatus status;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Column(name = "exited_at")
    private LocalDateTime exitedAt;

    @Column(name = "exited_by")
    private UUID exitedBy;

    @Column(name = "removal_reason")
    private String removalReason;
}
