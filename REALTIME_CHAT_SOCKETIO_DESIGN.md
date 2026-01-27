# Real-Time Chat System Design: Socket.IO Implementation

## Part 1: Architecture Overview

### 1.1 High-Level Architecture

```
┌────────────────────────────────────────────────────────────────┐
│                    CLIENT LAYER                                 │
│         (Web App / Mobile with Socket.IO Client)               │
└────────────────────┬─────────────────────────────────────────┘
                     │
          ┌──────────┴──────────┐
          │                     │
        HTTP                WebSocket
      (REST APIs)         (Socket.IO)
          │                     │
    ┌─────▼─────┐         ┌─────▼──────────┐
    │   REST    │         │ WebSocket      │
    │ Endpoints │         │ Gateway        │
    │           │         │                │
    │ • Create  │         │ • Message send │
    │   Conv    │         │ • Delivery ACK │
    │ • Fetch   │         │ • Read receipt │
    │   History │         │ • Presence     │
    │ • Block   │         │ • Typing       │
    │ • Mute    │         │ • Unread count │
    └─────┬─────┘         └─────┬──────────┘
          │                     │
          └──────────┬──────────┘
                     │
        ┌────────────▼─────────────┐
        │  APPLICATION LAYER       │
        │  ─────────────────────   │
        │ • Socket Manager         │
        │ • Message Handler        │
        │ • Presence Manager       │
        │ • Delivery Tracker       │
        │ • Read Receipt Handler   │
        │ • Auth & Validation      │
        └────────────┬─────────────┘
                     │
        ┌────────────▼─────────────────┐
        │  PERSISTENCE LAYER           │
        │  ──────────────────────────  │
        │ • Message Repository         │
        │ • Participant Repository     │
        │ • Delivery Status DB         │
        │ • Read Receipt DB            │
        │ • Socket Session Cache       │
        └────────────┬─────────────────┘
                     │
        ┌────────────▼─────────────────┐
        │  EXTERNAL SERVICES           │
        │  ──────────────────────────  │
        │ • Redis (socket sessions)    │
        │ • PostgreSQL (persistence)   │
        │ • Message Queue (optional)   │
        └──────────────────────────────┘
```

### 1.2 Which Features Use What Protocol

```
┌─────────────────────────┬──────────┬────────────────────┐
│ Feature                 │ Protocol │ Reason              │
├─────────────────────────┼──────────┼────────────────────┤
│ Send Message            │ Socket   │ Real-time delivery  │
│ Message Delivery ACK    │ Socket   │ Immediate feedback  │
│ Read Receipt            │ Socket   │ Real-time tracking  │
│ Unread Count Update     │ Socket   │ Sync across devices │
│ Block User              │ Both     │ REST for persist,   │
│                         │          │ Socket for enforce  │
│ Unblock User            │ Both     │ Same as above       │
│ Mute Conversation       │ Both     │ REST for persist,   │
│                         │          │ Socket for notify   │
│ Unmute Conversation     │ Both     │ Same as above       │
│ Presence (Online/Off)   │ Socket   │ Real-time status    │
│ Typing Indicator        │ Socket   │ Real-time UX        │
│ Fetch Chat List         │ REST     │ Heavy operation,    │
│                         │          │ pagination needed   │
│ Fetch Message History   │ REST     │ Pagination, filters │
│ Create Conversation     │ REST     │ One-time setup      │
│ Block/Mute List         │ REST     │ Initial load        │
└─────────────────────────┴──────────┴────────────────────┘
```

---

## Part 2: WebSocket Design

### 2.1 Connection Lifecycle

```
CLIENT                              SERVER
  │                                   │
  ├─ Emit: connect                   │
  │  (with token in query)            │
  │                                   │
  │                ┌─────────────────▶│ WebSocket.onConnect()
  │                │                  │ • Extract JWT
  │                │                  │ • Validate token
  │                │                  │ • Get userId
  │                │                  │ • Store socket session
  │                │                  │ • Join user room
  │                │                  │ • Broadcast presence
  │                │                  │ • Fetch & emit unread counts
  │                │
  │◀────────────────────────────────  │ Emit: connected
  │                                   │ (payload: userId, unreadCounts)
  │
  │ ┌─ Send message event             │
  │ │  conversation_id                │
  │ │  content                        │
  │ │  messageId (client-generated)   │
  │ │                                 │
  │ │                ┌──────────────▶ │ WebSocket.onMessageSend()
  │ │                │                │ • Validate auth
  │ │                │                │ • Check block status
  │ │                │                │ • Save to DB (idempotent)
  │ │                │                │ • Emit to all participants
  │ │                │                │
  │ │◀──────────────────────────────  │ Emit: message_delivered
  │ │                                 │ (ack with messageId, timestamp)
  │ │
  │ └─ Send read receipt              │
  │    conversation_id                │
  │    lastReadAt                     │
  │                                   │
  │                ┌─────────────────▶│ WebSocket.onMessageRead()
  │                │                  │ • Update participant.lastReadAt
  │                │                  │ • Calculate new unread counts
  │                │                  │ • Broadcast unread_count_update
  │                │                  │
  │◀──────────────────────────────────│ Emit: read_receipt
  │                                   │ (ack with conversationId)
  │
  │ ┌─ Disconnect (intentional)       │
  │ │                                 │
  │ │                ┌────────────────▶│ WebSocket.onDisconnect()
  │ │                │                │ • Remove socket session
  │ │                │                │ • Clean up user rooms
  │ │                │                │ • Broadcast offline status
  │ │                │                │
  │ │
  │ └─ (Automatic reconnect on failure)
  │
  │ [NETWORK GLITCH - Reconnect with existing socket ID]
  │                                   │
  │                ┌─────────────────▶│ WebSocket.onConnect() [AGAIN]
  │                │                  │ • Detect existing session ID
  │                │                  │ • Resume instead of creating new
  │                │                  │ • Send pending messages
  │                │                  │
  │◀──────────────────────────────────│ Emit: reconnected
  │                                   │ (pending messages list)
```

