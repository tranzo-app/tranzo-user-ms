# API Contract: Chat Module

**Base URL:** `http://localhost:8089`  
**Controllers:** CreateAndManageChatController, ChatSocketController  
**Total Endpoints:** 10 (9 REST + 1 WebSocket)  
**Response Format:** ResponseDto<T>

---

## Chat Overview

**Conversation Types:**
- **ONE_ON_ONE:** Private 1:1 conversation between two users
- **GROUP_CHAT:** Group conversation (created by trip publish event)

**Participant Roles:**
- **ADMIN:** Group admin (can manage participants)
- **MEMBER:** Regular member

**Message Types:**
- **TEXT:** Text message
- **IMAGE:** Image message
- **FILE:** File attachment
- **SYSTEM:** System notification

---

## 1. POST /conversations/one-to-one

**Purpose:** Create or fetch 1:1 conversation

### Request

```http
POST /conversations/one-to-one HTTP/1.1
Host: localhost:8089
Content-Type: application/json
Authorization: Bearer <access_token>
```

**Authentication:** Required

**Request Body:**
```json
{
  "otherUserId": "550e8400-e29b-41d4-a716-446655440050"
}
```

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| otherUserId | UUID | Yes | Valid UUID | User to chat with |

### Response

#### ✅ 200 OK (Existing conversation returned)
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Conversation fetched successfully",
  "data": {
    "conversationId": "550e8400-e29b-41d4-a716-446655440100",
    "type": "ONE_ON_ONE",
    "otherUserDetails": {
      "userId": "550e8400-e29b-41d4-a716-446655440050",
      "firstName": "Jane",
      "lastName": "Smith",
      "profilePicture": "https://s3.amazonaws.com/..."
    },
    "createdAt": "2026-01-15T10:30:00Z"
  }
}
```

#### ✅ 201 Created (New conversation created)
```json
{
  "statusCode": 201,
  "status": "SUCCESS",
  "statusMessage": "Conversation created successfully",
  "data": { /* same as 200 */ }
}
```

#### ❌ 400 Bad Request
**Scenarios:**
- Trying to create conversation with self
- Invalid UUID format

```json
{
  "statusCode": 400,
  "status": "ERROR",
  "statusMessage": "Cannot create conversation with yourself",
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
- Other user doesn't exist

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
  "statusMessage": "Failed to create/fetch conversation",
  "data": null
}
```

### Example Requests

**cURL:**
```bash
curl -X POST http://localhost:8089/conversations/one-to-one \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <access_token>" \
  -d '{ "otherUserId": "550e8400-e29b-41d4-a716-446655440050" }'
```

**JavaScript:**
```javascript
const response = await fetch('/conversations/one-to-one', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({ otherUserId: '550e8400-e29b-41d4-a716-446655440050' })
});
```

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Conversation already exists |
| 201 | New conversation created |
| 400 | Invalid input (self-chat) |
| 401 | Unauthorized |
| 404 | User not found |
| 500 | Server error |

---

## 2. GET /conversations/chat-list

**Purpose:** Get list of all conversations for current user

### Request

```http
GET /conversations/chat-list HTTP/1.1
Host: localhost:8089
Authorization: Bearer <access_token>
```

**Authentication:** Required

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page | Integer | 0 | Page number |
| size | Integer | 20 | Page size |
| sortBy | String | lastMessage | Sort by: lastMessage, name |

### Response

#### ✅ 200 OK
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Chat list fetched successfully",
  "data": [
    {
      "conversationId": "550e8400-e29b-41d4-a716-446655440100",
      "type": "ONE_ON_ONE",
      "participantName": "Jane Smith",
      "participantProfilePicture": "https://s3.amazonaws.com/...",
      "lastMessage": "See you tomorrow!",
      "lastMessageSenderId": "550e8400-e29b-41d4-a716-446655440050",
      "lastMessageTime": "2026-01-24T15:30:00Z",
      "unreadCount": 2,
      "isMuted": false,
      "isBlocked": false
    },
    {
      "conversationId": "550e8400-e29b-41d4-a716-446655440101",
      "type": "GROUP_CHAT",
      "groupName": "Manali Trip 2026",
      "participantCount": 8,
      "lastMessage": "Who's bringing the camping gear?",
      "lastMessageTime": "2026-01-24T14:20:00Z",
      "unreadCount": 5,
      "isMuted": false,
      "isBlocked": false
    }
  ]
}
```

#### ❌ 401 Unauthorized

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Chats fetched |
| 401 | Unauthorized |
| 500 | Server error |

