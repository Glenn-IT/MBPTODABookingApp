# PTODA System Flows

Key user journeys and system sequences.
**Last updated:** 2026-06-21

---

## 1. Auth Flow — Register → Login → Token Usage

### Register

```
Android App                     PHP Backend                MySQL
    │                               │                        │
    │── POST /auth/register ────────>│                        │
    │   { name, email, password,    │── INSERT users ────────>│
    │     role, license_no? }       │                        │
    │                               │  (if driver)           │
    │                               │── INSERT driver_info ──>│
    │                               │   approval_status='pending'
    │<── 201 { user_id: 5 } ────────│                        │
```

Driver accounts are created with `approval_status = pending`. They cannot log in until an admin approves them.

### Login

```
Android App                     PHP Backend                MySQL
    │                               │                        │
    │── POST /auth/login ───────────>│                        │
    │   { email, password }         │── SELECT users ────────>│
    │                               │<── user row ───────────│
    │                               │── password_verify()    │
    │                               │                        │
    │                               │  (if driver)           │
    │                               │── SELECT driver_info ──>│
    │                               │   check approval_status│
    │                               │                        │
    │                               │── JWT::encode(payload) │
    │<── 200 { token, user } ───────│                        │
    │                               │                        │
    │  save token + role            │                        │
    │  to PrefsManager              │                        │
    │  → route to role home         │                        │
```

### Using the Token

Every protected request injects the JWT via OkHttp interceptor in `ApiClient.kt`:
```
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...
```

On 401: interceptor clears `PrefsManager` and navigates to `LoginActivity`.

---

## 2. Booking Flow — Full Ride Lifecycle

### Booking Status Diagram

```
[requested] ──────────────────────────────────► [accepted]
    │                                               │
    │ (driver rejects)                              │ (driver starts — NOT YET BUILT)
    ▼                                               ▼
[rejected]                                     [in_progress]  ← NOT YET REACHABLE
                                                   │
    [requested] ──► [cancelled]  ← NOT YET BUILT   │ (driver completes)
                                                   ▼
                                               [completed]
```

### Step-by-Step

**1. Passenger creates booking**
```
Passenger → POST /bookings { pickup, dropoff }
         ← 201 { booking_id: 10 }
         → navigates to RideStatusActivity
         → polls GET /bookings/10 every 5 seconds
```

**2. Driver sees pending requests**
```
Driver → GET /driver/requests
       ← [ booking #10, status: "requested", passenger_name: "..." ]
       → taps View Request → RideRequestActivity (data from Intent extras)
```

**3a. Driver accepts**
```
Driver → POST /driver/accept/10
       ← 200 "Ride accepted"
PHP: sets status=accepted, driver_id=3
PHP: sends FCM push to passenger "Driver is on the way!"
```

**3b. Driver rejects**
```
Driver → POST /driver/reject/10
       ← 200 "Ride rejected"
PHP: sets status=rejected
(booking disappears from driver/requests list)
```

**4. Driver completes ride** *(currently skips in_progress)*
```
Driver → POST /driver/complete/10
       ← 200 "Ride marked as completed"
PHP: checks driver_id == auth.user_id
PHP: sets status=completed
PHP: sends FCM push to passenger "Your ride is complete!"
```

**5. Passenger sees status update**
- Via polling (current): `RideStatusActivity` polls every 5s, stops on terminal status
- Via FCM (better): `PTODAFirebaseMessagingService.onMessageReceived` receives push and updates UI

### Booking Object at Each Stage

| Stage | `status` | `driver_id` |
|-------|----------|-------------|
| Created | `requested` | `null` |
| Accepted | `accepted` | `3` (set) |
| In Progress | `in_progress` | `3` |
| Completed | `completed` | `3` |
| Rejected | `rejected` | `null` |
| Cancelled | `cancelled` | `null` |

---

## 3. Driver Approval Flow

### Registration

```
Driver fills RegisterActivity with license_no + vehicle_no
→ POST /auth/register { role: "driver", license_no, vehicle_no }
→ PHP inserts users row (status=active)
→ PHP inserts driver_info row (approval_status=pending)
← 201 { user_id }
→ App shows "Account pending approval" message
```

### Approval by Admin

```
Admin → GET /admin/drivers/pending
      ← [ { id, name, email, license_no, vehicle_no, approval_status: "pending" } ]

Admin taps Approve:
      → PUT /admin/driver/approve/{driver_id}
      ← 200 "Driver approved successfully"
      PHP: sets driver_info.approval_status = approved

Admin taps Reject:
      → PUT /admin/driver/reject/{driver_id}
      ← 200 "Driver rejected successfully"
      PHP: sets driver_info.approval_status = rejected
```

