# Tranzo User MS – API Contract (All Controllers)

Base URL: `http://localhost:8089` (or configured `server.port`). No context path.

**Common response wrapper** (most REST endpoints):

```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Message text",
  "data": { ... }
}
```

Error responses use `status`: `"ERROR"` or `"FAILURE"`; `data` is often `null`. **Auth: Required** = valid JWT (e.g. `Authorization: Bearer <token>`).

---

## Controllers index

| # | Controller | Base path / scope |
|---|------------|-------------------|
| 1 | SessionController | `/auth/session` |
| 2 | OtpController | `/auth/otp` |
| 3 | AadharController | `/aadhaar/otp` |
| 4 | UserController | `/user`, `/user/register`, etc. |
| 5 | PublicProfileController | `/public/profile` |
| 6 | TravelPalController | `/travel-pal` |
| 7 | RatingController | `/trips/{tripId}/ratings` |
| 8 | TripManagementController | `/trips` |
| 9 | TripJoinRequestController | `/trips`, `/join-requests` |
| 10 | TripWishlistController | `/users/me/wishlist` |
| 11 | CreateAndManageChatController | `/conversations` |
| 12 | NotificationController | `/notifications` |
| 13 | MediaController | `/media` |
| 14 | SplitwiseGroupController | `/api/splitwise/groups` |
| 15 | ExpenseController | `/api/splitwise/expenses` |
| 16 | SettlementController | `/api/splitwise/settlements` |
| 17 | BalanceController | `/api/splitwise/balances` |
| 18 | ActivityController | `/api/splitwise/activities` |
| 19 | ChatSocketController | WebSocket `/app/chat.send` |

---

## 1. SessionController

**Base path:** `/auth/session`  
**Auth:** Login/logout no token; refresh uses refresh token.

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/auth/session/login` | No | Create session (login). Body: `SessionRequestDto`. Response: `ResponseDto<SessionResponseDto>`. |
| POST | `/auth/session/refresh` | Refresh token | Refresh access token. Response: `ResponseDto<SessionResponseDto>`. |
| POST | `/auth/session/logout` | No | Logout. Response: `ResponseDto` (data null). |

---

## 2. OtpController

**Base path:** `/auth/otp`  
**Auth:** Not required for request/verify.

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/auth/otp/request` | No | Request OTP. Body: `RequestOtpDto`. Response: `ResponseDto<Void>`. |
| POST | `/auth/otp/verify` | No | Verify OTP, get tokens. Body: `VerifyOtpDto`. Response: `ResponseDto<VerifyOtpResponseDto>`. |

---

## 3. AadharController

**Base path:** `/aadhaar/otp`  
**Auth:** Required.

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/aadhaar/otp/request` | Yes | Request Aadhaar OTP. Body: `AadharNumberDto`. Response: `ResponseDto<Void>`. |

---

## 4. UserController

**Base path:** None (absolute paths).  
**Auth:** Required except register (register may use registration token).

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/user` | Yes | Get current user profile. Response: `ResponseDto<UserProfileDto>`. |
| POST | `/user/register` | No* | Register. Content-Type: `application/json` or `multipart/form-data`. Body: `UserProfileDto` (+ optional file). Response: `ResponseDto<UserProfileDto>`. |
| PATCH | `/user/update` | Yes | Update profile. Multipart: `profile` (UserProfileDto), `file` (optional). Response: `ResponseDto<UserProfileDto>`. |
| DELETE | `/user/delete-user` | Yes | Delete current user. Response: `ResponseDto<Void>`. |
| PUT | `/user/profile-picture` | Yes | Set profile picture URL. Body: `UrlDto`. Response: `ResponseDto<UserProfileDto>`. |
| DELETE | `/user/profile-picture` | Yes | Remove profile picture. Response: `ResponseDto<UserProfileDto>`. |
| PATCH | `/user/social-handles` | Yes | Upsert social handles. Body: `List<SocialHandleDto>`. Response: `ResponseDto<UserProfileDto>`. |
| POST | `/user/{reportedUserId}/report` | Yes | Report user. Path: `reportedUserId` (string). Body: `UserReportRequestDto`. Response: `ResponseDto<Void>`. |

---

## 5. PublicProfileController

