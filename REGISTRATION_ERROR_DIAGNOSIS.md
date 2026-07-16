# Registration Error: "Authentication required or token invalid"

## Root Cause Analysis

The error **"Authentication required or token invalid"** occurs because the `/user/register` endpoint requires a special **REGISTRATION token** that must be obtained through the OTP verification flow first.

### Why This Happens:

1. **Security Configuration** (`SecurityConfig.java`, line 50):
   ```java
   .requestMatchers("/user/register").authenticated()
   ```
   - The `/user/register` endpoint is marked as `authenticated()`, meaning it requires authentication
   - Unlike truly public endpoints like `/auth/otp/**`, it CANNOT be accessed without authentication

2. **Registration Token Filter** (`RegistrationTokenFilter.java`):
   - When a request comes to `/user/register`, a custom filter validates the Authorization header
   - It expects: `Authorization: Bearer <REGISTRATION_TOKEN>`
   - It validates that the token type is exactly `"REGISTRATION"`
   - If missing or invalid, it throws `UnauthorizedException("Registration token missing")`

3. **Token Generation** (`OtpService.java`, lines 135, 143):
   - The registration token is **ONLY** generated after successful OTP verification
   - It's returned in the `VerifyOtpResponseDto` response from `/auth/otp/verify`

---

## Correct Registration Flow

### Step 1: Request OTP (Public - No Auth Required)
```bash
POST /auth/otp/request
Content-Type: application/json

{
  "mobileNumber": "+1234567890"  // OR
  "emailId": "user@example.com"
}

Response: 200 OK
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "OTP sent successfully",
  "data": null
}
```

### Step 2: Verify OTP and Get Registration Token (Public - No Auth Required)
```bash
POST /auth/otp/verify
Content-Type: application/json

{
  "mobileNumber": "+1234567890",  // OR emailId
  "otp": "123456"
}

Response: 200 OK
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "OTP has been verified successfully",
  "data": {
    "userExists": false,
    "registrationToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

### Step 3: Register User with Registration Token (Requires Token)
```bash
POST /user/register
Authorization: Bearer <REGISTRATION_TOKEN_FROM_STEP_2>
Content-Type: application/json

{
  "identifier": "+1234567890",  // Must match the phone/email from OTP
  "firstName": "John",
  "lastName": "Doe",
  "profileImage": "..."
}

Response: 200 OK - User registered successfully
```

---

## What Frontend Should Do

### ❌ **WRONG** - Skipping OTP verification:
```javascript
// This will ALWAYS fail
const response = await fetch('/user/register', {
  method: 'POST',
  body: JSON.stringify({
    identifier: '+1234567890',
    firstName: 'John'
  })
});
// Error: "Authentication required or token invalid"
```

### ✅ **CORRECT** - Following the flow:
```javascript
// Step 1: Request OTP
const otpResponse = await fetch('/auth/otp/request', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ mobileNumber: '+1234567890' })
});

// Step 2: Verify OTP
const verifyResponse = await fetch('/auth/otp/verify', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    mobileNumber: '+1234567890',
    otp: userSuppliedOtp
  })
});

const verifyData = await verifyResponse.json();
const registrationToken = verifyData.data.registrationToken;

// Step 3: Register with token
const registerResponse = await fetch('/user/register', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${registrationToken}`  // ⚠️ REQUIRED
  },
  body: JSON.stringify({
    identifier: '+1234567890',
    firstName: 'John',
    lastName: 'Doe'
  })
});
```

---

## Debugging Checklist

- [ ] Is the frontend calling `/auth/otp/request` first?
- [ ] Is the frontend calling `/auth/otp/verify` with the correct OTP?
- [ ] Is the frontend extracting `registrationToken` from the `/auth/otp/verify` response?
- [ ] Is the frontend sending the token in the Authorization header like: `Authorization: Bearer <token>`?
- [ ] Does the identifier in `/user/register` match the phone/email used in OTP verification?
- [ ] Is there a typo in the "Bearer " prefix (note the space)?

---

## Key Code References

| File | Issue |
|------|-------|
| `SecurityConfig.java` line 50 | `/user/register` requires authentication |
| `RegistrationTokenFilter.java` line 29 | Validates registration token for `/user/register` |
| `OtpService.java` line 135 | Registration token generated only after OTP verify |
| `JwtAuthEntryPoint.java` line 28 | Returns the error message you're seeing |

---

## Additional Context

The design forces a specific flow for security reasons:
- Prevents random users from calling `/user/register` without being verified
- Ensures phone number or email is verified before account creation
- Associates the registration with an identifier that was OTP-verified
- Makes it harder for attackers to spam user creation

