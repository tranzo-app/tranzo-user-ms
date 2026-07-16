# API Contract: Trip Management Module

**Base URL:** `http://localhost:8089`  
**Controllers:** TripManagementController, TripJoinRequestController, TripWishlistController, DiscoveryController, AiItineraryController  
**Total Endpoints:** 32  
**Response Format:** ResponseDto<T>

---

## Trip Lifecycle States

- **DRAFT:** Initial state, can be edited/published
- **PUBLISHED:** Public, joinable, no longer editable
- **ONGOING:** Trip has started, no new members
- **COMPLETED:** Trip ended, can be rated
- **CANCELLED:** Trip cancelled, members notified

---

## 1. POST /trips/

**Purpose:** Create a new draft trip

### Request

```http
POST /trips/ HTTP/1.1
Host: localhost:8089
Content-Type: multipart/form-data
Authorization: Bearer <access_token>
```

**Authentication:** Required

**Request Body (Multipart):**
```json
{
  "trip": {
    "tripTitle": "Manali Adventure 2026",
    "tripDescription": "Join us for an adventure to Manali",
    "tripDestination": "Manali, Himachal Pradesh",
    "tripStartDate": "2026-03-01",
    "tripEndDate": "2026-03-05",
    "estimatedBudget": 15000.00,
    "maxParticipants": 10,
    "joinPolicy": "APPROVAL_REQUIRED",
    "visibilityStatus": "PUBLIC",
    "tripItineraries": [
      {
        "dayNumber": 1,
        "title": "Day 1 - Arrival",
        "description": "Travel to Manali",
        "activities": ["Travel", "Check-in"],
        "meals": ["Dinner"],
        "stay": "Hotel A"
      }
    ],
    "tags": ["adventure", "mountains", "hiking"]
  },
  "files": [/* optional images */]
}
```

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| tripTitle | String | Yes | Max 100 chars | Trip name |
| tripDescription | String | Yes | Max 1000 chars | Trip description |
| tripDestination | String | Yes | Max 100 chars | Destination |
| tripStartDate | Date | Yes | Future date | Start date (YYYY-MM-DD) |
| tripEndDate | Date | Yes | >= startDate | End date |
| estimatedBudget | Decimal | Yes | > 0 | Estimated budget |
| maxParticipants | Integer | Yes | > 0 | Max participants |
| joinPolicy | Enum | Yes | OPEN, APPROVAL_REQUIRED | Join policy |
| visibilityStatus | Enum | Yes | PUBLIC, PRIVATE | Visibility |
| tripItineraries | Array | No | Max 30 items | Daily itinerary |
| tags | Array | No | Max 10 items | Trip tags |

### Response

#### ✅ 201 Created
```json
{
  "statusCode": 201,
  "status": "SUCCESS",
  "statusMessage": "Draft trip has been created successfully",
  "data": {
    "tripId": "550e8400-e29b-41d4-a716-446655440000",
    "tripTitle": "Manali Adventure 2026",
    "tripStatus": "DRAFT",
    "tripDestination": "Manali",
    "tripStartDate": "2026-03-01",
    "tripEndDate": "2026-03-05",
    "createdAt": "2026-01-15T10:30:00Z",
    "createdBy": "550e8400-e29b-41d4-a716-446655440001"
  }
}
```

#### ❌ 400 Bad Request
**Scenarios:**
- Invalid dates (end before start)
- Max participants < 1
- Missing required fields

```json
{
  "statusCode": 400,
  "status": "ERROR",
  "statusMessage": "Validation failed",
  "data": {
    "fieldErrors": {
      "tripStartDate": "Start date must be in the future",
      "maxParticipants": "Max participants must be at least 1"
    }
  }
}
```

#### ❌ 401 Unauthorized

#### ❌ 503 Service Unavailable
**Scenarios:**
- S3 file upload failed

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 201 | Trip created |
| 400 | Validation error |
| 401 | Unauthorized |
| 503 | Service unavailable |
| 500 | Server error |

---

## 2. PUT /trips/{tripId}

**Purpose:** Update draft trip (complete replacement)

### Request

```http
PUT /trips/550e8400-e29b-41d4-a716-446655440000 HTTP/1.1
Host: localhost:8089
Content-Type: multipart/form-data
Authorization: Bearer <access_token>
```

**Authentication:** Required (host only)

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| tripId | UUID | Trip ID to update |

**Request Body:** Same as POST /trips/

### Response

