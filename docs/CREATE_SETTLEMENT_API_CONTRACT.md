# Create Settlement API Contract

## Overview
This endpoint allows authenticated users to record a settlement payment within a Splitwise group. Settlements track when one user pays another to settle outstanding debts from shared expenses.

## Endpoint

**URL:** `POST /api/splitwise/settlements`

**Authentication:** Required (Bearer token/JWT)

**Content-Type:** `application/json`

---

## Request Body

### Request Structure

```json
{
  "groupId": "uuid (required)",
  "paidById": "uuid (required)",
  "paidToId": "uuid (required)",
  "amount": 0.01 (required, minimum 0.01)",
  "paymentMethod": "string (optional, max 50 chars)",
  "notes": "string (optional, max 500 chars)"
}
```

### Field Descriptions

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| groupId | UUID | Yes | - | ID of the Splitwise group (both users must be members) |
| paidById | UUID | Yes | Must equal current user ID | ID of the user who made the payment |
| paidToId | UUID | Yes | - | ID of the user who received the payment |
| amount | decimal | Yes | Minimum 0.01 | Amount paid |
| paymentMethod | string | No | Max 50 characters | Payment method (e.g., "UPI", "Cash", "Bank Transfer") |
| notes | string | No | Max 500 characters | Optional notes about the settlement |

### Business Rules

- **Group Membership:** Both `paidById` and `paidToId` must be members of the specified group
- **Payer Validation:** The current authenticated user must be the payer (`paidById` must match current user ID)
- **Amount Validation:** The amount must match or be less than the outstanding debt between the two users
- **Balance Update:** Creating a settlement automatically updates the group's balance calculations
- **Status:** Settlements are created with status "COMPLETED" by default

---

## Response

### Success Response

**Status Code:** `200 OK`

**Response Body:**

```json
{
  "id": "uuid",
  "group": {
    "id": "uuid",
    "tripId": "uuid (optional)",
    "name": "string",
    "description": "string"
  },
  "paidBy": {
    "userUuid": "uuid",
    "name": "string",
    "email": "string",
    "mobileNumber": "string"
  },
  "paidTo": {
    "userUuid": "uuid",
    "name": "string",
    "email": "string",
    "mobileNumber": "string"
  },
  "amount": 0.01,
  "paymentMethod": "string",
  "transactionId": "uuid",
  "notes": "string",
  "settledAt": "ISO-8601 timestamp",
  "settledExpenses": [
    {
      "id": "uuid",
      "name": "string",
      "amount": 0.01
    }
  ],
  "isFullyAllocated": true,
  "remainingAmount": 0.00,
  "status": "COMPLETED"
}
```

### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Unique identifier of the settlement |
| group | object | Group information |
| group.id | UUID | Group ID |
| group.tripId | UUID | Associated trip ID (if any) |
| group.name | string | Group name (trip title if linked to trip) |
| group.description | string | Group description |
| paidBy | object | User who made the payment |
| paidBy.userUuid | UUID | User ID |
| paidBy.name | string | User's full name |
| paidBy.email | string | User's email |
| paidBy.mobileNumber | string | User's mobile number |
| paidTo | object | User who received the payment |
| paidTo.userUuid | UUID | User ID |
| paidTo.name | string | User's full name |
| paidTo.email | string | User's email |
| paidTo.mobileNumber | string | User's mobile number |
| amount | decimal | Amount paid |
| paymentMethod | string | Payment method used |
| notes | string | Settlement notes |
| settledAt | timestamp | When the settlement was recorded |
| settledExpenses | array | List of expenses this settlement covers |
| settledExpenses[].id | UUID | Expense ID |
| settledExpenses[].name | string | Expense name |
| settledExpenses[].amount | decimal | Amount of this expense covered |
| isFullyAllocated | boolean | Whether full amount is allocated to expenses |
| remainingAmount | decimal | Remaining unallocated amount |
| status | string | Settlement status (COMPLETED) |

---

## Error Responses

### 1. Unauthorized (401)

```json
{
  "statusCode": 401,
  "message": "Unauthorized",
  "data": null
}
```

**Cause:** Missing or invalid authentication token.

### 2. Forbidden (403)

```json
{
  "statusCode": 403,
  "message": "Only the payer can create a settlement for themselves",
  "data": null
}
```

