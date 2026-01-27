# Chat Architecture - Visual Components

## Simplified Layer Architecture

```
┌──────────────────────────────────────────────────────────┐
│                    CLIENT LAYER                           │
│        (Frontend App / Postman / Browser)                 │
└────────────────┬──────────────────────┬──────────────────┘
                 │                      │
          REST API (HTTP)        WebSocket (WS)
                 │                      │
         ┌───────▼──────┐      ┌────────▼─────────┐
         │   REST       │      │   WebSocket      │
         │ Controller   │      │   Interceptor    │
         │              │      │                  │
         │ • POST/GET   │      │ • JWT Validation │
         │ • JSON Body  │      │ • STOMP Protocol │
         │ • HTTP Codes │      │ • Real-time Msg  │
         └───────┬──────┘      └────────┬─────────┘
                 │                      │
                 └──────────┬───────────┘
                            │
        ┌───────────────────▼────────────────────┐
        │       SERVICE LAYER                    │
        │  (Business Logic & Orchestration)      │
        │                                        │
        │ ┌─ CreateAndManage...Service ────────┐ │
        │ │ ┌─ Create Conversation             │ │
        │ │ ├─ Send Message                    │ │
        │ │ ├─ Mark as Read                    │ │
        │ │ ├─ Block/Unblock User              │ │
        │ │ └─ Delete Message                  │ │
        │ └────────────────────────────────────┘ │
        │                                        │
        │ ┌─ Conversation Service ─────────────┐ │
        │ │ ├─ Get My Conversations            │ │
        │ │ └─ Fetch Messages (with read mark) │ │
        │ └────────────────────────────────────┘ │
        │                                        │
        └───────────────────┬────────────────────┘
                            │
        ┌───────────────────▼────────────────────┐
        │     REPOSITORY LAYER                   │
        │    (Data Access Objects)               │
        │                                        │
        │ ┌─────────────────────────────────┐   │
        │ │ ConversationRepository          │   │
        │ │ └─ findChatListForUser()        │   │
        │ │ └─ findOneToOneConversation()   │   │
        │ └─────────────────────────────────┘   │
        │ ┌─────────────────────────────────┐   │
        │ │ MessageRepository               │   │
        │ │ └─ findMessages()               │   │
        │ └─────────────────────────────────┘   │
        │ ┌─────────────────────────────────┐   │
        │ │ ParticipantRepository           │   │
        │ │ └─ save(), findById()           │   │
        │ └─────────────────────────────────┘   │
        │ ┌─────────────────────────────────┐   │
        │ │ Block/Mute Repository           │   │
        │ └─────────────────────────────────┘   │
        │                                        │
        └───────────────────┬────────────────────┘
                            │
        ┌───────────────────▼────────────────────┐
        │      PERSISTENCE LAYER                 │
        │      (JPA / Hibernate)                 │
        │                                        │
        │ ┌─────────────────────────────────┐   │
        │ │ ConversationEntity              │   │
        │ │ • conversationId (PK)           │   │
        │ │ • type, createdBy, createdAt    │   │
        │ │ • participants (1:many)         │   │
        │ └─────────────────────────────────┘   │
        │ ┌─────────────────────────────────┐   │
        │ │ MessageEntity                   │   │
        │ │ • messageId (PK)                │   │
        │ │ • conversation (FK)             │   │
        │ │ • senderId, content, createdAt  │   │
        │ └─────────────────────────────────┘   │
        │ ┌─────────────────────────────────┐   │
        │ │ ParticipantEntity               │   │
        │ │ • id (PK)                       │   │
        │ │ • conversation (FK)             │   │
        │ │ • userId, role                  │   │
        │ │ • lastReadAt (unread tracking)  │   │
        │ └─────────────────────────────────┘   │
        │ ┌─────────────────────────────────┐   │
        │ │ Block/Mute Entity               │   │
        │ │ • conversation (FK)             │   │
        │ │ • userId, relationshipData      │   │
        │ └─────────────────────────────────┘   │
        │                                        │
        └───────────────────┬────────────────────┘
                            │
        ┌───────────────────▼────────────────────┐
        │        DATABASE LAYER                  │
        │       (PostgreSQL / MySQL)             │
        │                                        │
        │ Tables:                                │
        │  • conversation                        │
        │  • message                             │
        │  • conversation_participant            │
        │  • conversation_block                  │
        │  • conversation_mute                   │
        │                                        │
        └────────────────────────────────────────┘
```

