# PTODA Data Models

MySQL schema + Kotlin data classes for all tables.
**Last updated:** 2026-06-21

---

## `users` Table

```sql
CREATE TABLE users (
    id         INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,           -- bcrypt hash
    role       ENUM('passenger','driver','admin') NOT NULL,
    status     ENUM('active','inactive') NOT NULL DEFAULT 'active',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**Kotlin:**
```kotlin
data class UserResponse(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,    // "passenger" | "driver" | "admin"
    val status: String   // "active" | "inactive"
)
```

---

## `driver_info` Table

```sql
CREATE TABLE driver_info (
    id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id         INT UNSIGNED NOT NULL UNIQUE,
    license_no      VARCHAR(50) NOT NULL,
    vehicle_no      VARCHAR(50) NOT NULL,
    approval_status ENUM('pending','approved','rejected') NOT NULL DEFAULT 'pending',
    current_lat     DECIMAL(10,7) DEFAULT NULL,
    current_lng     DECIMAL(10,7) DEFAULT NULL,
    -- is_online TINYINT(1) DEFAULT 1,  ← NOT YET ADDED (Phase 8.3)
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

**Kotlin:**
```kotlin
data class PendingDriver(
    val id: Int,
    val name: String,
    val email: String,
    val created_at: String,
    val license_no: String,
    val vehicle_no: String,
    val approval_status: String  // "pending" | "approved" | "rejected"
)
```

---

## `bookings` Table

```sql
CREATE TABLE bookings (
    id               INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    passenger_id     INT UNSIGNED NOT NULL,
    driver_id        INT UNSIGNED DEFAULT NULL,
    pickup_address   VARCHAR(255) NOT NULL,
    pickup_lat       DECIMAL(10,7) NOT NULL,
    pickup_lng       DECIMAL(10,7) NOT NULL,
    dropoff_address  VARCHAR(255) NOT NULL,
    dropoff_lat      DECIMAL(10,7) NOT NULL,
    dropoff_lng      DECIMAL(10,7) NOT NULL,
    status           ENUM('requested','accepted','in_progress','completed','cancelled','rejected')
                         NOT NULL DEFAULT 'requested',
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (passenger_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (driver_id)    REFERENCES users(id) ON DELETE SET NULL
);
```

**Kotlin:**
```kotlin
// ⚠️ KNOWN BUG: pickup_lat/lng and dropoff_lat/lng are typed String below but
// MySQL returns DECIMAL as JSON numbers. This causes Gson to produce null.
// FIX (Phase 7.2.1): Change all four to Double and remove all .toDouble() calls.

data class Booking(
    val id: Int,
    val passenger_id: Int,
    val driver_id: Int?,
    val pickup_address: String,
    val pickup_lat: String,      // ⚠️ Should be Double
    val pickup_lng: String,      // ⚠️ Should be Double
    val dropoff_address: String,
    val dropoff_lat: String,     // ⚠️ Should be Double
    val dropoff_lng: String,     // ⚠️ Should be Double
    val status: String,
    val created_at: String,
    val updated_at: String?,
    // ⚠️ Missing fields (Phase 7.2.2 / 7.2.3 — add these):
    // val passenger_name: String? = null,
    // val driver_name: String? = null,
    // val driver_email: String? = null
)
```

**Status values:**

| Value | Set by | Route | Reachable? |
|-------|--------|-------|------------|
| `requested` | System | `POST /bookings` | ✅ |
| `accepted` | Driver | `POST /driver/accept/{id}` | ✅ |
| `in_progress` | Driver | `POST /driver/start/{id}` | ❌ not built |
| `completed` | Driver | `POST /driver/complete/{id}` | ✅ |
| `rejected` | Driver | `POST /driver/reject/{id}` | ✅ |
| `cancelled` | Passenger | `POST /bookings/{id}/cancel` | ❌ not built |

---

## `booking_logs` Table

```sql
CREATE TABLE booking_logs (
    id          INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    booking_id  INT UNSIGNED NOT NULL,
    old_status  VARCHAR(20) NOT NULL,
    new_status  VARCHAR(20) NOT NULL,
    changed_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);
```

Written automatically by `Booking::logStatusChange()` on every `updateStatus()` call.
No read endpoint currently exists — it is a write-only audit trail.

---

## `fcm_tokens` Table

```sql
CREATE TABLE fcm_tokens (
    id         INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id    INT UNSIGNED NOT NULL UNIQUE,
    token      VARCHAR(255) NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

**Kotlin:**
```kotlin
data class FcmTokenRequest(
    val token: String
)
```

Upserted via `PUT /user/fcm-token`. One row per user (UNIQUE on `user_id`). Updated on every `onNewToken()` callback from FCM.

---

## Auth Models (Kotlin)

```kotlin
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String,
    val license_no: String? = null,
    val vehicle_no: String? = null
)

data class LoginRequest(val email: String, val password: String)

data class LoginResponse(val token: String, val user: UserResponse)
```

---

## Driver Models (Kotlin)

```kotlin
data class UpdateLocationRequest(val lat: Double, val lng: Double)
```

---

## Admin Models (Kotlin)

```kotlin
data class AdminUser(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val status: String,
    val created_at: String
)
```