#### ✅ 200 OK
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Draft trip has been updated successfully",
  "data": { /* updated trip data */ }
}
```

#### ❌ 400 Bad Request
**Scenarios:**
- Validation errors
- **Cannot update: Trip is already PUBLISHED**
- Cannot update: Trip is ONGOING/COMPLETED/CANCELLED

```json
{
  "statusCode": 400,
  "status": "ERROR",
  "statusMessage": "Trip is already published. Cannot update published trips.",
  "data": null
}
```

#### ❌ 401 Unauthorized

#### ❌ 403 Forbidden
**Scenarios:**
- Not the host
- Not co-host with edit permissions

```json
{
  "statusCode": 403,
  "status": "ERROR",
  "statusMessage": "You do not have permission to update this trip",
  "data": null
}
```

#### ❌ 404 Not Found

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Trip updated |
| 400 | Validation/state error |
| 401 | Unauthorized |
| 403 | Forbidden (not host) |
| 404 | Trip not found |
| 500 | Server error |

---

## 3. GET /trips/{tripId}

**Purpose:** Get trip details (view trip)

### Request

```http
GET /trips/550e8400-e29b-41d4-a716-446655440000 HTTP/1.1
Host: localhost:8089
Authorization: Bearer <access_token>
```

**Authentication:** Required

### Response

#### ✅ 200 OK
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Trip details have been fetched successfully",
  "data": {
    "tripId": "550e8400-e29b-41d4-a716-446655440000",
    "tripTitle": "Manali Adventure 2026",
    "tripDescription": "Join us for an adventure to Manali",
    "tripDestination": "Manali",
    "tripStatus": "PUBLISHED",
    "tripStartDate": "2026-03-01",
    "tripEndDate": "2026-03-05",
    "estimatedBudget": 15000.00,
    "currentParticipants": 5,
    "maxParticipants": 10,
    "isFull": false,
    "joinPolicy": "APPROVAL_REQUIRED",
    "visibilityStatus": "PUBLIC",
    "createdAt": "2026-01-15T10:30:00Z",
    "conversationId": "550e8400-e29b-41d4-a716-446655440100",
    "tripItineraries": [
      {
        "dayNumber": 1,
        "title": "Day 1 - Arrival",
        "activities": ["Travel", "Check-in"]
      }
    ],
    "tags": ["adventure", "mountains"]
  }
}
```

#### ❌ 401 Unauthorized

#### ❌ 403 Forbidden
**Scenarios:**
- Trip is PRIVATE and user is not a member

```json
{
  "statusCode": 403,
  "status": "ERROR",
  "statusMessage": "This trip is private. You don't have access to it.",
  "data": null
}
```

#### ❌ 404 Not Found

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Trip fetched |
| 401 | Unauthorized |
| 403 | Private trip, no access |
| 404 | Trip not found |
| 500 | Server error |

---

## 4. DELETE /trips/{tripId}

**Purpose:** Cancel trip

### Request

```http
DELETE /trips/550e8400-e29b-41d4-a716-446655440000 HTTP/1.1
Host: localhost:8089
Authorization: Bearer <access_token>
```

**Authentication:** Required (host only)

### Response

#### ✅ 200 OK
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Trip cancelled successfully",
  "data": null
}
```

#### ❌ 400 Bad Request
**Scenarios:**
- Trip already COMPLETED
- Trip already CANCELLED

```json
{
  "statusCode": 400,
  "status": "ERROR",
  "statusMessage": "Trip is already completed. Cannot cancel.",
  "data": null
}
```

#### ❌ 401 Unauthorized

#### ❌ 403 Forbidden
**Scenarios:**
- Not the host

```json
{
  "statusCode": 403,
  "status": "ERROR",
  "statusMessage": "Only the host can cancel the trip",
  "data": null
}
```

#### ❌ 404 Not Found

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Trip cancelled |
| 400 | Invalid trip state |
| 401 | Unauthorized |
| 403 | Not host |
| 404 | Trip not found |
| 500 | Server error |

---

## 5. POST /trips/{tripId}/publish

**Purpose:** Publish draft trip and make it public

### Request

```http
POST /trips/550e8400-e29b-41d4-a716-446655440000/publish HTTP/1.1
Host: localhost:8089
Authorization: Bearer <access_token>
```

**Authentication:** Required (host only)

### Response

#### ✅ 200 OK
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Trip published successfully",
  "data": {
    "tripId": "550e8400-e29b-41d4-a716-446655440000",
    "tripStatus": "PUBLISHED",
    "conversationId": "550e8400-e29b-41d4-a716-446655440100",
    "publishedAt": "2026-01-15T11:00:00Z"
  }
}
```

