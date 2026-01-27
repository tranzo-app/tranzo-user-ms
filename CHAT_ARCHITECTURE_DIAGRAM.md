# Chat Module Architecture Diagram

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           CLIENT (Frontend/Postman)                          │
└────────────────────────────────┬────────────────────────────────────────────┘
                                 │
                    ┌────────────┴────────────┐
                    │                         │
            REST API (HTTP)        WebSocket (Real-time)
                    │                         │
        ┌───────────▼──────────┐   ┌─────────▼────────────┐
        │  REST Controllers    │   │  WebSocket Config    │
        │  ─────────────────── │   │  ──────────────────  │
        │ • CreateAndManage    │   │ • WebSocketConfig    │
        │   ChatController     │   │ • WebSocketAuth      │
        │                      │   │   Interceptor        │
        └───────────┬──────────┘   └─────────┬────────────┘
                    │                         │
                    └────────────┬────────────┘
                                 │
        ┌────────────────────────▼────────────────────────┐
        │           SERVICE LAYER                         │
        │  ╔════════════════════════════════════════════╗ │
        │  ║ CreateAndManageConversationService         ║ │
        │  ║ ───────────────────────────────────────────║ │
        │  ║ • createOneToOneConversation()             ║ │
        │  ║ • sendMessage()                            ║ │
        │  ║ • markConversationAsRead()                 ║ │
        │  ║ • blockConversation()                      ║ │
        │  ║ • unblockConversation()                    ║ │
        │  ║ • deleteMessage()                          ║ │
        │  ╚════════════════════════════════════════════╝ │
        │  ╔════════════════════════════════════════════╗ │
        │  ║ ConversationService                        ║ │
        │  ║ ───────────────────────────────────────────║ │
        │  ║ • getMyConversations()                     ║ │
        │  ║ • fetchMessages()                          ║ │
        │  ╚════════════════════════════════════════════╝ │
        └────────────────────────┬─────────────────────────┘
                                 │
        ┌────────────────────────▼─────────────────────────┐
        │           REPOSITORY LAYER (Data Access)         │
        │  ╔════════════════════════════════════════════╗  │
        │  ║ ConversationRepository                    ║  │
        │  ║ • findChatListForUser()                   ║  │
        │  ║ • findOneToOneConversationBetweenUsers()  ║  │
        │  ╚════════════════════════════════════════════╝  │
        │  ╔════════════════════════════════════════════╗  │
        │  ║ MessageRepository                         ║  │
        │  ║ • findMessages()                          ║  │
        │  ╚════════════════════════════════════════════╝  │
        │  ╔════════════════════════════════════════════╗  │
        │  ║ ConversationParticipantRepository         ║  │
        │  ║ • findByConversationAndUser()             ║  │
        │  ║ • markAsRead()                            ║  │
        │  ╚════════════════════════════════════════════╝  │
        │  ╔════════════════════════════════════════════╗  │
        │  ║ ConversationBlockRepository               ║  │
        │  ║ • existsByConversationAndUser()           ║  │
        │  ╚════════════════════════════════════════════╝  │
        │  ╔════════════════════════════════════════════╗  │
        │  ║ ConversationMuteRepository                ║  │
        │  ║ • findByConversationAndUser()             ║  │
        │  ╚════════════════════════════════════════════╝  │
        └────────────────────────┬─────────────────────────┘
                                 │
        ┌────────────────────────▼─────────────────────────┐
        │              DATABASE ENTITIES (JPA)             │
        │  ╔════════════════════════════════════════════╗  │
        │  ║ ConversationEntity                        ║  │
        │  ║ ────────────────────────────────────────  ║  │
        │  ║ • conversationId: UUID                    ║  │
        │  ║ • type: ConversationType                  ║  │
        │  ║ • createdBy: UUID                         ║  │
        │  ║ • createdAt: LocalDateTime                ║  │
        │  ║ • participants: Set<Participant>          ║  │
        │  ║ • messages: Set<Message> (implicit)       ║  │
        │  ╚════════════════════════════════════════════╝  │
        │  ╔════════════════════════════════════════════╗  │
        │  ║ MessageEntity                             ║  │
        │  ║ ────────────────────────────────────────  ║  │
        │  ║ • messageId: UUID                         ║  │
        │  ║ • conversation: ConversationEntity        ║  │
        │  ║ • senderId: UUID                          ║  │
        │  ║ • content: String                         ║  │
        │  ║ • createdAt: LocalDateTime                ║  │
        │  ║ • isSystemMessage: Boolean                ║  │
        │  ╚════════════════════════════════════════════╝  │
        │  ╔════════════════════════════════════════════╗  │
        │  ║ ConversationParticipantEntity             ║  │
        │  ║ ────────────────────────────────────────  ║  │
        │  ║ • id: UUID                                ║  │
        │  ║ • conversation: ConversationEntity        ║  │
        │  ║ • userId: UUID                            ║  │
        │  ║ • role: ConversationRole                  ║  │
        │  ║ • joinedAt: LocalDateTime                 ║  │
        │  ║ • lastReadAt: LocalDateTime               ║  │
        │  ║ • leftAt: LocalDateTime                   ║  │
        │  ╚════════════════════════════════════════════╝  │
        │  ╔════════════════════════════════════════════╗  │
        │  ║ ConversationBlockEntity                   ║  │
        │  ║ ────────────────────────────────────────  ║  │
        │  ║ • id: UUID                                ║  │
        │  ║ • conversation: ConversationEntity        ║  │
        │  ║ • blockedBy: UUID (user who blocked)      ║  │
        │  ║ • blockedUser: UUID                       ║  │
        │  ║ • blockedAt: LocalDateTime                ║  │
        │  ╚════════════════════════════════════════════╝  │
        │  ╔════════════════════════════════════════════╗  │
        │  ║ ConversationMuteEntity                    ║  │
        │  ║ ────────────────────────────────────────  ║  │
        │  ║ • id: UUID                                ║  │
        │  ║ • conversation: ConversationEntity        ║  │
        │  ║ • userId: UUID                            ║  │
        │  ║ • mutedAt: LocalDateTime                  ║  │
        │  ╚════════════════════════════════════════════╝  │
        └────────────────────────┬─────────────────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │   DATABASE (PostgreSQL) │
                    │   ─────────────────────  │
                    │ • conversation          │
                    │ • message               │
                    │ • conversation_         │
                    │   participant           │
                    │ • conversation_block    │
                    │ • conversation_mute     │
                    └─────────────────────────┘
