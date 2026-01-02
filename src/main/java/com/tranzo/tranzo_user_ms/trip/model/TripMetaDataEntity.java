package com.tranzo.tranzo_user_ms.trip.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "trip_meta_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TripMetaDataEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "trip_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID tripId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "trip_id")
    private TripEntity trip;

    @Column(name = "trip_summary", columnDefinition = "jsonb")
    private Map<String, Object> tripSummary;

    @Column(name = "whats_included", columnDefinition = "jsonb")
    private Map<String, Object> whatsIncluded;

    @Column(name = "whats_excluded", columnDefinition = "jsonb")
    private Map<String, Object> whatsExcluded;
}
