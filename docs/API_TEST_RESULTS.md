# API test results and usage

This document describes the API integration tests added for the main controllers and how to run them.

## Prerequisites

- Java 21+
- Maven (or use `./mvnw`)

## Running the API tests

Run all API and data integration tests:

```bash
./mvnw test -Dtest='PublicProfileApiTest,RatingApiTest,NotificationApiTest,TripApiTest,TravelPalApiTest,AuthApiTest,DataSqlIntegrationTest'
```

Or run the full test suite (including existing unit tests):

```bash
./mvnw test
```

To run a single test class:

```bash
./mvnw test -Dtest=PublicProfileApiTest
./mvnw test -Dtest=AuthApiTest
```

## Test setup

- **Base class:** `ApiTestBase` – `@SpringBootTest` + `@AutoConfigureMockMvc`. Uses the same H2 in-memory DB and `data.sql` as the main app, so seed data (users, trips, ratings, notifications, travel pals) is loaded.
- **Authentication:** Secured endpoints are tested with `@WithMockUser(username = "<uuid>")`. The app treats the principal as the current user UUID (see `SecurityUtils.getCurrentUserUuid()`).
- **Constants:** Test UUIDs and IDs (e.g. `USER_UUID_1`, `COMPLETED_TRIP_ID`, `NOTIFICATION_ID_USER1`) are defined in `ApiTestBase` and match `data.sql`.

## Scenarios covered

### Public profile (`PublicProfileApiTest`)

| Scenario | Method | Endpoint | Auth | Expected |
|----------|--------|----------|------|----------|
| Success | GET | `/public/profile/{userId}` | Yes | 200, body with `trustScore`, `reviews` |
| No auth | GET | `/public/profile/{userId}` | No | 401, UNAUTHORIZED |
| Unknown user | GET | `/public/profile/{nonExistentUuid}` | Yes | 404, ERROR |

### Ratings (`RatingApiTest`)

| Scenario | Method | Endpoint | Auth | Expected |
|----------|--------|----------|------|----------|
| Valid trip rating | PUT | `/trips/{tripId}/ratings/trip` | Yes | 200, SUCCESS |
| Invalid rating (e.g. 0) | PUT | `/trips/{tripId}/ratings/trip` | Yes | 400, validation errors in `data` |
| No auth | PUT | `/trips/{tripId}/ratings/trip` | No | 401 |

### Notifications (`NotificationApiTest`)

| Scenario | Method | Endpoint | Auth | Expected |
|----------|--------|----------|------|----------|
| List notifications | GET | `/notifications?page=0&size=20` | Yes | 200, `data.content` array |
| Unread count | GET | `/notifications/unread-count` | Yes | 200, `data` number |
| Mark as read | PATCH | `/notifications/{notificationId}/read` | Yes | 200 |
| No auth | GET | `/notifications`, `/notifications/unread-count` | No | 401 |

### Trips (`TripApiTest`)

| Scenario | Method | Endpoint | Auth | Expected |
|----------|--------|----------|------|----------|
| User trips | GET | `/trips/user` | Yes | 200, `data` array |
| All completed trips | GET | `/trips` | Yes | 200, `data` array |
| Trip detail (existing) | GET | `/trips/{tripId}` | Yes | 200, `data.tripId` present |
| Trip not found | GET | `/trips/{nonExistentUuid}` | Yes | 400 (TRIP_NOT_FOUND) |
| No auth | GET | `/trips/user`, `/trips`, `/trips/{id}` | No | 401 |

### Travel-pal (`TravelPalApiTest`)

| Scenario | Method | Endpoint | Auth | Expected |
|----------|--------|----------|------|----------|
| My travel pals | GET | `/travel-pal/my` | Yes | 200, `data` present |
| Pending requests | GET | `/travel-pal/pending` | Yes | 200, `data` present |
| No auth | GET | `/travel-pal/my`, `/travel-pal/pending` | No | 401 |

### Auth – public endpoints (`AuthApiTest`)

| Scenario | Method | Endpoint | Body | Expected |
|----------|--------|----------|------|----------|
| OTP request (valid) | POST | `/auth/otp/request` | `countryCode` + `mobileNumber` | 200, "OTP sent successfully" |
| OTP request (invalid – both phone and email) | POST | `/auth/otp/request` | both identifiers | 400 |
| Session login (valid mobile) | POST | `/auth/session/login` | `{"mobileNumber": "9000000001"}` | 200, `data.authenticated: true` |
| Session login (user not found) | POST | `/auth/session/login` | non-existent mobile | 404 |
| Session login (invalid email format) | POST | `/auth/session/login` | `{"emailId": "not-an-email"}` | 400 |

## Expected test result summary

When all tests pass:

- **PublicProfileApiTest:** 3 tests
- **RatingApiTest:** 3 tests
- **NotificationApiTest:** 5 tests
- **TripApiTest:** 6 tests
- **TravelPalApiTest:** 4 tests
- **AuthApiTest:** 5 tests
- **DataSqlIntegrationTest:** 5 tests (data.sql verification)

**Total:** 31 tests (for the above classes). No production code changes are required; only test code and the `spring-security-test` dependency were added.
