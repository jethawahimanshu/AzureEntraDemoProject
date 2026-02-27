# Azure AD B2C Authentication & User Registration Flow

## Key Principle: **NO Passwords Ever Touch Your Backend**

---

## Part 1: JWT Claims Your Backend DOES Receive

When a user logs in via Azure AD B2C, your backend receives a JWT token containing these key claims:

### Identity (extracted in `JwtClaimsConverter`)
```java
oid          // Immutable user ID (Azure's unique identifier)
email        // User's email address
given_name   // First name
family_name  // Last name
```

### Authorization
```java
scp          // OAuth2 scopes (e.g., "products.read products.write")
roles        // Azure App Roles (e.g., ["Admin", "Viewer"])
```

### How your Java code accesses these:
```java
SecurityUtils.extractOid(jwt)           // → oid claim
SecurityUtils.extractDisplayName(jwt)   // → given_name + family_name
SecurityUtils.extractEmail(jwt)         // → email claim
```

---

## Part 2: Registration Flow (No Backend Password Storage)

### **Step 1: Frontend → User Redirects to Azure B2C Login Page**
```
Your App Frontend
    ↓
User clicks "Sign Up"
    ↓
Frontend redirects to:
https://yourb2ctenant.b2clogin.com/yourb2ctenant.onmicrosoft.com/oauth2/v2.0/authorize?...
```

**Azure B2C handles credential collection** (username, password, email verification) on a **Microsoft-hosted page**. Your backend is NOT involved.

### **Step 2: User Completes Registration on Azure B2C**
- Chooses username and password
- Microsoft hashes & stores that password
- Your backend never sees it

### **Step 3: User Redirected Back to Frontend with Code**
```
Azure B2C → Frontend
    ↓
Frontend exchanges code for JWT token (via your backend or own client library)
    ↓
JWT contains:
{
  "oid": "12345-abc-xyz",           // Immutable user ID
  "email": "user@example.com",
  "given_name": "John",
  "family_name": "Doe",
  "scp": "products.read products.write",
  "iat": 1234567890
}
```

### **Step 4: User Calls `/api/user/profile` (PUT)**

Frontend sends:
```json
{
  "displayName": "John Doe",
  "phone": "+1-555-1234"
}
```

**Your backend processes this:**
```java
// UserServiceImpl.upsertProfile()

User user = userRepository.findById(oid)          // Find existing user by oid
    .orElseGet(() -> buildNewUser(oid, jwt));     // Or auto-create from JWT claims

// Update only what the user provided
if (request.displayName() != null) {
    user.setDisplayName(request.displayName());   // Coming from frontend request
}
if (request.phone() != null) {
    user.setPhone(request.phone());               // Coming from frontend request
}

// For email & oid, pulled from JWT claims (not user input)
userRepository.save(user);
```

---

## Part 3: What Gets Stored in Your Database

### User Table (`users`)
```sql
oid         | display_name | email              | phone         | created_at | updated_at
------------|--------------|------------------|---------------|-----------|----------
12345-abc   | John Doe     | user@example.com  | +1-555-1234   | 2026-02... | 2026-02...
67890-def   | Jane Smith   | jane@example.com  | +1-555-5678   | 2026-02... | 2026-02...
```

**⚠️ NO PASSWORD COLUMN** — passwords are stored by Azure, encrypted, unhashable.

---

## Part 4: Login Flow (Subsequent Logins)

### **Step 1: User Logs In via Azure B2C**
```
Frontend → Azure B2C login page
User enters existing username + password
Azure validates against its database
→ User is authenticated
```

### **Step 2: Frontend Gets New JWT**
```
JWT claims include:
{
  "oid": "12345-abc-xyz",
  ... same claims as before
}
```

