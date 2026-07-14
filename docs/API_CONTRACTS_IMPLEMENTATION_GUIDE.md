# Tranzo User MS – API Contracts Implementation Guide

**Date:** July 14, 2026  
**Version:** 1.0  
**Total Contracts:** 7 modules with 60+ endpoints

---

## 📚 Quick Navigation

### Module Documents

| Module | File | Endpoints | Status Codes |
|--------|------|-----------|--------------|
| **Master Index** | `API_CONTRACTS_COMPLETE.md` | Overview | Reference only |
| **Authentication** | `API_CONTRACT_AUTHENTICATION.md` | 6 | Session, OTP, Aadhaar |
| **User Management** | `API_CONTRACT_USER_MANAGEMENT.md` | 19 | Users, Profiles, Travel Pals, Ratings |
| **Trip Management** | `API_CONTRACT_TRIP_MANAGEMENT.md` | 32 | Trips, Joins, Wishlist, Discovery, AI |
| **Chat** | `API_CONTRACT_CHAT.md` | 10 | Conversations, Messages, WebSocket |
| **Notifications** | `API_CONTRACT_NOTIFICATIONS.md` | 4 | Notifications, Unread Count |
| **Splitwise** | `API_CONTRACT_SPLITWISE.md` | 25+ | Groups, Expenses, Settlements, Balances |
| **Media** | `API_CONTRACT_MEDIA.md` | 2 | Upload, Presigned URLs |

**Total: 60+ REST endpoints + 1 WebSocket endpoint**

---

## 🚀 Getting Started

### Step 1: Understand Response Formats

**Type A: ResponseDto Wrapper** (Most endpoints)
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Message",
  "data": { /* resource data */ }
}
```

**Used by:** Authentication, User, Trip, Chat, Notification, Media

---

**Type B: Raw DTO** (Splitwise only)
```json
{
  "id": "1",
  "name": "Group Name",
  /* resource-specific fields */
}
```

**Used by:** SplitwiseGroupController, ExpenseController, SettlementController, BalanceController, ActivityController

---

### Step 2: Check Authentication Required

**No Auth Required:**
- POST /auth/session/login
- POST /auth/otp/request
- POST /auth/otp/verify
- POST /user/register
- GET /public/profile/{userId}

**Auth Required (JWT):**
- All other endpoints
- Header: `Authorization: Bearer <access_token>`
- Obtain token from: POST /auth/session/login or POST /auth/otp/verify

---

### Step 3: Follow HTTP Methods

| Method | Semantics | Status Code |
|--------|-----------|-------------|
| **GET** | Retrieve resource | 200 OK |
| **POST** | Create resource | 201 Created |
| **PUT** | Replace resource | 200 OK |
| **PATCH** | Partial update | 200 OK |
| **DELETE** | Remove resource | 204 No Content |

---

## 📖 Module-by-Module Guide

### 1️⃣ Authentication Module

**Start Here:** `API_CONTRACT_AUTHENTICATION.md`

**Entry Points:**
1. **POST /auth/session/login** → Get access token + refresh token
2. **POST /auth/otp/request** → Request OTP-based login
3. **POST /auth/otp/verify** → Verify OTP, get tokens

**Key Status Codes:**
- 200 OK (success)
- 400 Bad Request (validation error)
- 401 Unauthorized (invalid credentials)
- 429 Too Many Requests (rate limited)
- 503 Service Unavailable (SMS/email service down)

**Example Flow:**
```
1. User enters email/mobile
2. POST /auth/otp/request → Get request ID
3. User receives OTP via SMS
4. POST /auth/otp/verify (with OTP) → Get tokens
5. Use access token for all subsequent requests
```

---

### 2️⃣ User Management Module

**Start Here:** `API_CONTRACT_USER_MANAGEMENT.md`

**Core Operations:**
- **GET /user** → Get current user profile
- **POST /user/register** → Register new user
- **PATCH /user/update** → Update profile
- **DELETE /user/delete-user** → Delete account
- **PUT /user/profile-picture** → Set profile picture
- **PATCH /user/social-handles** → Add social links

**Travel Pal System:**
- **POST /travel-pal/request/{userId}** → Send friend request
- **POST /travel-pal/accept/{userId}** → Accept request
- **GET /travel-pal/my** → Get friends list

**Rating System:**
- **PUT /trips/{tripId}/ratings/trip** → Rate trip
- **PUT /trips/{tripId}/ratings/host** → Rate host
- **PUT /trips/{tripId}/ratings/members** → Rate members

**Key Status Codes:**
- 200 OK (success)
- 201 Created (registered)
- 400 Bad Request (validation)
- 409 Conflict (email/phone already exists)
- 503 Service Unavailable (S3/email service down)

---

### 3️⃣ Trip Management Module

**Start Here:** `API_CONTRACT_TRIP_MANAGEMENT.md`

**Trip Lifecycle:**
```
DRAFT (editable)
    ↓