#### ❌ 400 Bad Request
**Scenarios:**
- Trip not in DRAFT status
- **Required field missing** (title, dates, participants, etc.)
- Start date is in the past

```json
{
  "statusCode": 400,
  "status": "ERROR",
  "statusMessage": "Trip cannot be published: Start date must be in the future",
  "data": null
}
```

#### ❌ 401 Unauthorized

#### ❌ 403 Forbidden
**Scenarios:**
- Not the host

#### ❌ 404 Not Found

#### ❌ 409 Conflict
**Scenarios:**
- Trip already published

```json
{
  "statusCode": 409,
  "status": "ERROR",
  "statusMessage": "Trip is already published",
  "data": null
}
```

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Trip published |
| 400 | Validation failed |
| 401 | Unauthorized |
| 403 | Not host |
| 404 | Trip not found |
| 409 | Already published |
| 500 | Server error |

---

## 6. PATCH /trips/{tripId}

**Purpose:** Partially update published/ongoing trip

### Request

```http
PATCH /trips/550e8400-e29b-41d4-a716-446655440000 HTTP/1.1
Host: localhost:8089
Content-Type: application/json
Authorization: Bearer <access_token>
```

**Authentication:** Required (host/co-host only)

**Request Body (Partial Update):**
```json
{
  "tripDescription": "Updated description",
  "estimatedBudget": 16000.00
}
```

### Response

#### ✅ 200 OK
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Trip updated successfully",
  "data": { /* updated trip */ }
}
```

#### ❌ 400 Bad Request
**Scenarios:**
- Invalid field values
- Cannot modify certain fields (dates, participants limit) in ONGOING trips

#### ❌ 401 Unauthorized

#### ❌ 403 Forbidden
**Scenarios:**
- Not host/co-host

#### ❌ 404 Not Found

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Trip updated |
| 400 | Validation error |
| 401 | Unauthorized |
| 403 | Not host/co-host |
| 404 | Trip not found |
| 500 | Server error |

---

## 7. GET /trips/{tripId}/members

**Purpose:** Get trip members list and hierarchy

### Request

```http
GET /trips/550e8400-e29b-41d4-a716-446655440000/members HTTP/1.1
Host: localhost:8089
Authorization: Bearer <access_token>
```

**Authentication:** Required

### Response

#### ✅ 200 OK
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Trip members fetched successfully",
  "data": {
    "tripId": "550e8400-e29b-41d4-a716-446655440000",
    "hostUserId": "550e8400-e29b-41d4-a716-446655440001",
    "coHostUserIds": ["550e8400-e29b-41d4-a716-446655440002"],
    "members": [
      {
        "membershipId": "550e8400-e29b-41d4-a716-446655440010",
        "userId": "550e8400-e29b-41d4-a716-446655440001",
        "firstName": "John",
        "lastName": "Doe",
        "role": "HOST",
        "status": "ACTIVE",
        "joinedAt": "2026-01-15T10:30:00Z"
      },
      {
        "membershipId": "550e8400-e29b-41d4-a716-446655440011",
        "userId": "550e8400-e29b-41d4-a716-446655440003",
        "firstName": "Jane",
        "lastName": "Smith",
        "role": "MEMBER",
        "status": "ACTIVE",
        "joinedAt": "2026-01-16T14:00:00Z"
      }
    ],
    "totalJoined": 5
  }
}
```

#### ❌ 401 Unauthorized

#### ❌ 403 Forbidden
**Scenarios:**
- Private trip, not member

#### ❌ 404 Not Found

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Members fetched |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Trip not found |
| 500 | Server error |

---

## 8. POST /trips/{tripId}/qna

**Purpose:** Add a Q&A question to trip (by host/co-host)

### Request

```http
POST /trips/550e8400-e29b-41d4-a716-446655440000/qna HTTP/1.1
Host: localhost:8089
Content-Type: application/json
Authorization: Bearer <access_token>
```

**Authentication:** Required (host/co-host)

**Request Body:**
```json
{
  "question": "What's the expected fitness level?",
  "visibility": "HOST_AND_CO_HOSTS"
}
```

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| question | String | Yes | Max 500 chars | Question text |
| visibility | Enum | Yes | HOST_ONLY, HOST_AND_CO_HOSTS | Visibility |

### Response

