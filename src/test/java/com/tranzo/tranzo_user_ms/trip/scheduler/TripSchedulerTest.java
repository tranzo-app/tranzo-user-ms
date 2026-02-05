package com.tranzo.tranzo_user_ms.trip.scheduler;

import com.tranzo.tranzo_user_ms.trip.model.TaskLockEntity;
import com.tranzo.tranzo_user_ms.trip.repository.TaskLockRepository;
import com.tranzo.tranzo_user_ms.trip.service.TripManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TripScheduler Unit Tests")
class TripSchedulerTest {

    @Mock
    private TaskLockRepository taskLockRepository;

    @Mock
    private TripManagementService tripManagementService;

    @InjectMocks
    private TripScheduler tripScheduler;

    private TaskLockEntity taskLockEntity;
    private long hourInMillis;

    @BeforeEach
    void setUp() {
        hourInMillis = Duration.of(1, ChronoUnit.HOURS).toMillis();
        taskLockEntity = new TaskLockEntity("update_ongoing_trips", System.currentTimeMillis());
    }

    // ============== UPDATE TO ONGOING TESTS ==============

    @Test
    @DisplayName("Should update trips to ongoing when task lock is found and expired")
    void testUpdateToOngoing_Success() throws Exception {
        // Given
        long now = System.currentTimeMillis();
        long oldTime = now - hourInMillis - 1000; // More than 1 hour old

        TaskLockEntity ongoingLock = new TaskLockEntity("update_ongoing_trips", oldTime);

        when(taskLockRepository.findByTaskIdAndLastExecutionLessThan(
                eq("update_ongoing_trips"), anyLong()))
            .thenReturn(Optional.of(ongoingLock));

        when(taskLockRepository.save(any(TaskLockEntity.class)))
            .thenReturn(ongoingLock);

        doNothing().when(tripManagementService).autoMarkTripsAsOngoing();

        // When
        tripScheduler.updateToOngoing();

        // Then
        verify(tripManagementService, times(1)).autoMarkTripsAsOngoing();
        verify(taskLockRepository, times(1)).save(any(TaskLockEntity.class));

        ArgumentCaptor<TaskLockEntity> captor = ArgumentCaptor.forClass(TaskLockEntity.class);
        verify(taskLockRepository).save(captor.capture());

        TaskLockEntity savedLock = captor.getValue();
        assertNotNull(savedLock);
        assertEquals("update_ongoing_trips", savedLock.getTaskId());
        assertTrue(savedLock.getLastExecution() > oldTime);
    }

    @Test
    @DisplayName("Should not update ongoing trips when task lock is not found")
    void testUpdateToOngoing_NoLockFound() throws Exception {
        // Given
        when(taskLockRepository.findByTaskIdAndLastExecutionLessThan(
                eq("update_ongoing_trips"), anyLong()))
            .thenReturn(Optional.empty());

        // When
        tripScheduler.updateToOngoing();

        // Then
        verify(tripManagementService, never()).autoMarkTripsAsOngoing();
        verify(taskLockRepository, never()).save(any(TaskLockEntity.class));
    }

    @Test
    @DisplayName("Should not update ongoing trips when lock was recently executed")
    void testUpdateToOngoing_LockNotExpired() throws Exception {
        // Given
        long now = System.currentTimeMillis();
        long recentTime = now - (30 * 60 * 1000); // 30 minutes ago

        when(taskLockRepository.findByTaskIdAndLastExecutionLessThan(
                eq("update_ongoing_trips"), anyLong()))
            .thenReturn(Optional.empty());

        // When
        tripScheduler.updateToOngoing();

        // Then
        verify(tripManagementService, never()).autoMarkTripsAsOngoing();
        verify(taskLockRepository, never()).save(any(TaskLockEntity.class));
    }

    @Test
    @DisplayName("Should update task lock with current time when updating ongoing trips")
    void testUpdateToOngoing_UpdatesLockTime() throws Exception {
        // Given
        long now = System.currentTimeMillis();
        long oldTime = now - hourInMillis - 1000;

        TaskLockEntity ongoingLock = new TaskLockEntity("update_ongoing_trips", oldTime);

        when(taskLockRepository.findByTaskIdAndLastExecutionLessThan(
                eq("update_ongoing_trips"), anyLong()))
            .thenReturn(Optional.of(ongoingLock));

        when(taskLockRepository.save(any(TaskLockEntity.class)))
            .thenReturn(ongoingLock);

        doNothing().when(tripManagementService).autoMarkTripsAsOngoing();

        // When
        long beforeExecution = System.currentTimeMillis();
        tripScheduler.updateToOngoing();
        long afterExecution = System.currentTimeMillis();

        // Then
        ArgumentCaptor<TaskLockEntity> captor = ArgumentCaptor.forClass(TaskLockEntity.class);
        verify(taskLockRepository).save(captor.capture());

        TaskLockEntity savedLock = captor.getValue();
        assertTrue(savedLock.getLastExecution() >= beforeExecution);
        assertTrue(savedLock.getLastExecution() <= afterExecution + 1000); // Allow 1 second margin
    }