PUBLISHED (public, joinable)
    ↓
ONGOING (trip started, no new members)
    ↓
COMPLETED (can be rated, closed)

[CANCELLED] (anytime from DRAFT/PUBLISHED)
```

**Create Trip:**
```
1. POST /trips/ (multipart) → Create DRAFT
2. PUT /trips/{id} (multipart) → Update DRAFT
3. POST /trips/{id}/publish → Transition to PUBLISHED
4. PATCH /trips/{id} → Update published (limited fields)
```

**Manage Members:**
- **POST /trips/{id}/join-requests** → Create join request
- **POST /join-requests/{id}/approve** → Host approves
- **POST /join-requests/{id}/reject** → Host rejects
- **DELETE /trips/{id}/participants/{userId}** → Remove member
- **POST /trips/{id}/participants/{userId}/promote-cohost** → Promote

**Q&A System:**
- **POST /trips/{id}/qna** → Add question
- **POST /trips/{id}/qna/{qId}/answer** → Answer question
- **GET /trips/{id}/qna** → View all Q&A

**Discovery:**
- **GET /trips/featured** → Featured trips
- **GET /trips/trending-destinations** → Trending places
- **POST /trips/search** → Search with filters
- **GET /trips/mutual-with/{userId}** → Common trips

**Wishlist:**
- **POST /users/me/wishlist** → Add to wishlist
- **DELETE /users/me/wishlist/{tripId}** → Remove from wishlist
- **GET /users/me/wishlist** → Get wishlist

**Key Status Codes:**
- 200 OK (success)
- 201 Created (trip/request created)
- 400 Bad Request (validation, state conflict)
- 401 Unauthorized
- 403 Forbidden (not host/co-host)
- 404 Not Found (resource not found)
- 409 Conflict (already published, trip full)
- 503 Service Unavailable (S3, AI service down)

---

### 4️⃣ Chat Module

**Start Here:** `API_CONTRACT_CHAT.md`

**Conversation Types:**
- **ONE_ON_ONE:** Private 1:1 between two users
- **GROUP_CHAT:** Group created by trip publish

**Core Operations:**
- **POST /conversations/one-to-one** → Create 1:1 or get existing
- **GET /conversations/chat-list** → Get all conversations
- **POST /conversations/{id}/send-message** → Send message
- **GET /conversations/{id}/messages** → Get paginated messages

**Message Management:**
- **PATCH /conversations/{id}/read** → Mark as read
- **POST /conversations/{id}/mute** → Mute notifications
- **POST /conversations/{id}/unmute** → Unmute
- **POST /conversations/{id}/block** → Block (1:1 only)
- **POST /conversations/{id}/unblock** → Unblock

**Real-Time (WebSocket):**
- **STOMP /app/chat.send** → Send message in real-time
- Subscribe to `/user/queue/reply` for incoming messages

**Key Status Codes:**
- 200/201 OK/Created (success)
- 400 Bad Request (validation, self-chat)
- 401 Unauthorized
- 403 Forbidden (not a participant)
- 404 Not Found (conversation/message not found)
- 409 Conflict (already muted/blocked)

---

### 5️⃣ Notifications Module

**Start Here:** `API_CONTRACT_NOTIFICATIONS.md`

**Notification Types:**
- Trip-related: DRAFT_TRIP_REMINDER, UPCOMING_TRIP, TRIP_COMPLETED, TRIP_CANCELLED
- Member-related: MEMBER_JOINED, MEMBER_LEFT, MEMBER_PROMOTED
- Join requests: REQUEST_RECEIVED, REQUEST_APPROVED, REQUEST_REJECTED
- Trip events: DETAILS_CHANGED, QUESTION_ASKED, QUESTION_ANSWERED

**Operations:**
- **GET /notifications** → Paginated list (supports filtering by read status)
- **GET /notifications/unread-count** → Get count of unread
- **PATCH /notifications/{id}/read** → Mark one as read
- **PATCH /notifications/read-all** → Mark all as read

**Key Status Codes:**
- 200 OK (success)
- 400 Bad Request (invalid pagination)
- 401 Unauthorized
- 403 Forbidden (not owner)
- 404 Not Found (notification not found)
- 409 Conflict (already read)

---

### 6️⃣ Splitwise Module

**Start Here:** `API_CONTRACT_SPLITWISE.md`

**⚠️ Important:** Raw DTO response format (no ResponseDto wrapper)

**Group Management:**
- **POST /api/splitwise/groups** → Create group
- **GET /api/splitwise/groups/{id}** → Get group details
- **PUT /api/splitwise/groups/{id}** → Update group
- **DELETE /api/splitwise/groups/{id}** → Delete group
- **GET /api/splitwise/groups/my-groups** → Get user's groups
- **POST /api/splitwise/groups/{id}/members** → Add members
- **DELETE /api/splitwise/groups/{id}/members/{memberId}** → Remove member

**Expense Tracking:**
- **POST /api/splitwise/expenses** → Create expense with splits
- **GET /api/splitwise/expenses/{id}** → Get expense
- **PUT /api/splitwise/expenses/{id}** → Update expense
- **DELETE /api/splitwise/expenses/{id}** → Delete expense
- **GET /api/splitwise/expenses/group/{id}** → Get group expenses
- **GET /api/splitwise/expenses/my-expenses** → Get user's expenses

**Payment Settlements:**
- **POST /api/splitwise/settlements** → Record payment
- **GET /api/splitwise/settlements/{id}** → Get settlement
- **GET /api/splitwise/settlements/group/{id}** → Get group settlements
- **GET /api/splitwise/settlements/my-settlements** → Get user's settlements
- **GET /api/splitwise/settlements/optimize/{id}** → Get optimized settlement proposals

**Balance Calculations:**
- **GET /api/splitwise/balances/group/{id}** → Get all balances in group
- **GET /api/splitwise/balances/group/{id}/my-balance** → Get user's balance
- **GET /api/splitwise/balances/dashboard** → Get overall dashboard

**Activity Log:**
- **GET /api/splitwise/activities/group/{id}** → Get group activity
- **GET /api/splitwise/activities/my-activities** → Get user's activity

**Key Status Codes:**
- 200 OK (success)
- 204 No Content (delete success)
- 400 Bad Request (validation, state conflict)
- 401 Unauthorized
- 403 Forbidden (not member/creator)
- 404 Not Found (resource not found)

---

### 7️⃣ Media Module

**Start Here:** `API_CONTRACT_MEDIA.md`

**File Operations:**
- **POST /media/upload** → Upload file to S3 (max 10MB)
- **GET /media/url** → Get presigned URL (time-limited access)

**Supported Formats:**
- JPEG, PNG, GIF, WebP
- Max 10 MB per file

**Key Status Codes:**
- 200 OK (success)
- 400 Bad Request (validation, file too large)
- 401 Unauthorized
- 403 Forbidden (no permission to access)
- 415 Unsupported Media Type (not multipart/form-data)
- 503 Service Unavailable (S3 down)

---

## 🧭 Common Flows

### Flow 1: User Registration & First Login

```
1. POST /auth/otp/request (email or mobile)
   → Get request ID
   
