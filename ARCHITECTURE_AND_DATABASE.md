# Tranzo User Microservice – Architecture & Database

Single reference for architecture, database tables, API, and event flows.

---

## 1. Architecture Overview

### 1.1 Style

- **Modular monolith**: One Spring Boot application with clear packages (trip, chat, notification, user, commons).
- **In-process events**: Spring `ApplicationEventPublisher` for cross-module communication (no Kafka). Trip module publishes events; Chat and Notification modules subscribe.
- **Persistence**: JPA/Hibernate with H2 in-memory (dev). All entities in one schema.

### 1.2 Module Layout

```
com.tranzo.tranzo_user_ms
├── commons          # Shared DTOs, events, exceptions, security
├── user             # Users, profiles, auth (session, OTP)
├── trip             # Trips, members, join requests, Q&A, wishlist, reports
├── chat             # Conversations (1:1, group), messages, participants
└── notification     # User notifications (in-app), persistence + API
```

### 1.3 Event Flow (High Level)

- **Trip → Chat**: On trip publish, trip service publishes `TripPublishedEvent`; chat creates a group conversation and publishes `TripGroupChatCreatedEvent`; trip stores `conversationId`. On member join, trip publishes `ParticipantJoinedTripEvent`; chat adds user to that conversation.
- **Trip → Notification**: Trip and scheduler publish events; `TripNotificationEventListener` creates `UserNotificationEntity` records and serves them via Notification API.
- **Scheduler**: Cron jobs (draft reminder, upcoming trips) and auto status transitions (ongoing, completed) also publish events that drive notifications.

---

## 2. Database Tables (Entities)

All table names and main columns as defined in JPA entities.

### 2.1 User Module

| Table | Entity | Description |
|-------|--------|-------------|
| **users** | `UsersEntity` | Core user account. |
| **user_profile** | `UserProfileEntity` | Profile (name, picture, etc.). 1:1 with users. |
| **social_handle** | `SocialHandleEntity` | Social links. N:1 with users. |
| **verification** | `VerificationEntity` | OTP/verification. 1:1 with users. |
| **refresh_token** | `RefreshTokenEntity` | Refresh tokens. N:1 with users. |
| **user_report** | `UserReportEntity` | User reports. |
| **user_profile_history** | `UserProfileHistoryEntity` | Profile change history. |

**users**

| Column | Type | Notes |
|--------|------|-------|
| user_uuid | UUID | PK |
| country_code | varchar | e.g. +91 |
| email | varchar | Unique |
| mobile_number | varchar | Unique with country_code |
| role | enum | UserRole |
| account_status | enum | AccountStatus (e.g. ACTIVE) |
| created_at, updated_at | timestamp | |

---

### 2.2 Trip Module

| Table | Entity | Description |
|-------|--------|-------------|
| **core_trip_details** | `TripEntity` | Main trip. |
| **trip_policies** | `TripPolicyEntity` | Cancellation/refund. 1:1 with trip. |
| **trip_meta_data** | `TripMetaDataEntity` | Summary, included/excluded (JSON). 1:1 with trip. |
| **trip_itineraries** | `TripItineraryEntity` | Per-day itinerary. 1:N with trip. |
| **tags** | `TagEntity` | Tag master. |
| **trip_tag** | Join table | M:N trip–tag. |
| **trip_invites** | `TripInviteEntity` | Invites (in-app, link, email, phone). |
| **trip_join_requests** | `TripJoinRequestEntity` | Join requests (pending/approved/rejected). |
| **trip_members** | `TripMemberEntity` | Members (host, co-host, member; active/left/removed). |
| **trip_queries** | `TripQueryEntity` | Q&A (question, answer, asked_by). |
| **trip_wishlists** | `TripWishlistEntity` | User wishlist. (trip_id, user_id) unique. |
| **trip_reports** | `TripReportEntity` | Trip reports. (trip_id, reported_by) unique. |
| **task_lock** | `TaskLockEntity` | Scheduler/task locking. |

