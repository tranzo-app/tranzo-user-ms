# API Contract: User Management Module

**Base URL:** `http://localhost:8089`  
**Controllers:** UserController, PublicProfileController, TravelPalController, RatingController  
**Total Endpoints:** 19  
**Response Format:** ResponseDto<T> (most), Plain text (TravelPal)

---

## 1. GET /user

**Purpose:** Get current authenticated user's profile

### Request

```http
GET /user HTTP/1.1
Host: localhost:8089
Authorization: Bearer <access_token>
```

**Authentication:** Required (JWT token)

**Query Parameters:** None

### Response

#### ✅ 200 OK (Success)
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "User profile fetched successfully",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "mobileNumber": "+919876543210",
    "countryCode": "+91",
    "profilePicture": "https://s3.amazonaws.com/tranzo-bucket/...",
    "bio": "Travel enthusiast",
    "accountStatus": "ACTIVE",
    "createdAt": "2026-01-15T10:30:00Z",
    "role": "CUSTOMER",
    "socialHandles": [
      {
        "platform": "INSTAGRAM",
        "url": "https://instagram.com/johndoe"
      },
      {
        "platform": "FACEBOOK",
        "url": "https://facebook.com/johndoe"
      }
    ]
  }
}
```

#### ❌ 401 Unauthorized
**Scenarios:**
- Missing authentication token
- Invalid/expired token

```json
{
  "statusCode": 401,
  "status": "ERROR",
  "statusMessage": "Unauthorized: Invalid or expired token",
  "data": null
}
```

#### ❌ 404 Not Found
**Scenarios:**
- User record deleted
- User ID in token doesn't exist

```json
{
  "statusCode": 404,
  "status": "ERROR",
  "statusMessage": "User not found",
  "data": null
}
```

#### ❌ 500 Internal Server Error

```json
{
  "statusCode": 500,
  "status": "ERROR",
  "statusMessage": "Failed to fetch user profile",
  "data": null
}
```

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Profile fetched successfully |
| 401 | Unauthorized |
| 404 | User not found |
| 500 | Server error |

---

## 2. POST /user/register

**Purpose:** Register new user (with or without profile picture)

### Request

```http
POST /user/register HTTP/1.1
Host: localhost:8089
Content-Type: multipart/form-data
```

**Authentication:** Not required

**Request Body (Multipart):**
```
--boundary
Content-Disposition: form-data; name="profile"
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "mobileNumber": "9876543210",
  "countryCode": "+91",
  "password": "securePassword123",
  "bio": "Travel enthusiast"
}

--boundary
Content-Disposition: form-data; name="file"; filename="profile.jpg"
Content-Type: image/jpeg

[binary image data]
--boundary--
```

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| firstName | String | Yes | Max 50 chars | User first name |
| lastName | String | No | Max 50 chars | User last name |
| email | String | No* | Valid email | User email (either email or mobile required) |
| mobileNumber | String | No* | 10-15 digits | Phone number without country code |
| countryCode | String | Yes (if mobile) | E.164 format | Country code for mobile |
| password | String | Yes | Min 8 chars | Account password |
| bio | String | No | Max 500 chars | User bio |
| file | File | No | JPG/PNG, max 5MB | Profile picture |

*Must provide either email or mobileNumber

### Response

#### ✅ 201 Created (User Registered)
```json
{
  "statusCode": 201,
  "status": "SUCCESS",
  "statusMessage": "User registered successfully",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "mobileNumber": "+919876543210",
    "profilePicture": "https://s3.amazonaws.com/tranzo-bucket/...",
    "accountStatus": "ACTIVE",
    "createdAt": "2026-01-15T10:30:00Z"
  }
}
```

#### ❌ 400 Bad Request
**Scenarios:**
- Invalid email format
- Invalid mobile format
- Missing required fields
- Password too short
- Email/mobile already registered

```json
{
  "statusCode": 400,
  "status": "ERROR",
  "statusMessage": "Validation failed",
  "data": {
    "fieldErrors": {
      "email": "Email already registered",
      "password": "Password must be at least 8 characters"
    }
  }
}
```

#### ❌ 409 Conflict
**Scenarios:**
- Email already exists
- Mobile number already exists
- Username/handle already taken

```json
{
  "statusCode": 409,
  "status": "ERROR",
  "statusMessage": "Email john@example.com is already registered",
  "data": null
}
```

#### ❌ 503 Service Unavailable
**Scenarios:**
- S3 file upload failed
- Email verification service down

```json
{
  "statusCode": 503,
  "status": "ERROR",
  "statusMessage": "File upload service temporarily unavailable",
  "data": null
}
```

#### ❌ 500 Internal Server Error

```json
{
  "statusCode": 500,
  "status": "ERROR",
  "statusMessage": "Failed to register user",
  "data": null
}
```

### Example Requests

**cURL:**
```bash
curl -X POST http://localhost:8089/user/register \
  -F "profile={\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@example.com\",\"password\":\"securePassword123\"};type=application/json" \
  -F "file=@profile.jpg"
