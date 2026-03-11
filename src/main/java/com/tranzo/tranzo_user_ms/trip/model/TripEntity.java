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
@Table(name = "core_trip_details", indexes = {
        @Index(name = "idx_trip_status", columnList = "trip_status"),
        @Index(name = "idx_trip_status_start_date", columnList = "trip_status, trip_start_date"),
        @Index(name = "idx_trip_status_end_date", columnList = "trip_status, trip_end_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TripEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(name = "trip_id", updatable = false, nullable = false)
    private UUID tripId;

    @Column(name = "trip_description")
    private String tripDescription;

    @Column(name = "trip_title")
    private String tripTitle;

    @Column(name = "trip_destination")
    private String tripDestination;

    @Column(name = "trip_start_date")
    private LocalDate tripStartDate;

    @Column(name = "trip_end_date")
    private LocalDate tripEndDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "trip_status", nullable = false)
    private TripStatus tripStatus;

    @Column(name = "estimated_budget")
    private Double estimatedBudget;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "current_participants")
    private Integer currentParticipants = 0;

    @Column(name = "is_full")
    private Boolean isFull = false;

    @Column(name = "trip_full_reason")
    private String tripFullReason;

    @Column(name = "full_marked_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fullMarkedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "join_policy")
    private JoinPolicy joinPolicy;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility_status")
    private VisibilityStatus visibilityStatus;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @Column(name = "conversation_id")
    private UUID conversationID;

    @OneToOne(
            mappedBy = "trip",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private TripPolicyEntity tripPolicyEntity;

    @OneToOne(
            mappedBy = "trip",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private TripMetaDataEntity tripMetaData;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "trip_tag",
            joinColumns = @JoinColumn(name = "trip_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<TagEntity> tripTags = new HashSet<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TripItineraryEntity> tripItineraries = new HashSet<>();

    @OneToMany(mappedBy = "trip", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<TripInviteEntity> tripInvites = new HashSet<>();

    @OneToMany(mappedBy = "trip", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<TripJoinRequestEntity> tripJoinRequests = new HashSet<>();

    @OneToMany(
            mappedBy = "trip",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<TripMemberEntity> tripMembers = new HashSet<>();

    @OneToMany(mappedBy = "trip", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private Set<TripQueryEntity> tripQueries = new HashSet<>();

    @OneToMany(mappedBy = "trip", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<TripReportEntity> tripReports = new HashSet<>();

    @OneToMany(mappedBy = "trip", fetch = FetchType.LAZY)
    private Set<TripWishlistEntity> tripWishlists = new HashSet<>();
}


// Can we auto populate marked full at based on is_full field change
// create indexing on trip_id and host_id
// create indexing on trip_status and visibility_status
// create indexing on trip_start_date and trip_end_date
// create indexing on is_full and full_marked_at
// create indexing on join_policy and max_participants

