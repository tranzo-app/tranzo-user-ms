# API Contract: Notifications Module

**Base URL:** `http://localhost:8089`  
**Controller:** NotificationController  
**Total Endpoints:** 4  
**Response Format:** ResponseDto<T>

---

## Notification Types

| Type | Description | Trigger | Recipient |
|------|-------------|---------|-----------|
| DRAFT_TRIP_REMINDER | Reminder to publish draft trip | Scheduler (cron) | Trip host |
| UPCOMING_TRIP | Trip is coming up soon | Scheduler (3 days before) | Trip members |
| TRIP_COMPLETED | Trip has ended | Scheduler (after end date) | Trip members |
| TRIP_CANCELLED | Trip was cancelled | Host action | Active members |
| JOIN_REQUEST_RECEIVED | Someone requested to join | Member action | Trip host |
| JOIN_REQUEST_APPROVED | Join request approved | Host action | Requestor |
| JOIN_REQUEST_REJECTED | Join request rejected | Host action | Requestor |
| MEMBER_JOINED_TRIP | New member joined | Member action | Other members |
| MEMBER_LEFT_OR_REMOVED | Member left or removed | Member/Host action | Other members |
| MEMBER_PROMOTED_TO_CO_HOST | Member promoted to co-host | Host action | Co-host, all members |
| TRIP_FULL_CAPACITY_REACHED | Trip reached max participants | Auto | All members |
| TRIP_MARKED_FULL_BY_HOST | Host marked trip as full | Host action | All members |
| TRIP_DETAILS_CHANGED | Trip details updated | Host action | Members |
| TRIP_QUESTION_ASKED | New Q&A question posted | Member action | Host/co-hosts |
| TRIP_QUESTION_ANSWERED | Q&A question answered | Host action | Asker |

---

## 1. GET /notifications

**Purpose:** Get paginated list of notifications for current user

### Request

```http
GET /notifications?page=0&size=20 HTTP/1.1
Host: localhost:8089
Authorization: Bearer <access_token>
```

**Authentication:** Required

**Query Parameters:**
| Parameter | Type | Default | Max | Description |
|-----------|------|---------|-----|-------------|
| page | Integer | 0 | - | Page number (0-indexed) |
| size | Integer | 20 | 100 | Records per page |
| read | Boolean | - | - | Filter: true=read, false=unread, null=all |
| sort | String | createdAt,desc | - | Sort by field and direction |

### Response

