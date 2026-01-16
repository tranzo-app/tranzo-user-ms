package com.tranzo.tranzo_user_ms.trip.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "task_lock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskLockEntity {

    @Id
    @Column(name = "task_id", nullable = false)
    private String taskId;

    @Column(name = "last_execution", nullable = false)
    private Long lastExecution;
}

