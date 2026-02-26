package com.tranzo.tranzo_user_ms.notification.events;

import com.tranzo.tranzo_user_ms.commons.events.*;
import com.tranzo.tranzo_user_ms.notification.enums.NotificationType;
import com.tranzo.tranzo_user_ms.notification.service.NotificationService;
import com.tranzo.tranzo_user_ms.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class TripNotificationEventListener {

    private final NotificationService notificationService;
    private final UserProfileRepository userProfileRepository;

    @EventListener
    @Transactional
    public void onDraftTripReminder(DraftTripReminderEvent event) {
        log.info("Creating DRAFT_TRIP_REMINDER notifications for tripId={}, hostUserId={}", event.getTripId(), event.getHostUserId());
        notificationService.createNotification(
                event.getHostUserId(),
                event.getTripId(),
                NotificationType.DRAFT_TRIP_REMINDER,
                "Complete your draft trip",
                "Your trip \"" + (event.getTripTitle() != null ? event.getTripTitle() : "Untitled") + "\" is still a draft. Publish it when you're ready."
        );
    }

    @EventListener
    @Transactional
    public void onUpcomingTrip(UpcomingTripEvent event) {
        log.info("Creating UPCOMING_TRIP notifications for tripId={}, memberCount={}", event.getTripId(), event.getMemberUserIds().size());
        String title = "Trip starting soon";
        String body = "Gear up! \"" + (event.getTripTitle() != null ? event.getTripTitle() : "Your trip") + "\" starts on " + event.getStartDate() + ".";
        notificationService.createNotificationsForUsers(
                event.getMemberUserIds(),
                event.getTripId(),
                NotificationType.UPCOMING_TRIP,
                title,
                body
        );
    }

    @EventListener
    @Transactional
    public void onTripCompleted(TripCompletedEvent event) {
        log.info("Creating TRIP_COMPLETED notifications for tripId={}, memberCount={}", event.getTripId(), event.getMemberUserIds().size());
        String title = "Trip completed";
        String body = "How was \"" + (event.getTripTitle() != null ? event.getTripTitle() : "your trip") + "\"? Rate and share your feedback.";
        notificationService.createNotificationsForUsers(
                event.getMemberUserIds(),
                event.getTripId(),
                NotificationType.TRIP_COMPLETED,
                title,
                body
        );
    }

    @EventListener
    @Transactional
    public void onTripCancelled(TripCancelledEvent event) {
        log.info("Creating TRIP_CANCELLED notifications for tripId={}, memberCount={}", event.getTripId(), event.getMemberUserIds().size());
        String title = "Trip cancelled";
        String body = "The trip \"" + (event.getTripTitle() != null ? event.getTripTitle() : "you were part of") + "\" has been cancelled.";
        notificationService.createNotificationsForUsers(
                event.getMemberUserIds(),
                event.getTripId(),
                NotificationType.TRIP_CANCELLED,
                title,
                body
        );
    }

    @EventListener
    @Transactional
    public void onJoinRequestCreated(JoinRequestCreatedEvent event) {
        log.info("Creating JOIN_REQUEST_RECEIVED notification for tripId={}, hostUserId={}", event.getTripId(), event.getHostUserId());
        notificationService.createNotification(
                event.getHostUserId(),
                event.getTripId(),
                NotificationType.JOIN_REQUEST_RECEIVED,
                "New join request",
                "Someone requested to join your trip \"" + (event.getTripTitle() != null ? event.getTripTitle() : "") + "\"."
        );
    }

    @EventListener
    @Transactional
    public void onJoinRequestApproved(JoinRequestApprovedEvent event) {
        log.info("Creating JOIN_REQUEST_APPROVED notification for tripId={}, requestorUserId={}", event.getTripId(), event.getRequestorUserId());
        notificationService.createNotification(
                event.getRequestorUserId(),
                event.getTripId(),
                NotificationType.JOIN_REQUEST_APPROVED,
                "Join request approved",
                "Your request to join \"" + (event.getTripTitle() != null ? event.getTripTitle() : "the trip") + "\" was approved."
        );
    }

    @EventListener
    @Transactional
    public void onJoinRequestRejected(JoinRequestRejectedEvent event) {
        log.info("Creating JOIN_REQUEST_REJECTED notification for tripId={}, requestorUserId={}", event.getTripId(), event.getRequestorUserId());
        notificationService.createNotification(
                event.getRequestorUserId(),
                event.getTripId(),
                NotificationType.JOIN_REQUEST_REJECTED,
                "Join request declined",
                "Your request to join \"" + (event.getTripTitle() != null ? event.getTripTitle() : "the trip") + "\" was declined."
        );
    }

    @EventListener
    @Transactional
    public void onMemberJoinedTrip(MemberJoinedTripEvent event) {
        if (event.getOtherMemberUserIds() == null || event.getOtherMemberUserIds().isEmpty()) {
            return;
        }
        log.info("Creating MEMBER_JOINED_TRIP notifications for tripId={}, otherMemberCount={}", event.getTripId(), event.getOtherMemberUserIds().size());
        String title = "New member joined";
        String body = "A new member has joined your trip \"" + (event.getTripTitle() != null ? event.getTripTitle() : "") + "\".";
        notificationService.createNotificationsForUsers(
                event.getOtherMemberUserIds(),
                event.getTripId(),
                NotificationType.MEMBER_JOINED_TRIP,
                title,
                body
        );
    }

    @EventListener
    @Transactional
    public void onMemberLeftOrRemovedTrip(MemberLeftOrRemovedTripEvent event) {
        if (event.getOtherMemberUserIds() == null || event.getOtherMemberUserIds().isEmpty()) {
            return;
        }
        log.info("Creating MEMBER_LEFT_OR_REMOVED_TRIP notifications for tripId={}, otherMemberCount={}", event.getTripId(), event.getOtherMemberUserIds().size());
        String title = event.isRemovedByHost() ? "Member removed" : "Member left";
        String body = "A member has " + (event.isRemovedByHost() ? "been removed from" : "left") + " your trip \"" + (event.getTripTitle() != null ? event.getTripTitle() : "") + "\".";
        notificationService.createNotificationsForUsers(
                event.getOtherMemberUserIds(),
                event.getTripId(),
                NotificationType.MEMBER_LEFT_OR_REMOVED_TRIP,
                title,
                body
        );
    }

    @EventListener
    @Transactional
    public void onMemberPromotedToCoHost(MemberPromotedToCoHostEvent event) {
        if (event.getAllMemberUserIds() == null || event.getAllMemberUserIds().isEmpty()) {
            return;
        }
        log.info("Creating MEMBER_PROMOTED_TO_CO_HOST notifications for tripId={}, memberCount={}", event.getTripId(), event.getAllMemberUserIds().size());
        String title = "Member promoted to co-host";
        String body = "A member has been promoted to co-host for trip \"" + (event.getTripTitle() != null ? event.getTripTitle() : "") + "\".";
        notificationService.createNotificationsForUsers(
                event.getAllMemberUserIds(),
                event.getTripId(),
                NotificationType.MEMBER_PROMOTED_TO_CO_HOST,
                title,
                body
        );
    }

    @EventListener
    @Transactional
    public void onTripFullCapacityReached(TripFullCapacityReachedEvent event) {
        if (event.getMemberUserIds() == null || event.getMemberUserIds().isEmpty()) {
            return;
        }
        log.info("Creating TRIP_FULL_CAPACITY_REACHED notifications for tripId={}, memberCount={}", event.getTripId(), event.getMemberUserIds().size());
        String title = "Trip is full";
        String body = "The trip \"" + (event.getTripTitle() != null ? event.getTripTitle() : "") + "\" has reached maximum capacity.";
        notificationService.createNotificationsForUsers(
                event.getMemberUserIds(),
                event.getTripId(),
                NotificationType.TRIP_FULL_CAPACITY_REACHED,
                title,
                body
        );
    }

    @EventListener
    @Transactional
    public void onTripMarkedFullByHost(TripMarkedFullByHostEvent event) {
        if (event.getMemberUserIdsExcludingHost() == null || event.getMemberUserIdsExcludingHost().isEmpty()) {
            return;
        }
        log.info("Creating TRIP_MARKED_FULL_BY_HOST notifications for tripId={}, memberCount={}", event.getTripId(), event.getMemberUserIdsExcludingHost().size());
        String title = "Trip marked full";
        String body = "The host has marked the trip \"" + (event.getTripTitle() != null ? event.getTripTitle() : "") + "\" as full.";
        notificationService.createNotificationsForUsers(
                event.getMemberUserIdsExcludingHost(),
                event.getTripId(),
                NotificationType.TRIP_MARKED_FULL_BY_HOST,
                title,
                body
        );
    }

    @EventListener
    @Transactional
    public void onTripDetailsChanged(TripDetailsChangedEvent event) {
        if (event.getMemberUserIds() == null || event.getMemberUserIds().isEmpty()) {
            return;
        }
        log.info("Creating TRIP_DETAILS_CHANGED notifications for tripId={}, memberCount={}", event.getTripId(), event.getMemberUserIds().size());
        String title = "Trip details updated";
        String body = "The trip \"" + (event.getTripTitle() != null ? event.getTripTitle() : "") + "\" has been updated by the host.";
        notificationService.createNotificationsForUsers(
                event.getMemberUserIds(),
                event.getTripId(),
                NotificationType.TRIP_DETAILS_CHANGED,
                title,
                body
        );
    }

    @EventListener
    @Transactional
    public void onTripQuestionAsked(TripQuestionAskedEvent event) {
        if (event.getMemberUserIds() == null || event.getMemberUserIds().isEmpty()) {
            return;
        }
        log.info("Creating TRIP_QUESTION_ASKED notifications for tripId={}, memberCount={}", event.getTripId(), event.getMemberUserIds().size());
        String title = "New question on trip";
        String body = "A new question was posted for trip \"" + (event.getTripTitle() != null ? event.getTripTitle() : "") + "\".";
        notificationService.createNotificationsForUsers(
                event.getMemberUserIds(),
                event.getTripId(),
                NotificationType.TRIP_QUESTION_ASKED,
                title,
                body
        );
    }

    @EventListener
    @Transactional
    public void onTripQuestionAnswered(TripQuestionAnsweredEvent event) {
        log.info("Creating TRIP_QUESTION_ANSWERED notification for tripId={}, askedByUserId={}", event.getTripId(), event.getAskedByUserId());
        notificationService.createNotification(
                event.getAskedByUserId(),
                event.getTripId(),
                NotificationType.TRIP_QUESTION_ANSWERED,
                "Your question was answered",
                "Your question for trip \"" + (event.getTripTitle() != null ? event.getTripTitle() : "") + "\" has been answered."
        );
    }

    @EventListener
    @Transactional
    public void onTripInviteCreated(TripInviteCreatedEvent event) {
        if (event.getInvitedUserId() == null) {
            log.warn("Skipping TRIP_INVITED notification: invitedUserId is null");
            return;
        }
        log.info("Creating TRIP_INVITED notification for tripId={}, invitedUserId={}", event.getTripId(), event.getInvitedUserId());
        String inviterName = "A travel pal";
        if (event.getInvitedByUserId() != null) {
            inviterName = userProfileRepository.findAllUserProfileDetailByUserId(event.getInvitedByUserId())
                    .map(up -> up.getFirstName() != null && !up.getFirstName().isBlank() ? up.getFirstName() : "A travel pal")
                    .orElse("A travel pal");
        }
        String title = "Trip invite";
        String body = inviterName + " has invited you to join the trip \"" + (event.getTripTitle() != null ? event.getTripTitle() : "") + "\".";
        notificationService.createNotification(
                event.getInvitedUserId(),
                event.getTripId(),
                NotificationType.TRIP_INVITED,
                title,
                body
        );
    }

    @EventListener
    @Transactional
    public void onTripBroadcast(TripBroadcastEvent event) {
        if (event.getBroadcastToUserIds() == null || event.getBroadcastToUserIds().isEmpty()) {
            return;
        }
        log.info("Creating TRIP_BROADCAST notifications for tripId={}, recipientCount={}", event.getTripId(), event.getBroadcastToUserIds().size());
        String title = "New trip";
        String body = "A travel pal has hosted a trip \"" + (event.getTripTitle() != null ? event.getTripTitle() : "") + "\". Check it out!";
        notificationService.createNotificationsForUsers(
                event.getBroadcastToUserIds(),
                event.getTripId(),
                NotificationType.TRIP_BROADCAST,
                title,
                body
        );
    }
}