#### ✅ 200 OK
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Notifications fetched successfully",
  "data": {
    "content": [
      {
        "notificationId": "550e8400-e29b-41d4-a716-446655440300",
        "type": "JOIN_REQUEST_RECEIVED",
        "title": "Join Request",
        "body": "John Doe wants to join 'Manali Adventure'",
        "relatedTrip": {
          "tripId": "550e8400-e29b-41d4-a716-446655440000",
          "tripTitle": "Manali Adventure"
        },
        "relatedUser": {
          "userId": "550e8400-e29b-41d4-a716-446655440001",
          "firstName": "John",
          "lastName": "Doe"
        },
        "read": false,
        "readAt": null,
        "createdAt": "2026-01-24T15:45:00Z",
        "actionUrl": "/trips/550e8400-e29b-41d4-a716-446655440000/join-requests"
      },
      {
        "notificationId": "550e8400-e29b-41d4-a716-446655440301",
        "type": "MEMBER_JOINED_TRIP",
        "title": "New Member Joined",
        "body": "Jane Smith joined 'Manali Adventure'",
        "relatedTrip": {
          "tripId": "550e8400-e29b-41d4-a716-446655440000",
          "tripTitle": "Manali Adventure"
        },
        "relatedUser": {
          "userId": "550e8400-e29b-41d4-a716-446655440050",
          "firstName": "Jane",
          "lastName": "Smith"
        },
        "read": true,
        "readAt": "2026-01-24T15:50:00Z",
        "createdAt": "2026-01-24T15:45:00Z"
      },
      {
        "notificationId": "550e8400-e29b-41d4-a716-446655440302",
        "type": "TRIP_COMPLETED",
        "title": "Trip Completed",
        "body": "Your trip 'Manali Adventure' has ended. Please rate your experience!",
        "relatedTrip": {
          "tripId": "550e8400-e29b-41d4-a716-446655440000",
          "tripTitle": "Manali Adventure"
        },
        "read": false,
        "readAt": null,
        "createdAt": "2026-01-23T18:00:00Z",
        "actionUrl": "/trips/550e8400-e29b-41d4-a716-446655440000/ratings"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 42,
    "totalPages": 3,
    "isLast": false
  }
}
```

#### ❌ 400 Bad Request
**Scenarios:**
- Invalid page number (negative)
- Invalid size (> 100 or < 1)
- Invalid sort syntax

```json
{
  "statusCode": 400,
  "status": "ERROR",
  "statusMessage": "Invalid pagination parameters",
  "data": {
    "fieldErrors": {
      "size": "Size must be between 1 and 100"
    }
  }
}
```

#### ❌ 401 Unauthorized

```json
{
  "statusCode": 401,
  "status": "ERROR",
  "statusMessage": "Unauthorized",
  "data": null
}
```

#### ❌ 500 Internal Server Error

```json
{
  "statusCode": 500,
  "status": "ERROR",
  "statusMessage": "Failed to fetch notifications",
  "data": null
}
```

### Example Requests

**cURL (Get unread notifications):**
```bash
curl -X GET "http://localhost:8089/notifications?page=0&size=20&read=false" \
  -H "Authorization: Bearer <access_token>"
```

**cURL (Get latest first):**
```bash
curl -X GET "http://localhost:8089/notifications?page=0&size=20&sort=createdAt,desc" \
  -H "Authorization: Bearer <access_token>"
```

**JavaScript:**
```javascript
const response = await fetch('/notifications?page=0&size=20&read=false', {
  method: 'GET',
  headers: { 'Authorization': `Bearer ${token}` }
});
const data = await response.json();
console.log(data.data.content);
```

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Notifications fetched |
| 400 | Invalid parameters |
| 401 | Unauthorized |
| 500 | Server error |

---

## 2. GET /notifications/unread-count

**Purpose:** Get count of unread notifications for current user

### Request

```http
GET /notifications/unread-count HTTP/1.1
Host: localhost:8089
Authorization: Bearer <access_token>
```

**Authentication:** Required

**Query Parameters:** None

### Response

#### ✅ 200 OK
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Unread count fetched successfully",
  "data": 7
}
```

Data is simply a number representing unread notification count.

#### ❌ 401 Unauthorized

```json
{
  "statusCode": 401,
  "status": "ERROR",
  "statusMessage": "Unauthorized",
  "data": null
}
```

#### ❌ 500 Internal Server Error

```json
{
  "statusCode": 500,
  "status": "ERROR",
  "statusMessage": "Failed to fetch unread count",
  "data": null
}
```

### Example Requests

**cURL:**
```bash
curl -X GET http://localhost:8089/notifications/unread-count \
  -H "Authorization: Bearer <access_token>"
```

**JavaScript:**
```javascript
const response = await fetch('/notifications/unread-count', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const data = await response.json();
console.log('Unread notifications:', data.data); // e.g., 7
```

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Count fetched |
| 401 | Unauthorized |
| 500 | Server error |

---

## 3. PATCH /notifications/{notificationId}/read

**Purpose:** Mark a single notification as read

### Request

```http
PATCH /notifications/550e8400-e29b-41d4-a716-446655440300/read HTTP/1.1
Host: localhost:8089
Authorization: Bearer <access_token>
```

**Authentication:** Required

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| notificationId | UUID | Notification ID to mark as read |

**Request Body:** Empty

### Response

#### ✅ 200 OK
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Notification marked as read",
  "data": null
}
```

#### ❌ 401 Unauthorized

```json
{
  "statusCode": 401,
  "status": "ERROR",
  "statusMessage": "Unauthorized",
  "data": null
}
```

#### ❌ 403 Forbidden
**Scenarios:**
- Notification belongs to another user

```json
{
  "statusCode": 403,
  "status": "ERROR",
  "statusMessage": "You don't have permission to access this notification",
  "data": null
}
```

#### ❌ 404 Not Found
**Scenarios:**
- Notification doesn't exist or already deleted

```json
{
  "statusCode": 404,
  "status": "ERROR",
  "statusMessage": "Notification not found",
  "data": null
}
```

#### ❌ 409 Conflict
**Scenarios:**
- Notification already marked as read

```json
{
  "statusCode": 409,
  "status": "ERROR",
  "statusMessage": "Notification is already read",
  "data": null
}
```

#### ❌ 500 Internal Server Error

```json
{
  "statusCode": 500,
  "status": "ERROR",
  "statusMessage": "Failed to mark notification as read",
  "data": null
}
```

### Example Requests

**cURL:**
```bash
curl -X PATCH http://localhost:8089/notifications/550e8400-e29b-41d4-a716-446655440300/read \
  -H "Authorization: Bearer <access_token>"