    // ============== UPDATE TO COMPLETED TESTS ==============

    @Test
    @DisplayName("Should update trips to completed when task lock is found and expired")
    void testUpdateToCompleted_Success() throws Exception {
        // Given
        long now = System.currentTimeMillis();
        long oldTime = now - hourInMillis - 1000; // More than 1 hour old

        TaskLockEntity completedLock = new TaskLockEntity("update_completed_trips", oldTime);

        when(taskLockRepository.findByTaskIdAndLastExecutionLessThan(
                eq("update_completed_trips"), anyLong()))
            .thenReturn(Optional.of(completedLock));

        when(taskLockRepository.save(any(TaskLockEntity.class)))
            .thenReturn(completedLock);

        doNothing().when(tripManagementService).autoMarkTripsAsCompleted();

        // When
        tripScheduler.updateToCompleted();

        // Then
        verify(tripManagementService, times(1)).autoMarkTripsAsCompleted();
        verify(taskLockRepository, times(1)).save(any(TaskLockEntity.class));

        ArgumentCaptor<TaskLockEntity> captor = ArgumentCaptor.forClass(TaskLockEntity.class);
        verify(taskLockRepository).save(captor.capture());

        TaskLockEntity savedLock = captor.getValue();
        assertNotNull(savedLock);
        assertEquals("update_completed_trips", savedLock.getTaskId());
        assertTrue(savedLock.getLastExecution() > oldTime);
    }

    @Test
    @DisplayName("Should not update completed trips when task lock is not found")
    void testUpdateToCompleted_NoLockFound() throws Exception {
        // Given
        when(taskLockRepository.findByTaskIdAndLastExecutionLessThan(
                eq("update_completed_trips"), anyLong()))
            .thenReturn(Optional.empty());

        // When
        tripScheduler.updateToCompleted();

        // Then
        verify(tripManagementService, never()).autoMarkTripsAsCompleted();
        verify(taskLockRepository, never()).save(any(TaskLockEntity.class));
    }

    @Test
    @DisplayName("Should not update completed trips when lock was recently executed")
    void testUpdateToCompleted_LockNotExpired() throws Exception {
        // Given
        long now = System.currentTimeMillis();
        long recentTime = now - (30 * 60 * 1000); // 30 minutes ago

        when(taskLockRepository.findByTaskIdAndLastExecutionLessThan(
                eq("update_completed_trips"), anyLong()))
            .thenReturn(Optional.empty());

        // When
        tripScheduler.updateToCompleted();

        // Then
        verify(tripManagementService, never()).autoMarkTripsAsCompleted();
        verify(taskLockRepository, never()).save(any(TaskLockEntity.class));
    }

    @Test
    @DisplayName("Should update task lock with current time when updating completed trips")
    void testUpdateToCompleted_UpdatesLockTime() throws Exception {
        // Given
        long now = System.currentTimeMillis();
        long oldTime = now - hourInMillis - 1000;

        TaskLockEntity completedLock = new TaskLockEntity("update_completed_trips", oldTime);

        when(taskLockRepository.findByTaskIdAndLastExecutionLessThan(
                eq("update_completed_trips"), anyLong()))
            .thenReturn(Optional.of(completedLock));

        when(taskLockRepository.save(any(TaskLockEntity.class)))
            .thenReturn(completedLock);

        doNothing().when(tripManagementService).autoMarkTripsAsCompleted();

        // When
        long beforeExecution = System.currentTimeMillis();
        tripScheduler.updateToCompleted();
        long afterExecution = System.currentTimeMillis();

        // Then
        ArgumentCaptor<TaskLockEntity> captor = ArgumentCaptor.forClass(TaskLockEntity.class);
        verify(taskLockRepository).save(captor.capture());

        TaskLockEntity savedLock = captor.getValue();
        assertTrue(savedLock.getLastExecution() >= beforeExecution);
        assertTrue(savedLock.getLastExecution() <= afterExecution + 1000); // Allow 1 second margin
    }

    // ============== EDGE CASE TESTS ==============

    @Test
    @DisplayName("Should handle exception during autoMarkTripsAsOngoing gracefully")
    void testUpdateToOngoing_ServiceException() throws Exception {
        // Given
        long now = System.currentTimeMillis();
        long oldTime = now - hourInMillis - 1000;

        TaskLockEntity ongoingLock = new TaskLockEntity("update_ongoing_trips", oldTime);

        when(taskLockRepository.findByTaskIdAndLastExecutionLessThan(
                eq("update_ongoing_trips"), anyLong()))
            .thenReturn(Optional.of(ongoingLock));

        doThrow(new RuntimeException("Service error")).when(tripManagementService).autoMarkTripsAsOngoing();

        // When & Then
        assertThrows(RuntimeException.class, () -> tripScheduler.updateToOngoing());
        verify(tripManagementService, times(1)).autoMarkTripsAsOngoing();
    }

