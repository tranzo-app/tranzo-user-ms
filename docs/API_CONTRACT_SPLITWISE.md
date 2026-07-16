# API Contract: Splitwise Module (Expenses & Settlement)

**Base URL:** `http://localhost:8089`  
**Controllers:** SplitwiseGroupController, ExpenseController, SettlementController, BalanceController, ActivityController  
**Total Endpoints:** 25+  
**Response Format:** Raw DTOs (NO ResponseDto wrapper)

---

## Overview

**Note:** Splitwise endpoints return raw DTOs without ResponseDto wrapper. Error responses use standard HTTP status codes with error descriptions.

**Base Paths:**
- Groups: `/api/splitwise/groups`
- Expenses: `/api/splitwise/expenses`
- Settlements: `/api/splitwise/settlements`
- Balances: `/api/splitwise/balances`
- Activities: `/api/splitwise/activities`

---

## GROUP MANAGEMENT ENDPOINTS

## 1. POST /api/splitwise/groups

**Purpose:** Create a new expense group

### Request

```http
POST /api/splitwise/groups HTTP/1.1
Host: localhost:8089
Content-Type: application/json
Authorization: Bearer <access_token>
```

**Authentication:** Required

**Request Body:**
```json
{
  "name": "Manali Trip 2026",
  "tripId": "550e8400-e29b-41d4-a716-446655440000",
  "description": "Expense tracking for Manali trip"
}
```

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | String | Yes | Max 100 chars | Group name |
| tripId | UUID | No | Valid trip UUID | Associated trip (optional) |
| description | String | No | Max 500 chars | Group description |

### Response

#### ✅ 200 OK
```json
{
  "id": "1",
  "name": "Manali Trip 2026",
  "tripId": "550e8400-e29b-41d4-a716-446655440000",
  "description": "Expense tracking for Manali trip",
  "createdBy": "550e8400-e29b-41d4-a716-446655440001",
  "createdAt": "2026-01-15T10:30:00Z",
  "members": [
    {
      "id": "1",
      "userId": "550e8400-e29b-41d4-a716-446655440001",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com",
      "joinedAt": "2026-01-15T10:30:00Z"
    }
  ]
}
```

#### ❌ 400 Bad Request
**Scenarios:**
- Missing group name
- Invalid trip ID
- Duplicate group name for user

```json
{
  "statusCode": 400,
  "message": "Group name cannot be empty",
  "error": "BAD_REQUEST"
}
```

#### ❌ 401 Unauthorized

```json
{
  "statusCode": 401,
  "message": "Unauthorized",
  "error": "UNAUTHORIZED"
}
```

#### ❌ 404 Not Found
**Scenarios:**
- Trip not found (if tripId provided)

```json
{
  "statusCode": 404,
  "message": "Trip not found",
  "error": "NOT_FOUND"
}
```

#### ❌ 500 Internal Server Error

```json
{
  "statusCode": 500,
  "message": "Failed to create group",
  "error": "INTERNAL_ERROR"
}
```

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Group created |
| 400 | Validation error |
| 401 | Unauthorized |
| 404 | Trip not found |
| 500 | Server error |

---

## 2. GET /api/splitwise/groups/{groupId}

**Purpose:** Get single group details

### Request

```http
GET /api/splitwise/groups/1 HTTP/1.1
Host: localhost:8089
Authorization: Bearer <access_token>
```

### Response

#### ✅ 200 OK
```json
{
  "id": "1",
  "name": "Manali Trip 2026",
  "tripId": "550e8400-e29b-41d4-a716-446655440000",
  "description": "Expense tracking for Manali trip",
  "createdBy": "550e8400-e29b-41d4-a716-446655440001",
  "createdAt": "2026-01-15T10:30:00Z",
  "totalExpenses": 35000.00,
  "totalMembers": 5,
  "members": [
    {
      "id": "1",
      "userId": "550e8400-e29b-41d4-a716-446655440001",
      "firstName": "John",
      "lastName": "Doe"
    },
    {
      "id": "2",
      "userId": "550e8400-e29b-41d4-a716-446655440002",
      "firstName": "Jane",
      "lastName": "Smith"
    }
  ]
}
```

#### ❌ 401 Unauthorized

#### ❌ 403 Forbidden
**Scenarios:**
- User is not a member of group

```json
{
  "statusCode": 403,
  "message": "You don't have access to this group",
  "error": "FORBIDDEN"
}
```