```

**JavaScript:**
```javascript
const response = await fetch(
  '/notifications/550e8400-e29b-41d4-a716-446655440300/read',
  {
    method: 'PATCH',
    headers: { 'Authorization': `Bearer ${token}` }
  }
);
```

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Marked as read |
| 401 | Unauthorized |
| 403 | Not notification owner |
| 404 | Notification not found |
| 409 | Already read |
| 500 | Server error |

---

## 4. PATCH /notifications/read-all

**Purpose:** Mark all notifications as read

### Request

```http
PATCH /notifications/read-all HTTP/1.1
Host: localhost:8089
Authorization: Bearer <access_token>
```

**Authentication:** Required

**Request Body:** Empty

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| beforeDate | DateTime | Mark as read notifications before this date (optional) |

### Response

#### ✅ 200 OK
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "All notifications marked as read",
  "data": {
    "updatedCount": 12
  }
}
```

Data contains count of notifications that were marked as read.

#### ❌ 400 Bad Request
**Scenarios:**
- Invalid date format

```json
{
  "statusCode": 400,
  "status": "ERROR",
  "statusMessage": "Invalid date format",
  "data": null
}
```

#### ❌ 401 Unauthorized

```json
{
  "statusCode": 401,
  "status": "ERROR",
  "statusMessage": "Unauthorized",
  "data": null
}
```

#### ❌ 500 Internal Server Error

```json
{
  "statusCode": 500,
  "status": "ERROR",
  "statusMessage": "Failed to mark all notifications as read",
  "data": null
}
```

### Example Requests

**cURL (Mark all as read):**
```bash
curl -X PATCH http://localhost:8089/notifications/read-all \
  -H "Authorization: Bearer <access_token>"
```

**cURL (Mark as read before specific date):**
```bash
curl -X PATCH "http://localhost:8089/notifications/read-all?beforeDate=2026-01-24T00:00:00Z" \
  -H "Authorization: Bearer <access_token>"
```

**JavaScript:**
```javascript
const response = await fetch('/notifications/read-all', {
  method: 'PATCH',
  headers: { 'Authorization': `Bearer ${token}` }
});
const data = await response.json();
console.log('Marked as read:', data.data.updatedCount);
```

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | All marked as read |
| 400 | Invalid parameters |
| 401 | Unauthorized |
| 500 | Server error |

---

## Notification Payload Examples

### Example 1: Join Request Received

```json
{
  "notificationId": "550e8400-e29b-41d4-a716-446655440300",
  "type": "JOIN_REQUEST_RECEIVED",
  "title": "Join Request",
  "body": "John Doe has requested to join 'Manali Adventure'",
  "relatedTrip": {
    "tripId": "550e8400-e29b-41d4-a716-446655440000",
    "tripTitle": "Manali Adventure"
  },
  "relatedUser": {
    "userId": "550e8400-e29b-41d4-a716-446655440001",
    "firstName": "John",
    "lastName": "Doe",
    "profilePicture": "https://s3.amazonaws.com/..."
  },
  "actionUrl": "/trips/550e8400-e29b-41d4-a716-446655440000/join-requests",
  "actionButton": {
    "text": "View Request",
    "action": "VIEW_JOIN_REQUESTS"
  }
}
```

### Example 2: Member Joined Trip

