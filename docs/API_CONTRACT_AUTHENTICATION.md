# API Contract: Authentication Module

**Base URL:** `http://localhost:8089`  
**Controllers:** SessionController, OtpController, AadharController  
**Total Endpoints:** 6  
**Response Format:** ResponseDto<T>

---

## 1. POST /auth/session/login

**Purpose:** Authenticate user and create session (login)

### Request

```http
POST /auth/session/login HTTP/1.1
Host: localhost:8089
Content-Type: application/json
```

**Authentication:** Not required

**Request Body:**
```json
{
  "emailId": "user@example.com",
  "mobileNumber": "+919876543210",
  "password": "securePassword123"
}
```

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| emailId | String | No* | Valid email format | User email (either email OR mobile required) |
| mobileNumber | String | No* | E.164 format | User phone (either email OR mobile required) |
| password | String | Yes | Min 8 chars | User password |

*Must provide either emailId or mobileNumber, not both

### Response

#### ✅ 200 OK (Success)
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Session created successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 900,
    "tokenType": "Bearer",
    "user": {
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com",
      "mobileNumber": "+919876543210",
      "profilePicture": "https://s3.amazonaws.com/...",
      "accountStatus": "ACTIVE"
    }
  }
}
```

#### ❌ 400 Bad Request (Validation Error)
**Scenarios:**
- Missing emailId/mobileNumber
- Invalid email format
- Invalid mobile number format
- Missing password
- Password too short

```json
{
  "statusCode": 400,
  "status": "ERROR",
  "statusMessage": "Validation failed",
  "data": {
    "fieldErrors": {
      "emailId": "Invalid email format",
      "password": "Password must be at least 8 characters"
    }
  }
}
```

#### ❌ 401 Unauthorized (Invalid Credentials)
**Scenarios:**
- Email/mobile not found
- Password incorrect
- Account deactivated

```json
{
  "statusCode": 401,
  "status": "ERROR",
  "statusMessage": "Invalid email/password combination",
  "data": null
}
```

#### ❌ 403 Forbidden (Account Locked)
**Scenarios:**
- Account status = SUSPENDED
- Account status = DELETED
- Multiple failed login attempts (if rate limiting enabled)

```json
{
  "statusCode": 403,
  "status": "ERROR",
  "statusMessage": "Account is suspended. Please contact support.",
  "data": null
}
```

#### ❌ 404 Not Found
**Scenarios:**
- User with email/mobile doesn't exist

```json
{
  "statusCode": 404,
  "status": "ERROR",
  "statusMessage": "User not found with email: user@example.com",
  "data": null
}
```

#### ❌ 500 Internal Server Error
**Scenarios:**
- JWT token generation failure
- Database connection error
- Unexpected exception

```json
{
  "statusCode": 500,
  "status": "ERROR",
  "statusMessage": "An unexpected error occurred while creating session",
  "data": null
}
```

### Example Requests

**cURL:**
```bash
curl -X POST http://localhost:8089/auth/session/login \
  -H "Content-Type: application/json" \
  -d '{
    "emailId": "john@example.com",
    "password": "securePassword123"
  }'
```

**JavaScript:**
```javascript
const response = await fetch('/auth/session/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    emailId: 'john@example.com',
    password: 'securePassword123'
  })
});
const data = await response.json();
localStorage.setItem('accessToken', data.data.accessToken);
localStorage.setItem('refreshToken', data.data.refreshToken);
```

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Credentials valid, session created |
| 400 | Validation error (invalid format, missing fields) |
| 401 | Invalid credentials |
| 403 | Account suspended/locked |
| 404 | User not found |
| 500 | Server error |

---

## 2. POST /auth/session/refresh

**Purpose:** Refresh access token using refresh token

### Request

```http
POST /auth/session/refresh HTTP/1.1
Host: localhost:8089
Authorization: Bearer <refresh_token>
```

**Authentication:** Refresh token (via Authorization header or Cookie)

**Request Body:** Empty

### Response

#### ✅ 200 OK (Success)
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Session refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 900,
    "tokenType": "Bearer"
  }
}
```

#### ❌ 401 Unauthorized (Invalid Refresh Token)
**Scenarios:**
- Refresh token missing
- Refresh token invalid/malformed
- Refresh token expired
- Refresh token revoked

```json
{
  "statusCode": 401,
  "status": "ERROR",
  "statusMessage": "Invalid or expired refresh token",
  "data": null
}
```

#### ❌ 403 Forbidden (Token Compromised)
**Scenarios:**
- Refresh token belongs to suspended/deleted user
- Refresh token revoked after logout

```json
{
  "statusCode": 403,
  "status": "ERROR",
  "statusMessage": "Refresh token is no longer valid",
  "data": null
}
```

#### ❌ 500 Internal Server Error
**Scenarios:**
- JWT generation failure
- Database error

```json
{
  "statusCode": 500,
  "status": "ERROR",
  "statusMessage": "Failed to refresh session",
  "data": null
}
```

### Example Requests

**cURL:**
```bash
curl -X POST http://localhost:8089/auth/session/refresh \
  -H "Authorization: Bearer <refresh_token>"
```

