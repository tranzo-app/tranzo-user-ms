package com.tranzo.tranzo_user_ms.trip.repository;

import com.tranzo.tranzo_user_ms.trip.model.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends JpaRepository<TagEntity, UUID> {
    Optional<TagEntity> findByTagNameIgnoreCase(String tagName);
}