```

---

## Data Flow Diagram

### REST API Flow (Get Chat List)

```
Client
  │
  ├─ GET /conversations/chat-list
  │  (Authorization: Bearer TOKEN)
  │
  ▼
CreateAndManageChatController.getMyConversations()
  │
  ├─ SecurityUtils.getCurrentUserUuid()
  │
  ▼
ConversationService.getMyConversations(UUID currentUserId)
  │
  ├─ ConversationRepository.findChatListForUser(currentUserId)
  │
  ▼
SQL Query:
  SELECT new ChatListItemDto(
    conversation.conversationId,
    conversation.type,
    lastMessage.content,
    lastMessage.createdAt,
    isMuted,
    unreadCount
  )
  FROM ConversationEntity
  WHERE user is participant
  
  ▼
Database
  │
  ├─ Fetch conversations
  ├─ Calculate unread messages per conversation
  └─ Return ChatListItemDto list
  
  ▼
Response to Client:
[
  {
    "conversationId": "uuid",
    "type": "ONE_ON_ONE",
    "lastMessage": "Hello",
    "lastMessageTime": "2025-01-25T10:30:00Z",
    "isMuted": false,
    "unreadCount": 3
  }
]
```

### REST API Flow (Send Message)

```
Client
  │
  ├─ POST /conversations/{conversationId}/send-message
  │  Body: {"content": "Hello!"}
  │
  ▼
CreateAndManageChatController.sendMessage()
  │
  ├─ SecurityUtils.getCurrentUserUuid()
  │
  ▼
CreateAndManageConversationService.sendMessage()
  │
  ├─ ConversationRepository.findById(conversationId)
  ├─ ConversationParticipantRepository.findByConversationAndUser()
  ├─ ConversationBlockRepository.existsByConversationAndUser()
  │
  ▼