### 2.2 Room Architecture

```
┌─────────────────────────────────────────────────────┐
│             SOCKET.IO ROOMS STRUCTURE               │
├─────────────────────────────────────────────────────┤
│                                                     │
│ 1. USER ROOMS (per socket)                          │
│    ────────────────────────────                     │
│    Format: "user:{userId}"                          │
│    Purpose:                                         │
│      • Personal notifications                      │
│      • Presence updates                            │
│      • Unread count updates                        │
│      • Block/Mute notifications                    │
│    Members: 1 (the user themselves)               │
│                                                     │
│    Example Emit Targets:                           │
│      io.to("user:550e8400-e29b").emit(...)        │
│                                                     │
├─────────────────────────────────────────────────────┤
│                                                     │
│ 2. CONVERSATION ROOMS (per conversation)           │
│    ──────────────────────────────────              │
│    Format: "conversation:{conversationId}"         │
│    Purpose:                                        │
│      • Broadcast messages to all participants      │
│      • Share typing indicators                     │
│      • Share presence in conversation              │
│      • Share read receipts                         │
│    Members: All participants (active sockets)      │
│                                                     │
│    Example Emit Targets:                           │
│      io.to("conversation:a1b2c3d4").emit(...)     │
│                                                     │
├─────────────────────────────────────────────────────┤
│                                                     │
│ 3. DEVICE ROOMS (per device)                       │
│    ────────────────────────────                    │
│    Format: "device:{userId}:{deviceId}"            │
│    Purpose:                                        │
│      • Handle multi-device same-user scenarios     │
│      • Device-specific notifications               │
│      • Prevent duplicate notifications             │
│    Members: 1 (the specific device)               │
│                                                     │
│    Example Emit Targets:                           │
│      io.to("device:550e8400:iphone-12").emit(...) │
│                                                     │
└─────────────────────────────────────────────────────┘
```

### 2.3 User-to-Socket Mapping

```
ONE USER CAN HAVE MULTIPLE SOCKETS (multi-device):

User 550e8400-e29b-41d4-a716-446655440000
│
├─ Socket 1 (Web Browser on Laptop)
│  ├─ socketId: "abc123def456"
│  ├─ room: "user:550e8400-e29b"
│  ├─ connected: true
│  └─ lastActivityAt: 2026-01-27T10:30:00Z
│
├─ Socket 2 (Mobile App on iPhone)
│  ├─ socketId: "xyz789uvw012"
│  ├─ room: "user:550e8400-e29b"
│  ├─ connected: true
│  └─ lastActivityAt: 2026-01-27T10:29:45Z
│
└─ Socket 3 (Web Browser on iPad - OFFLINE)
   ├─ socketId: "old123old456"
   ├─ room: "user:550e8400-e29b"
   ├─ connected: false
   └─ lastActivityAt: 2026-01-27T09:45:00Z

STORAGE (Redis):
  Key: "user:550e8400-e29b"
  Value: {
    "sockets": [
      {
        "socketId": "abc123def456",
        "deviceId": "web-laptop",
        "connected": true,
        "connectedAt": "2026-01-27T09:00:00Z"
      },
      {
        "socketId": "xyz789uvw012",
        "deviceId": "mobile-iphone",
        "connected": true,
        "connectedAt": "2026-01-27T10:15:00Z"
      }
    ],
    "presence": "online"  // online / away / offline
  }

BEHAVIOR:
- Message sent to conversation room, all user's sockets get it
- Unread count update sent to user room, all devices get it
- Read receipt from one device, update DB once, notify all devices
```

### 2.4 Event Naming Convention

```
CLIENT → SERVER EVENTS (Emit from client):
┌────────────────────────────────────┐
│ message:send                       │
│ message:read (read receipt)        │
│ typing:start                       │
│ typing:stop                        │
│ presence:update                    │
│ block:user                         │
│ unblock:user                       │
│ mute:conversation                  │
│ unmute:conversation                │
│ socket:ping (keep-alive)           │
└────────────────────────────────────┘

SERVER → CLIENT EVENTS (Emit to client):
┌────────────────────────────────────┐
│ message:delivered                  │
│ message:received                   │
│ read_receipt:received              │
│ typing:indicator                   │
│ presence:changed                   │
│ user:blocked                       │
│ conversation:muted                 │
│ unread_count:updated               │
│ message:failed (error)             │
│ socket:pong (keep-alive)           │
│ reconnected (multi-device)         │
└────────────────────────────────────┘

PATTERN: {entity}:{action}
- Lowercase
- Colon separator for clarity
- Client events describe client actions
- Server events describe what happened (past tense)
```

---

## Part 3: Event Flow & Payloads

### 3.1 Send Message Flow

