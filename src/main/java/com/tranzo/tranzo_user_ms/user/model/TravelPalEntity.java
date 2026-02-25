package com.tranzo.tranzo_user_ms.user.model;

import com.tranzo.tranzo_user_ms.user.enums.TravelPalStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "travel_pal",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_travel_pal_unique_pair",
                columnNames = {"user_low_id", "user_high_id"}
        ),
        indexes = {
                @Index(name = "idx_user_low", columnList = "user_low_id"),
                @Index(name = "idx_user_high", columnList = "user_high_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TravelPalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(name = "travel_pal_uuid", nullable = false)
    private UUID travelPalUuid;

    @Column(name = "user_low_id", nullable = false)
    private UUID userLowId;

    @Column(name = "user_high_id", nullable = false)
    private UUID userHighId;

    @Column(name = "requested_by", nullable = false)
    private UUID requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TravelPalStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}