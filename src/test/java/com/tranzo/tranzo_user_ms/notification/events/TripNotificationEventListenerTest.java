package com.tranzo.tranzo_user_ms.notification.events;

import com.tranzo.tranzo_user_ms.commons.events.*;
import com.tranzo.tranzo_user_ms.notification.enums.NotificationType;
import com.tranzo.tranzo_user_ms.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TripNotificationEventListener Unit Tests")
class TripNotificationEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TripNotificationEventListener listener;

    private UUID tripId;
    private UUID userId;
    private String tripTitle;

    @BeforeEach
    void setUp() {
        tripId = UUID.randomUUID();
        userId = UUID.randomUUID();
        tripTitle = "Test Trip";
    }

    @Test
    @DisplayName("onDraftTripReminder creates notification for host")
    void testOnDraftTripReminder() {
        DraftTripReminderEvent event = new DraftTripReminderEvent(tripId, tripTitle, userId);

        listener.onDraftTripReminder(event);

        verify(notificationService).createNotification(
            eq(userId), eq(tripId), eq(NotificationType.DRAFT_TRIP_REMINDER),
            eq("Complete your draft trip"), contains("draft"));
    }

    @Test
    @DisplayName("onJoinRequestCreated creates notification for host")
    void testOnJoinRequestCreated() {
        UUID hostUserId = UUID.randomUUID();
        JoinRequestCreatedEvent event = new JoinRequestCreatedEvent(tripId, tripTitle, userId, hostUserId);

        listener.onJoinRequestCreated(event);

        verify(notificationService).createNotification(
            eq(hostUserId), eq(tripId), eq(NotificationType.JOIN_REQUEST_RECEIVED),
            eq("New join request"), anyString());
    }

    @Test
    @DisplayName("onJoinRequestApproved creates notification for requestor")
    void testOnJoinRequestApproved() {
        JoinRequestApprovedEvent event = new JoinRequestApprovedEvent(tripId, tripTitle, userId);

        listener.onJoinRequestApproved(event);

        verify(notificationService).createNotification(
            eq(userId), eq(tripId), eq(NotificationType.JOIN_REQUEST_APPROVED),
            eq("Join request approved"), anyString());
    }

    @Test
    @DisplayName("onJoinRequestRejected creates notification for requestor")
    void testOnJoinRequestRejected() {
        JoinRequestRejectedEvent event = new JoinRequestRejectedEvent(tripId, tripTitle, userId);

        listener.onJoinRequestRejected(event);

        verify(notificationService).createNotification(
            eq(userId), eq(tripId), eq(NotificationType.JOIN_REQUEST_REJECTED),
            eq("Join request declined"), anyString());
    }

    @Test
    @DisplayName("onMemberJoinedTrip creates notifications for other members")
    void testOnMemberJoinedTrip() {
        List<UUID> otherIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        MemberJoinedTripEvent event = new MemberJoinedTripEvent(tripId, tripTitle, userId, otherIds);

        listener.onMemberJoinedTrip(event);

        verify(notificationService).createNotificationsForUsers(
            eq(otherIds), eq(tripId), eq(NotificationType.MEMBER_JOINED_TRIP),
            eq("New member joined"), anyString());
    }

    @Test
    @DisplayName("onMemberJoinedTrip does nothing when otherMemberUserIds empty")
    void testOnMemberJoinedTrip_EmptyList() {
        MemberJoinedTripEvent event = new MemberJoinedTripEvent(tripId, tripTitle, userId, List.of());

        listener.onMemberJoinedTrip(event);

        verify(notificationService, never()).createNotificationsForUsers(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("onMemberLeftOrRemovedTrip creates notifications with removed title when removedByHost")
    void testOnMemberLeftOrRemovedTrip_RemovedByHost() {
        List<UUID> otherIds = List.of(UUID.randomUUID());
        MemberLeftOrRemovedTripEvent event = new MemberLeftOrRemovedTripEvent(tripId, tripTitle, userId, otherIds, true);

        listener.onMemberLeftOrRemovedTrip(event);

        verify(notificationService).createNotificationsForUsers(
            eq(otherIds), eq(tripId), eq(NotificationType.MEMBER_LEFT_OR_REMOVED_TRIP),
            eq("Member removed"), anyString());
    }

    @Test
    @DisplayName("onMemberLeftOrRemovedTrip creates notifications with left title when not removedByHost")
    void testOnMemberLeftOrRemovedTrip_Left() {
        List<UUID> otherIds = List.of(UUID.randomUUID());
        MemberLeftOrRemovedTripEvent event = new MemberLeftOrRemovedTripEvent(tripId, tripTitle, userId, otherIds, false);

        listener.onMemberLeftOrRemovedTrip(event);

        verify(notificationService).createNotificationsForUsers(
            eq(otherIds), eq(tripId), eq(NotificationType.MEMBER_LEFT_OR_REMOVED_TRIP),
            eq("Member left"), anyString());
    }

    @Test
    @DisplayName("onMemberPromotedToCoHost creates notifications for all members")
    void testOnMemberPromotedToCoHost() {
        List<UUID> allIds = List.of(UUID.randomUUID(), userId);
        MemberPromotedToCoHostEvent event = new MemberPromotedToCoHostEvent(tripId, tripTitle, userId, allIds);

        listener.onMemberPromotedToCoHost(event);

        verify(notificationService).createNotificationsForUsers(
            eq(allIds), eq(tripId), eq(NotificationType.MEMBER_PROMOTED_TO_CO_HOST),
            eq("Member promoted to co-host"), anyString());
    }

    @Test
    @DisplayName("onTripFullCapacityReached creates notifications for all members")
    void testOnTripFullCapacityReached() {
        List<UUID> memberIds = List.of(UUID.randomUUID(), userId);
        TripFullCapacityReachedEvent event = new TripFullCapacityReachedEvent(tripId, tripTitle, memberIds);

        listener.onTripFullCapacityReached(event);

        verify(notificationService).createNotificationsForUsers(
            eq(memberIds), eq(tripId), eq(NotificationType.TRIP_FULL_CAPACITY_REACHED),
            eq("Trip is full"), anyString());
    }

    @Test
    @DisplayName("onTripMarkedFullByHost creates notifications for members excluding host")
    void testOnTripMarkedFullByHost() {
        List<UUID> membersExcludingHost = List.of(UUID.randomUUID());
        TripMarkedFullByHostEvent event = new TripMarkedFullByHostEvent(tripId, tripTitle, membersExcludingHost);

        listener.onTripMarkedFullByHost(event);

        verify(notificationService).createNotificationsForUsers(
            eq(membersExcludingHost), eq(tripId), eq(NotificationType.TRIP_MARKED_FULL_BY_HOST),
            eq("Trip marked full"), anyString());
    }

    @Test
    @DisplayName("onTripDetailsChanged creates notifications for all members")
    void testOnTripDetailsChanged() {
        List<UUID> memberIds = List.of(UUID.randomUUID());
        TripDetailsChangedEvent event = new TripDetailsChangedEvent(tripId, tripTitle, memberIds);

        listener.onTripDetailsChanged(event);

        verify(notificationService).createNotificationsForUsers(
            eq(memberIds), eq(tripId), eq(NotificationType.TRIP_DETAILS_CHANGED),
            eq("Trip details updated"), anyString());
    }

    @Test
    @DisplayName("onTripQuestionAsked creates notifications for all members")
    void testOnTripQuestionAsked() {
        List<UUID> memberIds = List.of(UUID.randomUUID());
        TripQuestionAskedEvent event = new TripQuestionAskedEvent(tripId, tripTitle, userId, memberIds);

        listener.onTripQuestionAsked(event);

        verify(notificationService).createNotificationsForUsers(
            eq(memberIds), eq(tripId), eq(NotificationType.TRIP_QUESTION_ASKED),
            eq("New question on trip"), anyString());
    }

    @Test
    @DisplayName("onTripQuestionAnswered creates notification for asker")
    void testOnTripQuestionAnswered() {
        TripQuestionAnsweredEvent event = new TripQuestionAnsweredEvent(tripId, tripTitle, userId);

        listener.onTripQuestionAnswered(event);

        verify(notificationService).createNotification(
            eq(userId), eq(tripId), eq(NotificationType.TRIP_QUESTION_ANSWERED),
            eq("Your question was answered"), anyString());
    }

    @Test
    @DisplayName("onUpcomingTrip creates notifications for members")
    void testOnUpcomingTrip() {
        List<UUID> memberIds = List.of(userId);
        UpcomingTripEvent event = new UpcomingTripEvent(tripId, tripTitle, LocalDate.of(2026, 6, 1), memberIds);

        listener.onUpcomingTrip(event);

        verify(notificationService).createNotificationsForUsers(
            eq(memberIds), eq(tripId), eq(NotificationType.UPCOMING_TRIP),
            eq("Trip starting soon"), anyString());
    }

    @Test
    @DisplayName("onTripCompleted creates notifications for members")
    void testOnTripCompleted() {
        List<UUID> memberIds = List.of(userId);
        TripCompletedEvent event = new TripCompletedEvent(tripId, tripTitle, memberIds);

        listener.onTripCompleted(event);

        verify(notificationService).createNotificationsForUsers(
            eq(memberIds), eq(tripId), eq(NotificationType.TRIP_COMPLETED),
            eq("Trip completed"), anyString());
    }

    @Test
    @DisplayName("onTripCancelled creates notifications for members")
    void testOnTripCancelled() {
        List<UUID> memberIds = List.of(userId);
        TripCancelledEvent event = new TripCancelledEvent(tripId, tripTitle, memberIds);

        listener.onTripCancelled(event);

        verify(notificationService).createNotificationsForUsers(
            eq(memberIds), eq(tripId), eq(NotificationType.TRIP_CANCELLED),
            eq("Trip cancelled"), anyString());
    }
}