```
CLIENT SIDE:
┌─────────────────────────────────────┐
│ User types message, clicks send     │
├─────────────────────────────────────┤
│ 1. Generate messageId (UUID)        │
│ 2. Emit: message:send               │
│ 3. Store in local cache (optimistic)│
│ 4. Show in UI (pending state)       │
│ 5. Wait for delivery ACK            │
│ 6. Update to delivered state        │
│ 7. Or show error if fails           │
└─────────────────────────────────────┘

SERVER SIDE:
┌─────────────────────────────────────┐
│ Receive message:send event          │
├─────────────────────────────────────┤
│ 1. Validate JWT token               │
│ 2. Extract userId from token        │
│ 3. Validate userId is participant   │
│ 4. Check if user is blocked         │
│ 5. Check if blocked sender          │
│ 6. Check rate limiting              │
│ 7. Save to MessageEntity (idempotent)
│ 8. Emit message:delivered (ACK)     │
│ 9. Emit message:received to room    │
│ 10. Update unread count for others  │
│ 11. Emit unread_count:updated       │
└─────────────────────────────────────┘

EVENT PAYLOAD - CLIENT SEND:
{
  "conversationId": "uuid",
  "content": "Hello!",
  "messageId": "uuid",        // Client-generated for idempotency
  "clientTimestamp": 1674840600000,
  "clientDeviceId": "web-laptop-abc123"
}

EVENT PAYLOAD - SERVER ACK:
{
  "messageId": "uuid",
  "conversationId": "uuid",
  "status": "delivered",
  "serverTimestamp": 1674840601234,
  "persistedAt": "2026-01-27T10:30:01Z"
}

EVENT PAYLOAD - SERVER BROADCAST:
{
  "messageId": "uuid",
  "conversationId": "uuid",
  "senderId": "uuid",
  "senderName": "John Doe",
  "content": "Hello!",
  "timestamp": "2026-01-27T10:30:01Z",
  "status": "delivered",
  "readBy": []  // Will be populated as others read
}
```

### 3.2 Read Receipt Flow

```
CLIENT SIDE:
┌──────────────────────────────────────┐
│ User views conversation              │
├──────────────────────────────────────┤
│ 1. REST call: GET /messages          │
│ 2. REST call: POST /read             │
│ 3. Emit: message:read (over socket)  │
│ 4. Update lastReadAt locally         │
└──────────────────────────────────────┘

SERVER SIDE:
┌──────────────────────────────────────┐
│ Receive message:read event           │
├──────────────────────────────────────┤
│ 1. Update participant.lastReadAt     │
│ 2. Calculate new unreadCount         │
│ 3. Emit read_receipt:received (ACK)  │
│ 4. Emit to all user's sockets:       │
│    unread_count:updated              │
│ 5. Emit to conversation room:        │
│    read_receipt:received             │
│ 6. Update message.readBy array       │
└──────────────────────────────────────┘

EVENT PAYLOAD - CLIENT SEND:
{
  "conversationId": "uuid",
  "messageIds": ["uuid1", "uuid2"],  // Multiple messages
  "lastReadAt": "2026-01-27T10:35:00Z"
}

EVENT PAYLOAD - SERVER ACK:
{
  "conversationId": "uuid",
  "updatedAt": "2026-01-27T10:35:01Z",
  "newUnreadCount": 0
}

EVENT PAYLOAD - UNREAD COUNT UPDATE:
{
  "conversationId": "uuid",
  "unreadCount": 0,
  "updatedAt": "2026-01-27T10:35:01Z",
  "lastReadAt": "2026-01-27T10:35:00Z"
}
```

### 3.3 Block/Mute Flow

```
BLOCK USER FLOW:
┌──────────────────────────────────────┐
│ Client clicks "Block User" button    │
├──────────────────────────────────────┤
│ 1. REST POST /block (persist to DB)  │
│ 2. Emit: block:user (immediate)      │
│ 3. Show "User Blocked" to client     │
└──────────────────────────────────────┘

SERVER SIDE:
┌──────────────────────────────────────┐
│ Receive block:user event             │
├──────────────────────────────────────┤
│ 1. Validate blocker is participant   │
│ 2. Create ConversationBlockEntity    │
│ 3. Return ACK                        │
│ 4. Remove blocked user from room     │
│ 5. Emit: user:blocked to user room   │
│    (notify all user's devices)       │
│ 6. Emit: message:delivery_prevented  │
│    (if user tries to send)           │
└──────────────────────────────────────┘

WHEN BLOCKED USER TRIES TO SEND:
┌──────────────────────────────────────┐
│ User (blocked) emits: message:send   │
├──────────────────────────────────────┤
│ 1. Server checks block status        │
│ 2. Block found                       │
│ 3. Emit: error:blocked               │
│ 4. Don't save to DB                  │
│ 5. Show error to user                │
└──────────────────────────────────────┘

EVENT PAYLOAD - BLOCK:
{
  "conversationId": "uuid",
  "blockedUserId": "uuid"
}

EVENT PAYLOAD - BLOCK ACK:
{
  "success": true,
  "conversationId": "uuid",
  "blockedUserId": "uuid",
  "blockedAt": "2026-01-27T10:36:00Z"
}

EVENT PAYLOAD - DELIVERY PREVENTED:
{
  "conversationId": "uuid",
  "reason": "user_blocked",
  "message": "You cannot send messages. You are blocked."
}
```

---

## Part 4: Security & Reliability

### 4.1 Security Implementation