---

## 3. POST /conversations/{conversationId}/send-message

**Purpose:** Send message to conversation

### Request

```http
POST /conversations/550e8400-e29b-41d4-a716-446655440100/send-message HTTP/1.1
Host: localhost:8089
Content-Type: application/json
Authorization: Bearer <access_token>
```

**Authentication:** Required

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| conversationId | UUID | Conversation ID |

**Request Body:**
```json
{
  "content": "This is a test message"
}
```

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| content | String | Yes | Max 5000 chars, non-empty | Message content |

### Response

#### ✅ 201 Created
```json
{
  "statusCode": 201,
  "status": "SUCCESS",
  "statusMessage": "Message sent successfully",
  "data": {
    "messageId": "550e8400-e29b-41d4-a716-446655440200",
    "conversationId": "550e8400-e29b-41d4-a716-446655440100",
    "senderId": "550e8400-e29b-41d4-a716-446655440001",
    "senderName": "John Doe",
    "content": "This is a test message",
    "createdAt": "2026-01-24T15:45:00Z",
    "read": true
  }
}
```

#### ❌ 400 Bad Request
**Scenarios:**
- Empty message
- Message too long

```json
{
  "statusCode": 400,
  "status": "ERROR",
  "statusMessage": "Message content cannot be empty",
  "data": null
}
```

#### ❌ 401 Unauthorized

#### ❌ 403 Forbidden
**Scenarios:**
- User is not a participant
- User has blocked by recipient (1:1)
- Conversation is blocked

```json
{
  "statusCode": 403,
  "status": "ERROR",
  "statusMessage": "You do not have permission to send messages in this conversation",
  "data": null
}
```

#### ❌ 404 Not Found
**Scenarios:**
- Conversation doesn't exist

```json
{
  "statusCode": 404,
  "status": "ERROR",
  "statusMessage": "Conversation not found",
  "data": null
}
```

#### ❌ 500 Internal Server Error

### Example Requests

**cURL:**
```bash
curl -X POST http://localhost:8089/conversations/550e8400-e29b-41d4-a716-446655440100/send-message \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <access_token>" \
  -d '{ "content": "This is a test message" }'
```

### Status Code Summary
| Code | Reason |
|------|--------|
| 201 | Message sent |
| 400 | Invalid input |
| 401 | Unauthorized |
| 403 | No permission |
| 404 | Conversation not found |
| 500 | Server error |

---

## 4. GET /conversations/{conversationId}/messages

**Purpose:** Get messages from conversation (with pagination)

### Request

```http
GET /conversations/550e8400-e29b-41d4-a716-446655440100/messages?before=2026-01-24T15:45:00Z&limit=30 HTTP/1.1
Host: localhost:8089
Authorization: Bearer <access_token>
```

**Authentication:** Required

**Query Parameters:**
| Parameter | Type | Default | Validation | Description |
|-----------|------|---------|------------|-------------|
| before | DateTime | Now | ISO-8601 | Get messages before this timestamp |
| limit | Integer | 30 | 1-100 | Number of messages to fetch |

### Response