MessageRepository.save(MessageEntity)
  │
  ├─ Store message in database
  │
  ▼
Response to Client:
{
  "messageId": "uuid",
  "conversationId": "uuid",
  "senderId": "uuid",
  "content": "Hello!",
  "createdAt": "2025-01-25T10:35:00Z"
}

  ├─ (Simultaneously emit via WebSocket to recipients)
```

### WebSocket Flow (Real-time Messaging)

```
Client (Browser/App)
  │
  ├─ ws://localhost:8080/ws-chat?token=JWT_TOKEN
  │
  ▼
WebSocketConfig.registerStompEndpoints()
  │
  ├─ Endpoint: /ws-chat
  └─ Allowed Origins: *
  
  ▼
WebSocketAuthInterceptor.preSend()
  │
  ├─ Extract JWT from URL query parameter
  ├─ JwtService.validateAccessToken()
  ├─ Set SecurityContext with userId
  │
  ▼ (If valid)
STOMP Connection Established

Client sends:
SUBSCRIBE
destination:/topic/conversations/{conversationId}
id:sub-0

  ▼
Client receives real-time messages:
SEND
destination:/app/chat.sendMessage
Content-Type: application/json
{...message...}

  ▼
ChatSocketController.sendMessage()
  │
  ├─ SecurityUtils.getCurrentUserUuid()
  ├─ CreateAndManageConversationService.sendMessage()
  │
  ▼
SimpMessagingTemplate.convertAndSend()
  │
  ├─ Send to: /topic/conversations/{conversationId}
  │
  ▼
All subscribed clients receive message in real-time
```

---

## Module Dependencies

```
Controller
    │
    ├─ CreateAndManageChatController
    │   └─ depends on:
    │       ├─ CreateAndManageConversationService
    │       └─ ConversationService
    │
    └─ ChatSocketController
        └─ depends on:
            ├─ CreateAndManageConversationService
            └─ SimpMessagingTemplate

Service
    │
    ├─ CreateAndManageConversationService
    │   └─ depends on:
    │       ├─ ConversationRepository
    │       ├─ ConversationParticipantRepository
    │       ├─ ConversationBlockRepository
    │       ├─ MessageRepository
    │       ├─ ConversationMuteRepository
    │       └─ JwtService (security)
    │
    └─ ConversationService
        └─ depends on:
            ├─ ConversationRepository
            ├─ ConversationParticipantRepository
            ├─ MessageRepository
            └─ Transactional support

Repository
    │
    ├─ ConversationRepository
    │   └─ Query: findChatListForUser() - complex JPQL
    │
    ├─ MessageRepository
    │   └─ Query: findMessages()
    │
    ├─ ConversationParticipantRepository
    │   └─ Queries: findByConversationAndUser()
    │
    ├─ ConversationBlockRepository
    │   └─ Queries: existsByConversationAndUser()
    │
    └─ ConversationMuteRepository
        └─ Queries: findByConversationAndUser()

Entity Models
    │
    ├─ ConversationEntity (1 → many)
    │   ├─ participants: Set<ConversationParticipantEntity>
    │   └─ messages: implicit (no reverse ref in entity)
    │
    ├─ MessageEntity (many → 1)
    │   └─ conversation: ConversationEntity
    │
    ├─ ConversationParticipantEntity (many → 1)
    │   └─ conversation: ConversationEntity
    │
    ├─ ConversationBlockEntity (many → 1)
    │   └─ conversation: ConversationEntity
    │
    └─ ConversationMuteEntity (many → 1)
        └─ conversation: ConversationEntity
```

---

## Entity Relationship Diagram (ERD)

```
┌─────────────────────────────────┐
│      Conversation               │
├─────────────────────────────────┤
│ PK: conversation_id (UUID)      │
│    type (Enum)                  │
│    created_by (UUID)            │
│    created_at (DateTime)        │
└──────────────┬──────────────────┘
               │
               │ 1 : many
               ▼
