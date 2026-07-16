# API Contract: Create Settlement

## Endpoint
```
POST /api/splitwise/settlements
```

## HTTP Method
`POST`

## Content-Type
`application/json`

## Authentication
- **Required**: Yes
- **Type**: Bearer Token (JWT)
- **Header**: `Authorization: Bearer <token>`

## Request

### Request Body

**CreateSettlementRequest Structure**:
```json
{
  "groupId": "uuid (required)",
  "paidById": "uuid (required)",
  "paidToId": "uuid (required)",
  "amount": "number (required, minimum: 0.01)",
  "paymentMethod": "string (optional, max 50 characters)",
  "notes": "string (optional, max 500 characters)"
}
```

### Field Descriptions

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| groupId | UUID | Yes | Not null | ID of the Splitwise group |
| paidById | UUID | Yes | Not null | ID of the user who made the payment |
| paidToId | UUID | Yes | Not null | ID of the user who received the payment |
| amount | BigDecimal | Yes | Not null, min 0.01 | Amount settled |
| paymentMethod | String | No | Max 50 characters | Payment method used (e.g., "Cash", "UPI", "Bank Transfer") |
| notes | String | No | Max 500 characters | Additional notes about the settlement |

### Example Request (cURL)
```bash
curl -X POST https://api.example.com/api/splitwise/settlements \
  -H "Authorization: Bearer <jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "groupId": "550e8400-e29b-41d4-a716-446655440000",
    "paidById": "660e8400-e29b-41d4-a716-446655440001",
    "paidToId": "770e8400-e29b-41d4-a716-446655440002",
    "amount": 150.50,
    "paymentMethod": "UPI",
    "notes": "Settlement for dinner expenses"
  }'
```

### Example Request (JavaScript)
```javascript
const settlementData = {
  groupId: "550e8400-e29b-41d4-a716-446655440000",
  paidById: "660e8400-e29b-41d4-a716-446655440001",
  paidToId: "770e8400-e29b-41d4-a716-446655440002",
  amount: 150.50,
  paymentMethod: "UPI",
  notes: "Settlement for dinner expenses"
};

fetch('/api/splitwise/settlements', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify(settlementData)
});
```

## Response

### Success Response (200 OK)
```json
{
  "id": "uuid",
  "group": {
    "id": "uuid",
    "name": "string",
    "description": "string"
  },
  "paidBy": {
    "id": "uuid",
    "firstName": "string",
    "lastName": "string",
    "email": "string"
  },
  "paidTo": {
    "id": "uuid",
    "firstName": "string",
    "lastName": "string",
    "email": "string"
  },
  "amount": 150.50,
  "paymentMethod": "UPI",
  "notes": "Settlement for dinner expenses",
  "settledAt": "2026-04-22T18:30:00",
  "settledExpenses": [
    {
      "id": "uuid",
      "description": "string",
      "amount": 100.00
    }
  ],
  "isFullyAllocated": true,
  "remainingAmount": 0.00,
  "status": "SETTLED"
}
```

### Error Responses

#### 400 Bad Request
```json
{
  "statusCode": 400,
  "message": "Validation failed",
  "errors": [
    {
      "field": "amount",
      "message": "Amount must be greater than 0"
    }
  ]
}
```

#### 401 Unauthorized
```json
{
  "statusCode": 401,
  "message": "Authentication failed"
}
```

#### 404 Not Found
```json
{
  "statusCode": 404,
  "message": "Group not found"
}
```

#### 500 Internal Server Error
```json
{
  "statusCode": 500,
  "message": "Internal server error"
}
```

## Status Codes

| Code | Description |
|------|-------------|
| 200 | Settlement created successfully |
| 400 | Validation error in request data |
| 401 | Authentication failed or missing token |
| 404 | Group or user not found |
| 500 | Internal server error |

## Validation Rules

### Required Fields
- `groupId`: Must not be null, must be a valid UUID
- `paidById`: Must not be null, must be a valid UUID
- `paidToId`: Must not be null, must be a valid UUID
- `amount`: Must not be null, must be greater than or equal to 0.01

### Optional Fields
- `paymentMethod`: String, maximum 50 characters
- `notes`: String, maximum 500 characters

## Notes

- The user ID is extracted from the JWT token for authorization
- The settlement is recorded against the specified Splitwise group
- `paidById` and `paidToId` must be valid users in the system
- The amount must be a positive value (minimum 0.01)
- The response includes details of settled expenses, if any
- `isFullyAllocated` indicates whether the settlement amount is fully allocated to expenses
- `remainingAmount` shows any unallocated settlement amount
- The settlement timestamp is automatically set to the current time

## Related Endpoints

- `GET /api/splitwise/settlements/{settlementId}` - Get settlement by ID
- `GET /api/splitwise/settlements/group/{groupId}` - Get all settlements for a group
- `GET /api/splitwise/settlements/my-settlements` - Get settlements for current user
- `GET /api/splitwise/settlements/optimize/{groupId}` - Get optimized settlement proposals
