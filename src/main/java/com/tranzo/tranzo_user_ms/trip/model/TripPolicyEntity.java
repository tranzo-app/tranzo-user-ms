package com.tranzo.tranzo_user_ms.trip.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "trip_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TripPolicyEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "trip_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID tripPolicyId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "trip_id")
    private TripEntity trip;

    @Column(name = "cancellation_policy", length = 500)
    private String cancellationPolicy;

    @Column(name = "refund_policy", length = 500)
    private String refundPolicy;
}