#### ❌ 404 Not Found

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Group fetched |
| 401 | Unauthorized |
| 403 | Not a member |
| 404 | Group not found |
| 500 | Server error |

---

## 3. PUT /api/splitwise/groups/{groupId}

**Purpose:** Update group details

### Request

```http
PUT /api/splitwise/groups/1 HTTP/1.1
Host: localhost:8089
Content-Type: application/json
Authorization: Bearer <access_token>
```

**Request Body:** Same as POST /api/splitwise/groups

### Response

#### ✅ 200 OK
```json
{
  "id": "1",
  "name": "Manali Adventure 2026",
  "description": "Updated description"
}
```

#### ❌ 400 Bad Request

#### ❌ 401 Unauthorized

#### ❌ 403 Forbidden
**Scenarios:**
- Only group creator can update

#### ❌ 404 Not Found

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Group updated |
| 400 | Validation error |
| 401 | Unauthorized |
| 403 | Only creator can update |
| 404 | Group not found |
| 500 | Server error |

---

## 4. DELETE /api/splitwise/groups/{groupId}

**Purpose:** Delete group (only if no unsettled expenses)

### Request

```http
DELETE /api/splitwise/groups/1 HTTP/1.1
Host: localhost:8089
Authorization: Bearer <access_token>
```

### Response

#### ✅ 204 No Content
```
(No response body)
```

#### ❌ 400 Bad Request
**Scenarios:**
- Group has unsettled expenses

```json
{
  "statusCode": 400,
  "message": "Cannot delete group with unsettled expenditures",
  "error": "BAD_REQUEST"
}
```

#### ❌ 401 Unauthorized

#### ❌ 403 Forbidden
**Scenarios:**
- Only group creator can delete

#### ❌ 404 Not Found

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 204 | Group deleted |
| 400 | Has unsettled expenses |
| 401 | Unauthorized |
| 403 | Only creator can delete |
| 404 | Group not found |
| 500 | Server error |

---

## 5. GET /api/splitwise/groups/my-groups

**Purpose:** Get all groups for current user

### Request

```http
GET /api/splitwise/groups/my-groups HTTP/1.1
Host: localhost:8089
Authorization: Bearer <access_token>
```

**Query Parameters:**
| Parameter | Type | Default |
|-----------|------|---------|
| page | Integer | 0 |
| size | Integer | 20 |

### Response

#### ✅ 200 OK
```json
[
  {
    "id": "1",
    "name": "Manali Trip 2026",
    "totalMembers": 5,
    "totalExpenses": 35000.00
  },
  {
    "id": "2",
    "name": "Goa Getaway",
    "totalMembers": 3,
    "totalExpenses": 12000.00
  }
]
```

#### ❌ 401 Unauthorized

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Groups fetched |
| 401 | Unauthorized |
| 500 | Server error |

---

## 6. POST /api/splitwise/groups/{groupId}/members

**Purpose:** Add members to group

### Request

```http
POST /api/splitwise/groups/1/members HTTP/1.1
Host: localhost:8089
Content-Type: application/json
Authorization: Bearer <access_token>
```

**Request Body:**
```json
{
  "userIds": [
    "550e8400-e29b-41d4-a716-446655440003",
    "550e8400-e29b-41d4-a716-446655440004"
  ]
}
```

### Response

#### ✅ 200 OK
```json
{
  "id": "1",
  "members": [
    {
      "id": "1",
      "userId": "550e8400-e29b-41d4-a716-446655440001",
      "firstName": "John",
      "lastName": "Doe"
    },
    {
      "id": "3",
      "userId": "550e8400-e29b-41d4-a716-446655440003",
      "firstName": "Alice",
      "lastName": "Johnson"
    },
    {
      "id": "4",
      "userId": "550e8400-e29b-41d4-a716-446655440004",
      "firstName": "Bob",
      "lastName": "Williams"
    }
  ]
}
```

#### ❌ 400 Bad Request
**Scenarios:**
- User already in group
- Invalid user ID

#### ❌ 401 Unauthorized

#### ❌ 403 Forbidden

#### ❌ 404 Not Found

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Members added |
| 400 | User already member |
| 401 | Unauthorized |
| 403 | No permission |
| 404 | Group/user not found |
| 500 | Server error |

---

## 7. DELETE /api/splitwise/groups/{groupId}/members/{memberId}