    @Test
    @DisplayName("Should handle exception during autoMarkTripsAsCompleted gracefully")
    void testUpdateToCompleted_ServiceException() throws Exception {
        // Given
        long now = System.currentTimeMillis();
        long oldTime = now - hourInMillis - 1000;

        TaskLockEntity completedLock = new TaskLockEntity("update_completed_trips", oldTime);

        when(taskLockRepository.findByTaskIdAndLastExecutionLessThan(
                eq("update_completed_trips"), anyLong()))
            .thenReturn(Optional.of(completedLock));

        doThrow(new RuntimeException("Service error")).when(tripManagementService).autoMarkTripsAsCompleted();

        // When & Then
        assertThrows(RuntimeException.class, () -> tripScheduler.updateToCompleted());
        verify(tripManagementService, times(1)).autoMarkTripsAsCompleted();
    }

    @Test
    @DisplayName("Should correctly calculate time threshold (1 hour)")
    void testUpdateToOngoing_VerifyTimeThreshold() throws Exception {
        // Given
        long now = System.currentTimeMillis();
        long expectedThreshold = now - hourInMillis;

        when(taskLockRepository.findByTaskIdAndLastExecutionLessThan(
                eq("update_ongoing_trips"), anyLong()))
            .thenReturn(Optional.empty());

        // When
        tripScheduler.updateToOngoing();

        // Then
        ArgumentCaptor<Long> timeCaptor = ArgumentCaptor.forClass(Long.class);
        verify(taskLockRepository).findByTaskIdAndLastExecutionLessThan(
                eq("update_ongoing_trips"), timeCaptor.capture());

        Long capturedTime = timeCaptor.getValue();
        // Check if captured time is close to expected threshold (within 1 second)
        assertTrue(Math.abs(capturedTime - expectedThreshold) < 1000);
    }

    @Test
    @DisplayName("Should verify both methods use correct task IDs")
    void testTaskIdConstants() throws Exception {
        // Given
        when(taskLockRepository.findByTaskIdAndLastExecutionLessThan(anyString(), anyLong()))
            .thenReturn(Optional.empty());

        // When
        tripScheduler.updateToOngoing();
        tripScheduler.updateToCompleted();

        // Then
        ArgumentCaptor<String> taskIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(taskLockRepository, times(2)).findByTaskIdAndLastExecutionLessThan(
                taskIdCaptor.capture(), anyLong());

        java.util.List<String> capturedTaskIds = taskIdCaptor.getAllValues();
        assertEquals(2, capturedTaskIds.size());
        assertTrue(capturedTaskIds.contains("update_ongoing_trips"));
        assertTrue(capturedTaskIds.contains("update_completed_trips"));
    }

    @Test
    @DisplayName("Should not save lock if service throws exception during ongoing update")
    void testUpdateToOngoing_NoSaveOnException() throws Exception {
        // Given
        long now = System.currentTimeMillis();
        long oldTime = now - hourInMillis - 1000;

        TaskLockEntity ongoingLock = new TaskLockEntity("update_ongoing_trips", oldTime);

        when(taskLockRepository.findByTaskIdAndLastExecutionLessThan(
                eq("update_ongoing_trips"), anyLong()))
            .thenReturn(Optional.of(ongoingLock));

        doThrow(new RuntimeException("Service error")).when(tripManagementService).autoMarkTripsAsOngoing();

        // When & Then
        assertThrows(RuntimeException.class, () -> tripScheduler.updateToOngoing());
        verify(taskLockRepository, never()).save(any(TaskLockEntity.class));
    }

    @Test
    @DisplayName("Should not save lock if service throws exception during completed update")
    void testUpdateToCompleted_NoSaveOnException() throws Exception {
        // Given
        long now = System.currentTimeMillis();
        long oldTime = now - hourInMillis - 1000;

        TaskLockEntity completedLock = new TaskLockEntity("update_completed_trips", oldTime);

        when(taskLockRepository.findByTaskIdAndLastExecutionLessThan(
                eq("update_completed_trips"), anyLong()))
            .thenReturn(Optional.of(completedLock));

        doThrow(new RuntimeException("Service error")).when(tripManagementService).autoMarkTripsAsCompleted();

        // When & Then
        assertThrows(RuntimeException.class, () -> tripScheduler.updateToCompleted());
        verify(taskLockRepository, never()).save(any(TaskLockEntity.class));
    }
}