**core_trip_details**

| Column | Type | Notes |
|--------|------|-------|
| trip_id | UUID | PK |
| trip_title | varchar | |
| trip_description | varchar | |
| trip_destination | varchar | |
| trip_start_date, trip_end_date | date | |
| trip_status | enum | DRAFT, PUBLISHED, ONGOING, COMPLETED, CANCELLED |
| estimated_budget | double | |
| max_participants | int | |
| current_participants | int | Default 0 |
| is_full | boolean | Default false |
| trip_full_reason | varchar | |
| full_marked_at | timestamp | |
| join_policy | enum | OPEN, APPROVAL_REQUIRED |
| visibility_status | enum | PUBLIC, PRIVATE |
| conversation_id | UUID | FK to chat (group conversation). Set when trip is published. |
| created_at, updated_at | timestamp | |

**trip_members**

| Column | Type | Notes |
|--------|------|-------|
| membership_id | UUID | PK |
| trip_id | UUID | FK. Unique with user_id. |
| user_id | UUID | Logical FK |
| role | enum | HOST, CO_HOST, MEMBER |
| status | enum | ACTIVE, LEFT, REMOVED |
| joined_at | timestamp | |
| exited_at | timestamp | |
| exited_by | UUID | |
| removal_reason | varchar | |

**trip_join_requests**

| Column | Type | Notes |
|--------|------|-------|
| request_id | UUID | PK |
| trip_id | UUID | FK |
| user_id | UUID | Requestor |
| source | enum | DIRECT, INVITE |
| status | enum | PENDING, APPROVED, REJECTED, CANCELLED, AUTO_APPROVED |
| reviewed_by | UUID | |
| reviewed_at | timestamp | |
| created_at, updated_at | timestamp | |

**trip_queries**

| Column | Type | Notes |
|--------|------|-------|
| query_id | UUID | PK |
| trip_id | UUID | FK |
| asked_by | UUID | |
| question | varchar | |
| answer | varchar | |
| visibility | enum | HOST_ONLY, HOST_AND_CO_HOSTS |
| created_at | timestamp | |
| answered_at | timestamp | |

**trip_itineraries**

| Column | Type | Notes |
|--------|------|-------|
| itinerary_id | UUID | PK |
| trip_id | UUID | FK. Unique with day_number. |
| day_number | int | |
| title | varchar | |
| description | varchar | |
| activities, meals, stay | JSON | |
| created_at | timestamp | |

**trip_policies**

| Column | Type | Notes |
|--------|------|-------|
| trip_id | UUID | PK, FK |
| cancellation_policy | varchar(500) | |
| refund_policy | varchar(500) | |

**trip_meta_data**

| Column | Type | Notes |
|--------|------|-------|
| trip_id | UUID | PK, FK |
| trip_summary | JSON | |
| whats_included | JSON | |
| whats_excluded | JSON | |

**trip_invites**

| Column | Type | Notes |
|--------|------|-------|
| invite_id | UUID | PK |
| trip_id | UUID | FK |
| invited_by | UUID | |
| invite_type | enum | IN_APP, LINK, EMAIL, PHONE |
| invite_source | enum | DIRECT, TRAVELPAL |
| invited_user_id | UUID | Nullable |
| invited_email | varchar | Nullable |
| invited_phone | varchar | Nullable |
| token_hash | varchar | Unique |
| status | enum | PENDING, ACCEPTED, DECLINED, EXPIRED |
| expires_at, last_reminded_at | timestamp | |
| created_at | timestamp | |

**trip_wishlists**

| Column | Type | Notes |
|--------|------|-------|
| trip_wishlist_id | UUID | PK |
| trip_id | UUID | FK. Unique with user_id. |
| user_id | UUID | |
| created_at | timestamp | |

**trip_reports**