**JavaScript:**
```javascript
const refreshToken = localStorage.getItem('refreshToken');
const response = await fetch('/auth/session/refresh', {
  method: 'POST',
  headers: { 'Authorization': `Bearer ${refreshToken}` }
});
const data = await response.json();
localStorage.setItem('accessToken', data.data.accessToken);
```

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Token refreshed successfully |
| 401 | Invalid/expired refresh token |
| 403 | Token revoked or user account invalid |
| 500 | Server error |

---

## 3. POST /auth/session/logout

**Purpose:** Logout user and invalidate refresh token

### Request

```http
POST /auth/session/logout HTTP/1.1
Host: localhost:8089
```

**Authentication:** Not required (stateless)

**Request Body:** Empty

### Response

#### ✅ 200 OK (Success)
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Logged out successfully",
  "data": null
}
```

#### ❌ 500 Internal Server Error
**Scenarios:**
- Token revocation failure
- Database error

```json
{
  "statusCode": 500,
  "status": "ERROR",
  "statusMessage": "Failed to logout",
  "data": null
}
```

### Example Requests

**cURL:**
```bash
curl -X POST http://localhost:8089/auth/session/logout
```

**JavaScript:**
```javascript
await fetch('/auth/session/logout', { method: 'POST' });
localStorage.removeItem('accessToken');
localStorage.removeItem('refreshToken');
```

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Logout successful |
| 500 | Server error |

---

## 4. POST /auth/otp/request

**Purpose:** Request OTP for mobile/email verification

### Request

```http
POST /auth/otp/request HTTP/1.1
Host: localhost:8089
Content-Type: application/json
```

**Authentication:** Not required

**Request Body:**
```json
{
  "mobileNumber": "+919876543210",
  "countryCode": "+91"
}
```

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| mobileNumber | String | Yes | E.164 format | Phone number for OTP |
| countryCode | String | Yes | E.164 format | Country code |

### Response

#### ✅ 200 OK (OTP Sent)
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "OTP sent successfully to +919876543210",
  "data": {
    "maskedPhone": "+91****543210",
    "expiresIn": 300,
    "requestId": "req_550e8400-e29b-41d4-a716-446655440000"
  }
}
```

#### ❌ 400 Bad Request
**Scenarios:**
- Invalid mobile format
- Invalid country code
- Missing required fields

```json
{
  "statusCode": 400,
  "status": "ERROR",
  "statusMessage": "Invalid mobile number format",
  "data": null
}
```

#### ❌ 429 Too Many Requests (Rate Limited)
**Scenarios:**
- More than 3 OTP requests within 1 minute
- More than 5 failed OTP attempts

```json
{
  "statusCode": 429,
  "status": "ERROR",
  "statusMessage": "Too many OTP requests. Please try again after 1 minute.",
  "data": null
}
```

#### ❌ 503 Service Unavailable
**Scenarios:**
- Twilio/SMS gateway down
- External SMS service error

```json
{
  "statusCode": 503,
  "status": "ERROR",
  "statusMessage": "SMS service temporarily unavailable. Please try again later.",
  "data": null
}
```

#### ❌ 500 Internal Server Error
**Scenarios:**
- Database error
- Unexpected exception

```json
{
  "statusCode": 500,
  "status": "ERROR",
  "statusMessage": "Failed to send OTP",
  "data": null
}
```

### Example Requests

**cURL:**
```bash
curl -X POST http://localhost:8089/auth/otp/request \
  -H "Content-Type: application/json" \
  -d '{
    "mobileNumber": "+919876543210",
    "countryCode": "+91"
  }'
```

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | OTP sent successfully |
| 400 | Invalid input format |
| 429 | Rate limit exceeded |
| 503 | SMS service unavailable |
| 500 | Server error |

---

## 5. POST /auth/otp/verify

**Purpose:** Verify OTP and get access/refresh tokens

### Request

```http
POST /auth/otp/verify HTTP/1.1
Host: localhost:8089
Content-Type: application/json
```

**Authentication:** Not required

**Request Body:**
```json
{
  "mobileNumber": "+919876543210",
  "otp": "123456",
  "requestId": "req_550e8400-e29b-41d4-a716-446655440000"
}
```

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| mobileNumber | String | Yes | E.164 format | Phone number OTP was sent to |
| otp | String | Yes | 6 digits | OTP received by user |
| requestId | String | Yes | UUID | Request ID from OTP request |

### Response

