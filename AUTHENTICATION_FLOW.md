# Tranzo User MS - Authentication Flow

This document describes the authentication and session management flow in the Tranzo User microservice.

---

## 1. Overview

The service uses **OTP-based authentication** (email or mobile) with **JWT** for API access. Key characteristics:

- **Stateless** access tokens; **stateful** refresh tokens (stored in DB)
- Tokens delivered via **HttpOnly cookies** (web) or **Authorization header** (API clients)
- **Single session per user**: new login revokes all previous refresh tokens

---

## 2. Token Types

| Token | Purpose | Expiry | Storage | Where Used |
|-------|---------|--------|---------|------------|
| **Access** | Authorize API requests | 15 min | Cookie / Bearer header | Protected endpoints |
| **Refresh** | Obtain new access token | 7 days | Cookie only | `/auth/session/refresh` |
| **Registration** | Complete profile for new users | 10 min | Response body | `Authorization: Bearer` on `/user/register` |

---

## 3. Authentication Flows

### 3.1 Request OTP

**Endpoint:** `POST /auth/otp/request`

**Request body (email):**
```json
{
  "emailId": "user@example.com"
}
```

**Request body (mobile):**
```json
{
  "countryCode": "+91",
  "mobileNumber": "9876543210"
}
```

**Flow:**
1. Validate identifier (either email OR mobile, not both)
2. Generate 6-digit OTP
3. Store OTP in cache: key `OTP:{identifier}`
4. Send OTP via SMS/Email (currently logged; integration TODO)

---

### 3.2 Verify OTP (Login / Registration Entry Point)

**Endpoint:** `POST /auth/otp/verify`

**Request body:**
```json
{
  "otp": "123456",
  "emailId": "user@example.com"
}
```
(or `countryCode` + `mobileNumber` for mobile)

**Flow:**

```
                    ┌─────────────────┐
                    │  Verify OTP     │
                    └────────┬────────┘
                             │
              ┌──────────────┴──────────────┐
              │  OTP valid in cache?        │
              └──────────────┬──────────────┘
                             │
           ┌─────────────────┼─────────────────┐
           │ No              │                 │ Yes
           ▼                 │                 ▼
    ┌──────────────┐         │      ┌─────────────────────┐
    │ Throw        │         │      │ User exists in DB?  │
    │ OtpException │         │      └──────────┬──────────┘
    └──────────────┘         │                 │
                             │     ┌───────────┴───────────┐
                             │     │ No                    │ Yes
                             │     ▼                       ▼
                             │  ┌──────────────┐    ┌──────────────────┐
                             │  │ NEW USER     │    │ EXISTING USER    │
                             │  │ 1. Create    │    │ 1. createSession │
                             │  │    user      │    │ 2. Set cookies   │
                             │  │ 2. Generate  │    │    (ACCESS +     │
                             │  │    reg token │    │     REFRESH)     │
                             │  │ 3. Return    │    │ 3. Return        │
                             │  │    {userExists:│   │    {userExists:  │
                             │  │     false,   │    │     true}        │
                             │  │     regToken}│    └──────────────────┘
                             │  └──────────────┘
                             │
```

**Responses:**
- **New user:** `{ "userExists": false, "registrationToken": "eyJ..." }` — client must call `/user/register` with this token
- **Existing user:** `{ "userExists": true }` — session cookies set; client can call protected APIs

---

### 3.3 Complete Registration (New Users Only)

**Endpoint:** `POST /user/register`  
**Auth:** `Authorization: Bearer {registrationToken}`

The registration token is returned after OTP verification for new users. It is short-lived (10 min) and identifies the user by email or phone.

**Flow:**
1. `RegistrationTokenFilter` validates the Bearer token (type must be `REGISTRATION`)
2. Extracts identifier from token subject
3. User controller completes profile (name, DOB, etc.) and links to the user created during OTP verify

---

### 3.4 Direct Login (Alternative)

**Endpoint:** `POST /auth/session/login`

**Request body:** Same as OTP request (email OR mobile)

**Flow:**
1. Find user by identifier
2. Create session (access + refresh tokens)
3. Set cookies
4. Return `{ "authenticated": true }`

*Note: This skips OTP. Typically used when OTP is verified elsewhere or for testing.*

---

### 3.5 Refresh Session

**Endpoint:** `POST /auth/session/refresh`  
**Auth:** `REFRESH_TOKEN` cookie (no header needed)

**Flow:**
1. Extract refresh token from cookie
2. Look up stored token in DB by `user_uuid` (must be non-revoked)
3. Verify token hash matches
4. Validate JWT (signature, expiry)
5. **Rotate** refresh token: update stored hash with new token
6. Generate new access token
7. Set new `ACCESS_TOKEN` and `REFRESH_TOKEN` cookies

---

### 3.6 Logout

**Endpoint:** `POST /auth/session/logout`  
**Auth:** `REFRESH_TOKEN` cookie

**Flow:**
1. Extract refresh token from cookie
2. Find and revoke the stored token (set `revoked = true`)
3. Clear `ACCESS_TOKEN` and `REFRESH_TOKEN` cookies

Logout always succeeds (200) even if the token is invalid or expired.

---

## 4. Protected API Access

**How it works:**

1. Client sends request with credentials:
   - **Cookie:** `ACCESS_TOKEN` (automatically sent by browser)
   - **Header:** `Authorization: Bearer {accessToken}` (for API clients)
   - **Query:** `?token={accessToken}` (for WebSocket handshake)

2. `JwtAuthenticationFilter` runs:
   - Extracts token (header → query → cookie)
   - Validates token (signature, expiry, `type == ACCESS`)
   - Sets `SecurityContext` with `userUuid` as principal

