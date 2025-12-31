package com.tranzo.tranzo_user_ms.trip.repository;

import com.tranzo.tranzo_user_ms.trip.model.TaskLockEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskLockRepository extends JpaRepository<TaskLockEntity, String> {

    Optional<TaskLockEntity> findByTaskIdAndLastExecutionLessThan(
            String taskId,
            Long lastExecutionCutoff
    );
}