```

### Status Code Summary
| Code | Reason |
|------|--------|
| 201 | User registered successfully |
| 400 | Validation error |
| 409 | Email/mobile already exists |
| 503 | Service unavailable |
| 500 | Server error |

---

## 3. PATCH /user/update

**Purpose:** Update user profile information

### Request

```http
PATCH /user/update HTTP/1.1
Host: localhost:8089
Content-Type: multipart/form-data
Authorization: Bearer <access_token>
```

**Authentication:** Required

**Request Body (Multipart):**
```
--boundary
Content-Disposition: form-data; name="profile"
Content-Type: application/json

{
  "firstName": "Johnny",
  "lastName": "Doe Jr",
  "bio": "Updated bio",
  "email": "newemail@example.com"
}

--boundary
Content-Disposition: form-data; name="file"; filename="newprofile.jpg"
Content-Type: image/jpeg

[binary image data]
--boundary--
```

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| firstName | String | No | Max 50 chars | New first name |
| lastName | String | No | Max 50 chars | New last name |
| bio | String | No | Max 500 chars | New bio |
| email | String | No | Valid email | New email |
| file | File | No | JPG/PNG, max 5MB | New profile picture |

### Response

#### ✅ 200 OK (Updated)
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "User profile updated successfully",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "firstName": "Johnny",
    "lastName": "Doe Jr",
    "email": "newemail@example.com",
    "bio": "Updated bio",
    "profilePicture": "https://s3.amazonaws.com/tranzo-bucket/...",
    "updatedAt": "2026-01-20T14:45:00Z"
  }
}
```

#### ❌ 400 Bad Request
**Scenarios:**
- Invalid email format
- Bio exceeds max length
- Invalid file format/size

```json
{
  "statusCode": 400,
  "status": "ERROR",
  "statusMessage": "Validation failed",
  "data": {
    "fieldErrors": {
      "bio": "Bio must be less than 500 characters",
      "file": "File size must be less than 5MB"
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

#### ❌ 409 Conflict
**Scenarios:**
- New email already registered by another user

```json
{
  "statusCode": 409,
  "status": "ERROR",
  "statusMessage": "Email newemail@example.com is already registered",
  "data": null
}
```

#### ❌ 503 Service Unavailable
**Scenarios:**
- S3 upload failed

```json
{
  "statusCode": 503,
  "status": "ERROR",
  "statusMessage": "File upload service temporarily unavailable",
  "data": null
}
```

#### ❌ 500 Internal Server Error

```json
{
  "statusCode": 500,
  "status": "ERROR",
  "statusMessage": "Failed to update user profile",
  "data": null
}
```

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Profile updated successfully |
| 400 | Validation error |
| 401 | Unauthorized |
| 409 | Email already registered by another user |
| 503 | Service unavailable |
| 500 | Server error |

---

## 4. DELETE /user/delete-user

**Purpose:** Permanently delete user account and all related data

### Request

```http
DELETE /user/delete-user HTTP/1.1
Host: localhost:8089
Authorization: Bearer <access_token>
```

**Authentication:** Required

**Request Body:** Empty

### Response

#### ✅ 200 OK (Deleted)
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "User account deleted successfully",
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
- User has active trips (as host)
- User has pending settlements

```json
{
  "statusCode": 403,
  "status": "ERROR",
  "statusMessage": "Cannot delete account with active trips. Please cancel all trips first.",
  "data": null
}
```

#### ❌ 404 Not Found

```json
{
  "statusCode": 404,
  "status": "ERROR",
  "statusMessage": "User not found",
  "data": null
}
```

#### ❌ 500 Internal Server Error

```json
{
  "statusCode": 500,
  "status": "ERROR",
  "statusMessage": "Failed to delete user account",
  "data": null
}
```

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Account deleted successfully |
| 401 | Unauthorized |
| 403 | Cannot delete (active trips/settlements) |
| 404 | User not found |
| 500 | Server error |

---

## 5. PUT /user/profile-picture

**Purpose:** Update or set new profile picture URL

### Request

```http
PUT /user/profile-picture HTTP/1.1
Host: localhost:8089
Content-Type: application/json
Authorization: Bearer <access_token>
```

**Authentication:** Required

**Request Body:**
```json
{
  "url": "https://cdn.example.com/profile-pictures/abc123.jpg"
}
```

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| url | String | Yes | Valid HTTPS URL | Profile picture URL |

### Response

#### ✅ 200 OK (Updated)
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Profile picture updated successfully",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "profilePicture": "https://cdn.example.com/profile-pictures/abc123.jpg",
    "updatedAt": "2026-01-20T15:00:00Z"
  }
}
```