| Column | Type | Notes |
|--------|------|-------|
| report_id | UUID | PK |
| trip_id | UUID | FK. Unique with reported_by. |
| reported_by | UUID | |
| reason | varchar | |
| status | enum | e.g. OPEN |
| created_at | timestamp | |

**tags**

| Column | Type | Notes |
|--------|------|-------|
| tag_id | UUID | PK |
| tag_name | varchar | Unique |

---

### 2.3 Chat Module

| Table | Entity | Description |
|-------|--------|-------------|
| **conversation** | `ConversationEntity` | 1:1 or group chat. |
| **conversation_participant** | `ConversationParticipantEntity` | Participants. (conversation_id, user_id) unique. |
| **message** | `MessageEntity` | Messages. |
| **conversation_block** | `ConversationBlockEntity` | Block list. |
| **conversation_mute** | `ConversationMuteEntity` | Mute list. |

**conversation**

| Column | Type | Notes |
|--------|------|-------|
| conversation_id | UUID | PK |
| type | enum | ONE_ON_ONE, GROUP_CHAT |
| created_by | UUID | |
| created_at | timestamp | |
| name | varchar | For group only |

**conversation_participant**

| Column | Type | Notes |
|--------|------|-------|
| id | UUID | PK |
| conversation_id | UUID | FK |
| user_id | UUID | |
| role | enum | ConversationRole |
| joined_at | timestamp | |
| left_at | timestamp | Nullable = active |
| last_read_at | timestamp | |

**message**

| Column | Type | Notes |
|--------|------|-------|
| message_id | UUID | PK |
| conversation_id | UUID | FK |
| sender_id | UUID | Null = system message |
| content | varchar | |
| created_at | timestamp | |

---

### 2.4 Notification Module

| Table | Entity | Description |
|-------|--------|-------------|
| **user_notification** | `UserNotificationEntity` | In-app notifications. |

**user_notification**

| Column | Type | Notes |
|--------|------|-------|
| notification_id | UUID | PK |
| user_id | UUID | Recipient |
| trip_id | UUID | Optional, for trip-related notifications |
| type | varchar(50) | NotificationType enum name |
| title | varchar(255) | |
| body | varchar(1000) | |
| read_at | timestamp | Null = unread |
| created_at | timestamp | |

Indexes: `user_id`, `(user_id, read_at)`.

---

## 3. Enums (Key)

### Trip

- **TripStatus**: DRAFT, PUBLISHED, ONGOING, COMPLETED, CANCELLED  
- **JoinPolicy**: OPEN, APPROVAL_REQUIRED  
- **VisibilityStatus**: PUBLIC, PRIVATE  
- **TripMemberRole**: HOST, CO_HOST, MEMBER  
- **TripMemberStatus**: ACTIVE, LEFT, REMOVED  
- **JoinRequestStatus**: PENDING, APPROVED, REJECTED, CANCELLED, AUTO_APPROVED  
- **JoinRequestSource**: DIRECT, INVITE  
- **TripQueryVisibility**: HOST_ONLY, HOST_AND_CO_HOSTS  
- **TripReportStatus**: OPEN, etc.  
- **InviteType**, **InviteStatus**, **InviteSource**: for trip_invites |

### Notification

- **NotificationType**: DRAFT_TRIP_REMINDER, UPCOMING_TRIP, TRIP_COMPLETED, TRIP_CANCELLED, JOIN_REQUEST_RECEIVED, JOIN_REQUEST_APPROVED, JOIN_REQUEST_REJECTED, MEMBER_JOINED_TRIP, MEMBER_LEFT_OR_REMOVED_TRIP, MEMBER_PROMOTED_TO_CO_HOST, TRIP_FULL_CAPACITY_REACHED, TRIP_MARKED_FULL_BY_HOST, TRIP_DETAILS_CHANGED, TRIP_QUESTION_ASKED, TRIP_QUESTION_ANSWERED

### Chat