#### ✅ 200 OK
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Q&A added successfully",
  "data": null
}
```

#### ❌ 400 Bad Request

#### ❌ 401 Unauthorized

#### ❌ 403 Forbidden
**Scenarios:**
- Not host/co-host

#### ❌ 404 Not Found

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Q&A added |
| 400 | Invalid input |
| 401 | Unauthorized |
| 403 | Not host/co-host |
| 404 | Trip not found |
| 500 | Server error |

---

## 9. POST /trips/{tripId}/qna/{qnaId}/answer

**Purpose:** Answer a trip Q&A

### Request

```http
POST /trips/550e8400-e29b-41d4-a716-446655440000/qna/550e8400-e29b-41d4-a716-446655440050/answer HTTP/1.1
Host: localhost:8089
Content-Type: application/json
Authorization: Bearer <access_token>
```

**Authentication:** Required (host/co-host)

**Request Body:**
```json
{
  "answer": "We recommend good fitness level for hiking activities"
}
```

### Response

#### ✅ 200 OK
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Q&A answered successfully",
  "data": null
}
```

#### ❌ 401 Unauthorized

#### ❌ 403 Forbidden

#### ❌ 404 Not Found
**Scenarios:**
- Trip not found
- Q&A not found

#### ❌ 409 Conflict
**Scenarios:**
- Q&A already answered

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Q&A answered |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not found |
| 409 | Already answered |
| 500 | Server error |

---

## 10. GET /trips/{tripId}/qna

**Purpose:** Get all Q&A for a trip

### Request

```http
GET /trips/550e8400-e29b-41d4-a716-446655440000/qna HTTP/1.1
Host: localhost:8089
Authorization: Bearer <access_token>
```

### Response

#### ✅ 200 OK
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Q&A list fetched successfully",
  "data": [
    {
      "qnaId": "550e8400-e29b-41d4-a716-446655440050",
      "question": "What's the fitness level?",
      "answer": "Good fitness required",
      "askedBy": "550e8400-e29b-41d4-a716-446655440003",
      "createdAt": "2026-01-16T14:00:00Z",
      "answeredAt": "2026-01-16T15:00:00Z"
    }
  ]
}
```

#### ❌ 401 Unauthorized

#### ❌ 404 Not Found

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Q&A fetched |
| 401 | Unauthorized |
| 404 | Trip not found |
| 500 | Server error |

---

## 11. POST /trips/{tripId}/reports

**Purpose:** Report a trip for violation

### Request

```http
POST /trips/550e8400-e29b-41d4-a716-446655440000/reports HTTP/1.1
Host: localhost:8089
Content-Type: application/json
Authorization: Bearer <access_token>
```

**Request Body:**
```json
{
  "reason": "Inappropriate content",
  "description": "Trip description contains offensive language"
}
```

### Response

#### ✅ 200 OK
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Trip reported successfully",
  "data": null
}
```

#### ❌ 400 Bad Request
**Scenarios:**
- Duplicate report from same user

#### ❌ 401 Unauthorized

#### ❌ 404 Not Found

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Trip reported |
| 400 | Duplicate report |
| 401 | Unauthorized |
| 404 | Trip not found |
| 500 | Server error |

---

## 12. POST /trips/{tripId}/participants/{participantUserId}/promote-cohost

**Purpose:** Promote a member to co-host

### Request

```http
POST /trips/550e8400-e29b-41d4-a716-446655440000/participants/550e8400-e29b-41d4-a716-446655440003/promote-cohost HTTP/1.1
Host: localhost:8089
Authorization: Bearer <access_token>
```

**Authentication:** Required (host only)

### Response

#### ✅ 200 OK
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Member promoted to co-host successfully",
  "data": null
}
```

#### ❌ 400 Bad Request
**Scenarios:**
- Member already co-host
- User is not a member

#### ❌ 401 Unauthorized

#### ❌ 403 Forbidden
**Scenarios:**
- Not the host

#### ❌ 404 Not Found
**Scenarios:**
- Trip or member not found

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Member promoted |
| 400 | Invalid state |
| 401 | Unauthorized |
| 403 | Not host |
| 404 | Not found |
| 500 | Server error |

---

## 13. POST /trips/{tripId}/mark-full

**Purpose:** Mark trip as full (host only)

### Request

```http
POST /trips/550e8400-e29b-41d4-a716-446655440000/mark-full HTTP/1.1
Host: localhost:8089
Authorization: Bearer <access_token>
```

### Response

#### ✅ 200 OK
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Trip marked as full",
  "data": null
}
```

#### ❌ 400 Bad Request
**Scenarios:**
- Trip already full
- Participants not at max