2. POST /auth/otp/verify (with OTP)
   → Get accessToken + refreshToken
   → System auto-creates user profile
   
3. POST /user/register (optional update)
   → Set profile picture, name, bio
   
4. POST /user/profile-picture (optional)
   → Set profile picture URL
   
5. PATCH /user/social-handles (optional)
   → Add social media links
   
6. Header: Authorization: Bearer {accessToken}
   → Use for all subsequent requests
```

---

### Flow 2: Create & Publish Trip

```
1. POST /trips/ (multipart with trip data + images)
   → Returns tripId in DRAFT status
   
2. PUT /trips/{tripId} (update any fields)
   → Can update as many times as needed (only in DRAFT)
   
3. POST /trips/{tripId}/publish
   → Transition to PUBLISHED
   → System creates group chat automatically
   
4. POST /trips/{tripId}/invites/travel-pal/{userId}
   → Invite specific travel pals
   
5. POST /trips/{tripId}/qna (optional)
   → Add Q&A for members
   
6. POST /trips/{tripId}/mark-full (when full)
   → Prevent new members from joining
```

---

### Flow 3: Join Trip & Member Management

```
1. GET /trips/{tripId}
   → View trip details
   
2. POST /trips/{tripId}/join-requests
   → Create join request (if APPROVAL_REQUIRED)
   → OR auto-join (if OPEN)
   