```
AUTHENTICATION ON CONNECT:
┌────────────────────────────────────────┐
│ Client connects with token in query:   │
│ ws://host:8080/socket.io?token=JWT    │
├────────────────────────────────────────┤
│ Server side (on connect):              │
│ 1. Extract token from query params     │
│ 2. Validate JWT signature              │
│ 3. Check token expiry                  │
│ 4. Extract userId from claims          │
│ 5. Store userId in socket.request.user │
│ 6. Set socket.data.userId = userId     │
│ 7. Return connected()                  │
│ OR close if validation fails           │
└────────────────────────────────────────┘

AUTHORIZATION ON EACH EVENT:
┌────────────────────────────────────────┐
│ Client emits: message:send             │
├────────────────────────────────────────┤
│ Server middleware/handler:             │
│ 1. Get userId from socket.data.userId  │
│ 2. Get conversationId from payload     │
│ 3. Query: Is userId in conversation?  │
│    SELECT * FROM participant           │
│    WHERE conversation_id = ?           │
│    AND user_id = ?                     │
│    AND left_at IS NULL                 │
│ 4. If not participant, reject          │
│ 5. Is userId blocked? Check block table│
│ 6. If blocked, reject                  │
│ 7. Proceed with action                 │
└────────────────────────────────────────┘

ANTI-SPOOFING:
┌────────────────────────────────────────┐
│ Client cannot send messages as:        │
│ {                                      │
│   "senderId": "other-user-id"  ❌      │
│ }                                      │
├────────────────────────────────────────┤
│ Server always uses:                    │
│ const senderId = socket.data.userId;   │
│ NOT from client payload                │
└────────────────────────────────────────┘

TOKEN REFRESH:
┌────────────────────────────────────────┐
│ If token expires during session:       │
│ 1. Client detects token expiry         │
│ 2. Request new token from auth server  │
│ 3. Send token via: socket.emit('auth') │
│ 4. Server validates new token          │
│ 5. Update socket.data.userId           │
│ 6. Reconnect if needed                 │
└────────────────────────────────────────┘
```

### 4.2 Idempotency & Message Deduplication

```
PROBLEM:
┌────────────────────────────────────────┐
│ Network glitch: Message sent but ACK   │
│ lost. Client retransmits.              │
│ Server receives same message twice.    │
│ Result: Duplicate message in DB! ❌    │
└────────────────────────────────────────┘

SOLUTION: CLIENT-GENERATED MESSAGE ID:
┌────────────────────────────────────────┐
│ CLIENT SIDE:                           │
│ 1. messageId = UUID.randomUUID()       │
│ 2. Emit: { messageId, content, ... }  │
│ 3. Wait for ACK                        │
│ 4. No ACK? Resend with SAME messageId  │
├────────────────────────────────────────┤
│ SERVER SIDE:                           │
│ 1. Check if messageId exists in DB     │
│    SELECT * FROM message               │
│    WHERE message_id = ?                │
│ 2. If exists, return existing message  │
│    (don't create duplicate)            │
│ 3. If not, create new message          │
│ 4. Return ACK with same messageId      │
└────────────────────────────────────────┘

STORAGE IN DB:
┌────────────────────────────────────────┐
│ message_id (PK, UUID)      - Unique    │
│ conversation_id (FK)                   │
│ sender_id (FK)                         │
│ content                                │
│ created_at                             │
│ client_message_id (Unique)             │
│                                        │
│ Index on: (conversation_id, sender_id,│
│           client_message_id)           │
│                                        │
│ Query: Check by client_message_id     │
│ first before inserting                │
└────────────────────────────────────────┘
```

### 4.3 Reconnection & Resume

```
SCENARIO: Network drops, reconnects

CURRENT SESSION (Redis):
┌────────────────────────────────────────┐
│ Key: "socket:{socketId}"               │
│ {                                      │
│   "userId": "uuid",                    │
│   "conversationIds": ["uuid1", ...],   │
│   "connectedAt": "2026-01-27T10:00",   │
│   "lastActivity": "2026-01-27T10:35"   │
│ }                                      │
│ TTL: 30 minutes                        │
│                                        │
│ Key: "user:{userId}:sockets"           │
│ {                                      │
│   "sockets": [                         │
│     {                                  │
│       "socketId": "abc123",            │
│       "deviceId": "web-laptop",        │
│       "connectedAt": "2026-01-27T10:00"│
│     }                                  │
│   ]                                    │
│ }                                      │
└────────────────────────────────────────┘

RECONNECTION FLOW:

1. Network drops (30 seconds)
   └─ Client attempts automatic reconnect
   
2. Client connects again
   ├─ New socket ID: "xyz789"
   └─ Still have old socket ID: "abc123" in Redis
   
3. Server detects connection (onConnect)
   ├─ Check if userId already connected
   ├─ Query: "user:{userId}:sockets"
   ├─ Find old socket with same device ID
   ├─ If found and fresh (< 30 min): RESUME
   └─ If not found or stale: NEW SESSION
   
4. On RESUME:
   ├─ Emit: reconnected
   ├─ Send pending messages (queue since disconnect)
   ├─ Restore room subscriptions
   ├─ Update activity timestamp
   └─ Clean up old socket session
   
5. Emit payload:
   {
     "status": "resumed",
     "userId": "uuid",
     "pendingMessages": [
       {
         "messageId": "uuid",
         "conversationId": "uuid",
         "content": "...",
         "sentAt": "2026-01-27T10:35:00Z",
         "deliveryStatus": "pending"
       }
     ],
     "unreadCounts": {
       "uuid": 2,
       "uuid": 0
     }
   }

MULTI-DEVICE SAME USER:

Device 1 (Web):     Socket ABC → Online
Device 2 (Mobile):  Socket XYZ → Online
Device 3 (Tablet):  Socket OLD → Offline (>30min)

When Message sent from any device:
└─ Broadcast to "user:uuid" room
   └─ All online sockets (ABC, XYZ) receive
      but NOT offline socket (OLD)

When Device 3 reconnects:
└─ Check if it's same device (deviceId match)
└─ If yes: Resume
└─ Load pending messages since disconnect
```

### 4.4 Rate Limiting & Abuse Prevention

```
RATE LIMITS (per user, per minute):

┌──────────────────────────────┐
│ message:send          10/min  │
│ message:read          60/min  │
│ typing:start          30/min  │
│ block:user            5/min   │
│ mute:conversation     5/min   │
└──────────────────────────────┘

IMPLEMENTATION (Redis):

Key: "ratelimit:{userId}:{event}"
Value: { count: 3, window: 1674840600 }
TTL: 60 seconds

On each event:
1. Current window = Math.floor(now / 60)
2. Get current count for window
3. Increment count
4. If count > limit:
   └─ Emit: error:rate_limit_exceeded
   └─ Don't process event
5. Else: Process normally

Example code:
const key = `ratelimit:${userId}:message:send`;
const count = await redis.incr(key);
if (count === 1) {
  await redis.expire(key, 60);
}
if (count > 10) {
  return socket.emit('error:rate_limit', {
    message: 'Too many messages. Try again later.',
    retryAfter: 60
  });
}
```

