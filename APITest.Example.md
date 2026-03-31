# Task Manager API — Complete Test Guide 🧪

> Base URL: `http://localhost:8080`
> Test Tool: Postman

---

## 📋 Quick Reference Table

| # | Method | Endpoint | Auth | Expected |
|---|--------|----------|------|----------|
| 1 | POST | /auth/register | None | 200 - success message |
| 2 | POST | /auth/register | None | 200 - duplicate message |
| 3 | POST | /auth/login | None | 200 - JWT token |
| 4 | POST | /auth/login | None | 401 - wrong password |
| 5 | POST | /tasks | USER token | 200 - task created |
| 6 | GET | /tasks | USER token | 200 - my tasks only |
| 7 | PUT | /tasks/{id} | USER token | 200 - task updated |
| 8 | DELETE | /tasks/{id} | USER token | 200 - task deleted |
| 9 | GET | /tasks/all | USER token | 403 - forbidden |
| 10 | GET | /tasks/all | ADMIN token | 200 - all tasks |
| 11 | GET | /tasks | No token | 401 - unauthorized |
| 12 | GET | /tasks | Fake token | 401 - unauthorized |

---

## ⚙️ How to Add Token in Postman

```
Headers tab:
Key   → Authorization
Value → Bearer eyJhbGciOiJIUzI1NiJ9.xxxxx
                ↑
         one space after Bearer
```

> ⚠️ Common mistakes:
> - ❌ `bearer eyJ...`  (lowercase b)
> - ❌ `BearereyJ...`   (no space)
> - ❌ `"Bearer eyJ..."` (with quotes)
> - ✅ `Bearer eyJ...`  (correct)

---

## 🟡 PART 1 — Register Users

### Test 1.1 — Register USER

```
Method  → POST
URL     → http://localhost:8080/auth/register
Headers → Content-Type: application/json
Body    → raw → JSON
```

```json
{
    "username": "dikshant",
    "password": "pass123",
    "role": "ROLE_USER"
}
```

**Expected Response — 200 OK:**
```
"User registered successfully!"
```

---

### Test 1.2 — Register ADMIN

```
Method  → POST
URL     → http://localhost:8080/auth/register
Headers → Content-Type: application/json
Body    → raw → JSON
```

```json
{
    "username": "admin",
    "password": "admin123",
    "role": "ROLE_ADMIN"
}
```

**Expected Response — 200 OK:**
```
"User registered successfully!"
```

---

### Test 1.3 — Register Duplicate Username

```
Method  → POST
URL     → http://localhost:8080/auth/register
Headers → Content-Type: application/json
Body    → raw → JSON
```

```json
{
    "username": "dikshant",
    "password": "anypassword",
    "role": "ROLE_USER"
}
```

**Expected Response — 200 OK:**
```
"Username already exists!"
```

> ✅ Duplicate check working.

---

## 🟢 PART 2 — Login and Get Tokens

### Test 2.1 — Login as USER

```
Method  → POST
URL     → http://localhost:8080/auth/login
Headers → Content-Type: application/json
Body    → raw → JSON
```

```json
{
    "username": "dikshant",
    "password": "pass123"
}
```

**Expected Response — 200 OK:**
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9.xxxxx",
    "username": "dikshant",
    "role": "ROLE_USER"
}
```

> 🔑 Copy the token value → save as **USER_TOKEN**

---

### Test 2.2 — Login as ADMIN

```
Method  → POST
URL     → http://localhost:8080/auth/login
Headers → Content-Type: application/json
Body    → raw → JSON
```

```json
{
    "username": "admin",
    "password": "admin123"
}
```

**Expected Response — 200 OK:**
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9.yyyyy",
    "username": "admin",
    "role": "ROLE_ADMIN"
}
```

> 🔑 Copy the token value → save as **ADMIN_TOKEN**

---

### Test 2.3 — Login with Wrong Password

```
Method  → POST
URL     → http://localhost:8080/auth/login
Headers → Content-Type: application/json
Body    → raw → JSON
```

```json
{
    "username": "dikshant",
    "password": "wrongpassword"
}
```

**Expected Response — 401 Unauthorized:**
```json
{
    "status": 401,
    "error": "Unauthorized"
}
```

> ✅ BCrypt verification working. Wrong password = blocked.

---

## 🔵 PART 3 — Task APIs (as USER)

> Add this header to every request below:
> `Authorization: Bearer <USER_TOKEN>`

---

### Test 3.1 — Create Task 1

```
Method  → POST
URL     → http://localhost:8080/tasks
Headers → Authorization: Bearer <USER_TOKEN>
          Content-Type: application/json
Body    → raw → JSON
```

```json
{
    "title": "Learn Spring Security",
    "description": "Complete task manager project",
    "status": "PENDING"
}
```

**Expected Response — 200 OK:**
```json
{
    "id": 1,
    "title": "Learn Spring Security",
    "description": "Complete task manager project",
    "status": "PENDING",
    "user": {
        "id": 1,
        "username": "dikshant",
        "role": "ROLE_USER"
    }
}
```

> ✅ User automatically linked from token — not from request body.

---

### Test 3.2 — Create Task 2

```
Method  → POST
URL     → http://localhost:8080/tasks
Headers → Authorization: Bearer <USER_TOKEN>
          Content-Type: application/json
Body    → raw → JSON
```