- **ConversationType**: ONE_ON_ONE, GROUP_CHAT  
- **ConversationRole**: ADMIN, MEMBER (etc.)  
- **MessageType**: TEXT, IMAGE, FILE, SYSTEM (if used) |

---

## 4. Events (Commons)

All in `com.tranzo.tranzo_user_ms.commons.events`. Published by Trip/Scheduler; consumed by Chat and/or Notification.

| Event | Publisher | Consumer | Purpose |
|-------|-----------|----------|---------|
| TripPublishedEvent | Trip | Chat | Create group conversation; trip stores conversationId via TripGroupChatCreatedEvent. |
| TripGroupChatCreatedEvent | Chat | Trip | Trip saves conversationId. |
| ParticipantJoinedTripEvent | Trip | Chat | Add user to trip’s group conversation. |
| JoinRequestCreatedEvent | Trip | Notification | Notify host. |
| JoinRequestApprovedEvent | Trip | Notification | Notify requestor. |
| JoinRequestRejectedEvent | Trip | Notification | Notify requestor. |
| MemberJoinedTripEvent | Trip | Notification | Notify other members. |
| MemberLeftOrRemovedTripEvent | Trip | Notification | Notify other members. |
| MemberPromotedToCoHostEvent | Trip | Notification | Notify all members. |
| TripFullCapacityReachedEvent | Trip | Notification | Notify all members (incl. host). |
| TripMarkedFullByHostEvent | Trip | Notification | Notify all members except host. |
| TripDetailsChangedEvent | Trip | Notification | Notify all members. |
| TripQuestionAskedEvent | Trip | Notification | Notify all members. |
| TripQuestionAnsweredEvent | Trip | Notification | Notify asker. |
| TripCancelledEvent | Trip | Notification | Notify all active members. |
| TripCompletedEvent | Trip (scheduler) | Notification | Notify all active members. |
| UpcomingTripEvent | Trip (scheduler) | Notification | Notify members (e.g. next 3 days). |
| DraftTripReminderEvent | Trip (scheduler) | Notification | Notify host. |

---

## 5. REST API Summary

Base URL: `http://localhost:8085` (no context path in default config). All authenticated endpoints use JWT (userId from token).

### 5.1 Trips – `/trips`

| Method | Path | Description |
|--------|------|-------------|
| POST | / | Create draft trip |
| PUT | /{tripId} | Update draft trip |
| GET | /{tripId} | Get trip details (view) |
| DELETE | /{tripId} | Cancel trip |
| POST | /{tripId}/publish | Publish draft |
| PATCH | /{tripId} | Update published/ongoing trip |
| POST | /{tripId}/qna | Add Q&A (host) |
| POST | /{tripId}/qna/{qnaId}/answer | Answer Q&A |
| GET | /{tripId}/qna | List Q&A |
| POST | /{tripId}/reports | Report trip |
| POST | /{tripId}/participants/{participantUserId}/promote-cohost | Promote to co-host |
| POST | /{tripId}/mark-full | Mark trip full (host) |

### 5.2 Join Requests – no base path (root)

| Method | Path | Description |
|--------|------|-------------|
| POST | /trips/{tripId}/join-requests | Create join request |
| POST | /join-requests/{id}/approve | Approve (host) |
| POST | /join-requests/{id}/reject | Reject (host) |
| GET | /trips/{tripId}/join-requests | List join requests (host, optional status) |
| DELETE | /join-requests/{id}/cancel | Cancel own request |
| DELETE | /trips/{tripId}/participants/{participantUserId} | Leave or remove participant (body: RemoveParticipantRequestDto) |

### 5.3 Wishlist – `/users/me/wishlist`

| Method | Path | Description |
|--------|------|-------------|
| POST | / | Add to wishlist |
| DELETE | /{tripId} | Remove from wishlist |
| GET | / | Get my wishlist |

### 5.4 Notifications – `/notifications`

| Method | Path | Description |
|--------|------|-------------|
| GET | / | Get my notifications (page, size) |
| GET | /unread-count | Unread count |
| PATCH | /{notificationId}/read | Mark one read |
| PATCH | /read-all | Mark all read |