**Base path:** `/public/profile`  
**Auth:** Not required.

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/public/profile/{userId}` | No | Get public profile. Query: `page` (default 0), `size` (default 20). Response: `ResponseDto<PublicProfileResponseDto>`. |

---

## 6. TravelPalController

**Base path:** `/travel-pal`  
**Auth:** Required for all.

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/travel-pal/request/{receiverId}` | Yes | Send travel pal request. Response: 200 plain text. |
| POST | `/travel-pal/accept/{requesterId}` | Yes | Accept request. Response: 200 plain text. |
| POST | `/travel-pal/reject/{requesterId}` | Yes | Reject request. Response: 200 plain text. |
| DELETE | `/travel-pal/{otherUserId}` | Yes | Remove travel pal. Response: 200 plain text. |
| GET | `/travel-pal/my` | Yes | Get my travel pals. Response: `ResponseDto<List<UUID>>`. |
| GET | `/travel-pal/pending` | Yes | Get incoming pending requests. Response: `ResponseDto<?>`. |

---

## 7. RatingController

**Base path:** `/trips/{tripId}/ratings`  
**Auth:** Required for all.

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| PUT | `/trips/{tripId}/ratings/trip` | Yes | Submit trip rating. Body: `SubmitTripRatingRequest`. Response: `ResponseDto<Void>`. |
| PUT | `/trips/{tripId}/ratings/host` | Yes | Submit host rating. Body: `SubmitHostRatingRequest`. Response: `ResponseDto<Void>`. |
| PUT | `/trips/{tripId}/ratings/members` | Yes | Submit member ratings. Body: `SubmitMemberRatingsRequest`. Response: `ResponseDto<Void>`. |

---

## 8. TripManagementController

**Base path:** `/trips`  
**Auth:** Required for all.

| Method | Path | Description |
|--------|------|-------------|
| POST | `/trips/` | Create draft trip. Body: `TripDto` (DraftChecks). Response: 201 `ResponseDto<TripResponseDto>`. |
| PUT | `/trips/{tripId}` | Update draft trip. Body: `TripDto`. Response: `ResponseDto<TripResponseDto>`. |
| GET | `/trips/{tripId}` | Get trip details. Response: `ResponseDto<TripViewDto>`. |
| GET | `/trips/{tripId}/members` | Get trip members (host, co-hosts, list, totalJoined). Response: `ResponseDto<TripMembersListResponseDto>`. |
| GET | `/trips/mutual-with/{otherUserId}` | Get mutual completed trips. Response: `ResponseDto<List<TripViewDto>>`. |
| GET | `/trips/user` | Get trips for current user. Response: `ResponseDto<List<TripViewDto>>`. |
| GET | `/trips` | Get all completed trips. Response: `ResponseDto<List<TripViewDto>>`. |
| DELETE | `/trips/{tripId}` | Cancel trip. Response: `ResponseDto<Void>`. |
| POST | `/trips/{tripId}/publish` | Publish draft. Response: `ResponseDto<TripResponseDto>`. |
| PATCH | `/trips/{tripId}` | Update published trip. Body: `TripDto`. Response: `ResponseDto<TripResponseDto>`. |
| POST | `/trips/{tripId}/qna` | Add trip QnA. Body: `CreateQnaRequestDto`. Response: `ResponseDto<Void>`. |
| POST | `/trips/{tripId}/qna/{qnaId}/answer` | Answer QnA. Body: `AnswerQnaRequestDto`. Response: `ResponseDto<Void>`. |
| GET | `/trips/{tripId}/qna` | Get trip QnA list. Response: `ResponseDto<List<TripQnaResponseDto>>`. |
| POST | `/trips/{tripId}/reports` | Report trip. Body: `ReportTripRequestDto`. Response: `ResponseDto<Void>`. |
| POST | `/trips/{tripId}/participants/{participantUserId}/promote-cohost` | Promote to co-host. Response: `ResponseDto<Void>`. |
| POST | `/trips/{tripId}/mark-full` | Mark trip full. Response: `ResponseDto<Void>`. |
| POST | `/trips/{tripId}/invites/travel-pal/bulk` | Invite all travel pals. Response: `ResponseDto<Void>`. |
| POST | `/trips/{tripId}/invites/travel-pal/{travelPalUserId}` | Invite one travel pal. Response: `ResponseDto<Void>`. |
| POST | `/trips/{tripId}/broadcast` | Broadcast trip to travel pals. Response: `ResponseDto<Void>`. |
| POST | `/trips/search` | Search trips. Body: `SearchRequest`. Response: `ResponseDto<Page<TripViewDto>>`. |

**GET `/trips/{tripId}/members` – data shape:** `tripId`, `hostUserId`, `coHostUserIds` (List<UUID>), `members` (List of `membershipId`, `userId`, `role` (HOST|CO_HOST|MEMBER), `joinedAt`), `totalJoined` (int).

---

## 9. TripJoinRequestController

**Base path:** No class-level path; paths are absolute.  
**Auth:** Required for all.