┌─────────────────────────────────┐
│  ConversationParticipant        │
├─────────────────────────────────┤
│ PK: id (UUID)                   │
│ FK: conversation_id             │
│    user_id (UUID)               │
│    role (Enum: MEMBER/ADMIN)    │
│    joined_at (DateTime)         │
│    last_read_at (DateTime)      │ ◄── Tracks read status
│    left_at (DateTime)           │
└─────────────────────────────────┘

Unique Constraint: (conversation_id, user_id)
```

```
┌─────────────────────────────────┐
│      Message                    │
├─────────────────────────────────┤
│ PK: message_id (UUID)           │
│ FK: conversation_id             │
│    sender_id (UUID)             │
│    content (Text)               │
│    created_at (DateTime)        │
│    is_system_message (Boolean)  │
└─────────────────────────────────┘
       ▲
       │ many : 1
       │
       └─ belongs to ConversationEntity
```

```
┌──────────────────────────────────┐
│   ConversationBlock              │
├──────────────────────────────────┤
│ PK: id (UUID)                    │
│ FK: conversation_id              │
│    blocked_by (UUID)             │
│    blocked_user (UUID)           │
│    blocked_at (DateTime)         │
└──────────────────────────────────┘
```

```
┌──────────────────────────────────┐
│   ConversationMute               │
├──────────────────────────────────┤
│ PK: id (UUID)                    │
│ FK: conversation_id              │
│    user_id (UUID)                │
│    muted_at (DateTime)           │
└──────────────────────────────────┘
```

---

## Key Features & How They Work

### 1. Unread Message Count
```
Logic:
  unreadCount = COUNT(messages WHERE
    createdAt > COALESCE(participant.lastReadAt, conversation.createdAt)
    AND senderId != currentUserId
  )

When participant joins:
  → lastReadAt = NOW
  → All messages after join are unread

When user views messages:
  → GET /conversations/{id}/messages
  → fetchMessages() calls participant.markAsRead()
  → lastReadAt = NOW
  → Next unreadCount = 0 (if no new messages)
```

### 2. Real-time Messaging (WebSocket)
```
Flow:
  1. Client connects: ws://host:8080/ws-chat?token=JWT
  2. WebSocketAuthInterceptor validates token
  3. Client subscribes: /topic/conversations/{id}
  4. Client sends: /app/chat.sendMessage
  5. ChatSocketController processes
  6. SimpMessagingTemplate broadcasts to all subscribers
  7. All connected clients receive in real-time
```

### 3. Blocking & Muting
```
Blocking:
  - User A blocks User B in conversation
  - ConversationBlockEntity stores relationship
  - User B cannot send messages to User A
  - Checked on sendMessage()

Muting:
  - User mutes a conversation
  - ConversationMuteEntity stores relationship
  - Chat list shows muted status
  - User still receives messages but marked as muted
```

---

## API Endpoints

```
REST APIs:

1. Get Chat List
   GET /conversations/chat-list
   Response: List<ChatListItemDto>

2. Create Conversation
   POST /conversations/create/one-to-one
   Body: {"otherUserId": "UUID"}

3. Send Message
   POST /conversations/{conversationId}/send-message
   Body: {"content": "text"}

4. Fetch Messages
   GET /conversations/{conversationId}/messages?limit=30
   Response: List<MessageResponseDto>

5. Mark as Read
   POST /conversations/{conversationId}/read

6. Block Conversation
   POST /conversations/{conversationId}/block

7. Unblock Conversation
   POST /conversations/{conversationId}/unblock

WebSocket:

1. Connect
   ws://localhost:8080/ws-chat?token=JWT

2. Subscribe
   SUBSCRIBE /topic/conversations/{conversationId}

3. Send Message
   SEND /app/chat.sendMessage
   Payload: {"conversationId":"uuid","sendMessageRequestDto":{...}}

4. Receive Messages
   /topic/conversations/{conversationId}
```

---

## Summary

This is a **multi-layer chat architecture** with:

- **REST API** for CRUD operations and chat history
- **WebSocket** for real-time messaging
- **Service Layer** for business logic
- **Repository Layer** for data persistence
- **Entity Models** for database structure
- **JWT Authentication** for security
- **Unread Count Tracking** via participant.lastReadAt
- **Blocking & Muting** features for user control

