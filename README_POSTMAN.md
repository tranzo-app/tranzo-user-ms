# 🚀 Complete Splitwise API Testing Guide

## 📁 File to Import
**Import this file into Postman:**
```
Tranzo_Splitwise_API_Collection.postman_collection.json
```

## ⚡ Quick Setup

### 1. Import Collection
1. Open **Postman**
2. Click **Import** (top-left)
3. Select `Tranzo_Splitwise_API_Collection.postman_collection.json`
4. Click **Import**

### 2. Set Environment
1. Click the **👁️ eye icon** (top-right)
2. Click **Edit** next to "Tranzo Splitwise Module"
3. Set `baseUrl` to: `http://localhost:8080`
4. Click **Save**

### 3. Start Application
```
Start your Spring Boot application
Wait for: "Started TranzoUserMsApplication"
Port: 8080
URL: http://localhost:8080
```

---

## 🎯 **Complete Testing Flow - 20 Steps**

### **Phase 1: Setup & Authentication** (Must do first)

#### **Step 1: Login as User1** 🔑
```
POST /auth/session/login
Headers: Content-Type: application/json
Body: {
  "email": "user1@mail.com", 
  "password": "password123"
}
Expected: 200 OK
Response: { "data": { "accessToken": "eyJ..." } }
Result: {{accessToken}} saved automatically
```

---

### **Phase 2: Groups - Create & Read** (Foundation)

#### **Step 2: Get All Groups** 📋
```
GET /api/splitwise/groups
Headers: Authorization: Bearer {{accessToken}}
Expected: 200 OK
Purpose: See existing groups from data.sql
```

#### **Step 3: Get Specific Group** 👥
```
GET /api/splitwise/groups/1
Headers: Authorization: Bearer {{accessToken}}
Expected: 200 OK
Purpose: View Group 1 details (Manali Trip)
```

#### **Step 4: Create New Group** ➕
```
POST /api/splitwise/groups
Headers: 
  - Content-Type: application/json
  - Authorization: Bearer {{accessToken}}
Body: {
  "name": "Weekend Gateway",
  "tripId": "test-trip-123",
  "description": "Quick weekend trip testing"
}
Expected: 200 OK
Purpose: Create your own group for testing
```

---

### **Phase 3: Expenses - Create & Manage** (Core Functionality)

#### **Step 5: Get Group Expenses** 💸
```
GET /api/splitwise/expenses/group/1
Headers: Authorization: Bearer {{accessToken}}
Expected: 200 OK
Purpose: See existing expenses in Group 1
```

#### **Step 6: Create Equal Split Expense** ⚖️
```
POST /api/splitwise/expenses
Headers: 
  - Content-Type: application/json
  - Authorization: Bearer {{accessToken}}
Body: {
  "name": "Team Lunch",
  "description": "Celebration lunch at restaurant",
  "amount": 2400.00,
  "groupId": 1,
  "splitType": "EQUAL",
  "category": "FOOD",
  "expenseDate": "2026-03-05",
  "splits": [
    {
      "userId": "11111111-1111-4111-8111-111111111111",
      "amount": 600.00
    },
    {
      "userId": "22222222-2222-4222-8222-222222222222", 
      "amount": 600.00
    },
    {
      "userId": "33333333-3333-4333-8333-333333333333",
      "amount": 600.00
    },
    {
      "userId": "55555555-5555-4555-8555-555555555555",
      "amount": 600.00
    }
  ]
}
Expected: 200 OK
Purpose: Test equal split functionality
```

#### **Step 7: Create Unequal Split Expense** 🎯
```
POST /api/splitwise/expenses
Headers: 
  - Content-Type: application/json
  - Authorization: Bearer {{accessToken}}
Body: {
  "name": "Taxi Ride",
  "description": "Airport to hotel transfer",
  "amount": 1200.00,
  "groupId": 1,
  "splitType": "UNEQUAL",
  "category": "TRANSPORT",
  "expenseDate": "2026-03-05",
  "splits": [
    {
      "userId": "11111111-1111-4111-8111-111111111111",
      "amount": 600.00
    },
    {
      "userId": "22222222-2222-4222-8222-222222222222",
      "amount": 400.00
    },
    {
      "userId": "33333333-3333-4333-8333-333333333333",
      "amount": 200.00
    }
  ]
}
Expected: 200 OK
Purpose: Test unequal split functionality
```

---

### **Phase 4: Balances - Check Calculations** (Financial Status)

#### **Step 8: Get Group Balances** ⚖️
```
GET /api/splitwise/balances/group/1
Headers: Authorization: Bearer {{accessToken}}
Expected: 200 OK
Purpose: See who owes whom in Group 1
Note: Should show balances from your new expenses
```

#### **Step 9: Get Your Balance** 💰
```
GET /api/splitwise/balances/group/1/my-balance
Headers: Authorization: Bearer {{accessToken}}
Expected: 200 OK
Purpose: See your personal balance in the group
```

---

### **Phase 5: Settlements - Clear Debts** (Payment Flow)

#### **Step 10: Create Settlement** 🏦
```
POST /api/splitwise/settlements
Headers: 
  - Content-Type: application/json
  - Authorization: Bearer {{accessToken}}
Body: {
  "groupId": 1,
  "paidById": "22222222-2222-4222-8222-222222222222",
  "paidToId": "11111111-1111-4111-8111-111111111111",
  "amount": 1000.00,
  "paymentMethod": "UPI",
  "transactionId": "TEST_TXN_001",
  "notes": "Settlement for lunch expenses"
}
Expected: 200 OK
Purpose: Test payment settlement
```

