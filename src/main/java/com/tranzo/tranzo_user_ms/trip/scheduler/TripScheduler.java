package com.tranzo.tranzo_user_ms.trip.scheduler;

import com.tranzo.tranzo_user_ms.trip.repository.TaskLockRepository;
import com.tranzo.tranzo_user_ms.trip.service.TripManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Slf4j
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
        log.info("TripScheduler initialized - auto-marking trips as ongoing/completed is enabled");
    }

    @Transactional
    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    public void updateToOngoing() {
        log.debug("Trip scheduler: Checking for trips to mark as ongoing");
        long now = System.currentTimeMillis();
        long rate = Duration.of(1, ChronoUnit.HOURS).toMillis();

        var ongoingTaskLock = taskLockRepository.findByTaskIdAndLastExecutionLessThan(
                UPDATE_ONGOING_TASK, now - rate
        );
        
        ongoingTaskLock.ifPresent(lock -> {
            log.info("Trip scheduler: Executing auto-mark trips as ongoing");
            tripManagementService.autoMarkTripsAsOngoing();
            lock.setLastExecution(now);
            taskLockRepository.save(lock);
            log.info("Trip scheduler: Completed auto-mark trips as ongoing");
        });
        
        if (ongoingTaskLock.isEmpty()) {
            log.debug("Trip scheduler: No task lock found for ongoing trips or not yet time to execute");
        }
    }

    @Transactional
    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    public void updateToCompleted() {
        log.debug("Trip scheduler: Checking for trips to mark as completed");
        long now = System.currentTimeMillis();
        long rate = Duration.of(1, ChronoUnit.HOURS).toMillis();

        var completedTaskLock = taskLockRepository.findByTaskIdAndLastExecutionLessThan(
                UPDATE_COMPLETED_TASK, now - rate
        );
        
        completedTaskLock.ifPresent(lock -> {
            log.info("Trip scheduler: Executing auto-mark trips as completed");
            tripManagementService.autoMarkTripsAsCompleted();
            lock.setLastExecution(now);
            taskLockRepository.save(lock);
            log.info("Trip scheduler: Completed auto-mark trips as completed");
        });
        
        if (completedTaskLock.isEmpty()) {
            log.debug("Trip scheduler: No task lock found for completed trips or not yet time to execute");
        }
    }
}