3. Controllers use `SecurityUtils.getCurrentUserUuid()` to get the authenticated user.

**Public endpoints (no auth):**
- `/auth/otp/**` — OTP request and verify
- `/auth/session/**` — Login, refresh, logout
- `/h2-console/**` — H2 console (dev)
- `/ws-chat/**` — WebSocket (auth may be done at connection time)

---

## 5. Database Tables (Auth-Related)

Authentication and session management use the following tables:

### 5.1 users

Core user account. Created during OTP verify (new user) or looked up for existing users.

| Column | Type | Notes |
|--------|------|-------|
| user_uuid | UUID | PK |
| country_code | varchar | e.g. +91 (nullable if login via email) |
| email | varchar | Unique (nullable if login via mobile) |
| mobile_number | varchar | Unique with country_code |
| role | enum | UserRole (e.g. NORMAL_USER) |
| account_status | enum | AccountStatus (e.g. ACTIVE) |
| created_at | timestamp | |
| updated_at | timestamp | |

**Constraints:** `uk_app_user_email` (email), `uk_app_user_mobile` (country_code, mobile_number)

**Auth usage:**
- OTP verify (new user): `INSERT` new row
- OTP verify (existing): `SELECT` by email or mobile_number
- Session creation: `SELECT` user for token generation

---

### 5.2 refresh_token

Stores SHA-256 hash of refresh tokens. One active (non-revoked) token per user under single-session policy.

| Column | Type | Notes |
|--------|------|-------|
| refresh_token_uuid | UUID | PK |
| user_uuid | UUID | FK → users |
| token_hash | varchar | SHA-256 hash, **unique** |
| expires_at | timestamp | |
| revoked | boolean | false = active, true = invalidated |
| created_at | timestamp | |
| updated_at | timestamp | |

**Constraints:** `token_hash` unique; index `idx_refresh_token_user_revoked` (user_uuid, revoked)

**Auth usage:**
- Create session: `INSERT` new row (after revoking old ones)
- Refresh session: `SELECT` by user_uuid where revoked=false; `UPDATE` token_hash, expires_at
- Logout: `UPDATE` revoked=true

---

### 5.3 user_profile

Profile data (name, DOB, etc.). Created/updated during `/user/register` for new users.

| Column | Type | Notes |
|--------|------|-------|
| user_profile_uuid | UUID | PK |
| user_uuid | UUID | FK → users, unique (1:1) |
| FIRST_NAME, LAST_NAME, etc. | | |

**Auth usage:** Linked to user created in OTP flow; completed during registration.

---

### 5.4 OTP Storage (Cache, Not DB)

OTPs are stored in **Caffeine cache** (in-memory), not in the database:
- **Key:** `OTP:{identifier}` (e.g. `OTP:user@example.com` or `OTP:+919876543210`)
- **Value:** 6-digit OTP string

---

### 5.5 Entity Relationship (Auth)

```
users (1) ────────────────────── (*) refresh_token
  │                                    │
  │                                    token_hash (unique)
  │                                    revoked
  │
  └── (1:1) user_profile
```

---

## 6. Token Storage and Security

| Aspect | Implementation |
|--------|----------------|
| **Refresh token storage** | SHA-256 hash stored in `refresh_token` table; raw token never persisted |
| **Uniqueness** | Each refresh token has `jti` (JWT ID) to avoid hash collisions |
| **Session policy** | One active session per user; new login revokes all previous refresh tokens |
| **Cookies** | HttpOnly, Secure, Path=/ to reduce XSS and MITM risk |
| **CORS** | Credentials allowed from configured origin (e.g. `http://localhost:3000`) |

---

## 7. Configuration (application.yaml)

```yaml
spring:
  jwt:
    secret: <base64-or-hex-secret>
    access-token-expiry-minutes: 15
    refresh-token-expiry-days: 7
    registration-token-expiry-minutes: 10
    issuer: tranzo-auth
```

---

## 8. Sequence Diagram (Full Flow)

```
Client          OTP Service      SessionService     DB            JWT
  │                   │                 │            │            │
  │──POST /otp/request─────────────────>│            │            │
  │                   │  generate OTP   │            │            │
  │                   │  cache.put()    │            │            │
  │<──200 OK──────────│                 │            │            │
  │                   │                 │            │            │
  │──POST /otp/verify (otp)────────────>│            │            │
  │                   │  validate OTP   │            │            │
  │                   │  user exists?   │            │            │
  │                   │       │         │            │            │
  │                   │       ├─No──> create user ──>│            │
  │                   │       │         │            │   regToken │
  │                   │       │         │            │<───────────│
  │                   │       │         │            │            │
  │                   │       └─Yes─> createSession  │            │
  │                   │                 │  revoke old tokens ────>│
  │                   │                 │  access+refresh tokens  │
  │                   │                 │<────────────────────────│
  │                   │                 │  save new token hash ──>│
  │                   │                 │  set cookies            │
  │<──200 + Set-Cookie─────────────────│            │            │
  │                   │                 │            │            │
  │──GET /trips (Cookie)────────────────────────────────────────>│
  │                   │                 │  validate ACCESS token  │
  │                   │                 │  SecurityContext.set()  │
  │<──200 + data─────────────────────────────────────────────────│
```

---

## 9. Error Handling

| Scenario | Response |
|----------|----------|
| OTP expired / not found | `OtpException` |
| Invalid OTP | `OtpException` |
| User not found (login) | `UserNotFoundException` |
| Invalid or expired token | `UnauthorizedException` |
| Missing registration token | `UnauthorizedException` |
| Session expired (refresh) | `AuthException` |