#### **Step 11: Get Settlements** 📊
```
GET /api/splitwise/settlements/group/1
Headers: Authorization: Bearer {{accessToken}}
Expected: 200 OK
Purpose: View all settlements in the group
```

---

### **Phase 6: Activities - Audit Trail** (Transaction History)

#### **Step 12: Get Activity Log** 📋
```
GET /api/splitwise/activities/group/1
Headers: Authorization: Bearer {{accessToken}}
Expected: 200 OK
Purpose: See complete activity history
Note: Should show your expense creation and settlement
```

#### **Step 13: Get Your Activities** 👤
```
GET /api/splitwise/activities/my-activities
Headers: Authorization: Bearer {{accessToken}}
Expected: 200 OK
Purpose: See your personal activity history
```

---

### **Phase 7: Multi-User Testing** (Advanced)

#### **Step 14: Login as User2** 👥
```
POST /auth/session/login
Headers: Content-Type: application/json
Body: {
  "email": "user2@mail.com",
  "password": "password123"
}
Expected: 200 OK
Result: {{accessToken_user2}} saved
```

#### **Step 15: Check Balances as User2** 💰
```
GET /api/splitwise/balances/group/1/my-balance
Headers: Authorization: Bearer {{accessToken_user2}}
Expected: 200 OK
Purpose: See User2's perspective of balances
```

#### **Step 16: Create Expense as User2** 💸
```
POST /api/splitwise/expenses
Headers: 
  - Content-Type: application/json
  - Authorization: Bearer {{accessToken_user2}}
Body: {
  "name": "Coffee Break",
  "description": "Team coffee at cafe",
  "amount": 800.00,
  "groupId": 1,
  "splitType": "EQUAL",
  "category": "FOOD",
  "expenseDate": "2026-03-05",
  "splits": [
    {
      "userId": "11111111-1111-4111-8111-111111111111",
      "amount": 400.00
    },
    {
      "userId": "22222222-2222-4222-8222-222222222222",
      "amount": 400.00
    }
  ]
}
Expected: 200 OK
Purpose: Test expense creation by different user
```

---

### **Phase 8: Error Handling & Edge Cases** (Robustness Testing)

#### **Step 17: Test Invalid Group** ❌
```
GET /api/splitwise/groups/999
Headers: Authorization: Bearer {{accessToken}}
Expected: 404 Not Found
Purpose: Test error handling
```

#### **Step 18: Test Unauthorized Access** 🚫
```
GET /api/splitwise/groups/1
Headers: (no Authorization header)
Expected: 401 Unauthorized
Purpose: Test security
```

#### **Step 19: Test Invalid Expense** ⚠️
```
POST /api/splitwise/expenses
Headers: 
  - Content-Type: application/json
  - Authorization: Bearer {{accessToken}}
Body: {
  "name": "Invalid Expense",
  "amount": -1000.00,
  "groupId": 999
}
Expected: 400 Bad Request
Purpose: Test validation
```

#### **Step 20: Final Balance Check** ✅
```
GET /api/splitwise/balances/group/1
Headers: Authorization: Bearer {{accessToken}}
Expected: 200 OK
Purpose: Verify final state after all operations
```

---

## 🎯 **Complete Flow Summary**

```
1. Login (Get Token) → 2. Get Groups → 3. Create Group
4. Get Expenses → 5. Create Expense → 6. Check Balances  
7. Create Settlement → 8. Get Activities → 9. Multi-User Test
10. Error Handling
```

## ✅ **Success Indicators**

- **All 200 OK responses** for valid operations
- **404/400 responses** for invalid operations (expected)
- **Balances update** after expenses and settlements
- **Activities track** all operations
- **Multi-user isolation** works correctly

## 🚨 **Important Notes**

1. **Always run Step 1 first** - Login is required for all operations
2. **Use Group ID 1** for initial testing (pre-loaded data)
3. **Check balances after each expense** to see calculations
4. **Test with both users** to verify multi-user functionality
5. **Expected data**: 3 groups, 6+ expenses, 11+ balances from data.sql

## 📋 **Test Data Available**

- **3 Groups** with 12 total members
- **6 Expenses** with different split types  
- **11 Balances** calculated from expenses
- **3 Settlements** (2 completed, 1 pending)
- **9 Activities** tracking all operations

## 🎉 **What You'll Test**

Following this sequence tests complete Splitwise functionality:
- ✅ Authentication
- ✅ Group management  
- ✅ Expense creation (equal/unequal splits)
- ✅ Balance calculations
- ✅ Payment settlements
- ✅ Activity tracking
- ✅ Multi-user scenarios
- ✅ Error handling

## 🆘 **Troubleshooting**

**Problem:** 401 Unauthorized  
**Solution:** Run Step 1 (Login) first to get a fresh token

**Problem:** Collection won't import  
**Solution:** Make sure you're importing the `.json` file, not this README

**Problem:** No data returned  
**Solution:** Check that `data.sql` was loaded and application started successfully

**Problem:** Token not working  
**Solution:** Check environment variables in Postman (👁️ icon)

---

**Ready to test! Start with Step 1 and follow the sequence! 🚀**