### **Step 3: Backend Validates JWT & Auto-Syncs Profile**
```java
@GetMapping("/api/user/profile")
public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal Jwt jwt) {
    String oid = jwt.getClaimAsString("oid");
    
    // Does this user exist in *our* database?
    userRepository.findById(oid)
        .orElseGet(() -> {
            // First login: auto-provision from JWT
            return User.builder()
                .oid(oid)
                .displayName(jwt.getClaimAsString("given_name") + " " + jwt.getClaimAsString("family_name"))
                .email(jwt.getClaimAsString("email"))
                .build();
        });
}
```

---

## Part 5: Architecture Diagram

```
┌─────────────────────────────────────────┐
│     Your Spring Boot Backend           │
├─────────────────────────────────────────┤
│                                         │
│  JwtClaimsConverter                     │
│  ├─ Extracts: oid, email, roles, scopes│
│  └─ Sets principal = oid                │
│                                         │
│  UserService.upsertProfile()            │
│  ├─ Reads JWT claims (oid, email)       │
│  ├─ Merges with user form input         │
│  │   (displayName, phone)               │
│  └─ Saves to User table                 │
│                                         │
│  @PreAuthorize("hasAuthority('SCOPE_*')") │
│  └─ Enforces scopes from JWT            │
│                                         │
└─────────────────────────────────────────┘
         ↕ JWT tokens (stateless)
┌─────────────────────────────────────────┐
│   Azure AD B2C Tenant                   │
├─────────────────────────────────────────┤
│                                         │
│  🔐 Password Storage (Hashed)           │
│  🔐 User Registry                       │
│  🔐 Multi-factor Auth, etc.             │
│                                         │
└─────────────────────────────────────────┘
```

---

## Part 6: Code Locations in Your Project

### **1. JWT Claims Extraction**
[JwtClaimsConverter.java](src/main/java/com/example/b2cdemo/config/JwtClaimsConverter.java#L1)
- Converts JWT → Spring Security authorities
- Principal name = `oid`

### **2. User Entity (No Passwords)**
[User.java](src/main/java/com/example/b2cdemo/domain/entity/User.java#L1)
- Only stores: oid, displayName, email, phone
- oid is the immutable primary key

### **3. Registration / Profile Upsert**
[UserController.java](src/main/java/com/example/b2cdemo/controller/UserController.java#L1)
- `PUT /api/user/profile` — create or update profile
- `GET /api/user/profile` — auto-provision on first call

### **4. Profile Creation Logic**
[UserServiceImpl.java](src/main/java/com/example/b2cdemo/service/UserServiceImpl.java#L1) 
- Line 30-46: `upsertProfile()` method
- Line 68-76: `buildNewUser()` pulls displayName + email from JWT

---

## Part 7: Frontend Behavior (Overview)

```js
// User clicks "Sign Up"
// Frontend redirects to Azure B2C:
window.location.href = "https://yourb2ctenant.b2clogin.com/...";

// ✋ User enters password on Azure's page (NOT your app)

// Azure redirects with code
// Frontend exchanges code for token
// Frontend calls your backend:

fetch("/api/user/profile", {
  method: "PUT",
  headers: { "Authorization": "Bearer <JWT>" },
  body: JSON.stringify({
    displayName: "John Doe",    // User's enhanced profile
    phone: "+1-555-1234"        // Additional app-specific data
  })
})
```

**Key point:** The application user data (displayName, phone) comes from your frontend form, **not** from passwords or Azure-managed fields.

---

## Summary

| Aspect | Azure B2C | Your Backend |
|--------|-----------|--------------|
| **Stores passwords?** | ✅ Yes (hashed) | ❌ No |
| **Receives passwords?** | ✅ From user registration form | ❌ No (JWT only) |
| **Authenticates users?** | ✅ Yes | ❌ Validates JWT only |
| **Stores user data?** | ✅ Identity (email, name, etc.) | ✅ Profile extensions (phone, preferences) |
| **Issues tokens?** | ✅ JWT | ❌ No |
| **Grants scopes?** | ✅ Based on App Registration | ✅ Enforces in @PreAuthorize |