#### ❌ 401 Unauthorized

#### ❌ 403 Forbidden

#### ❌ 404 Not Found

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Trip marked full |
| 400 | Invalid state |
| 401 | Unauthorized |
| 403 | Not host |
| 404 | Trip not found |
| 500 | Server error |

---

## 14. POST /trips/{tripId}/invites/travel-pal

**Purpose:** Invite travel pals to trip

### Request

```http
POST /trips/550e8400-e29b-41d4-a716-446655440000/invites/travel-pal/550e8400-e29b-41d4-a716-446655440050 HTTP/1.1
Host: localhost:8089
Authorization: Bearer <access_token>
```

### Response

#### ✅ 200 OK
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Travel pal invited successfully",
  "data": null
}
```

#### ❌ 400 Bad Request
**Scenarios:**
- User already member
- Not a travel pal

#### ❌ 401 Unauthorized

#### ❌ 403 Forbidden

#### ❌ 404 Not Found

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Invited |
| 400 | Invalid state |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not found |
| 500 | Server error |

---

## 15. POST /trips/{tripId}/broadcast

**Purpose:** Broadcast trip to all travel pals

### Request

```http
POST /trips/550e8400-e29b-41d4-a716-446655440000/broadcast HTTP/1.1
Host: localhost:8089
Authorization: Bearer <access_token>
```

### Response

#### ✅ 200 OK
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Trip broadcasted successfully",
  "data": null
}
```

#### ❌ 401 Unauthorized

#### ❌ 403 Forbidden

#### ❌ 404 Not Found

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Broadcasted |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Trip not found |
| 500 | Server error |

---

## 16. POST /trips/search

**Purpose:** Search trips with filters

### Request

```http
POST /trips/search HTTP/1.1
Host: localhost:8089
Content-Type: application/json
Authorization: Bearer <access_token>
```

**Request Body:**
```json
{
  "destination": "Manali",
  "startDate": "2026-03-01",
  "endDate": "2026-03-31",
  "minBudget": 10000,
  "maxBudget": 50000,
  "tags": ["adventure", "hiking"],
  "page": 0,
  "size": 20
}
```

### Response

#### ✅ 200 OK
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Trips fetched successfully",
  "data": {
    "content": [
      {
        "tripId": "uuid",
        "tripTitle": "Manali Adventure",
        "destination": "Manali",
        "startDate": "2026-03-01",
        "currentParticipants": 5,
        "maxParticipants": 10
      }
    ],
    "totalElements": 42,
    "totalPages": 3,
    "currentPage": 0
  }
}
```

#### ❌ 400 Bad Request

#### ❌ 401 Unauthorized

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Results fetched |
| 400 | Invalid filter |
| 401 | Unauthorized |
| 500 | Server error |

---

## 17. GET /trips/mutual-with/{otherUserId}

**Purpose:** Get mutual completed trips with another user

### Request

```http
GET /trips/mutual-with/550e8400-e29b-41d4-a716-446655440050 HTTP/1.1
Host: localhost:8089
Authorization: Bearer <access_token>
```

### Response

#### ✅ 200 OK
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Mutual trips fetched",
  "data": [/* list of completed trips */]
}
```

#### ❌ 404 Not Found

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Trips fetched |
| 404 | User not found |
| 500 | Server error |

---

## 18. GET /trips/user

**Purpose:** Get all trips for current user

### Request

```http
GET /trips/user HTTP/1.1
Host: localhost:8089
Authorization: Bearer <access_token>
```

**Query Parameters:**
| Parameter | Type | Default |
|-----------|------|---------|
| page | Integer | 0 |
| size | Integer | 20 |
| status | String | All |

### Response

#### ✅ 200 OK
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "User trips fetched",
  "data": [/* list of user's trips */]
}
```

#### ❌ 401 Unauthorized

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Trips fetched |
| 401 | Unauthorized |
| 500 | Server error |

---

## 19. GET /trips

**Purpose:** Get all completed trips (discovery)

### Response

#### ✅ 200 OK
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Completed trips fetched",
  "data": [/* list of completed trips */]
}
```

---

## 20-25. Join Request Endpoints

**Base Path:** Multiple paths

### 20. POST /trips/{tripId}/join-requests

**Purpose:** Create join request for a trip

#### Request
```json
{
  "message": "I'd like to join this trip!"
}
```

#### Response (201 Created)
```json
{
  "statusCode": 201,
  "status": "SUCCESS",
  "statusMessage": "Join request created",
  "data": {
    "requestId": "uuid",
    "status": "PENDING",
    "createdAt": "2026-01-17T10:00:00Z"
  }
}
```