---

## Class Diagram (Simplified)

```
┌─────────────────────────────┐
│   CreateAndManageChatController
├─────────────────────────────┤
│ - createAndManageService    │
│ - conversationService       │
├─────────────────────────────┤
│ + sendMessage()             │
│ + createConversation()      │
│ + fetchMessages()           │
│ + markAsRead()              │
│ + blockConversation()       │
└──────────────┬──────────────┘
               │ uses
               ▼
┌─────────────────────────────┐         ┌──────────────────────┐
│CreateAndManageConversationSvc├────────►│ConversationRepository│
├─────────────────────────────┤         └──────────────────────┘
│ - conversationRepo          │
│ - participantRepo           │         ┌──────────────────────┐
│ - blockRepo                 │────────►│MessageRepository     │
│ - muteRepo                  │         └──────────────────────┘
├─────────────────────────────┤
│ + sendMessage()             │         ┌──────────────────────┐
│ + createConversation()      │────────►│ParticipantRepository │
│ + markConversationAsRead()  │         └──────────────────────┘
│ + blockConversation()       │
│ + deleteMessage()           │         ┌──────────────────────┐
└─────────────────────────────┘────────►│BlockRepository       │
                                        └──────────────────────┘
               
┌─────────────────────────────┐         ┌──────────────────────┐
│  ConversationService        │────────►│MuteRepository        │
├─────────────────────────────┤         └──────────────────────┘
│ - conversationRepo          │
│ - participantRepo           │
│ - messageRepo               │
├─────────────────────────────┤
│ + getMyConversations()      │
│ + fetchMessages()           │
│   (marks as read)           │
└─────────────────────────────┘


┌──────────────────────────┐
│  ConversationEntity      │
├──────────────────────────┤
│ - conversationId: UUID   │
│ - type: enum             │
│ - createdBy: UUID        │
│ - createdAt: DateTime    │
│ - participants: Set<>    │◄─── 1:many
└──────────┬───────────────┘
           │
           ▼
┌──────────────────────────────────┐
│  ConversationParticipantEntity   │
├──────────────────────────────────┤
│ - id: UUID                       │
│ - conversation: FK               │
│ - userId: UUID                   │
│ - role: enum                     │
│ - joinedAt: DateTime             │
│ - lastReadAt: DateTime ◄─────┐  │
│ - leftAt: DateTime           │  │
└──────────────────────────────┘  │
                                   │
                    ┌──────────────┘
                    │ Tracks unread
                    │
                    ▼
            Unread Count Logic:
            COUNT(messages WHERE
              createdAt > lastReadAt
              AND senderId != userId)


┌──────────────────────────┐
│     MessageEntity        │
├──────────────────────────┤
│ - messageId: UUID        │
│ - conversation: FK       │
│ - senderId: UUID         │
│ - content: String        │
│ - createdAt: DateTime    │
│ - isSystemMessage: bool  │
└──────────────────────────┘
```

---

## WebSocket Real-time Message Flow

