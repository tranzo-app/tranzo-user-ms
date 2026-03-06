# Splitwise MVP - Low Level Design (LLD)

## Overview
This document provides a comprehensive low-level design for a Splitwise-like expense splitting application built with Spring Boot and Java.

## Features
1. Add, edit and delete expenses
2. Splitwise logic for a group
3. Log activities after adding, editing, deleting and settling
4. Settle expenses
5. Get all expenses of a group
6. Show balances for each user
7. Show summary with share of the given user
8. Fetch a given expense - show people involved, amount, settled dates, expense name etc.

---

## 1. API Endpoints

### Group Management
- `POST /api/groups` - Create a new group
- `GET /api/groups/{groupId}` - Get group details
- `PUT /api/groups/{groupId}` - Update group details
- `DELETE /api/groups/{groupId}` - Delete group
- `POST /api/groups/{groupId}/members` - Add member to group
- `DELETE /api/groups/{groupId}/members/{userId}` - Remove member from group
- `GET /api/groups/{groupId}/members` - Get all group members

### Expense Management
- `POST /api/expenses` - Add new expense
- `GET /api/expenses/{expenseId}` - Get expense details
- `PUT /api/expenses/{expenseId}` - Update expense
- `DELETE /api/expenses/{expenseId}` - Delete expense
- `GET /api/groups/{groupId}/expenses` - Get all expenses of a group

### Balance & Settlement
- `GET /api/groups/{groupId}/balances` - Show balances for each user
- `GET /api/groups/{groupId}/balances/{userId}` - Show summary with share of given user
- `POST /api/settlements` - Settle expenses between users
- `GET /api/settlements/{settlementId}` - Get settlement details

### Activity Logging
- `GET /api/groups/{groupId}/activities` - Get activity log for group
- `GET /api/users/{userId}/activities` - Get activity log for user

---

## 2. Entity Design

### User
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String passwordHash;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### Group
```java
@Entity
@Table(name = "groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<GroupMember> members = new ArrayList<>();
}
```

### GroupMember
```java
@Entity
@Table(name = "group_members")
public class GroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Enumerated(EnumType.STRING)
    private MemberRole role = MemberRole.MEMBER;
    
    @CreationTimestamp
    private LocalDateTime joinedAt;
    
    public enum MemberRole {
        ADMIN, MEMBER
    }
}
```

### Expense
```java
@Entity
@Table(name = "expenses")
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @ManyToOne
    @JoinColumn(name = "paid_by")
    private User paidBy;
    
    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;
    
    @Enumerated(EnumType.STRING)
    private SplitType splitType;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL)
    private List<ExpenseSplit> splits = new ArrayList<>();
    
    public enum SplitType {
        EQUAL, UNEQUAL, PERCENTAGE
    }
}
```

### ExpenseSplit
```java
@Entity
@Table(name = "expense_splits")
public class ExpenseSplit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "expense_id")
    private Expense expense;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Column
    private BigDecimal percentage;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}
```

### Balance
```java
@Entity
@Table(name = "balances")
public class Balance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;
    
    @ManyToOne
    @JoinColumn(name = "owed_by")
    private User owedBy;
    
    @ManyToOne
    @JoinColumn(name = "owed_to")
    private User owedTo;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @CreationTimestamp
    private LocalDateTime lastUpdated;
}
```

### Settlement
```java
@Entity
@Table(name = "settlements")
public class Settlement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;
    
    @ManyToOne
    @JoinColumn(name = "paid_by")
    private User paidBy;
    
    @ManyToOne
    @JoinColumn(name = "paid_to")
    private User paidTo;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @CreationTimestamp
    private LocalDateTime settledAt;
    
    @OneToMany(mappedBy = "settlement", cascade = CascadeType.ALL)
    private List<SettlementExpense> settledExpenses = new ArrayList<>();
}
```