| Method | Path | Description |
|--------|------|-------------|
| POST | `/trips/{tripId}/join-requests` | Create join request. Body: `TripJoinRequestDto`. Response: 201 `ResponseDto<TripJoinRequestResponseDto>`. |
| POST | `/join-requests/{id}/approve` | Approve join request. Response: `ResponseDto<TripJoinRequestResponseDto>`. |
| POST | `/join-requests/{id}/reject` | Reject join request. Response: `ResponseDto<TripJoinRequestResponseDto>`. |
| GET | `/trips/{tripId}/join-requests` | Get join requests. Query: `status` (optional). Response: `ResponseDto<List<TripJoinRequestResponseDto>>`. |
| DELETE | `/join-requests/{id}/cancel` | Cancel own join request. Response: `ResponseDto<Void>`. |
| DELETE | `/trips/{tripId}/participants/{participantUserId}` | Remove participant or leave trip. Body: `RemoveParticipantRequestDto`. Response: `ResponseDto<Void>`. |

---

## 10. TripWishlistController

**Base path:** `/users/me/wishlist`  
**Auth:** Required for all.

| Method | Path | Description |
|--------|------|-------------|
| POST | `/users/me/wishlist/` | Add trip to wishlist. Body: `TripWishlistRequestDto`. Response: 201 `ResponseDto<TripWishlistResponseDto>`. |
| DELETE | `/users/me/wishlist/{tripId}` | Remove from wishlist. Response: `ResponseDto<TripWishlistResponseDto>`. |
| GET | `/users/me/wishlist/` | Get my wishlist. Response: `ResponseDto<List<TripWishlistResponseDto>>`. |

---

## 11. CreateAndManageChatController

**Base path:** `/conversations`  
**Auth:** Required for all.

| Method | Path | Description |
|--------|------|-------------|
| POST | `/conversations/{conversationId}/send-message` | Send message. Body: `SendMessageRequestDto` (`content`). Response: 201 `ResponseDto<SendMessageResponseDto>`. |
| POST | `/conversations/one-to-one` | Create 1:1 conversation. Body: `CreateConversationRequestDto` (`otherUserId`). Response: `ResponseDto<CreateConversationResponseDto>`. |
| GET | `/conversations/chat-list` | Get my chat list. Response: `ResponseDto<List<ChatListItemDto>>`. |
| GET | `/conversations/{conversationId}/messages` | Get messages. Query: `before` (optional ISO-8601), `limit` (optional 1–100, default 30). Response: `ResponseDto<List<MessageResponseDto>>`. |
| PATCH | `/conversations/{conversationId}/read` | Mark as read. Response: `ResponseDto<Void>`. |
| POST | `/conversations/{conversationId}/mute` | Mute conversation. Response: `ResponseDto<Void>`. |
| POST | `/conversations/{conversationId}/unmute` | Unmute. Response: `ResponseDto<Void>`. |
| POST | `/conversations/{conversationId}/block` | Block conversation. Response: `ResponseDto<Void>`. |
| POST | `/conversations/{conversationId}/unblock` | Unblock. Response: `ResponseDto<Void>`. |

**MessageResponseDto:** `messageId`, `conversationId`, `senderId`, `content`, `createdAt`.

---

## 12. NotificationController

**Base path:** `/notifications`  
**Auth:** Required for all.

| Method | Path | Description |
|--------|------|-------------|
| GET | `/notifications` | Get my notifications. Query: `page` (default 0), `size` (default 20). Response: `ResponseDto<Page<NotificationResponseDto>>`. |
| GET | `/notifications/unread-count` | Get unread count. Response: `ResponseDto<Long>`. |
| PATCH | `/notifications/{notificationId}/read` | Mark one as read. Response: `ResponseDto<Void>`. |
| PATCH | `/notifications/read-all` | Mark all as read. Response: `ResponseDto<Void>`. |

---

## 13. MediaController

**Base path:** `/media`  
**Auth:** Required for all.

| Method | Path | Description |
|--------|------|-------------|
| POST | `/media/upload` | Upload file. Request: multipart `file` (required). Response: `ResponseDto<UploadResponseDto>`. 400 no file; 503 S3 not configured. |
| GET | `/media/url` | Get presigned URL. Query: `key` (required), `expiryMinutes` (optional). Response: `ResponseDto<PresignedUrlResponseDto>`. 400 missing key; 503 S3 not configured. |

---

## 14. SplitwiseGroupController