```
┌──────────────────────┐
│   Client Browser     │
│  (WebSocket.js)      │
└──────────┬───────────┘
           │
           │ 1. Connect
           │ ws://host:8080/ws-chat?token=JWT
           ▼
┌──────────────────────────────────────┐
│  WebSocketConfig                     │
├──────────────────────────────────────┤
│ • registerStompEndpoints()           │
│ • configureMessageBroker()           │
│ • configureClientInboundChannel()    │
└──────────┬──────────────────────────┘
           │
           │ 2. Validate Token
           ▼
┌──────────────────────────────────────┐
│  WebSocketAuthInterceptor            │
├──────────────────────────────────────┤
│ • extractToken from URL              │
│ • validateAccessToken()              │
│ • setSecurityContext()               │
└──────────┬──────────────────────────┘
           │
           │ 3. Connection Established
           ▼ (STOMP Protocol)
┌──────────────────────────────────────┐
│  Message Broker (SimpleBroker)       │
├──────────────────────────────────────┤
│ • Application Prefix: /app           │
│ • Broker Prefix: /topic              │
└──────────┬──────────────────────────┘
           │
    ┌──────┴──────┐
    │             │
    │ 4. Subscribe
    │ /topic/conversations/{id}
    │             │
    │             ▼
    │    ┌────────────────────┐
    │    │  ClientA           │
    │    │ (Subscribed)       │
    │    └────────────────────┘
    │
    │ 5. Send Message
    │ /app/chat.sendMessage
    │
    ▼
┌──────────────────────────────────────┐
│  ChatSocketController                │
├──────────────────────────────────────┤
│ @MessageMapping("/chat.sendMessage") │
│                                      │
│ • Get current user from context      │
│ • Call sendMessage service           │
│ • Get conversationId from payload    │
└──────────┬──────────────────────────┘
           │
           ▼
┌──────────────────────────────────────┐
│  CreateAndManageConversationService  │
├──────────────────────────────────────┤
│ • Save message to database           │
│ • Return SendMessageResponseDto      │
└──────────┬──────────────────────────┘
           │
           │ 6. Broadcast
           ▼
┌──────────────────────────────────────┐
│  SimpMessagingTemplate               │
├──────────────────────────────────────┤
│ convertAndSend(                      │
│   "/topic/conversations/{id}",       │
│   messageResponse                    │
│ )                                    │
└──────────┬──────────────────────────┘
           │
           │ 7. Send to all subscribers
           ▼
    ┌──────────┬──────────┐
    ▼          ▼          ▼
┌────────┐ ┌────────┐ ┌────────┐
│ClientA │ │ClientB │ │ClientC │
│Receive │ │Receive │ │Receive │
└────────┘ └────────┘ └────────┘
(All subscribed to /topic/conversations/{id})
```

---

## Message Unread Tracking

```
Timeline: User B's Perspective

10:00 AM - Conversation Created
│
├─ User A joins → User A.lastReadAt = 10:00 AM
├─ User B joins → User B.lastReadAt = 10:00 AM
│
10:05 AM - User A sends message
│
├─ Message stored: createdAt = 10:05 AM
│
10:06 AM - User B checks chat list
│
├─ Query: COUNT(messages WHERE
│    createdAt > lastReadAt
│    AND senderId != User B)
│
├─ Result: 10:05 AM > 10:00 AM? YES
├─ unreadCount = 1 ✅
│
10:07 AM - User B views messages
│
├─ GET /conversations/{id}/messages
├─ fetchMessages() calls:
│    participant.markAsRead()
│    participant.lastReadAt = 10:07 AM
│    participantRepository.save()
│
├─ Message READ in database
│
10:08 AM - User B checks chat list again
│
├─ Query: COUNT(messages WHERE
│    createdAt > lastReadAt
│    AND senderId != User B)
│
├─ Result: 10:05 AM > 10:07 AM? NO
├─ unreadCount = 0 ✅
│
10:10 AM - User A sends NEW message
│
├─ Message stored: createdAt = 10:10 AM
├─ Query: 10:10 AM > 10:07 AM? YES
└─ unreadCount = 1 again ✅
```

---

## Key Features Overview

```
┌─────────────────────────────────────────────┐
│         CHAT SYSTEM FEATURES                │
├─────────────────────────────────────────────┤
│                                             │
│  ✅ ONE-TO-ONE Conversations                │
│    └─ Create via createOneToOneConversation()
│                                             │
│  ✅ REAL-TIME Messaging (WebSocket)         │
│    └─ STOMP protocol with SimpMessaging    │
│                                             │
│  ✅ UNREAD COUNT Tracking                   │
│    └─ Based on participant.lastReadAt      │
│    └─ Auto-updated on fetchMessages()      │
│                                             │
│  ✅ BLOCK Feature                           │
│    └─ Prevent messages from blocked users  │
│    └─ ConversationBlockEntity tracks it    │
│                                             │
│  ✅ MUTE Feature                            │
│    └─ Hide notifications, still receive    │
│    └─ ConversationMuteEntity tracks it     │
│                                             │
│  ✅ JWT Authentication                      │
│    └─ Token validation in WebSocket        │
│    └─ SecurityContext for user tracking    │
│                                             │
│  ✅ MESSAGE Persistence                     │
│    └─ All messages saved to database       │
│    └─ Support system messages (auto)       │
│                                             │
│  ✅ PAGINATION Support                      │
│    └─ Fetch messages with limit            │
│    └─ Fetch before timestamp               │
│                                             │
└─────────────────────────────────────────────┘
```