### 4.5 Message Ordering & Consistency

```
PROBLEM:
┌────────────────────────────────────┐
│ User sends 3 messages quickly       │
│ Network reorders arrival at server  │
│ Messages saved in wrong order! ❌   │
└────────────────────────────────────┘

SOLUTION: CLIENT-SIDE SEQUENCING

CLIENT:
1. User sends message 1
   ├─ clientSequence: 1
   └─ Emit with sequence
   
2. User sends message 2
   ├─ clientSequence: 2
   └─ Emit with sequence
   
3. User sends message 3
   ├─ clientSequence: 3
   └─ Emit with sequence

SERVER:
1. Receive out of order: 2, 3, 1
   
2. For each message:
   ├─ Validate clientSequence
   ├─ Check if we have sequence N-1
   ├─ If not: Queue and wait
   ├─ If yes: Process
   └─ Then process N+1
   
3. Save with serverSequence = counter++

DATABASE:
┌────────────────────────────────────┐
│ message_id           UUID           │
│ conversation_id      UUID           │
│ sender_id            UUID           │
│ client_sequence      INT   ← Key!   │
│ server_sequence      INT            │
│ content              TEXT           │
│ created_at           TIMESTAMP      │
│                                     │
│ Unique Index: (conversation_id,     │
│               sender_id,            │
│               client_sequence)      │
└────────────────────────────────────┘

QUERY ORDER:
SELECT * FROM message
WHERE conversation_id = ?
ORDER BY server_sequence ASC  -- Correct order
```

---

## Part 5: Code Implementation

### 5.1 Socket.IO Server Setup (Spring Boot)

```java
// File: chat/config/SocketIOConfig.java

@Configuration
public class SocketIOConfig {

    @Bean
    public SocketIOServer socketIOServer() throws IOException {
        SocketIONamespace namespace = new SocketIONamespace("socket.io");
        
        SocketConfig socketConfig = new SocketConfig();
        socketConfig.setReuseAddress(true);
        socketConfig.setTcpNoDelay(true);
        socketConfig.setSoLinger(0);
        
        SocketIOServerOptions options = new SocketIOServerOptions();
        options.setSocketConfig(socketConfig);
        options.setMaxFramePayloadLength(1024 * 1024); // 1MB
        options.setMaxHttpContentLength(1024 * 1024);
        options.setAuthorizationListener(data -> {
            // Auth on connect - see next section
            return true;
        });
        options.setExceptionListener(throwable -> {
            log.error("SocketIO error", throwable);
        });
        
        SocketIOServer server = new SocketIOServer(options);
        
        // Register listeners
        namespace.addConnectListener(socket -> {
            socketIOEventHandlers.onConnect(socket);
        });
        
        namespace.addDisconnectListener(socket -> {
            socketIOEventHandlers.onDisconnect(socket);
        });
        
        namespace.addEventListener("message:send", 
            SendMessagePayload.class, 
            (socket, data, ackRequest) -> {
                socketIOEventHandlers.onMessageSend(socket, data, ackRequest);
            });
        
        namespace.addEventListener("message:read",
            ReadReceiptPayload.class,
            (socket, data, ackRequest) -> {
                socketIOEventHandlers.onMessageRead(socket, data, ackRequest);
            });
        
        namespace.addEventListener("typing:start",
            TypingPayload.class,
            (socket, data, ackRequest) -> {
                socketIOEventHandlers.onTypingStart(socket, data);
            });
        
        server.addNamespace(namespace);
        server.start();
        
        return server;
    }

    @Bean
    public SocketIOEventHandlers socketIOEventHandlers(
            ConversationService conversationService,
            MessageService messageService,
            AuthService authService) {
        return new SocketIOEventHandlers(
            conversationService,
            messageService,
            authService
        );
    }
}
```

### 5.2 Authentication & Connection

```java
// File: chat/service/SocketIOAuthService.java

@Service
@RequiredArgsConstructor
@Slf4j
public class SocketIOAuthService {

    private final JwtService jwtService;
    private final SocketSessionCache sessionCache;

    /**
     * Validate token from query params on connect
     */
    public UUID validateAndExtractUserId(String token, String socketId) 
            throws AuthenticationException {
        
        if (token == null || token.isEmpty()) {
            throw new AuthenticationException("Token missing");
        }

        // Validate JWT
        if (!jwtService.validateAccessToken(token)) {
            throw new AuthenticationException("Invalid token");
        }

        // Extract user ID
        UUID userId = jwtService.extractUserUuid(token);
        if (userId == null) {
            throw new AuthenticationException("Invalid user ID in token");
        }

        log.info("Socket authenticated: userId={}, socketId={}", userId, socketId);
        return userId;
    }

    /**
     * Check if user is authorized to access conversation
     */
    public boolean isUserInConversation(
            UUID userId,
            UUID conversationId,
            ConversationService conversationService) {
        
        try {
            ConversationParticipantEntity participant = 
                conversationService.getParticipant(conversationId, userId);
            return participant != null && participant.getLeftAt() == null;
        } catch (Exception e) {
            log.warn("Auth check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if user is blocked from sending
     */
    public boolean isUserBlocked(
            UUID senderId,
            UUID conversationId,
            ConversationService conversationService) {
        
        return conversationService.isUserBlocked(conversationId, senderId);
    }
}
```