#### ❌ 400 Bad Request
**Scenarios:**
- Invalid URL format
- URL not HTTPS

```json
{
  "statusCode": 400,
  "status": "ERROR",
  "statusMessage": "Invalid URL. Must be a valid HTTPS URL.",
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
  "statusMessage": "Failed to update profile picture",
  "data": null
}
```

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Profile picture updated |
| 400 | Invalid URL format |
| 401 | Unauthorized |
| 500 | Server error |

---

## 6. DELETE /user/profile-picture

**Purpose:** Remove/delete profile picture

### Request

```http
DELETE /user/profile-picture HTTP/1.1
Host: localhost:8089
Authorization: Bearer <access_token>
```

**Authentication:** Required

**Request Body:** Empty

### Response

#### ✅ 200 OK (Removed)
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Profile picture removed successfully",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "profilePicture": null
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
  "statusMessage": "Failed to remove profile picture",
  "data": null
}
```

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Profile picture removed |
| 401 | Unauthorized |
| 500 | Server error |

---

## 7. PATCH /user/social-handles

**Purpose:** Add or update social media handles

### Request

```http
PATCH /user/social-handles HTTP/1.1
Host: localhost:8089
Content-Type: application/json
Authorization: Bearer <access_token>
```

**Authentication:** Required

**Request Body:**
```json
[
  {
    "platform": "INSTAGRAM",
    "url": "https://instagram.com/johndoe"
  },
  {
    "platform": "FACEBOOK",
    "url": "https://facebook.com/johndoe"
  },
  {
    "platform": "TWITTER",
    "url": "https://twitter.com/johndoe"
  }
]
```

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| platform | Enum | Yes | INSTAGRAM, FACEBOOK, TWITTER, LINKEDIN, YOUTUBE | Social platform |
| url | String | Yes | Valid HTTPS URL | Profile URL on that platform |

### Response

#### ✅ 200 OK (Updated)
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Social handles updated successfully",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "socialHandles": [
      {
        "platform": "INSTAGRAM",
        "url": "https://instagram.com/johndoe"
      },
      {
        "platform": "FACEBOOK",
        "url": "https://facebook.com/johndoe"
      },
      {
        "platform": "TWITTER",
        "url": "https://twitter.com/johndoe"
      }
    ]
  }
}
```

#### ❌ 400 Bad Request
**Scenarios:**
- Invalid URL format
- Invalid platform name

```json
{
  "statusCode": 400,
  "status": "ERROR",
  "statusMessage": "Invalid social handle",
  "data": {
    "fieldErrors": {
      "platform": "Platform must be one of: INSTAGRAM, FACEBOOK, TWITTER, etc."
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
  "statusMessage": "Failed to update social handles",
  "data": null
}
```

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Social handles updated |
| 400 | Invalid input |
| 401 | Unauthorized |
| 500 | Server error |

---

## 8. POST /user/{reportedUserId}/report

**Purpose:** Report another user for violation

### Request

