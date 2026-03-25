package com.tranzo.tranzo_user_ms.trip.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tranzo.tranzo_user_ms.commons.converter.JsonMapConverter;
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
    private UUID tripMetaDataId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "trip_id")
    @JsonIgnore
    private TripEntity trip;

    @Convert(converter = JsonMapConverter.class)
    @Column(name = "trip_summary", columnDefinition = "TEXT")
    private Map<String, Object> tripSummary;

    @Convert(converter = JsonMapConverter.class)
    @Column(name = "whats_included", columnDefinition = "TEXT")
    private Map<String, Object> whatsIncluded;

    @Convert(converter = JsonMapConverter.class)
    @Column(name = "whats_excluded", columnDefinition = "TEXT")
    private Map<String, Object> whatsExcluded;
}