### 5.5 Chat – `/conversations`

| Method | Path | Description |
|--------|------|-------------|
| POST | /one-to-one | Create 1:1 conversation |
| GET | /chat-list | My conversations |
| POST | /{conversationId}/send-message | Send message |
| GET | /{conversationId}/messages | Get messages (pagination) |
| PATCH | /{conversationId}/read | Mark as read |
| POST | /{conversationId}/mute | Mute |
| POST | /{conversationId}/unmute | Unmute |
| POST | /{conversationId}/block | Block |
| POST | /{conversationId}/unblock | Unblock |

(WebSocket for real-time chat is separate; see ChatSocketController if present.)

### 5.6 User – no base path (root)

| Method | Path | Description |
|--------|------|-------------|
| GET | /user | Get current user profile |
| POST | /user/create | Register user |
| PATCH | /user/update | Update profile |
| DELETE | /user/delete-user | Delete user |
| PUT | /user/profile-picture | Update profile picture |
| DELETE | /user/profile-picture | Remove profile picture |
| PATCH | /user/social-handles | Upsert social handles |
| POST | /User/{reportedUserId}/report | Report user |

### 5.7 Auth – `/auth/session`, `/auth/otp`

| Method | Path | Description |
|--------|------|-------------|
| POST | /auth/session/login | Login |
| POST | /auth/session/refresh | Refresh token |
| POST | /auth/session/logout | Logout |
| POST | /auth/otp/request | Request OTP |
| POST | /auth/otp/verify | Verify OTP |

---

## 6. Configuration (application.yaml)

- **Server**: port 8085.  
- **DB**: H2 in-memory `jdbc:h2:mem:tranzo_user_db`, `ddl-auto: create`, H2 console at `/h2-console`.  
- **Redis**: localhost:6379 (e.g. session/cache).  
- **JWT**: secret, access-token-expiry-minutes (15), refresh-token-expiry-days (7), issuer.  
- **Logging**: SQL and transaction debug in dev.  
- Optional: `trip.notification.draft-reminder-cron`, `upcoming-cron` for scheduler.

---

## 7. Trip Lifecycle (Summary)

1. **Create** → DRAFT.  
2. **Update** → Only draft updatable (title, dates, policy, metadata, itinerary, tags).  
3. **Publish** → PUBLISHED; validation; Chat creates group conversation; trip stores conversationId.  
4. **Join**  
   - OPEN: create join request → AUTO_APPROVED → add member, publish ParticipantJoined + MemberJoinedTrip (and TripFullCapacityReached if full).  
   - APPROVAL_REQUIRED: create join request → PENDING → notify host; host approves/rejects → notify requestor and optionally MemberJoinedTrip / TripFullCapacityReached.  
5. **Leave/Remove** → Member status LEFT/REMOVED; MemberLeftOrRemovedTripEvent to other members.  
6. **Scheduler**  
   - Draft reminder → DraftTripReminderEvent → host.  
   - Upcoming (e.g. next 3 days) → UpcomingTripEvent → members.  
   - Start date reached → PUBLISHED → ONGOING.  
   - End date passed → ONGOING → COMPLETED; TripCompletedEvent → members.  
7. **Cancel** → CANCELLED (from DRAFT or PUBLISHED); TripCancelledEvent → active members.  
8. **Mark full** → Manually (host) or when current_participants = max_participants; corresponding events to members.

---

## 8. Notification Rules (Summary)

- **Host**: Draft reminder, join request received.  
- **Requestor**: Join approved, join rejected.  
- **All other members**: Member joined, member left/removed, member promoted to co-host, trip full (capacity or host-marked), trip details changed, question asked, trip cancelled, trip completed, upcoming trip.  
- **Asker only**: Question answered.

---

*Document generated for tranzo-user-ms. Keep in sync with code and schema changes.*
