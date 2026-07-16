# Tranzo User MS – Complete API Contracts (All Endpoints with Status Codes)

**Version:** 1.0  
**Last Updated:** July 14, 2026  
**Base URL:** `http://localhost:8089` (no context path)  
**Total Endpoints:** 60+  
**Total Controllers:** 21

---

## 📋 Master Index

### Module Overview

| # | Module | Controllers | Endpoints | Response Format |
|---|--------|-------------|-----------|-----------------|
| 1 | **Authentication** | SessionController, OtpController, AadharController | 5 | ResponseDto<T> |
| 2 | **User Management** | UserController, PublicProfileController, TravelPalController, RatingController | 16 | Mixed |
| 3 | **Trip Management** | TripManagementController, TripJoinRequestController, TripWishlistController, DiscoveryController, AiItineraryController | 26 | ResponseDto<T> |
| 4 | **Chat** | CreateAndManageChatController, ChatSocketController | 10 | ResponseDto<T> + WebSocket |
| 5 | **Notifications** | NotificationController | 4 | ResponseDto<T> |
| 6 | **Splitwise (Expenses & Settlement)** | SplitwiseGroupController, ExpenseController, SettlementController, BalanceController, ActivityController | 25+ | Raw DTOs |
| 7 | **Media** | MediaController | 2 | ResponseDto<T> |

---

## 📑 Detailed Contracts by Module

### **Module 1: Authentication** (5 endpoints)
📄 **File:** `API_CONTRACT_AUTHENTICATION.md`
- POST /auth/session/login
- POST /auth/session/refresh
- POST /auth/session/logout
- POST /auth/otp/request
- POST /auth/otp/verify
- POST /aadhaar/otp/request

### **Module 2: User Management** (16 endpoints)
📄 **File:** `API_CONTRACT_USER_MANAGEMENT.md`
- GET /user
- POST /user/register
- PATCH /user/update
- DELETE /user/delete-user
- PUT /user/profile-picture
- DELETE /user/profile-picture
- PATCH /user/social-handles
- POST /user/{reportedUserId}/report
- GET /public/profile/{userId}
- POST /travel-pal/request/{receiverId}
- POST /travel-pal/accept/{requesterId}
- POST /travel-pal/reject/{requesterId}
- DELETE /travel-pal/{otherUserId}
- GET /travel-pal/my
- GET /travel-pal/pending
- GET /travel-pal/suggested
- PUT /trips/{tripId}/ratings/trip
- PUT /trips/{tripId}/ratings/host
- PUT /trips/{tripId}/ratings/members

### **Module 3: Trip Management** (26 endpoints)
📄 **File:** `API_CONTRACT_TRIP_MANAGEMENT.md`
- POST /trips/
- PUT /trips/{tripId}
- GET /trips/{tripId}
- DELETE /trips/{tripId}
- POST /trips/{tripId}/publish
- PATCH /trips/{tripId}
- GET /trips/{tripId}/members
- POST /trips/{tripId}/qna
- POST /trips/{tripId}/qna/{qnaId}/answer
- GET /trips/{tripId}/qna
- POST /trips/{tripId}/reports
- POST /trips/{tripId}/participants/{participantUserId}/promote-cohost
- POST /trips/{tripId}/mark-full
- POST /trips/{tripId}/invites/travel-pal
- POST /trips/{tripId}/broadcast
- POST /trips/search
- GET /trips/mutual-with/{otherUserId}
- GET /trips/user
- GET /trips
- POST /trips/{tripId}/join-requests
- POST /join-requests/{id}/approve
- POST /join-requests/{id}/reject
- GET /trips/{tripId}/join-requests
- DELETE /join-requests/{id}/cancel
- DELETE /trips/{tripId}/participants/{participantUserId}
- GET /trips/{tripId}/join-request-status
- POST /users/me/wishlist/
- DELETE /users/me/wishlist/{tripId}
- GET /users/me/wishlist/
- GET /trips/featured
- GET /trips/trending-destinations
- POST /api/v1/ai/itinerary