```http
POST /user/550e8400-e29b-41d4-a716-446655440001/report HTTP/1.1
Host: localhost:8089
Content-Type: application/json
Authorization: Bearer <access_token>
```

**Authentication:** Required

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| reportedUserId | UUID | ID of user being reported |

**Request Body:**
```json
{
  "reason": "Inappropriate behavior",
  "description": "User was rude and disrespectful during the trip"
}
```

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| reason | String | Yes | Max 50 chars | Report reason |
| description | String | No | Max 1000 chars | Detailed description |

### Response

#### ✅ 200 OK (Reported)
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Report submitted successfully",
  "data": null
}
```

#### ❌ 400 Bad Request
**Scenarios:**
- Reporting self
- Missing reason

```json
{
  "statusCode": 400,
  "status": "ERROR",
  "statusMessage": "Cannot report yourself",
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

#### ❌ 404 Not Found
**Scenarios:**
- Reported user doesn't exist

```json
{
  "statusCode": 404,
  "status": "ERROR",
  "statusMessage": "User not found",
  "data": null
}
```

#### ❌ 409 Conflict
**Scenarios:**
- User already reported by this person

```json
{
  "statusCode": 409,
  "status": "ERROR",
  "statusMessage": "You have already reported this user",
  "data": null
}
```

#### ❌ 500 Internal Server Error

```json
{
  "statusCode": 500,
  "status": "ERROR",
  "statusMessage": "Failed to submit report",
  "data": null
}
```

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Report submitted |
| 400 | Invalid request (self-report) |
| 401 | Unauthorized |
| 404 | User not found |
| 409 | Already reported |
| 500 | Server error |

---

## 9. GET /public/profile/{userId}

**Purpose:** Get public profile of any user (no auth required)

### Request

```http
GET /public/profile/550e8400-e29b-41d4-a716-446655440001 HTTP/1.1
Host: localhost:8089
```

**Authentication:** Not required

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| userId | UUID | User ID |

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page | Integer | 0 | Page number for trips |
| size | Integer | 20 | Page size |

### Response

#### ✅ 200 OK (Public Profile)
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Public profile fetched successfully",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440001",
    "firstName": "Jane",
    "lastName": "Smith",
    "profilePicture": "https://s3.amazonaws.com/...",
    "bio": "Adventure seeker",
    "socialHandles": [
      {
        "platform": "INSTAGRAM",
        "url": "https://instagram.com/janesmith"
      }
    ],
    "tripCount": 15,
    "rating": 4.8,
    "ratingCount": 42,
    "recentTrips": [
      {
        "tripId": "123e4567-e89b-12d3-a456-426614174000",
        "tripTitle": "Manali Adventure",
        "tripDestination": "Manali",
        "startDate": "2026-02-01",
        "endDate": "2026-02-05",
        "currentParticipants": 8,
        "maxParticipants": 10
      }
    ]
  }
}
```

#### ❌ 404 Not Found

```json
{
  "statusCode": 404,
  "status": "ERROR",
  "statusMessage": "User not found",
  "data": null
}
```

#### ❌ 500 Internal Server Error

```json
{
  "statusCode": 500,
  "status": "ERROR",
  "statusMessage": "Failed to fetch public profile",
  "data": null
}
```

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Profile fetched |
| 404 | User not found |
| 500 | Server error |

---

## 10-15. Travel Pal Endpoints (Plain Text Responses)

**Base Path:** `/travel-pal`  
**Authentication:** Required for all  
**Response Format:** Plain text (200 OK) or ResponseDto error (rest)

### 10. POST /travel-pal/request/{receiverId}

**Purpose:** Send travel pal friend request

#### Response
```
✅ 200 OK
Travel pal request sent

❌ 400 Bad Request
Cannot send request to self

❌ 401 Unauthorized
Unauthorized

❌ 404 Not Found
User not found

❌ 409 Conflict
Already travel pals or request already sent
```

---

### 11. POST /travel-pal/accept/{requesterId}

**Purpose:** Accept travel pal request

#### Response
```
✅ 200 OK
Travel pal request accepted

❌ 400 Bad Request
No pending request found

❌ 401 Unauthorized
Unauthorized
```

---

### 12. POST /travel-pal/reject/{requesterId}

**Purpose:** Reject travel pal request

#### Response
```
✅ 200 OK
Travel pal request rejected

