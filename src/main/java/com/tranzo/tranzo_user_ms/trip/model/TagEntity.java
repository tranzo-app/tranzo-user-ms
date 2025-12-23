package com.tranzo.tranzo_user_ms.trip.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tags", uniqueConstraints = {@UniqueConstraint(columnNames = {"tag_name"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TagEntity {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    @Column(name = "tag_id", updatable = false, nullable = false)
    private UUID tagId;

    @Column(name = "tag_name", nullable = false, unique = true)
    private String tagName;

    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private Set<TripEntity> trips = new HashSet<>();
}