### SettlementExpense
```java
@Entity
@Table(name = "settlement_expenses")
public class SettlementExpense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "settlement_id")
    private Settlement settlement;
    
    @ManyToOne
    @JoinColumn(name = "expense_id")
    private Expense expense;
    
    @Column(nullable = false)
    private BigDecimal amount;
}
```

### Activity
```java
@Entity
@Table(name = "activities")
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Enumerated(EnumType.STRING)
    private ActivityType activityType;
    
    @Column
    private String description;
    
    @Column
    private Long relatedId; // expense_id, settlement_id, etc.
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    public enum ActivityType {
        EXPENSE_ADDED, EXPENSE_UPDATED, EXPENSE_DELETED,
        SETTLEMENT_CREATED, MEMBER_ADDED, MEMBER_REMOVED
    }
}
```

---

## 3. DTOs (Data Transfer Objects)

### Request DTOs

#### CreateGroupRequest
```java
public class CreateGroupRequest {
    @NotBlank(message = "Group name is required")
    private String name;
    
    private String description;
    
    @NotEmpty(message = "At least one member is required")
    private List<Long> memberIds;
}
```

#### CreateExpenseRequest
```java
public class CreateExpenseRequest {
    @NotBlank(message = "Expense name is required")
    private String name;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotNull(message = "Group ID is required")
    private Long groupId;
    
    @NotNull(message = "Paid by user ID is required")
    private Long paidById;
    
    @NotNull(message = "Split type is required")
    private Expense.SplitType splitType;
    
    @NotEmpty(message = "At least one split is required")
    private List<ExpenseSplitRequest> splits;
}
```

#### ExpenseSplitRequest
```java
public class ExpenseSplitRequest {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @DecimalMin(value = "0", message = "Percentage cannot be negative")
    @DecimalMax(value = "100", message = "Percentage cannot exceed 100")
    private BigDecimal percentage;
}
```

#### UpdateExpenseRequest
```java
public class UpdateExpenseRequest {
    private String name;
    
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    private Expense.SplitType splitType;
    
    private List<ExpenseSplitRequest> splits;
}
```

#### CreateSettlementRequest
```java
public class CreateSettlementRequest {
    @NotNull(message = "Group ID is required")
    private Long groupId;
    
    @NotNull(message = "Paid by user ID is required")
    private Long paidById;
    
    @NotNull(message = "Paid to user ID is required")
    private Long paidToId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
}
```

### Response DTOs

#### GroupResponse
```java
public class GroupResponse {
    private Long id;
    private String name;
    private String description;
    private UserResponse createdBy;
    private LocalDateTime createdAt;
    private List<UserResponse> members;
}
```

#### ExpenseResponse
```java
public class ExpenseResponse {
    private Long id;
    private String name;
    private BigDecimal amount;
    private UserResponse paidBy;
    private GroupResponse group;
    private Expense.SplitType splitType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ExpenseSplitResponse> splits;
    private Boolean isSettled;
}
```

#### ExpenseSplitResponse
```java
public class ExpenseSplitResponse {
    private Long id;
    private UserResponse user;
    private BigDecimal amount;
    private BigDecimal percentage;
}
```

#### BalanceResponse
```java
public class BalanceResponse {
    private UserResponse user;
    private BigDecimal totalOwed;
    private BigDecimal totalOwing;
    private Map<Long, BigDecimal> balances; // userId -> amount
}
```

#### UserBalanceResponse
```java
public class UserBalanceResponse {
    private UserResponse user;
    private BigDecimal totalShare;
    private BigDecimal totalPaid;
    private BigDecimal netBalance;
    private List<IndividualBalanceResponse> individualBalances;
}
```

#### IndividualBalanceResponse
```java
public class IndividualBalanceResponse {
    private UserResponse otherUser;
    private BigDecimal amount;
    private String type; // "OWED" or "OWING"
}
```

#### SettlementResponse
```java
public class SettlementResponse {
    private Long id;
    private GroupResponse group;
    private UserResponse paidBy;
    private UserResponse paidTo;
    private BigDecimal amount;
    private LocalDateTime settledAt;
    private List<ExpenseResponse> settledExpenses;
}
```