### **Module 4: Chat** (10 endpoints)
📄 **File:** `API_CONTRACT_CHAT.md`
- POST /conversations/one-to-one
- GET /conversations/chat-list
- POST /conversations/{conversationId}/send-message
- GET /conversations/{conversationId}/messages
- PATCH /conversations/{conversationId}/read
- POST /conversations/{conversationId}/mute
- POST /conversations/{conversationId}/unmute
- POST /conversations/{conversationId}/block
- POST /conversations/{conversationId}/unblock
- WEBSOCKET /app/chat.send

### **Module 5: Notifications** (4 endpoints)
📄 **File:** `API_CONTRACT_NOTIFICATIONS.md`
- GET /notifications
- GET /notifications/unread-count
- PATCH /notifications/{notificationId}/read
- PATCH /notifications/read-all

### **Module 6: Splitwise (Expenses & Settlement)** (25+ endpoints)
📄 **File:** `API_CONTRACT_SPLITWISE.md`
- POST /api/splitwise/groups
- GET /api/splitwise/groups/{groupId}
- PUT /api/splitwise/groups/{groupId}
- DELETE /api/splitwise/groups/{groupId}
- GET /api/splitwise/groups/my-groups
- POST /api/splitwise/groups/{groupId}/members
- DELETE /api/splitwise/groups/{groupId}/members/{memberId}
- GET /api/splitwise/groups/{groupId}/members
- POST /api/splitwise/expenses
- GET /api/splitwise/expenses/{expenseId}
- PUT /api/splitwise/expenses/{expenseId}
- DELETE /api/splitwise/expenses/{expenseId}
- GET /api/splitwise/expenses/group/{groupId}
- GET /api/splitwise/expenses/my-expenses
- POST /api/splitwise/settlements
- GET /api/splitwise/settlements/{settlementId}
- GET /api/splitwise/settlements/group/{groupId}
- GET /api/splitwise/settlements/my-settlements
- GET /api/splitwise/settlements/optimize/{groupId}
- GET /api/splitwise/balances/group/{groupId}
- GET /api/splitwise/balances/group/{groupId}/user/{userId}
- GET /api/splitwise/balances/group/{groupId}/my-balance
- GET /api/splitwise/balances/dashboard
- GET /api/splitwise/activities/group/{groupId}
- GET /api/splitwise/activities/my-activities

### **Module 7: Media** (2 endpoints)
📄 **File:** `API_CONTRACT_MEDIA.md`
- POST /media/upload
- GET /media/url

---

## 🔑 Common Response Formats