**Purpose:** Remove member from group

### Response

#### ✅ 200 OK
```json
{
  "id": "1",
  "members": [ /* updated members list */ ]
}
```

#### ❌ 400 Bad Request
**Scenarios:**
- Cannot remove last member
- Member has outstanding balance

#### ❌ 401 Unauthorized

#### ❌ 403 Forbidden

#### ❌ 404 Not Found

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Member removed |
| 400 | Cannot remove (last member/balance) |
| 401 | Unauthorized |
| 403 | No permission |
| 404 | Group/member not found |
| 500 | Server error |

---

## 8. GET /api/splitwise/groups/{groupId}/members

**Purpose:** Get all group members

### Response

#### ✅ 200 OK
```json
{
  "id": "1",
  "members": [
    {
      "id": "1",
      "userId": "550e8400-e29b-41d4-a716-446655440001",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com",
      "joinedAt": "2026-01-15T10:30:00Z"
    }
  ]
}
```

#### ❌ 401 Unauthorized

#### ❌ 403 Forbidden

#### ❌ 404 Not Found

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Members fetched |
| 401 | Unauthorized |
| 403 | Not a member |
| 404 | Group not found |
| 500 | Server error |

---

## EXPENSE ENDPOINTS

## 9. POST /api/splitwise/expenses

**Purpose:** Create expense in group

### Request

```http
POST /api/splitwise/expenses HTTP/1.1
Host: localhost:8089
Content-Type: application/json
Authorization: Bearer <access_token>
```

**Request Body:**
```json
{
  "name": "Team Dinner",
  "description": "Restaurant dinner for team",
  "amount": 3000.00,
  "groupId": 1,
  "splitType": "EQUAL",
  "category": "FOOD",
  "expenseDate": "2026-01-24",
  "splits": [
    {
      "userId": "550e8400-e29b-41d4-a716-446655440001",
      "amount": 750.00
    },
    {
      "userId": "550e8400-e29b-41d4-a716-446655440002",
      "amount": 750.00
    },
    {
      "userId": "550e8400-e29b-41d4-a716-446655440003",
      "amount": 750.00
    },
    {
      "userId": "550e8400-e29b-41d4-a716-446655440004",
      "amount": 750.00
    }
  ]
}
```

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | String | Yes | Max 100 chars | Expense name |
| description | String | No | Max 500 chars | Description |
| amount | Decimal | Yes | > 0 | Total expense amount |
| groupId | Long | Yes | Valid group | Group ID |
| splitType | Enum | Yes | EQUAL, UNEQUAL, ITEMIZE | Split type |
| category | Enum | Yes | FOOD, TRANSPORT, ACCOMMODATION, ENTERTAINMENT, MISCELLANEOUS | Category |
| expenseDate | Date | Yes | Valid date | Expense date (YYYY-MM-DD) |
| splits | Array | Yes | Length >= 2 | Split details (userId + amount) |

### Response

#### ✅ 200 OK
```json
{
  "id": "100",
  "name": "Team Dinner",
  "description": "Restaurant dinner for team",
  "amount": 3000.00,
  "group": {
    "id": "1",
    "name": "Manali Trip 2026"
  },
  "paidBy": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "firstName": "John",
    "lastName": "Doe"
  },
  "splitType": "EQUAL",
  "category": "FOOD",
  "expenseDate": "2026-01-24",
  "splits": [
    {
      "id": "1",
      "userId": "550e8400-e29b-41d4-a716-446655440001",
      "amount": 750.00
    },
    {
      "id": "2",
      "userId": "550e8400-e29b-41d4-a716-446655440002",
      "amount": 750.00
    }
  ],
  "createdAt": "2026-01-24T15:30:00Z"
}
```

#### ❌ 400 Bad Request
**Scenarios:**
- Split amounts don't match total
- Invalid split type
- Invalid category

```json
{
  "statusCode": 400,
  "message": "Split amounts do not match total expense amount",
  "error": "BAD_REQUEST"
}
```

#### ❌ 401 Unauthorized

#### ❌ 404 Not Found
**Scenarios:**
- Group not found

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Expense created |
| 400 | Validation error |
| 401 | Unauthorized |
| 404 | Group not found |
| 500 | Server error |

---

## 10. GET /api/splitwise/expenses/{expenseId}

**Purpose:** Get expense details

### Response

#### ✅ 200 OK
```json
{
  "id": "100",
  "name": "Team Dinner",
  /* same as POST response */
}
```