**Base path:** `/api/splitwise/groups`  
**Auth:** Required for all. **Response format:** Raw DTOs (no `ResponseDto` wrapper).

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/splitwise/groups` | Create group. Body: `CreateGroupRequest`. Response: `GroupResponse`. |
| GET | `/api/splitwise/groups/{groupId}` | Get group. Response: `GroupResponse`. |
| PUT | `/api/splitwise/groups/{groupId}` | Update group. Body: `CreateGroupRequest`. Response: `GroupResponse`. |
| DELETE | `/api/splitwise/groups/{groupId}` | Delete group. Response: 204 No Content. |
| GET | `/api/splitwise/groups/my-groups` | Get my groups. Response: `List<GroupResponse>`. |
| POST | `/api/splitwise/groups/{groupId}/members` | Add members. Body: `AddGroupMemberRequest`. Response: `GroupResponse`. |
| DELETE | `/api/splitwise/groups/{groupId}/members/{memberId}` | Remove member. Response: `GroupResponse`. |
| GET | `/api/splitwise/groups/{groupId}/members` | Get group members. Response: `GroupResponse` (includes members). |

---

## 15. ExpenseController

**Base path:** `/api/splitwise/expenses`  
**Auth:** Required for all. **Response format:** Raw DTOs.

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/splitwise/expenses` | Create expense. Body: `CreateExpenseRequest`. Response: `ExpenseResponse`. |
| GET | `/api/splitwise/expenses/{expenseId}` | Get expense. Response: `ExpenseResponse`. |
| PUT | `/api/splitwise/expenses/{expenseId}` | Update expense. Body: `UpdateExpenseRequest`. Response: `ExpenseResponse`. |
| DELETE | `/api/splitwise/expenses/{expenseId}` | Delete expense. Response: 204 No Content. |
| GET | `/api/splitwise/expenses/group/{groupId}` | Get group expenses. Response: `List<ExpenseResponse>`. |
| GET | `/api/splitwise/expenses/my-expenses` | Get my expenses. Response: `List<ExpenseResponse>`. |

---

## 16. SettlementController

**Base path:** `/api/splitwise/settlements`  
**Auth:** Required for POST and my-settlements.

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/splitwise/settlements` | Create settlement. Body: `CreateSettlementRequest`. Response: `SettlementResponse`. |
| GET | `/api/splitwise/settlements/{settlementId}` | Get settlement. Response: `SettlementResponse`. |
| GET | `/api/splitwise/settlements/group/{groupId}` | Get group settlements. Response: `List<SettlementResponse>`. |
| GET | `/api/splitwise/settlements/my-settlements` | Get my settlements. Response: `List<SettlementResponse>`. |
| GET | `/api/splitwise/settlements/optimize/{groupId}` | Get optimized settlement proposals. Response: `List<SettlementProposal>`. |

---

## 17. BalanceController

**Base path:** `/api/splitwise/balances`  
**Auth:** Required for my-balance.

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/splitwise/balances/group/{groupId}` | Get balances for all users in group. Response: `List<BalanceResponse>`. |
| GET | `/api/splitwise/balances/group/{groupId}/user/{userId}` | Get balance for user in group. Response: `BalanceResponse`. |
| GET | `/api/splitwise/balances/group/{groupId}/my-balance` | Get my balance in group. Response: `BalanceResponse`. |

---

## 18. ActivityController

**Base path:** `/api/splitwise/activities`  
**Auth:** my-activities requires auth; group activities may vary.

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/splitwise/activities/group/{groupId}` | Get group activities. Query: `limit` (default 10), `offset` (default 0). Response: `List<ActivityResponse>`. |
| GET | `/api/splitwise/activities/my-activities` | Get my activities. Query: `limit`, `offset`. Response: `List<ActivityResponse>`. 401 if not authenticated. |

---

## 19. ChatSocketController (WebSocket)

**Type:** STOMP WebSocket (not REST).  
**Auth:** Principal from WebSocket handshake (user id).

| Message mapping | Description |
|------------------|-------------|
| `/app/chat.send` | Send chat message. Payload: `ChatMessageRequest` – `conversationId`, `content`. Server processes and may broadcast to conversation subscribers. |

---

## Error response format

Structured errors (e.g. `GlobalExceptionHandler`):

```json
{
  "statusCode": 400,
  "status": "ERROR",
  "statusMessage": "Human-readable message",
  "data": null
}
```

- **400** – Validation, bad request (`BadRequestException`, `TripPublishException`). Validation errors may have field → message map in `data`.
- **401** – Unauthenticated (`AuthException`).
- **403** – Forbidden (e.g. private trip, cancelled trip non-host).
- **404** – Not found (trip, conversation, join request, etc.).
- **500** – Unhandled exception.