```json
{
  "notificationId": "550e8400-e29b-41d4-a716-446655440301",
  "type": "MEMBER_JOINED_TRIP",
  "title": "New Member Joined",
  "body": "Jane Smith has joined 'Manali Adventure'",
  "relatedTrip": {
    "tripId": "550e8400-e29b-41d4-a716-446655440000",
    "tripTitle": "Manali Adventure"
  },
  "relatedUser": {
    "userId": "550e8400-e29b-41d4-a716-446655440050",
    "firstName": "Jane",
    "lastName": "Smith"
  },
  "actionUrl": "/trips/550e8400-e29b-41d4-a716-446655440000/members"
}
```

### Example 3: Trip Completed

```json
{
  "notificationId": "550e8400-e29b-41d4-a716-446655440302",
  "type": "TRIP_COMPLETED",
  "title": "Trip Completed",
  "body": "Your trip 'Manali Adventure' has ended. Please rate your experience!",
  "relatedTrip": {
    "tripId": "550e8400-e29b-41d4-a716-446655440000",
    "tripTitle": "Manali Adventure",
    "tripDestination": "Manali",
    "tripEndDate": "2026-03-05"
  },
  "actionUrl": "/trips/550e8400-e29b-41d4-a716-446655440000/ratings",
  "actionButton": {
    "text": "Rate Trip",
    "action": "RATE_TRIP"
  }
}
```

### Example 4: Join Request Approved

```json
{
  "notificationId": "550e8400-e29b-41d4-a716-446655440303",
  "type": "JOIN_REQUEST_APPROVED",
  "title": "Request Approved",
  "body": "Your join request for 'Manali Adventure' has been approved!",
  "relatedTrip": {
    "tripId": "550e8400-e29b-41d4-a716-446655440000",
    "tripTitle": "Manali Adventure"
  },
  "relatedUser": {
    "userId": "550e8400-e29b-41d4-a716-446655440001",
    "firstName": "John",
    "lastName": "Doe"
  },
  "actionUrl": "/trips/550e8400-e29b-41d4-a716-446655440000",
  "actionButton": {
    "text": "View Trip",
    "action": "VIEW_TRIP"
  }
}
```

### Example 5: Trip Cancelled

```json
{
  "notificationId": "550e8400-e29b-41d4-a716-446655440304",
  "type": "TRIP_CANCELLED",
  "title": "Trip Cancelled",
  "body": "'Manali Adventure' has been cancelled by the host",
  "relatedTrip": {
    "tripId": "550e8400-e29b-41d4-a716-446655440000",
    "tripTitle": "Manali Adventure",
    "cancellationReason": "Due to unforeseen circumstances"
  },
  "relatedUser": {
    "userId": "550e8400-e29b-41d4-a716-446655440001",
    "firstName": "John",
    "lastName": "Doe"
  }
}
```

---

## Notification Delivery

Notifications are delivered through:
1. **In-app API** (via /notifications endpoints)
2. **Email** (async, sent to user's email)
3. **Push notifications** (if mobile app enabled)
4. **Real-time updates** (via WebSocket if subscribed)

---

## Best Practices

### Frontend Implementation

**Polling (Simple):**
```javascript
// Check for new notifications every 15 seconds
setInterval(async () => {
  const response = await fetch('/notifications/unread-count', {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  const data = await response.json();
  updateNotificationBadge(data.data);
}, 15000);
```

**Batch Loading:**
```javascript
// Load notifications only when user clicks bell icon
async function loadNotifications() {
  const response = await fetch('/notifications?page=0&size=50');
  const data = await response.json();
  displayNotifications(data.data.content);
}
```

**Mark as Read on View:**
```javascript
function markAsRead(notificationId) {
  fetch(`/notifications/${notificationId}/read`, {
    method: 'PATCH',
    headers: { 'Authorization': `Bearer ${token}` }
  });
}
```

---

## Summary Table

| # | Endpoint | Method | Auth | Response | Status Codes |
|---|----------|--------|------|----------|--------------|
| 1 | /notifications | GET | Yes | ResponseDto<Page> | 200, 400, 401, 500 |
| 2 | /notifications/unread-count | GET | Yes | ResponseDto<Long> | 200, 401, 500 |
| 3 | /notifications/{id}/read | PATCH | Yes | ResponseDto<Void> | 200, 401, 403, 404, 409, 500 |
| 4 | /notifications/read-all | PATCH | Yes | ResponseDto<Object> | 200, 400, 401, 500 |

---

**Last Updated:** July 14, 2026