#### ❌ 401 Unauthorized

#### ❌ 403 Forbidden
**Scenarios:**
- User not in group

#### ❌ 404 Not Found

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Expense fetched |
| 401 | Unauthorized |
| 403 | Not a group member |
| 404 | Expense not found |
| 500 | Server error |

---

## 11. PUT /api/splitwise/expenses/{expenseId}

**Purpose:** Update expense

### Response

#### ✅ 200 OK
```json
{
  /* updated expense */
}
```

#### ❌ 400 Bad Request
**Scenarios:**
- Cannot modify settled expense
- Invalid splits

#### ❌ 401 Unauthorized

#### ❌ 403 Forbidden
**Scenarios:**
- Only creator can modify

#### ❌ 404 Not Found

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Expense updated |
| 400 | Cannot modify/validation error |
| 401 | Unauthorized |
| 403 | Only creator can modify |
| 404 | Expense not found |
| 500 | Server error |

---

## 12. DELETE /api/splitwise/expenses/{expenseId}

**Purpose:** Delete expense

### Response

#### ✅ 204 No Content

#### ❌ 400 Bad Request
**Scenarios:**
- Cannot delete settled expense

#### ❌ 401 Unauthorized

#### ❌ 403 Forbidden

#### ❌ 404 Not Found

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 204 | Expense deleted |
| 400 | Cannot delete settled |
| 401 | Unauthorized |
| 403 | No permission |
| 404 | Expense not found |
| 500 | Server error |

---

## 13. GET /api/splitwise/expenses/group/{groupId}

**Purpose:** Get all expenses in group

### Response

#### ✅ 200 OK
```json
[
  {
    "id": "100",
    "name": "Team Dinner",
    /* expense details */
  },
  {
    "id": "101",
    "name": "Taxi",
    /* expense details */
  }
]
```

#### ❌ 401 Unauthorized

#### ❌ 403 Forbidden

#### ❌ 404 Not Found

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Expenses fetched |
| 401 | Unauthorized |
| 403 | Not a member |
| 404 | Group not found |
| 500 | Server error |

---

## 14. GET /api/splitwise/expenses/my-expenses

**Purpose:** Get expenses for current user across all groups

### Response

#### ✅ 200 OK
```json
[
  {
    "id": "100",
    "name": "Team Dinner",
    /* expense details */
  }
]
```

#### ❌ 401 Unauthorized

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Expenses fetched |
| 401 | Unauthorized |
| 500 | Server error |

---

## SETTLEMENT ENDPOINTS

## 15. POST /api/splitwise/settlements

**Purpose:** Create payment settlement

### Request

```http
POST /api/splitwise/settlements HTTP/1.1
Host: localhost:8089
Content-Type: application/json
Authorization: Bearer <access_token>
```

**Request Body:**
```json
{
  "groupId": 1,
  "paidById": "550e8400-e29b-41d4-a716-446655440002",
  "paidToId": "550e8400-e29b-41d4-a716-446655440001",
  "amount": 750.00,
  "paymentMethod": "UPI",
  "notes": "Settlement for dinner"
}
```

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| groupId | Long | Yes | Valid group | Group ID |
| paidById | UUID | Yes | Valid user | User who paid |
| paidToId | UUID | Yes | Valid user | User who received |
| amount | Decimal | Yes | > 0, <= outstanding | Settlement amount |
| paymentMethod | String | No | Max 50 chars | Payment method (UPI, Cash, etc.) |
| notes | String | No | Max 500 chars | Notes |

### Response

#### ✅ 200 OK
```json
{
  "id": "200",
  "group": {
    "id": "1",
    "name": "Manali Trip 2026"
  },
  "paidBy": {
    "id": "550e8400-e29b-41d4-a716-446655440002",
    "firstName": "Jane",
    "lastName": "Smith"
  },
  "paidTo": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "firstName": "John",
    "lastName": "Doe"
  },
  "amount": 750.00,
  "paymentMethod": "UPI",
  "notes": "Settlement for dinner",
  "status": "PENDING",
  "createdAt": "2026-01-24T16:00:00Z"
}
```

#### ❌ 400 Bad Request
**Scenarios:**
- Amount exceeds outstanding
- Users same
- Invalid amount

```json
{
  "statusCode": 400,
  "message": "Settlement amount exceeds outstanding balance",
  "error": "BAD_REQUEST"
}
```