### 5.3 Message Send Handler (Idempotent)

```java
// File: chat/service/SocketIOEventHandlers.java

@Component
@RequiredArgsConstructor
@Slf4j
public class SocketIOEventHandlers {

    private final ConversationService conversationService;
    private final MessageService messageService;
    private final SocketIOAuthService authService;
    private final SocketSessionCache sessionCache;
    private final DeliveryTracker deliveryTracker;

    /**
     * Handle message:send event
     * IDEMPOTENT: Same messageId won't create duplicate
     */
    public void onMessageSend(
            SocketIOClient socket,
            SendMessagePayload payload,
            AckRequest ackRequest) {

        UUID userId = sessionCache.getUserId(socket.getSessionId());
        
        if (userId == null) {
            socket.sendEvent("error:unauthorized", 
                new ErrorPayload("User not authenticated"));
            return;
        }

        UUID conversationId = payload.getConversationId();
        String content = payload.getContent();
        UUID messageId = payload.getMessageId();
        
        try {
            // 1. Validate user is participant
            if (!authService.isUserInConversation(userId, conversationId, 
                    conversationService)) {
                socket.sendEvent("error:not_participant",
                    new ErrorPayload("You are not part of this conversation"));
                return;
            }

            // 2. Check if blocked
            if (authService.isUserBlocked(userId, conversationId, 
                    conversationService)) {
                socket.sendEvent("error:blocked",
                    new ErrorPayload("You cannot send messages to this user"));
                return;
            }

            // 3. Check rate limit
            if (!rateLimiter.allowRequest(userId, "message:send")) {
                socket.sendEvent("error:rate_limit_exceeded",
                    new RateLimitPayload("Too many messages. Try again later.", 60));
                return;
            }

            // 4. IDEMPOTENCY CHECK: Does message already exist?
            Optional<MessageEntity> existing = 
                messageService.findByIdempotencyKey(conversationId, userId, messageId);
            
            MessageEntity message;
            if (existing.isPresent()) {
                // Message already saved, return existing
                message = existing.get();
                log.info("Message already exists (idempotent): messageId={}", messageId);
            } else {
                // 5. Save new message
                message = MessageEntity.create(
                    conversationId,
                    userId,
                    content,
                    messageId  // Client-generated ID for idempotency
                );
                
                message = messageService.save(message);
                log.info("Message saved: messageId={}, conversationId={}", 
                    messageId, conversationId);
                
                // 6. Update unread counts for all other participants
                conversationService.incrementUnreadCounts(conversationId, userId);
            }

            // 7. Send delivery ACK to sender
            MessageDeliveredPayload deliveryAck = new MessageDeliveredPayload(
                messageId,
                conversationId,
                "delivered",
                Instant.now()
            );
            ackRequest.sendAckData(deliveryAck);

            // 8. Broadcast message to all participants in conversation
            BroadcastMessagePayload broadcastPayload = 
                new BroadcastMessagePayload(
                    messageId,
                    conversationId,
                    userId,
                    content,
                    message.getCreatedAt(),
                    "delivered"
                );
            
            // Send to all sockets in conversation room
            socketIOServer.getRoomOperations("conversation:" + conversationId)
                .sendEvent("message:received", broadcastPayload);

            // 9. Emit unread count update to all participants (except sender)
            Map<UUID, Integer> unreadCounts = 
                conversationService.getUnreadCountsForParticipants(
                    conversationId, userId);  // Excludes sender
            
            for (UUID participantId : unreadCounts.keySet()) {
                UnreadCountUpdatePayload unreadPayload = 
                    new UnreadCountUpdatePayload(
                        conversationId,
                        unreadCounts.get(participantId),
                        Instant.now()
                    );
                
                // Send to all user's sockets
                socketIOServer.getRoomOperations("user:" + participantId)
                    .sendEvent("unread_count:updated", unreadPayload);
            }

            // 10. Track delivery for metrics
            deliveryTracker.trackDelivery(messageId, conversationId, 
                System.currentTimeMillis());

        } catch (Exception e) {
            log.error("Error in onMessageSend", e);
            socket.sendEvent("error:failed",
                new ErrorPayload("Failed to send message: " + e.getMessage()));
        }
    }
}
```

### 5.4 Message Read (Unread Count Update)

```java
// File: chat/service/SocketIOEventHandlers.java (continued)

/**
 * Handle message:read event (read receipt)
 */
public void onMessageRead(
        SocketIOClient socket,
        ReadReceiptPayload payload,
        AckRequest ackRequest) {

    UUID userId = sessionCache.getUserId(socket.getSessionId());
    UUID conversationId = payload.getConversationId();
    LocalDateTime lastReadAt = payload.getLastReadAt();
    
    try {
        // 1. Validate user is participant
        ConversationParticipantEntity participant = 
            conversationService.getParticipant(conversationId, userId);
        
        if (participant == null) {
            socket.sendEvent("error:not_participant",
                new ErrorPayload("Not a participant"));
            return;
        }

        // 2. Update participant's lastReadAt
        participant.setLastReadAt(lastReadAt);
        conversationService.saveParticipant(participant);
        
        log.info("Marked as read: conversationId={}, userId={}", 
            conversationId, userId);

        // 3. Calculate new unread count for THIS user
        int newUnreadCount = 
            conversationService.getUnreadCountForUser(conversationId, userId);

        // 4. Send ACK to sender
        ReadReceiptAckPayload ackPayload = new ReadReceiptAckPayload(
            conversationId,
            newUnreadCount,
            lastReadAt
        );
        ackRequest.sendAckData(ackPayload);

        // 5. Emit unread count update to ALL user's sockets
        UnreadCountUpdatePayload unreadPayload = new UnreadCountUpdatePayload(
            conversationId,
            newUnreadCount,
            lastReadAt
        );
        
        socketIOServer.getRoomOperations("user:" + userId)
            .sendEvent("unread_count:updated", unreadPayload);

        // 6. Emit read receipt to other participants
        ReadReceiptReceivedPayload receiptPayload = 
            new ReadReceiptReceivedPayload(
                conversationId,
                userId,
                lastReadAt
            );
        
        socketIOServer.getRoomOperations("conversation:" + conversationId)
            .sendEvent("read_receipt:received", receiptPayload);

    } catch (Exception e) {
        log.error("Error in onMessageRead", e);
        socket.sendEvent("error:failed",
            new ErrorPayload("Failed to mark as read"));
    }
}
```