**Cause:** Current user is not the payer (`paidById` doesn't match current user ID).

```json
{
  "statusCode": 403,
  "message": "User {userId} is not a member of group {groupId}",
  "data": null
}
```

**Cause:** Current user is not a member of the specified group.

### 3. Validation Error (400)

```json
{
  "statusCode": 400,
  "message": "Validation failed",
  "data": {
    "fieldErrors": [
      {
        "field": "amount",
        "message": "Amount must be greater than 0"
      }
    ]
  }
}
```

**Cause:** Invalid request data (missing fields, invalid values).

### 4. Group Not Found (404)

```json
{
  "statusCode": 404,
  "message": "Group not found with ID: {groupId}",
  "data": null
}
```

**Cause:** Specified group does not exist.

### 5. Balance Validation Error (400)

```json
{
  "statusCode": 400,
  "message": "Settlement amount exceeds outstanding debt",
  "data": null
}
```

**Cause:** Amount exceeds what the payer owes the payee.

---

## Example Requests

### Basic Settlement

**Request:**
```bash
curl -X POST http://localhost:8080/api/splitwise/settlements \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "groupId": "550e8400-e29b-41d4-a716-446655440000",
    "paidById": "123e4567-e89b-12d3-a456-426614174000",
    "paidToId": "123e4567-e89b-12d3-a456-426614174001",
    "amount": 50.00,
    "paymentMethod": "UPI",
    "notes": "Settling dinner expense"
  }'
```

**Response:**
```json
{
  "id": "999e8888-e77b-66d7-c811-234567890123",
  "group": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "tripId": "110e8400-e29b-41d4-a716-446655440001",
    "name": "Paris Trip 2024",
    "description": "Paris trip with friends"
  },
  "paidBy": {
    "userUuid": "123e4567-e89b-12d3-a456-426614174000",
    "name": "Alice Johnson",
    "email": "alice@example.com",
    "mobileNumber": "+1234567890"
  },
  "paidTo": {
    "userUuid": "123e4567-e89b-12d3-a456-426614174001",
    "name": "Bob Smith",
    "email": "bob@example.com",
    "mobileNumber": "+0987654321"
  },
  "amount": 50.00,
  "paymentMethod": "UPI",
  "transactionId": "789e0123-e45b-67d8-a912-345678901234",
  "notes": "Settling dinner expense",
  "settledAt": "2026-04-19T14:30:00",
  "settledExpenses": [
    {
      "id": "222e3333-e44b-55d6-f677-890123456789",
      "name": "Dinner at Restaurant",
      "amount": 50.00
    }
  ],
  "isFullyAllocated": true,
  "remainingAmount": 0.00,
  "status": "COMPLETED"
}
```

### Settlement with Cash Payment

**Request:**
```bash
curl -X POST http://localhost:8080/api/splitwise/settlements \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "groupId": "550e8400-e29b-41d4-a716-446655440000",
    "paidById": "123e4567-e89b-12d3-a456-426614174000",
    "paidToId": "123e4567-e89b-12d3-a456-426614174002",
    "amount": 33.33,
    "paymentMethod": "Cash",
    "notes": "Settling share of hotel bill"
  }'
```

---

## Use Case Example

**Scenario:** During a Paris trip, Alice paid $100 for dinner. Bob and Charlie each owe Alice $33.33. Bob pays Alice his share via UPI.

**Flow:**
1. Alice creates an expense: "Dinner" for $100, split equally among Alice, Bob, Charlie
2. System calculates balances: Bob owes Alice $33.33, Charlie owes Alice $33.33
3. Bob pays Alice $33.33 via UPI
4. Bob calls this endpoint to record the settlement
5. System updates balances: Bob's debt to Alice is now $0.00
6. Alice and Bob can see the settlement in their transaction history

---

## Notes

- **Group Context:** Settlements are always tied to a group. There is no standalone 1-on-1 settlement outside of a group.
- **Payer Restriction:** Only the person making the payment can create the settlement record.
- **Balance Tracking:** The system automatically updates group balances when settlements are created.
- **Activity Logging:** Settlements are logged and visible to all group members.
- **Deletion:** Settlements can be deleted (with balance reversal) if a payment was recorded incorrectly.
- **Audit Trail:** All settlements are permanently recorded for audit purposes.

---

## Related Endpoints

- `GET /api/splitwise/settlements/{settlementId}` - Get a specific settlement
- `GET /api/splitwise/settlements/group/{groupId}` - Get all settlements for a group
- `GET /api/splitwise/settlements/my-settlements` - Get settlements for current user
- `GET /api/splitwise/settlements/optimize/{groupId}` - Get optimized settlement proposals
- `DELETE /api/splitwise/settlements/{settlementId}` - Delete a settlement (reverses balance)

---

## Testing Checklist

- [ ] Test with valid group and user IDs
- [ ] Test with payer ID not matching current user (should fail)
- [ ] Test with non-member user (should fail)
- [ ] Test with invalid group ID (should fail)
- [ ] Test with amount exceeding debt (should fail)
- [ ] Test with negative or zero amount (should fail)
- [ ] Test without authentication token (should fail)
- [ ] Verify balance updates after settlement creation
- [ ] Verify activity logging after settlement creation
- [ ] Test settlement deletion and balance reversal