#### ❌ 401 Unauthorized

#### ❌ 404 Not Found

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Settlement created |
| 400 | Validation error |
| 401 | Unauthorized |
| 404 | Group/user not found |
| 500 | Server error |

---

## 16-19. Settlement Read-Only Endpoints

### 16. GET /api/splitwise/settlements/{settlementId}

**Purpose:** Get settlement details

### 17. GET /api/splitwise/settlements/group/{groupId}

**Purpose:** Get all settlements in group

### 18. GET /api/splitwise/settlements/my-settlements

**Purpose:** Get user's settlements

### 19. GET /api/splitwise/settlements/optimize/{groupId}

**Purpose:** Get optimized settlement proposals (minimize transactions)

All follow same error patterns (200, 401, 403, 404, 500)

---

## BALANCE ENDPOINTS

## 20-23. Balance Calculations

### 20. GET /api/splitwise/balances/group/{groupId}

**Purpose:** Get all balances in group

### Response

#### ✅ 200 OK
```json
[
  {
    "userId": "550e8400-e29b-41d4-a716-446655440001",
    "userName": "John Doe",
    "groupId": 1,
    "balance": 150.00,
    "status": "OWED_TO_ME"
  },
  {
    "userId": "550e8400-e29b-41d4-a716-446655440002",
    "userName": "Jane Smith",
    "groupId": 1,
    "balance": -150.00,
    "status": "I_OWE"
  }
]
```

#### ❌ 401 Unauthorized

#### ❌ 403 Forbidden

#### ❌ 404 Not Found

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Balances fetched |
| 401 | Unauthorized |
| 403 | Not a member |
| 404 | Group not found |
| 500 | Server error |

---

### 21. GET /api/splitwise/balances/group/{groupId}/user/{userId}

**Purpose:** Get specific user's balance in group

---

### 22. GET /api/splitwise/balances/group/{groupId}/my-balance

**Purpose:** Get current user's balance in group

### Response

#### ✅ 200 OK
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440001",
  "userName": "John Doe",
  "groupId": 1,
  "balance": 500.00,
  "status": "OWED_TO_ME",
  "breakdown": {
    "totalSpent": 3000.00,
    "totalOwed": 2500.00
  }
}
```

#### ❌ 401 Unauthorized

#### ❌ 404 Not Found

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Balance fetched |
| 401 | Unauthorized |
| 404 | Group/user not found |
| 500 | Server error |

---

### 23. GET /api/splitwise/balances/dashboard

**Purpose:** Get user's overall financial dashboard across all groups

### Response

#### ✅ 200 OK
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440001",
  "totalBalance": 2500.00,
  "status": "OWED_TO_ME",
  "groups": [
    {
      "groupId": 1,
      "groupName": "Manali Trip",
      "balance": 1500.00,
      "status": "OWED_TO_ME"
    },
    {
      "groupId": 2,
      "groupName": "Goa Getaway",
      "balance": 1000.00,
      "status": "OWED_TO_ME"
    }
  ],
  "summary": {
    "totalExpenses": 25000.00,
    "totalSettled": 22500.00,
    "pending": 2500.00
  }
}
```

#### ❌ 401 Unauthorized

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Dashboard fetched |
| 401 | Unauthorized |
| 500 | Server error |

---

## ACTIVITY ENDPOINTS

## 24. GET /api/splitwise/activities/group/{groupId}

**Purpose:** Get activity log for group

### Request

```http
GET /api/splitwise/activities/group/1?limit=10&offset=0 HTTP/1.1
Host: localhost:8089
Authorization: Bearer <access_token>
```

**Query Parameters:**
| Parameter | Type | Default |
|-----------|------|---------|
| limit | Integer | 10 |
| offset | Integer | 0 |

### Response

#### ✅ 200 OK
```json
[
  {
    "id": "300",
    "type": "EXPENSE_CREATED",
    "description": "John created expense: Team Dinner (₹3000)",
    "actor": {
      "userId": "550e8400-e29b-41d4-a716-446655440001",
      "firstName": "John",
      "lastName": "Doe"
    },
    "relatedExpense": {
      "id": "100",
      "name": "Team Dinner"
    },
    "timestamp": "2026-01-24T15:30:00Z"
  },
  {
    "id": "301",
    "type": "SETTLEMENT_CREATED",
    "description": "Jane paid ₹750 to John",
    "actor": {
      "userId": "550e8400-e29b-41d4-a716-446655440002",
      "firstName": "Jane"
    },
    "relatedSettlement": {
      "id": "200"
    },
    "timestamp": "2026-01-24T16:00:00Z"
  }
]
```