❌ 400 Bad Request
No pending request found

❌ 401 Unauthorized
Unauthorized
```

---

### 13. DELETE /travel-pal/{otherUserId}

**Purpose:** Remove travel pal

#### Response
```
✅ 200 OK
Travel pal removed

❌ 404 Not Found
Travel pal relationship not found

❌ 401 Unauthorized
Unauthorized
```

---

### 14. GET /travel-pal/my

**Purpose:** Get list of travel pals

#### Response
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Travel pals retrieved",
  "data": ["uuid1", "uuid2", "uuid3"]
}
```

---

### 15. GET /travel-pal/pending

**Purpose:** Get pending travel pal requests

#### Response
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Pending requests retrieved",
  "data": [
    {
      "requestId": "uuid",
      "fromUserId": "uuid",
      "fromUserName": "John",
      "createdAt": "2026-01-15T10:30:00Z"
    }
  ]
}
```

---

## 16-18. Rating Endpoints

**Base Path:** `/trips/{tripId}/ratings`  
**Authentication:** Required  
**Response Format:** ResponseDto<Void>

### 16. PUT /trips/{tripId}/ratings/trip

**Purpose:** Submit trip rating after completion

#### Request
```json
{
  "rating": 4.5,
  "review": "Amazing trip with great hosts!"
}
```

#### Response
```
✅ 200 OK
Rating submitted

❌ 400 Bad Request
Trip not completed or rating already submitted

❌ 401 Unauthorized
Unauthorized

❌ 403 Forbidden
Only members can rate

❌ 404 Not Found
Trip not found
```

---

### 17. PUT /trips/{tripId}/ratings/host

**Purpose:** Submit rating for trip host

#### Response (Same as above)

---

### 18. PUT /trips/{tripId}/ratings/members

**Purpose:** Submit ratings for other members

#### Request
```json
{
  "memberRatings": [
    {
      "userId": "uuid",
      "rating": 4.0,
      "review": "Good team member"
    }
  ]
}
```

#### Response (Same as above)

---

## Summary Table

| # | Endpoint | Method | Auth | Response | Status Codes |
|---|----------|--------|------|----------|--------------|
| 1 | /user | GET | Yes | ResponseDto | 200, 401, 404, 500 |
| 2 | /user/register | POST | No | ResponseDto | 201, 400, 409, 503, 500 |
| 3 | /user/update | PATCH | Yes | ResponseDto | 200, 400, 401, 409, 503, 500 |
| 4 | /user/delete-user | DELETE | Yes | ResponseDto | 200, 401, 403, 404, 500 |
| 5 | /user/profile-picture | PUT | Yes | ResponseDto | 200, 400, 401, 500 |
| 6 | /user/profile-picture | DELETE | Yes | ResponseDto | 200, 401, 500 |
| 7 | /user/social-handles | PATCH | Yes | ResponseDto | 200, 400, 401, 500 |
| 8 | /user/{id}/report | POST | Yes | ResponseDto | 200, 400, 401, 404, 409, 500 |
| 9 | /public/profile/{id} | GET | No | ResponseDto | 200, 404, 500 |
| 10 | /travel-pal/request/{id} | POST | Yes | Plain text | 200, 400, 401, 404, 409 |
| 11 | /travel-pal/accept/{id} | POST | Yes | Plain text | 200, 400, 401 |
| 12 | /travel-pal/reject/{id} | POST | Yes | Plain text | 200, 400, 401 |
| 13 | /travel-pal/{id} | DELETE | Yes | Plain text | 200, 401, 404 |
| 14 | /travel-pal/my | GET | Yes | ResponseDto | 200, 401 |
| 15 | /travel-pal/pending | GET | Yes | ResponseDto | 200, 401 |
| 16 | /trips/{id}/ratings/trip | PUT | Yes | ResponseDto | 200, 400, 401, 403, 404, 500 |
| 17 | /trips/{id}/ratings/host | PUT | Yes | ResponseDto | 200, 400, 401, 403, 404, 500 |
| 18 | /trips/{id}/ratings/members | PUT | Yes | ResponseDto | 200, 400, 401, 403, 404, 500 |

---

**Last Updated:** July 14, 2026