3. [Host] POST /join-requests/{id}/approve
   → Approve user to join
   
4. POST /conversations/one-to-one
   → Chat with other members
   
5. [When trip ends] PUT /trips/{tripId}/ratings/trip
   → Rate the trip
   
6. [Optional] PUT /trips/{tripId}/ratings/members
   → Rate other members
```

---

### Flow 4: Track Expenses & Settle

```
1. POST /api/splitwise/groups
   → Create expense group (optional, done auto in trip)
   
2. POST /api/splitwise/groups/{id}/members
   → Add group members
   
3. POST /api/splitwise/expenses
   → Create expense with equal/unequal splits
   
4. GET /api/splitwise/balances/group/{id}
   → Check who owes whom
   
5. POST /api/splitwise/settlements
   → Record payment
   
6. GET /api/splitwise/settlements/optimize/{id}
   → Get settlement optimization (minimize transactions)
   
7. GET /api/splitwise/balances/dashboard
   → View overall financial status
```

---

### Flow 5: Send Messages

```
1. POST /conversations/one-to-one
   → Create/get conversation
   
2. Either:
   
   A. REST: POST /conversations/{id}/send-message
      → Send message, get response
   
   B. WebSocket: Connect to /app/chat.send
      → Send real-time messages
      → Subscribe to /user/queue/reply for incoming
   
3. GET /conversations/{id}/messages
   → Fetch message history (paginated)
   
4. PATCH /conversations/{id}/read
   → Mark as read
   
5. POST /conversations/{id}/mute or unmute
   → Control notifications
```

---

### Flow 6: Manage Notifications

```
1. GET /notifications/unread-count
   → Get unread notification count (for badge)
   
2. GET /notifications?page=0&size=20
   → Fetch paginated notifications
   
3. PATCH /notifications/{id}/read
   → Mark individual as read
   
4. PATCH /notifications/read-all
   → Mark all as read
   
5. Polling or push notifications
   → Real-time alert mechanism