#### ❌ 401 Unauthorized

#### ❌ 403 Forbidden

#### ❌ 404 Not Found

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Activities fetched |
| 401 | Unauthorized |
| 403 | Not a member |
| 404 | Group not found |
| 500 | Server error |

---

## 25. GET /api/splitwise/activities/my-activities

**Purpose:** Get user's activities across all groups

### Request

```http
GET /api/splitwise/activities/my-activities?limit=10&offset=0 HTTP/1.1
Host: localhost:8089
Authorization: Bearer <access_token>
```

### Response

#### ✅ 200 OK
```json
[
  /* activities array */
]
```

#### ❌ 401 Unauthorized

```json
{
  "statusCode": 401,
  "message": "Unauthorized",
  "error": "UNAUTHORIZED"
}
```

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Activities fetched |
| 401 | Unauthorized |
| 500 | Server error |

---

## Common Error Response Format (All Splitwise Endpoints)

```json
{
  "statusCode": 400,
  "message": "Human-readable error message",
  "error": "ERROR_CODE",
  "timestamp": "2026-01-24T16:00:00Z"
}
```

**Common Error Codes:**
- `BAD_REQUEST` (400)
- `UNAUTHORIZED` (401)
- `FORBIDDEN` (403)
- `NOT_FOUND` (404)
- `CONFLICT` (409)
- `INTERNAL_ERROR` (500)
- `SERVICE_UNAVAILABLE` (503)

---

## Summary Table

| # | Endpoint | Method | Response | Code | Codes |
|---|----------|--------|----------|------|-------|
| 1 | /groups | POST | GroupResponse | 200 | 200,400,401,404,500 |
| 2 | /groups/{id} | GET | GroupResponse | 200 | 200,401,403,404,500 |
| 3 | /groups/{id} | PUT | GroupResponse | 200 | 200,400,401,403,404,500 |
| 4 | /groups/{id} | DELETE | — | 204 | 204,400,401,403,404,500 |
| 5 | /groups/my-groups | GET | List | 200 | 200,401,500 |
| 6 | /groups/{id}/members | POST | GroupResponse | 200 | 200,400,401,403,404,500 |
| 7 | /groups/{id}/members/{mid} | DELETE | GroupResponse | 200 | 200,400,401,403,404,500 |
| 8 | /groups/{id}/members | GET | GroupResponse | 200 | 200,401,403,404,500 |
| 9 | /expenses | POST | ExpenseResponse | 200 | 200,400,401,404,500 |
| 10 | /expenses/{id} | GET | ExpenseResponse | 200 | 200,401,403,404,500 |
| 11 | /expenses/{id} | PUT | ExpenseResponse | 200 | 200,400,401,403,404,500 |
| 12 | /expenses/{id} | DELETE | — | 204 | 204,400,401,403,404,500 |
| 13 | /expenses/group/{id} | GET | List | 200 | 200,401,403,404,500 |
| 14 | /expenses/my-expenses | GET | List | 200 | 200,401,500 |
| 15 | /settlements | POST | SettlementResponse | 200 | 200,400,401,404,500 |
| 16 | /settlements/{id} | GET | SettlementResponse | 200 | 200,401,403,404,500 |
| 17 | /settlements/group/{id} | GET | List | 200 | 200,401,403,404,500 |
| 18 | /settlements/my-settlements | GET | List | 200 | 200,401,500 |
| 19 | /settlements/optimize/{id} | GET | List | 200 | 200,401,403,404,500 |
| 20 | /balances/group/{id} | GET | List | 200 | 200,401,403,404,500 |
| 21 | /balances/group/{id}/user/{uid} | GET | BalanceResponse | 200 | 200,401,403,404,500 |
| 22 | /balances/group/{id}/my-balance | GET | BalanceResponse | 200 | 200,401,404,500 |
| 23 | /balances/dashboard | GET | DashboardResponse | 200 | 200,401,500 |
| 24 | /activities/group/{id} | GET | List | 200 | 200,401,403,404,500 |
| 25 | /activities/my-activities | GET | List | 200 | 200,401,500 |

---

**Last Updated:** July 14, 2026