#### Status Codes
| Code | Reason |
|------|--------|
| 201 | Request created |
| 400 | Already member/request exists |
| 401 | Unauthorized |
| 403 | Trip is private |
| 404 | Trip not found |
| 409 | Trip full |
| 500 | Server error |

---

### 21. POST /join-requests/{id}/approve

**Purpose:** Approve join request (host only)

#### Status Codes
| Code | Reason |
|------|--------|
| 200 | Approved |
| 401 | Unauthorized |
| 403 | Not host |
| 404 | Request not found |
| 409 | Trip full |
| 500 | Server error |

---

### 22. POST /join-requests/{id}/reject

**Purpose:** Reject join request (host only)

#### Status Codes
| Code | Reason |
|------|--------|
| 200 | Rejected |
| 401 | Unauthorized |
| 403 | Not host |
| 404 | Request not found |
| 500 | Server error |

---

### 23. GET /trips/{tripId}/join-requests

**Purpose:** Get join requests for a trip (host only)

**Query Parameters:**
| Parameter | Type |
|-----------|------|
| status | PENDING, APPROVED, REJECTED |

#### Status Codes
| Code | Reason |
|------|--------|
| 200 | Requests fetched |
| 401 | Unauthorized |
| 403 | Not host |
| 404 | Trip not found |
| 500 | Server error |

---

### 24. DELETE /join-requests/{id}/cancel

**Purpose:** Cancel own join request

#### Status Codes
| Code | Reason |
|------|--------|
| 200 | Cancelled |
| 401 | Unauthorized |
| 404 | Request not found |
| 500 | Server error |

---

### 25. DELETE /trips/{tripId}/participants/{participantUserId}

**Purpose:** Remove participant or leave trip

#### Request
```json
{
  "removalReason": "Personal reasons"
}
```

#### Status Codes
| Code | Reason |
|------|--------|
| 200 | Removed/Left |
| 401 | Unauthorized |
| 403 | Cannot remove host |
| 404 | Trip/participant not found |
| 500 | Server error |

---

## 26. GET /trips/{tripId}/join-request-status

**Purpose:** Get current user's join request status for a trip

#### Status Codes
| Code | Reason |
|------|--------|
| 200 | Status fetched |
| 401 | Unauthorized |
| 404 | Trip not found |
| 500 | Server error |

---

## 27-29. Wishlist Endpoints

**Base Path:** `/users/me/wishlist`

### 27. POST /users/me/wishlist

**Purpose:** Add trip to wishlist

#### Status Codes
| Code | Reason |
|------|--------|
| 201 | Added to wishlist |
| 401 | Unauthorized |
| 404 | Trip not found |
| 409 | Already in wishlist |
| 500 | Server error |

---

### 28. DELETE /users/me/wishlist/{tripId}

**Purpose:** Remove trip from wishlist

#### Status Codes
| Code | Reason |
|------|--------|
| 200 | Removed |
| 401 | Unauthorized |
| 404 | Not in wishlist |
| 500 | Server error |

---

### 29. GET /users/me/wishlist

**Purpose:** Get user's wishlist

#### Status Codes
| Code | Reason |
|------|--------|
| 200 | Wishlist fetched |
| 401 | Unauthorized |
| 500 | Server error |

---

## 30-32. Discovery Endpoints

**Base Path:** `/trips`

### 30. GET /trips/featured

**Purpose:** Get featured trips

**Query Parameters:**
| Parameter | Type | Default |
|-----------|------|---------|
| page | Integer | 0 |
| size | Integer | 20 |

#### Status Codes
| Code | Reason |
|------|--------|
| 200 | Featured trips fetched |
| 500 | Server error |

---

### 31. GET /trips/trending-destinations

**Purpose:** Get trending destinations

#### Status Codes
| Code | Reason |
|------|--------|
| 200 | Trends fetched |
| 500 | Server error |

---

### 32. POST /api/v1/ai/itinerary

**Purpose:** Generate AI itinerary

#### Request
```json
{
  "destination": "Manali",
  "days": 5,
  "interests": ["hiking", "adventure", "nature"],
  "budget": 15000
}
```

#### Status Codes
| Code | Reason |
|------|--------|
| 200 | Itinerary generated |
| 400 | Invalid input |
| 401 | Unauthorized |
| 503 | AI service unavailable |
| 500 | Server error |

---

**Last Updated:** July 14, 2026