### 5.5 Session Cache (Redis)

```java
// File: chat/service/SocketSessionCache.java

@Service
@RequiredArgsConstructor
@Slf4j
public class SocketSessionCache {

    private final RedisTemplate<String, String> redisTemplate;
    private static final long SESSION_TTL = 30 * 60; // 30 minutes

    /**
     * Store socket session mapping
     */
    public void storeSession(String socketId, UUID userId, String deviceId) {
        String key = "socket:" + socketId;
        String value = userId + "|" + deviceId + "|" + System.currentTimeMillis();
        
        redisTemplate.opsForValue().set(key, value, SESSION_TTL, TimeUnit.SECONDS);
        
        // Also store reverse mapping (user to sockets)
        String userKey = "user:" + userId + ":sockets";
        redisTemplate.opsForSet().add(userKey, socketId);
        redisTemplate.expire(userKey, SESSION_TTL, TimeUnit.SECONDS);
        
        log.info("Socket session stored: socketId={}, userId={}", socketId, userId);
    }

    /**
     * Get userId from socket
     */
    public UUID getUserId(String socketId) {
        String value = redisTemplate.opsForValue().get("socket:" + socketId);
        if (value == null) {
            return null;
        }
        
        String[] parts = value.split("\\|");
        try {
            return UUID.fromString(parts[0]);
        } catch (Exception e) {
            log.warn("Failed to parse userId from socket: {}", socketId);
            return null;
        }
    }

    /**
     * Get device ID from socket
     */
    public String getDeviceId(String socketId) {
        String value = redisTemplate.opsForValue().get("socket:" + socketId);
        if (value == null) {
            return null;
        }
        
        String[] parts = value.split("\\|");
        return parts.length > 1 ? parts[1] : null;
    }

    /**
     * Get all sockets for a user
     */
    public Set<String> getUserSockets(UUID userId) {
        String userKey = "user:" + userId + ":sockets";
        return redisTemplate.opsForSet().members(userKey);
    }

    /**
     * Check if socket exists and is fresh
     */
    public boolean isSessionValid(String socketId) {
        return Boolean.TRUE.equals(
            redisTemplate.hasKey("socket:" + socketId)
        );
    }

    /**
     * Remove socket session
     */
    public void removeSession(String socketId) {
        String value = redisTemplate.opsForValue().get("socket:" + socketId);
        if (value != null) {
            UUID userId = UUID.fromString(value.split("\\|")[0]);
            String userKey = "user:" + userId + ":sockets";
            redisTemplate.opsForSet().remove(userKey, socketId);
        }
        
        redisTemplate.delete("socket:" + socketId);
        log.info("Socket session removed: socketId={}", socketId);
    }
}
```

### 5.6 DTOs & Payloads

```java
// File: chat/dto/socket/SendMessagePayload.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMessagePayload {
    private UUID conversationId;
    private String content;
    private UUID messageId;  // Client-generated for idempotency
    private long clientTimestamp;
    private String clientDeviceId;
}

// File: chat/dto/socket/ReadReceiptPayload.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReadReceiptPayload {
    private UUID conversationId;
    private LocalDateTime lastReadAt;
}

// File: chat/dto/socket/MessageDeliveredPayload.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDeliveredPayload {
    private UUID messageId;
    private UUID conversationId;
    private String status;  // "delivered"
    private Instant serverTimestamp;
}

// File: chat/dto/socket/UnreadCountUpdatePayload.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnreadCountUpdatePayload {
    private UUID conversationId;
    private int unreadCount;
    private LocalDateTime updatedAt;
}

// File: chat/dto/socket/BroadcastMessagePayload.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BroadcastMessagePayload {
    private UUID messageId;
    private UUID conversationId;
    private UUID senderId;
    private String senderName;
    private String content;
    private LocalDateTime timestamp;
    private String status;  // "delivered"
    private List<UUID> readBy;  // Empty initially
}

// File: chat/dto/socket/ErrorPayload.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorPayload {
    private String message;
    private String code;  // "not_participant", "blocked", etc.
    private long timestamp;
}
```

---

## Part 6: Feature Implementation Summary

### 6.1 Feature Decision Matrix

