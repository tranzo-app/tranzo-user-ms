package com.tranzo.tranzo_user_ms.trip.scheduler;

import com.tranzo.tranzo_user_ms.commons.events.DraftTripReminderEvent;
import com.tranzo.tranzo_user_ms.commons.events.UpcomingTripEvent;
import com.tranzo.tranzo_user_ms.trip.enums.TripMemberRole;
import com.tranzo.tranzo_user_ms.trip.enums.TripMemberStatus;
import com.tranzo.tranzo_user_ms.trip.enums.TripStatus;
import com.tranzo.tranzo_user_ms.trip.model.TripEntity;
import com.tranzo.tranzo_user_ms.trip.model.TripMemberEntity;
import com.tranzo.tranzo_user_ms.trip.repository.TripMemberRepository;
import com.tranzo.tranzo_user_ms.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Publishes events for notification: draft reminders and upcoming trips.
 * Runs on schedule; notification module listens and creates user notifications.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TripNotificationScheduler {

    private static final int UPCOMING_DAYS = 3;

    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Scheduled(cron = "${trip.notification.draft-reminder-cron:0 0 9 * * ?}")
    @Transactional(readOnly = true)
    public void publishDraftTripReminders() {
        List<TripEntity> drafts = tripRepository.findByTripStatus(TripStatus.DRAFT);
        for (TripEntity trip : drafts) {
            UUID hostUserId = tripMemberRepository.findByTrip_TripIdAndStatus(trip.getTripId(), TripMemberStatus.ACTIVE)
                    .stream()
                    .filter(m -> m.getRole() == TripMemberRole.HOST)
                    .map(TripMemberEntity::getUserId)
                    .findFirst()
                    .orElse(null);
            if (hostUserId != null) {
                applicationEventPublisher.publishEvent(
                        new DraftTripReminderEvent(trip.getTripId(), trip.getTripTitle(), hostUserId));
            }
        }
        if (!drafts.isEmpty()) {
            log.info("Published draft trip reminders for {} drafts", drafts.size());
        }
    }

    @Scheduled(cron = "${trip.notification.upcoming-cron:0 0 8 * * ?}")
    @Transactional(readOnly = true)
    public void publishUpcomingTripReminders() {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(UPCOMING_DAYS);
        List<TripStatus> statuses = List.of(TripStatus.PUBLISHED, TripStatus.ONGOING);
        List<TripEntity> upcoming = tripRepository.findByTripStatusInAndTripStartDateBetween(statuses, today, endDate);
        for (TripEntity trip : upcoming) {
            List<UUID> memberUserIds = tripMemberRepository
                    .findByTrip_TripIdAndStatus(trip.getTripId(), TripMemberStatus.ACTIVE)
                    .stream()
                    .map(TripMemberEntity::getUserId)
                    .collect(Collectors.toList());
            if (!memberUserIds.isEmpty()) {
                applicationEventPublisher.publishEvent(
                        new UpcomingTripEvent(trip.getTripId(), trip.getTripTitle(), trip.getTripStartDate(), memberUserIds));
            }
        }
        if (!upcoming.isEmpty()) {
            log.info("Published upcoming trip reminders for {} trips", upcoming.size());
        }
    }
}
