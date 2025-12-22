package com.tranzo.tranzo_user_ms.trip.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "trip_policies")
@Getter
@Setter
public class TripPolicy {

    @Id
    @Column(name = "trip_id", nullable = false)
    private UUID tripId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "trip_id")
    private TripEntity trip;

    @Column(name = "cancellation_policy", length = 500)
    private String cancellationPolicy;

    @Column(name = "refund_policy", length = 500)
    private String refundPolicy;
}
