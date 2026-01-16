package com.tranzo.tranzo_user_ms.trip.scheduler;

import com.tranzo.tranzo_user_ms.trip.repository.TaskLockRepository;
import com.tranzo.tranzo_user_ms.trip.service.TripManagementService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableScheduling
public class TripScheduler {

    private static final String UPDATE_ONGOING_TASK = "update_ongoing_trips";
    private static final String UPDATE_COMPLETED_TASK = "update_completed_trips";

    private final TaskLockRepository taskLockRepository;
    private final TripManagementService tripManagementService;

    public TripScheduler(TaskLockRepository taskLockRepository,
                         TripManagementService tripManagementService) {
        this.taskLockRepository = taskLockRepository;
        this.tripManagementService = tripManagementService;
    }

    @Transactional
    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
    public void updateToOngoing() {
        long now = System.currentTimeMillis();
        long rate = Duration.of(1, ChronoUnit.HOURS).toMillis();

        taskLockRepository.findByTaskIdAndLastExecutionLessThan(
                UPDATE_ONGOING_TASK, now - rate
        ).ifPresent(lock -> {
            tripManagementService.autoMarkTripsAsOngoing();
            lock.setLastExecution(now);
            taskLockRepository.save(lock);
        });
    }

    @Transactional
    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
    public void updateToCompleted() {
        long now = System.currentTimeMillis();
        long rate = Duration.of(1, ChronoUnit.HOURS).toMillis();

        taskLockRepository.findByTaskIdAndLastExecutionLessThan(
                UPDATE_COMPLETED_TASK, now - rate
        ).ifPresent(lock -> {
            tripManagementService.autoMarkTripsAsCompleted();
            lock.setLastExecution(now);
            taskLockRepository.save(lock);
        });
    }
}

