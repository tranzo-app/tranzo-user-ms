package com.tranzo.tranzo_user_ms.trip.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "trip_tag")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TripTagEntity {
    @Id
    @Column(name = "trip_id", nullable = false)
    @EqualsAndHashCode.Include
    private UUID tripId;

    @Column(name = "tag_id", nullable = false)
    private UUID tagId;

    @ManyToMany(
        fetch = FetchType.LAZY,
        cascade = { CascadeType.PERSIST, CascadeType.MERGE }
    )
    private TripEntity trip;
}