```

---

## ⚙️ Error Handling

### Standard Error Response

```json
{
  "statusCode": 400,
  "status": "ERROR",
  "statusMessage": "Human-readable error",
  "data": {
    "fieldErrors": {
      "field1": "error message 1",
      "field2": "error message 2"
    }
  }
}
```

---

### HTTP Status Code Meanings

| Status | Meaning | Action |
|--------|---------|--------|
| **2xx** | Success | Proceed |
| **400** | Bad Request | Check validation errors in response |
| **401** | Unauthorized | Refresh token or re-login |
| **403** | Forbidden | Check permissions/ownership |
| **404** | Not Found | Verify resource ID exists |
| **409** | Conflict | Resource state doesn't allow operation |
| **429** | Too Many Requests | Wait before retrying |
| **503** | Service Unavailable | Retry after delay (external service down) |
| **500** | Server Error | Report to support, retry later |

---

## 💡 Best Practices

### 1. Token Management
```javascript
// Store tokens securely
localStorage.setItem('accessToken', response.data.accessToken);
localStorage.setItem('refreshToken', response.data.refreshToken);

// Include in all requests
headers.Authorization = `Bearer ${localStorage.getItem('accessToken')}`;

// Refresh when expired
if (response.status === 401) {
  const newTokens = await refreshToken();
  retry(originalRequest);
}
```

---

### 2. Error Handling
```javascript
try {
  const response = await fetch(endpoint, options);
  if (!response.ok) {
    const error = await response.json();
    console.error(error.statusMessage);
    
    // Handle specific status codes
    if (response.status === 401) {
      redirectToLogin();
    } else if (response.status === 403) {
      showPermissionDenied();
    }
  }
} catch (error) {
  console.error('Network error:', error);
}
```

---

### 3. Pagination
```javascript
// Always paginate large datasets
const response = await fetch('/notifications?page=0&size=20');

// For infinite scroll
if (!data.isLast) {
  loadMore(data.currentPage + 1);
}
```

---

### 4. File Uploads
```javascript
// Validate before upload
if (file.size > 10 * 1024 * 1024) {
  alert('File too large');
  return;
}

// Use FormData for multipart
const formData = new FormData();
formData.append('trip', JSON.stringify(tripData));
formData.append('files', file);

const response = await fetch('/trips/', {
  method: 'POST',
  body: formData
});
```

---

### 5. Real-Time Updates
```javascript
// WebSocket connection
const socket = new SockJS('/ws-chat');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
  // Subscribe to user-specific queue
  stompClient.subscribe('/user/queue/reply', (message) => {
    const newMessage = JSON.parse(message.body);
    updateUI(newMessage);
  });
});
```

---

## 📋 Checklist for Implementation

**Before Going Live:**

- [ ] All 60+ endpoints implemented
- [ ] Status codes match contracts
- [ ] Error messages are user-friendly
- [ ] Request/response schemas validated
- [ ] Auth middleware in place
- [ ] Rate limiting configured (especially OTP)
- [ ] S3 storage configured and tested
- [ ] Email/SMS services configured
- [ ] WebSocket endpoints tested
- [ ] CORS configured for frontend
- [ ] Logging/monitoring in place
- [ ] Security headers configured
- [ ] API documentation deployed
- [ ] Frontend adapted to documented contracts
- [ ] Load testing completed

---

## 🔗 Reference Links

**Within This Document Set:**
- Master Index: `docs/API_CONTRACTS_COMPLETE.md`
- Individual modules: `docs/API_CONTRACT_*.md`

**External Resources:**
- Spring Boot REST Best Practices
- HTTP Status Code Reference
- OAuth 2.0 with JWT
- WebSocket/STOMP Protocol

---

## 🆘 Support

**Questions on specific endpoints?**
1. Check the relevant module document
2. Look for "Example Requests" section
3. Review status codes and error scenarios
4. Consult the respective controller source code

**Found an error or discrepancy?**
- Update the relevant API_CONTRACT_*.md file
- Sync with actual controller implementation
- Update this document

---

**Last Updated:** July 14, 2026  
**Version:** 1.0  
**Status:** Complete - All 60+ endpoints documented with status codes and error scenarios

