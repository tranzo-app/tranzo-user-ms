package com.tranzo.tranzo_user_ms.trip.model;

import com.tranzo.tranzo_user_ms.trip.enums.InviteSource;
import com.tranzo.tranzo_user_ms.trip.enums.InviteStatus;
import com.tranzo.tranzo_user_ms.trip.enums.InviteType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "trip_invites",
        indexes = {
                @Index(name = "idx_trip_invite_trip", columnList = "trip_id"),
                @Index(name = "idx_trip_invite_status", columnList = "status"),
                @Index(name = "idx_trip_invite_token", columnList = "token_hash")
        },
        uniqueConstraints = {@UniqueConstraint(name = "UniqueTripIdAndInvitedUserId", columnNames = {"trip_id", "invited_user_id"}),
                             @UniqueConstraint(name = "UniqueTripIdAndInvitedEmail", columnNames = {"trip_id", "invited_email"}),
                             @UniqueConstraint(name = "UniqueTripIdAndInvitedPhone", columnNames = {"trip_id", "invited_phone"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TripInviteEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "invite_id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID inviteId;

<<<<<<< HEAD
    @Column(name = "trip_id", nullable = false, updatable = false, insertable = false)
    private UUID tripId;

=======
>>>>>>> c7a5e61799d77c0f6ab9fba4db1e45fa877221ea
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private TripEntity trip;

    @Column(name = "invited_by", nullable = false)
    private UUID invitedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "invite_type", nullable = false, length = 20)
    private InviteType inviteType;

    @Enumerated(EnumType.STRING)
    @Column(name = "invite_source", nullable = false, length = 30)
    private InviteSource inviteSource;

    @Column(name = "invited_user_id")
    private UUID invitedUserId;

    @Column(name = "invited_email", length = 255)
    private String invitedEmail;

    @Column(name = "invited_phone", length = 30)
    private String invitedPhone;

    @Column(name = "token_hash", unique = true)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InviteStatus status;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "last_reminded_at")
    private LocalDateTime lastRemindedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