#### ✅ 200 OK
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Messages fetched successfully",
  "data": [
    {
      "messageId": "550e8400-e29b-41d4-a716-446655440200",
      "conversationId": "550e8400-e29b-41d4-a716-446655440100",
      "senderId": "550e8400-e29b-41d4-a716-446655440001",
      "senderName": "John Doe",
      "senderProfilePicture": "https://s3.amazonaws.com/...",
      "content": "Hey, how are you?",
      "createdAt": "2026-01-24T15:30:00Z",
      "isDeleted": false
    },
    {
      "messageId": "550e8400-e29b-41d4-a716-446655440201",
      "conversationId": "550e8400-e29b-41d4-a716-446655440100",
      "senderId": "550e8400-e29b-41d4-a716-446655440050",
      "senderName": "Jane Smith",
      "senderProfilePicture": "https://s3.amazonaws.com/...",
      "content": "I'm doing great! You?",
      "createdAt": "2026-01-24T15:35:00Z",
      "isDeleted": false
    }
  ]
}
```

#### ❌ 400 Bad Request
**Scenarios:**
- Invalid date format
- limit > 100 or < 1

```json
{
  "statusCode": 400,
  "status": "ERROR",
  "statusMessage": "Invalid query parameters",
  "data": {
    "fieldErrors": {
      "limit": "Limit must be between 1 and 100"
    }
  }
}
```

#### ❌ 401 Unauthorized

#### ❌ 403 Forbidden
**Scenarios:**
- User is not a participant

#### ❌ 404 Not Found

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Messages fetched |
| 400 | Invalid pagination |
| 401 | Unauthorized |
| 403 | No permission |
| 404 | Conversation not found |
| 500 | Server error |

---

## 5. PATCH /conversations/{conversationId}/read

**Purpose:** Mark all messages in conversation as read

### Request

```http
PATCH /conversations/550e8400-e29b-41d4-a716-446655440100/read HTTP/1.1
Host: localhost:8089
Authorization: Bearer <access_token>
```

**Authentication:** Required

**Request Body:** Empty

### Response

#### ✅ 200 OK
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Conversation marked as read",
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
| 200 | Marked as read |
| 401 | Unauthorized |
| 403 | No permission |
| 404 | Conversation not found |
| 500 | Server error |

---

## 6. POST /conversations/{conversationId}/mute

**Purpose:** Mute conversation notifications

### Request

```http
POST /conversations/550e8400-e29b-41d4-a716-446655440100/mute HTTP/1.1
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
  "statusMessage": "Conversation muted successfully",
  "data": null
}
```

#### ❌ 401 Unauthorized

#### ❌ 403 Forbidden

#### ❌ 404 Not Found

#### ❌ 409 Conflict
**Scenarios:**
- Conversation already muted

```json
{
  "statusCode": 409,
  "status": "ERROR",
  "statusMessage": "Conversation is already muted",
  "data": null
}
```

#### ❌ 500 Internal Server Error

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Muted |
| 401 | Unauthorized |
| 403 | No permission |
| 404 | Conversation not found |
| 409 | Already muted |
| 500 | Server error |

---

## 7. POST /conversations/{conversationId}/unmute

**Purpose:** Unmute conversation notifications

### Request

```http
POST /conversations/550e8400-e29b-41d4-a716-446655440100/unmute HTTP/1.1
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
  "statusMessage": "Conversation unmuted successfully",
  "data": null
}
```

#### ❌ 409 Conflict
**Scenarios:**
- Conversation already unmuted

```json
{
  "statusCode": 409,
  "status": "ERROR",
  "statusMessage": "Conversation is already unmuted",
  "data": null
}
```

#### ❌ Other errors: Same as mute

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Unmuted |
| 401 | Unauthorized |
| 403 | No permission |
| 404 | Conversation not found |
| 409 | Already unmuted |
| 500 | Server error |

---

## 8. POST /conversations/{conversationId}/block

**Purpose:** Block conversation (for 1:1 only)

### Request

```http
POST /conversations/550e8400-e29b-41d4-a716-446655440100/block HTTP/1.1
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
  "statusMessage": "Conversation blocked successfully",
  "data": null
}
```

#### ❌ 400 Bad Request
**Scenarios:**
- Cannot block group conversations

```json
{
  "statusCode": 400,
  "status": "ERROR",
  "statusMessage": "Can only block 1:1 conversations",
  "data": null
}
```

#### ❌ 409 Conflict
**Scenarios:**
- Conversation already blocked

#### ❌ Other errors: Same as mute

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Blocked |
| 400 | Invalid conversation type |
| 401 | Unauthorized |
| 403 | No permission |
| 404 | Conversation not found |
| 409 | Already blocked |
| 500 | Server error |

---

## 9. POST /conversations/{conversationId}/unblock

**Purpose:** Unblock conversation

### Request

```http
POST /conversations/550e8400-e29b-41d4-a716-446655440100/unblock HTTP/1.1
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
  "statusMessage": "Conversation unblocked successfully",
  "data": null
}
```

#### ❌ 409 Conflict
**Scenarios:**
- Conversation already unblocked

#### ❌ Other errors: Same as block

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Unblocked |
| 400 | Invalid conversation type |
| 401 | Unauthorized |
| 403 | No permission |
| 404 | Conversation not found |
| 409 | Already unblocked |
| 500 | Server error |

---

## 10. WebSocket: /app/chat.send

**Purpose:** Send messages in real-time via WebSocket (STOMP)

**Type:** WebSocket (STOMP protocol)

### Connection

```javascript
// JavaScript using SockJS and Stomp
const socket = new SockJS('/ws-chat');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
  console.log('Connected: ' + frame.server);
  
  // Subscribe to conversation updates
  stompClient.subscribe('/user/queue/reply', function(message) {
    console.log('Received message: ' + message.body);
  });
});
```

### Send Message

```javascript
// Send message via WebSocket
stompClient.send("/app/chat.send", {}, JSON.stringify({
  conversationId: '550e8400-e29b-41d4-a716-446655440100',
  content: 'Real-time message'
}));
```

### Request Payload

```json
{
  "conversationId": "550e8400-e29b-41d4-a716-446655440100",
  "content": "Real-time message content"
}
```

### Response (Broadcast to all participants)

```json
{
  "messageId": "550e8400-e29b-41d4-a716-446655440200",
  "conversationId": "550e8400-e29b-41d4-a716-446655440100",
  "senderId": "550e8400-e29b-41d4-a716-446655440001",
  "senderName": "John Doe",
  "content": "Real-time message content",
  "timestamp": "2026-01-24T15:45:00Z",
  "status": "DELIVERED"
}
```

### Error Response

```json
{
  "error": true,
  "message": "Error message",
  "code": "ERROR_CODE"
}
```

### WebSocket Status Codes / States

| Status | Meaning |
|--------|---------|
| CONNECTING | WebSocket connecting |
| CONNECTED | Connected to server |
| SUBSCRIBED | Subscribed to message queue |
| MESSAGE_SENT | Message sent to server |
| MESSAGE_DELIVERED | Message delivered to recipients |
| ERROR | Connection/messaging error |
| DISCONNECTED | Connection closed |

### Common Error Scenarios

**❌ Invalid Conversation ID**
```json
{
  "error": true,
  "message": "Conversation not found",
  "code": "CONV_NOT_FOUND"
}
```

**❌ Not a Participant**
```json
{
  "error": true,
  "message": "You are not a participant",
  "code": "NOT_PARTICIPANT"
}
```

**❌ Blocked**
```json
{
  "error": true,
  "message": "Conversation is blocked",
  "code": "CONVERSATION_BLOCKED"
}
```

**❌ Empty Message**
```json
{
  "error": true,
  "message": "Message content cannot be empty",
  "code": "EMPTY_MESSAGE"
}
```

### Example WebSocket Implementation

**HTML:**
```html
<div id="chat">
  <div id="messages"></div>
  <input id="messageInput" type="text" />
  <button onclick="sendMessage()">Send</button>
