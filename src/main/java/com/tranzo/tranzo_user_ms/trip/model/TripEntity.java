package com.tranzo.tranzo_user_ms.trip.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tranzo.tranzo_user_ms.trip.enums.JoinPolicy;
import com.tranzo.tranzo_user_ms.trip.enums.TripStatus;
import com.tranzo.tranzo_user_ms.trip.enums.VisibilityStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "core_trip_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TripEntity {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    @Column(name = "trip_id", updatable = false, nullable = false)
    private UUID tripId;

    @Column(name = "host_user_id", nullable = false)
    private UUID hostUserId;

    @Column(name = "trip_description", nullable = false, length = 500)
    private String tripDescription;

    @Column(name = "trip_title", nullable = false)
    private String tripTitle;

    @Column(name = "trip_destination", nullable = false)
    private String tripDestination;

    @Column(name = "trip_start_date", nullable = false)
    private LocalDate tripStartDate;

    @Column(name = "trip_end_date", nullable = false)
    private LocalDate tripEndDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "trip_status", nullable = false)
    private TripStatus tripStatus;

    @Column(name = "estimated_budget", nullable = false)
    private Double estimatedBudget;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @Column(name = "is_full", nullable = false)
    private Boolean isFull;

    @Column(name = "trip_full_reason", length = 300)
    private String tripFullReason;

    @Column(name = "full_marked_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fullMarkedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "join_policy", nullable = false)
    private JoinPolicy joinPolicy;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility_status", nullable = false)
    private VisibilityStatus visibilityStatus;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(
            mappedBy = "trip",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            optional = true
    )
    private TripPolicy tripPolicy;

    @OneToOne(
            mappedBy = "trip",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            optional = true
    )
    private TripMetaDataEntity tripMetaData;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TripTagEntity> tripTags = new HashSet<>();
}


// Can we auto populate marked full at based on is_full field change
// create indexing on trip_id and host_id
// create indexing on trip_status and visibility_status
// create indexing on trip_start_date and trip_end_date
// create indexing on is_full and full_marked_at
// create indexing on join_policy and max_participants