```json
{
    "title": "Practice LeetCode",
    "description": "Solve 5 problems today",
    "status": "IN_PROGRESS"
}
```

**Expected Response — 200 OK:**
```json
{
    "id": 2,
    "title": "Practice LeetCode",
    "description": "Solve 5 problems today",
    "status": "IN_PROGRESS",
    "user": {
        "id": 1,
        "username": "dikshant",
        "role": "ROLE_USER"
    }
}
```

---

### Test 3.3 — Get My Tasks

```
Method  → GET
URL     → http://localhost:8080/tasks
Headers → Authorization: Bearer <USER_TOKEN>
Body    → none
```

**Expected Response — 200 OK:**
```json
[
    {
        "id": 1,
        "title": "Learn Spring Security",
        "status": "PENDING"
    },
    {
        "id": 2,
        "title": "Practice LeetCode",
        "status": "IN_PROGRESS"
    }
]
```

> ✅ Only YOUR tasks returned. Never admin's tasks.

---

### Test 3.4 — Update a Task

```
Method  → PUT
URL     → http://localhost:8080/tasks/1
Headers → Authorization: Bearer <USER_TOKEN>
          Content-Type: application/json
Body    → raw → JSON
```

```json
{
    "title": "Learn Spring Security",
    "description": "All steps completed!",
    "status": "DONE"
}
```

**Expected Response — 200 OK:**
```json
{
    "id": 1,
    "title": "Learn Spring Security",
    "description": "All steps completed!",
    "status": "DONE"
}
```

---

### Test 3.5 — Delete a Task

```
Method  → DELETE
URL     → http://localhost:8080/tasks/2
Headers → Authorization: Bearer <USER_TOKEN>
Body    → none
```

**Expected Response — 200 OK:**
```
"Task deleted successfully!"
```

---

## 🔴 PART 4 — Role Protection Tests

### Test 4.1 — USER Tries ADMIN Endpoint → 403

```
Method  → GET
URL     → http://localhost:8080/tasks/all
Headers → Authorization: Bearer <USER_TOKEN>
Body    → none
```

**Expected Response — 403 Forbidden:**
```json
{
    "status": 403,
    "error": "Forbidden"
}
```

> ✅ USER token on ADMIN endpoint = 403.
> "I know who you are but you don't have permission."

---

### Test 4.2 — ADMIN Gets All Tasks → 200

```
Method  → GET
URL     → http://localhost:8080/tasks/all
Headers → Authorization: Bearer <ADMIN_TOKEN>
Body    → none
```

**Expected Response — 200 OK:**
```json
[
    {
        "id": 1,
        "title": "Learn Spring Security",
        "user": { "username": "dikshant" }
    },
    {
        "id": 3,
        "title": "Admin Task",
        "user": { "username": "admin" }
    }
]
```

> ✅ Admin sees ALL tasks from ALL users.

---

## ⚫ PART 5 — Security Edge Cases

### Test 5.1 — No Token → 401

```
Method  → GET
URL     → http://localhost:8080/tasks
Headers → (none)
Body    → none
```

**Expected Response — 401 Unauthorized:**
```json
{
    "status": 401,
    "error": "Unauthorized"
}
```

> ✅ No token = blocked immediately.

---

### Test 5.2 — Fake/Invalid Token → 401

```
Method  → GET
URL     → http://localhost:8080/tasks
Headers → Authorization: Bearer thisIsAFakeToken123
Body    → none
```

**Expected Response — 401 Unauthorized:**
```json
{
    "status": 401,
    "error": "Unauthorized"
}
```

> ✅ Tampered/fake token = rejected by JwtFilter.

---

## ✅ Final Checklist

Run all tests in this order and verify:

```
1.  POST /auth/register      (dikshant)       → success ✅
2.  POST /auth/register      (admin)           → success ✅
3.  POST /auth/register      (dikshant again)  → duplicate msg ✅
4.  POST /auth/login         (dikshant)        → copy USER token ✅
5.  POST /auth/login         (admin)           → copy ADMIN token ✅
6.  POST /auth/login         (wrong password)  → 401 ✅
7.  POST /tasks              (USER token)      → task 1 created ✅
8.  POST /tasks              (USER token)      → task 2 created ✅
9.  GET  /tasks              (USER token)      → my tasks only ✅
10. PUT  /tasks/1            (USER token)      → task updated ✅
11. DELETE /tasks/2          (USER token)      → task deleted ✅
12. GET  /tasks/all          (USER token)      → 403 ✅
13. GET  /tasks/all          (ADMIN token)     → all tasks ✅
14. GET  /tasks              (no token)        → 401 ✅
15. GET  /tasks              (fake token)      → 401 ✅
```

---

## 🧠 Key Concepts Behind Each Test

| Test | Concept Verified |
|------|-----------------|
| Register duplicate | existsByUsername() check |
| Login wrong password | BCrypt.matches() verification |
| Create task | @AuthenticationPrincipal — user from token not body |
| Get my tasks | findByUser() — data isolation |
| USER hits /tasks/all | Role-based access — 403 |
| ADMIN hits /tasks/all | @PreAuthorize("hasRole('ADMIN')") |
| No token → 401 | JwtFilter — missing header |
| Fake token → 401 | JwtFilter — validateToken() fails |

---

*Task Manager API — Spring Security Implementation*