### ResponseDto Wrapper (Used by: Auth, User, Trip, Chat, Notification, Media)
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Operation completed successfully",
  "data": { /* resource-specific data */ }
}
```

### Error Response (All modules)
```json
{
  "statusCode": 400,
  "status": "ERROR",
  "statusMessage": "Descriptive error message",
  "data": null
}
```

### Splitwise Raw DTO Format (No wrapper)
```json
{
  "id": "uuid",
  "name": "string",
  /* resource-specific fields */
}
```

---

## 📊 HTTP Status Codes Reference

| Code | Status | Meaning | When Used |
|------|--------|---------|-----------|
| **200** | OK | Request successful, resource returned | GET, PATCH operations |
| **201** | Created | Resource created successfully | POST operations (create) |
| **204** | No Content | Operation successful, no content to return | DELETE operations |
| **400** | Bad Request | Invalid input, validation error | Invalid data, malformed request |
| **401** | Unauthorized | Missing or invalid authentication | Invalid/expired token |
| **403** | Forbidden | Authenticated but denied access | Insufficient permissions, resource private |
| **404** | Not Found | Resource doesn't exist | Invalid ID, deleted resource |
| **409** | Conflict | Resource conflict, state incompatibility | Duplicate, already exists, invalid state transition |
| **422** | Unprocessable Entity | Semantic error in request | Business logic violation |
| **500** | Internal Server Error | Server error | Unhandled exception |
| **503** | Service Unavailable | External service down | S3, Twilio, OpenAI, Email unavailable |

---

## 🔐 Authentication Strategies

### 1. JWT Token (Most endpoints)
- **Header:** `Authorization: Bearer <jwt-token>`
- **Expiry:** 15 minutes (access token)
- **Refresh:** POST /auth/session/refresh (with refresh token)

### 2. Refresh Token (Session management)
- **Cookie/Header:** Based on configuration
- **Expiry:** 7 days
- **Usage:** Obtain new access token without re-login

### 3. Public Endpoints (No auth required)
- POST /auth/session/login
- POST /auth/otp/request
- POST /auth/otp/verify
- POST /user/register
- GET /public/profile/{userId}

---

## 🛡️ Permission Model

### Trip Permissions
- **HOST:** Full trip control (create, publish, update, delete, manage members)
- **CO_HOST:** Assist host (update, manage members, read Q&A)
- **MEMBER:** Limited access (view, leave, rate)
- **NON-MEMBER:** Read-only for public trips

### Chat Permissions
- **PARTICIPANT:** Send/receive messages, mute, unmute, block
- **NON-PARTICIPANT:** Cannot access

### Group/Settlement Permissions
- **GROUP_MEMBER:** View expenses, balances, settlements
- **GROUP_ADMIN:** Create settlements, manage members
- **NON_MEMBER:** Cannot access

---

## 📝 Common Error Scenarios

### Validation Errors (400)
```json
{
  "statusCode": 400,
  "status": "ERROR",
  "statusMessage": "Validation failed",
  "data": {
    "fieldErrors": {
      "tripTitle": "Trip title is required",
      "estimatedBudget": "Budget must be greater than 0"
    }
  }
}
```

### Authentication Error (401)
```json
{
  "statusCode": 401,
  "status": "ERROR",
  "statusMessage": "Unauthorized: Invalid or expired token",
  "data": null
}
```

### Permission Error (403)
```json
{
  "statusCode": 403,
  "status": "ERROR",
  "statusMessage": "Forbidden: You do not have permission to perform this operation",
  "data": null
}
```

### Resource Not Found (404)
```json
{
  "statusCode": 404,
  "status": "ERROR",
  "statusMessage": "Trip not found with ID: <tripId>",
  "data": null
}
```

### State Conflict (409)
```json
{
  "statusCode": 409,
  "status": "ERROR",
  "statusMessage": "Trip is already published. Cannot publish again.",
  "data": null
}
```

---

## 🌐 External Dependencies & Service Availability

| Service | Endpoints Affected | Fallback | Status Code |
|---------|-------------------|----------|-------------|
| **AWS S3** | POST /media/upload, GET /media/url | Not configured error | 503 |
| **AWS SES** | User registration, notifications | Email not sent (async) | 200 (queued) |
| **AWS SNS** | Notifications, OTP | SMS not sent (async) | 200 (queued) |
| **Twilio** | OTP delivery | Twilio down error | 503 |
| **OpenAI API** | POST /api/v1/ai/itinerary | API unavailable | 503 |
| **Google Gemini API** | POST /api/v1/ai/itinerary | API unavailable | 503 |
| **Database** | All endpoints | Connection pool exhausted | 500 |

---

## 📌 Important Notes

1. **All timestamps** are in ISO-8601 format (e.g., `2026-03-15T10:30:00Z`)
2. **All IDs** are UUIDs (version 4)
3. **Pagination** uses `page` (0-indexed) and `size` (default 20, max 100) query parameters
4. **Validation groups** used for different contexts:
   - DraftChecks (for draft trips)
   - PublishChecks (for publishing trips)
5. **Multipart requests** used for file uploads (trip images, profile pictures)
6. **Error messages** are context-aware and include specific field names and values

---

## 🚀 Next Steps

Refer to individual module contracts for:
- Complete request/response schemas
- Field validations and constraints
- Example requests (cURL, JavaScript)
- All applicable status codes for each endpoint
- Error recovery strategies
- Rate limiting policies (if applicable)

---

**Last Updated:** July 14, 2026  
**For questions or updates:** Refer to respective module documentation