</div>

<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
<script>
  let stompClient = null;
  const conversationId = '550e8400-e29b-41d4-a716-446655440100';

  function connect() {
    const socket = new SockJS('/ws-chat');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
      stompClient.subscribe('/user/queue/reply', function(msg) {
        displayMessage(JSON.parse(msg.body));
      });
    });
  }

  function sendMessage() {
    const content = document.getElementById('messageInput').value;
    stompClient.send("/app/chat.send", {}, JSON.stringify({
      conversationId: conversationId,
      content: content
    }));
    document.getElementById('messageInput').value = '';
  }

  function displayMessage(message) {
    const messagesDiv = document.getElementById('messages');
    messagesDiv.innerHTML += `<p><strong>${message.senderName}:</strong> ${message.content}</p>`;
  }

  window.onload = function() {
    connect();
  };
</script>
```

---

## Summary Table

| # | Endpoint | Method | Auth | Response | Status Codes |
|---|----------|--------|------|----------|--------------|
| 1 | /conversations/one-to-one | POST | Yes | ResponseDto | 200, 201, 400, 401, 404, 500 |
| 2 | /conversations/chat-list | GET | Yes | ResponseDto | 200, 401, 500 |
| 3 | /conversations/{id}/send-message | POST | Yes | ResponseDto | 201, 400, 401, 403, 404, 500 |
| 4 | /conversations/{id}/messages | GET | Yes | ResponseDto | 200, 400, 401, 403, 404, 500 |
| 5 | /conversations/{id}/read | PATCH | Yes | ResponseDto | 200, 401, 403, 404, 500 |
| 6 | /conversations/{id}/mute | POST | Yes | ResponseDto | 200, 401, 403, 404, 409, 500 |
| 7 | /conversations/{id}/unmute | POST | Yes | ResponseDto | 200, 401, 403, 404, 409, 500 |
| 8 | /conversations/{id}/block | POST | Yes | ResponseDto | 200, 400, 401, 403, 404, 409, 500 |
| 9 | /conversations/{id}/unblock | POST | Yes | ResponseDto | 200, 400, 401, 403, 404, 409, 500 |
| 10 | /app/chat.send | WebSocket | Yes | JSON | CONNECTED, ERROR, DELIVERED |

---

**Last Updated:** July 14, 2026