#### ActivityResponse
```java
public class ActivityResponse {
    private Long id;
    private GroupResponse group;
    private UserResponse user;
    private Activity.ActivityType activityType;
    private String description;
    private Long relatedId;
    private LocalDateTime createdAt;
}
```

---

## 4. Validation Rules

### Group Validations
- Group name: Required, max 100 characters
- Description: Optional, max 500 characters
- Member IDs: At least one member required (including creator)

### Expense Validations
- Expense name: Required, max 200 characters
- Amount: Required, must be positive
- Group ID: Required, must exist
- Paid by user ID: Required, must be group member
- Split type: Required (EQUAL, UNEQUAL, PERCENTAGE)
- Splits: 
  - At least one split required
  - All split users must be group members
  - For EQUAL split: amounts must be equal
  - For PERCENTAGE split: percentages must sum to 100
  - For UNEQUAL split: amounts must sum to total expense amount

### Settlement Validations
- Group ID: Required, must exist
- Paid by/to user IDs: Required, must be group members, cannot be same
- Amount: Required, must be positive
- Cannot settle more than owed amount

### General Validations
- All user IDs must exist in system
- All group operations must verify user membership
- Amount precision: Use BigDecimal with scale 2 for monetary values

---

## 5. Database Schema

### Tables
1. **users** - User information
2. **groups** - Group details
3. **group_members** - Group membership mapping
4. **expenses** - Expense records
5. **expense_splits** - Expense split details
6. **balances** - Current balances between users
7. **settlements** - Settlement records
8. **settlement_expenses** - Settlement to expense mapping
9. **activities** - Activity log

### Indexes
- Primary keys on all id columns
- Foreign key indexes on all relationship columns
- Composite indexes on (group_id, user_id) for balances
- Composite indexes on (group_id, created_at) for expenses and activities

---

## 6. Service Layer Design

### Core Services
1. **GroupService** - Group management operations
2. **ExpenseService** - Expense CRUD operations
3. **BalanceService** - Balance calculations and updates
4. **SettlementService** - Settlement operations
5. **ActivityService** - Activity logging
6. **UserService** - User management

### Key Business Logic
1. **Splitwise Algorithm**: Calculate optimal settlements to minimize transactions
2. **Balance Calculation**: Real-time balance updates after expense changes
3. **Activity Logging**: Automatic logging of all significant actions
4. **Validation**: Comprehensive validation at service layer

---

## 7. Error Handling

### Custom Exceptions
- `GroupNotFoundException` - Group not found
- `UserNotMemberException` - User not part of group
- `InvalidSplitException` - Invalid expense split
- `InsufficientBalanceException` - Insufficient balance for settlement
- `ValidationException` - General validation errors

### HTTP Status Codes
- 200 - Success
- 201 - Created
- 400 - Bad Request (validation errors)
- 401 - Unauthorized
- 403 - Forbidden (not group member)
- 404 - Not Found
- 409 - Conflict (duplicate entries, etc.)

---

## 8. Security Considerations

1. **Authentication**: JWT-based authentication
2. **Authorization**: Role-based access control within groups
3. **Input Validation**: Comprehensive validation of all inputs
4. **SQL Injection**: Use parameterized queries
5. **Data Encryption**: Sensitive data encryption at rest

---

## 9. Performance Considerations

1. **Database Indexing**: Proper indexes on frequently queried columns
2. **Caching**: Redis caching for frequently accessed data
3. **Pagination**: Large result sets should be paginated
4. **Batch Operations**: Bulk operations for balance updates
5. **Connection Pooling**: Database connection pooling

---

## 10. Next Steps

1. Implement the entities with proper relationships
2. Create the repository interfaces
3. Implement the service layer with business logic
4. Create the REST controllers with proper validation
5. Add comprehensive unit and integration tests
6. Set up proper logging and monitoring
7. Implement authentication and authorization
8. Add API documentation (Swagger/OpenAPI)