### Driver Login Gate

```
Driver → POST /auth/login
       → PHP checks driver_info.approval_status
       → if pending:  403 "Your driver account is pending admin approval."
       → if rejected: 403 "Your driver account has been rejected."
       → if approved: 200 { token, user }
```

---

## 4. Android Setup — Network + FCM + Maps

### API Connection

| Environment | URL |
|-------------|-----|
| PC browser / Postman | `http://localhost/ptoda_booking_api/` |
| Android Emulator | `http://10.0.2.2/ptoda_booking_api/` |
| Physical device (any Wi-Fi) | Set via ⚙ Server dialog in the app (see below) |

#### Runtime Server URL — no rebuild needed

The app stores the server base URL in `SharedPreferences` (via `PrefsManager.getServerUrl` / `saveServerUrl`). `ApiClient` reads it on every API call and rebuilds the Retrofit instance when it changes.

**To change the server IP on your phone:**

1. Run `ipconfig` on the PC → note the **Wi-Fi IPv4 address** (e.g. `10.240.57.14`)
2. Open the app → Login screen → tap **"⚙ Server"** (small faint text at the bottom)
3. Type `http://<pc-ip>/ptoda_booking_api/` → tap **Save**
4. The app uses the new URL immediately — no restart, no rebuild

This setting survives logout and app restarts. You only need to change it when you move to a different Wi-Fi network.

**Implementation files:**
- `utils/Constants.kt` — `BASE_URL_DEVICE` is the default fallback (currently `10.240.57.14`)
- `data/local/PrefsManager.kt` — `getServerUrl()` / `saveServerUrl()` (stored in `ptoda_dev_prefs`, separate from login data so logout doesn't wipe it)
- `data/api/ApiClient.kt` — `instance` property rebuilds Retrofit when URL changes
- `ui/auth/LoginActivity.kt` — `showServerConfigDialog()` wired to `tvServerConfig`
- `res/layout/activity_login.xml` — `tvServerConfig` TextView at bottom of login screen
- `res/xml/network_security_config.xml` — whitelists current PC IP for cleartext HTTP

**Cleartext HTTP** is permitted via `res/xml/network_security_config.xml`:
```xml
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="false">10.240.57.14</domain>
        <domain includeSubdomains="false">10.0.2.2</domain>
    </domain-config>
</network-security-config>
```

> ⚠️ If you change Wi-Fi networks and the PC gets a new IP, you must also update `network_security_config.xml` and rebuild once so Android allows cleartext HTTP to the new IP. After that rebuild, use the ⚙ Server dialog for all future IP changes on that network.

Remove the cleartext config entirely in production when HTTPS is enabled.

### FCM Token Sync

```
App starts → PTODAFirebaseMessagingService.onNewToken() fires
           → stores token in PrefsManager
           
User logs in → AuthRepository.login() success
             → calls syncFcmTokenIfAvailable()
             → PUT /user/fcm-token { token }
             → PHP upserts fcm_tokens table
```

### FCM Receive (Push Notifications)

`PTODAFirebaseMessagingService.onMessageReceived()`:
- Extracts `booking_id` and `status` from data payload
- Builds a `Notification` with `PendingIntent`
- Tap routes to: driver → `DriverHomeActivity`, passenger → `RideStatusActivity`

> ⚠️ FCM push currently broken: `FCM_SERVER_KEY` in `config/config.php` is a placeholder, and the Legacy FCM API was shut down June 2025. Both must be fixed (Phase 7.1.3–7.1.5).

### Retrofit + OkHttp Setup

```kotlin
// ApiClient.kt — simplified

private val authInterceptor = Interceptor { chain ->
    val token = prefs.getToken()
    val request = if (token != null)
        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
    else chain.request()
    
    val response = chain.proceed(request)
    if (response.code == 401) { prefs.clearSession(); /* navigate to login */ }
    response
}

val retrofit = Retrofit.Builder()
    .baseUrl(Constants.BASE_URL)
    .addConverterFactory(GsonConverterFactory.create())
    .client(OkHttpClient.Builder().addInterceptor(authInterceptor).build())
    .build()
```

### Google Maps Setup

1. `AndroidManifest.xml` — Maps API key in `<meta-data android:name="com.google.android.geo.API_KEY">`
2. `SupportMapFragment` in passenger and driver layouts
3. `FusedLocationProviderClient` for current location
4. Request `ACCESS_FINE_LOCATION` at runtime before any map operations
5. Guard all `GoogleMap` operations with `map?.let { }` to prevent NPE on rotation

> ⚠️ Maps API key is currently hardcoded in the manifest. Move to `local.properties` + `BuildConfig` (Phase 7.4.2).