#### ✅ 200 OK (Success)
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "OTP verified successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 900,
    "tokenType": "Bearer",
    "isNewUser": true,
    "user": {
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "mobileNumber": "+919876543210",
      "accountStatus": "ACTIVE"
    }
  }
}
```

#### ❌ 400 Bad Request
**Scenarios:**
- Invalid OTP format (not 6 digits)
- Missing fields
- Invalid phone format

```json
{
  "statusCode": 400,
  "status": "ERROR",
  "statusMessage": "Invalid OTP format. OTP must be 6 digits.",
  "data": null
}
```

#### ❌ 401 Unauthorized
**Scenarios:**
- OTP incorrect
- OTP expired (> 300 seconds)
- Request ID invalid/not found
- OTP already used

```json
{
  "statusCode": 401,
  "status": "ERROR",
  "statusMessage": "Invalid OTP. Please request a new OTP.",
  "data": null
}
```

#### ❌ 429 Too Many Requests
**Scenarios:**
- More than 3 failed OTP verification attempts

```json
{
  "statusCode": 429,
  "status": "ERROR",
  "statusMessage": "Too many failed attempts. Please request a new OTP.",
  "data": null
}
```

#### ❌ 503 Service Unavailable
**Scenarios:**
- User creation service down
- Database unavailable

```json
{
  "statusCode": 503,
  "status": "ERROR",
  "statusMessage": "Service temporarily unavailable. Please try again later.",
  "data": null
}
```

#### ❌ 500 Internal Server Error
**Scenarios:**
- JWT generation failure
- Database error

```json
{
  "statusCode": 500,
  "status": "ERROR",
  "statusMessage": "Failed to verify OTP",
  "data": null
}
```

### Example Requests

**cURL:**
```bash
curl -X POST http://localhost:8089/auth/otp/verify \
  -H "Content-Type: application/json" \
  -d '{
    "mobileNumber": "+919876543210",
    "otp": "123456",
    "requestId": "req_550e8400-e29b-41d4-a716-446655440000"
  }'
```

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | OTP verified, tokens issued |
| 400 | Invalid input format |
| 401 | Incorrect/expired OTP |
| 429 | Too many failed attempts |
| 503 | Service unavailable |
| 500 | Server error |

---

## 6. POST /aadhaar/otp/request

**Purpose:** Request OTP for Aadhaar verification (KYC)

### Request

```http
POST /aadhaar/otp/request HTTP/1.1
Host: localhost:8089
Content-Type: application/json
Authorization: Bearer <access_token>
```

**Authentication:** Required (JWT token)

**Request Body:**
```json
{
  "aadhaarNumber": "123456789012"
}
```

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| aadhaarNumber | String | Yes | 12 digits | Aadhaar ID number |

### Response

#### ✅ 200 OK (OTP Sent)
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "OTP sent to registered Aadhaar mobile",
  "data": {
    "maskedAadhaar": "XXXX XXXX 6789",
    "expiresIn": 300,
    "requestId": "req_550e8400-e29b-41d4-a716-446655440000"
  }
}
```

#### ❌ 400 Bad Request
**Scenarios:**
- Invalid Aadhaar format (not 12 digits)
- Missing Aadhaar number
- Invalid Aadhaar (checksum fails)

```json
{
  "statusCode": 400,
  "status": "ERROR",
  "statusMessage": "Invalid Aadhaar number. Must be 12 digits.",
  "data": null
}
```

#### ❌ 401 Unauthorized
**Scenarios:**
- No JWT token provided
- Invalid/expired token

```json
{
  "statusCode": 401,
  "status": "ERROR",
  "statusMessage": "Unauthorized. Please login first.",
  "data": null
}
```

#### ❌ 429 Too Many Requests
**Scenarios:**
- More than 3 Aadhaar OTP requests per day
- Rate limit exceeded

```json
{
  "statusCode": 429,
  "status": "ERROR",
  "statusMessage": "Too many Aadhaar verification attempts. Please try again tomorrow.",
  "data": null
}
```

#### ❌ 503 Service Unavailable
**Scenarios:**
- Aadhaar UIDAI gateway down
- SMS service unavailable

```json
{
  "statusCode": 503,
  "status": "ERROR",
  "statusMessage": "Aadhaar service temporarily unavailable. Please try again later.",
  "data": null
}
```

#### ❌ 500 Internal Server Error
**Scenarios:**
- Database error
- Unexpected exception

```json
{
  "statusCode": 500,
  "status": "ERROR",
  "statusMessage": "Failed to send Aadhaar OTP",
  "data": null
}
```

### Example Requests

**cURL:**
```bash
curl -X POST http://localhost:8089/aadhaar/otp/request \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <access_token>" \
  -d '{ "aadhaarNumber": "123456789012" }'
```

**JavaScript:**
```javascript
const response = await fetch('/aadhaar/otp/request', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${accessToken}`
  },
  body: JSON.stringify({ aadhaarNumber: '123456789012' })
});
```

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | OTP sent successfully |
| 400 | Invalid Aadhaar format |
| 401 | Unauthorized (missing/invalid token) |
| 429 | Rate limit exceeded |
| 503 | Aadhaar service unavailable |
| 500 | Server error |

---

## 🔗 Common Issues & Solutions

| Issue | Status Code | Solution |
|-------|-------------|----------|
| "Cannot login after registration" | 401 | Verify email/mobile and password. Check account status. |
| "Refresh token expired" | 401 | Login again to get new tokens. |
| "OTP never arrives" | 503 | SMS gateway down. Try after few minutes. |
| "Rate limited on OTP requests" | 429 | Wait 1 minute before requesting new OTP. |
| "Account suspended" | 403 | Contact support to restore account. |
| "Token generation failed" | 500 | Retry. If persistent, server issue. |

---

**Last Updated:** July 14, 2026