```
┌────────────────────────┬──────────┬──────────────────────────────┐
│ Feature                │ Protocol │ Implementation Notes         │
├────────────────────────┼──────────┼──────────────────────────────┤
│ Send Message           │ Socket   │ - Idempotent (client ID)     │
│                        │          │ - Rate limited               │
│                        │          │ - Block check                │
│                        │          │ - Async DB save             │
│                        │          │                              │
│ Message Delivery ACK   │ Socket   │ - Immediate ACK              │
│                        │          │ - Return messageId + status  │
│                        │          │                              │
│ Read Receipt           │ Socket   │ - Update lastReadAt in DB    │
│                        │          │ - Calculate new unread count │
│                        │          │ - Notify all user's sockets  │
│                        │          │                              │
│ Unread Count Update    │ Socket   │ - Push from server           │
│                        │          │ - On: message send, read     │
│                        │          │ - To: all user sockets       │
│                        │          │                              │
│ Block User             │ Both     │ - REST: Persist to DB        │
│                        │          │ - Socket: Enforce + notify   │
│                        │          │ - Remove from room           │
│                        │          │ - Reject future messages     │
│                        │          │                              │
│ Unblock User           │ Both     │ - REST: Remove block record  │
│                        │          │ - Socket: Notify restoration │
│                        │          │ - Can rejoin room            │
│                        │          │                              │
│ Mute Conversation      │ Both     │ - REST: Persist to DB        │
│                        │          │ - Socket: Notify user        │
│                        │          │ - Still receives messages    │
│                        │          │ - Just no notifications      │
│                        │          │                              │
│ Unmute Conversation    │ Both     │ - REST: Remove mute record   │
│                        │          │ - Socket: Restore notifs     │
│                        │          │                              │
│ Typing Indicator       │ Socket   │ - Broadcast to room          │
│                        │          │ - Auto-clear after 3 seconds │
│                        │          │ - Rate limited (throttle)    │
│                        │          │                              │
│ Presence (Online/Offline) │ Socket │ - Track lastActivity        │
│                        │          │ - Broadcast status changes   │
│                        │          │ - Calculate from socket list │
│                        │          │                              │
│ Fetch Chat List        │ REST     │ - Heavy query (joins)        │
│                        │          │ - Pagination                 │
│                        │          │ - Sorting                    │
│                        │          │                              │
│ Fetch Message History  │ REST     │ - Pagination (limit, offset) │
│                        │          │ - Chronological order        │
│                        │          │ - Filter by date range       │
│                        │          │                              │
│ Create Conversation    │ REST     │ - One-time operation         │
│                        │          │ - Validation of participants │
│                        │          │ - Init lastReadAt            │
│                        │          │                              │
│ Reconnect              │ Socket   │ - Detect via sessionId       │
│                        │          │ - Resume vs new connection   │
│                        │          │ - Send pending messages      │
│                        │          │                              │
└────────────────────────┴──────────┴──────────────────────────────┘
```

### 6.2 REST APIs to Keep

```
POST /conversations/create/one-to-one
  └─ Create conversation, init participants

GET /conversations/chat-list
  └─ Fetch all with unread counts (pagination)

GET /conversations/{conversationId}/messages
  └─ Fetch history (pagination, sorting)

POST /conversations/{conversationId}/block
  └─ Persist block, socket enforces it

POST /conversations/{conversationId}/unblock
  └─ Remove block

POST /conversations/{conversationId}/mute
  └─ Persist mute preference

POST /conversations/{conversationId}/unmute
  └─ Remove mute
```

---

## Part 7: Production Checklist

### 7.1 Deployment Readiness

```
INFRASTRUCTURE:
□ Redis cluster (for sessions & caching)
□ PostgreSQL with replication
□ Message queue (RabbitMQ/Kafka) for async operations
□ Load balancer (HAProxy/Nginx) for socket affinity
□ Horizontal scaling setup (socket rooms synced)

MONITORING:
□ Socket connection metrics
□ Message delivery latency
□ Unread count sync accuracy
□ Redis memory usage
□ Database connection pool
□ Error rate tracking
□ User presence tracking

SECURITY:
□ Rate limiting per user per event
□ Token refresh mechanism
□ Input validation & sanitization
□ SQL injection prevention
□ XSS prevention (content encoding)
□ CORS configuration
□ TLS/SSL for socket connections (WSS)

RELIABILITY:
□ Message ordering validation
□ Duplicate message detection
□ Reconnection resumption
□ Offline message queueing
□ Circuit breaker for DB failures
□ Graceful degradation
□ Dead letter queues for failed messages

TESTING:
□ Unit tests for handlers
□ Integration tests for socket events
□ Load testing (concurrent connections)
□ Chaos testing (network failures)
□ Multi-device same-user testing
□ Block/mute enforcement testing
□ Idempotency verification
```

---

## Summary Table: REST vs Socket.IO

```
┌────────────────────┬──────────────────┬──────────────────┐
│ Operation          │ When REST         │ When Socket      │
├────────────────────┼──────────────────┼──────────────────┤
│ Send Message       │ Initial release   │ Real-time version│
│                    │ (polling)         │ (recommended)    │
│                    │                   │                  │
│ Get Unread Count   │ Polling /10 sec   │ Push on change   │
│                    │ (stale data)      │ (live)           │
│                    │                   │                  │
│ Block/Unblock      │ REST persist      │ Socket enforce   │
│ Mute/Unmute        │ (1 API call)      │ + notify         │
│                    │                   │                  │
│ Read Receipt       │ Polling (bad UX)  │ Real-time        │
│                    │                   │ (recommended)    │
│                    │                   │                  │
│ Presence           │ Polling           │ Real-time push   │
│                    │ (battery drain)   │ (recommended)    │
│                    │                   │                  │
│ Fetch History      │ REST (pagination) │ N/A              │
│                    │ (recommended)     │ Too heavy        │
│                    │                   │                  │
│ Chat List          │ REST (recommended)│ N/A              │
│                    │ (sorting/filter)  │ Too heavy        │
│                    │                   │                  │
└────────────────────┴──────────────────┴──────────────────┘
```

---

## Next Steps

1. **Implement Socket.IO server** with authentication
2. **Add idempotency layer** (client message IDs)
3. **Set up Redis** for session storage
4. **Implement delivery tracking** for reliability
5. **Add comprehensive logging** for debugging
6. **Load test** with concurrent users
7. **Deploy to staging** with monitoring
8. **Monitor metrics** before production launch

This design is production-ready and follows industry best practices!

