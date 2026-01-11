package com.tranzo.tranzo_user_ms.trip.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "trip_itineraries",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"trip_id", "day_number"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TripItineraryEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "itinerary_id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID itineraryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private TripEntity trip;

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "description")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "activities", columnDefinition = "jsonb")
    private Map<String, Object> activities;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meals", columnDefinition = "jsonb")
    private Map<String, Object> meals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "stay", columnDefinition = "jsonb")
    private Map<String, Object> stay;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
