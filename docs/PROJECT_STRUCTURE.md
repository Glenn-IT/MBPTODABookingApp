# PTODA Booking System — Project Structure

## Overview

Full project structure for both the **PHP REST API** (backend) and the **Android (Kotlin)** app (frontend).

_Last updated: 2026-06-21_

---

## Backend — PHP REST API (`C:\xampp\htdocs\ptoda_booking_api\`)

```
ptoda_booking_api/
├── config/
│   ├── config.php                # JWT secret, FCM key, app constants
│   └── database.php              # PDO connection singleton
│
├── helpers/
│   ├── JWT.php                   # HS256 encode/decode
│   ├── Response.php              # json_response() standardized output helper
│   └── FCM.php                   # Firebase Cloud Messaging (cURL push sender)
│
├── middleware/
│   └── AuthMiddleware.php        # Bearer token verifier; sets $auth context
│
├── models/
│   ├── User.php                  # User model (all roles); FCM token management
│   ├── Booking.php               # Booking CRUD + status transitions + log writer
│   └── Admin.php                 # Admin queries (getPendingDrivers, approveDriver,
│                                 # rejectDriver, activateUser, deactivateUser, deleteUser,
│                                 # getAllBookings, getAllDrivers [unused])
│
├── controllers/
│   ├── BookingController.php     # create, index (role-filtered), getById
│   ├── DriverController.php      # getPendingRequests, acceptRide, rejectRide,
│   │                             # completeRide, updateLocation
│   └── AdminController.php       # getAllUsers, getPendingDrivers, getAllBookings,
│                                 # approveDriver, rejectDriver, activateUser,
│                                 # deactivateUser, deleteUser
│
│   ⚠️  NOTE: AuthController.php does NOT exist as a separate file.
│       Auth routes (register, login) are handled inline in index.php.
│       PassengerController.php does NOT exist.
│       routes/api.php does NOT exist (routing is in index.php).
│
├── database/
│   └── seed.sql                  # Sample data for testing
│
│   ⚠️  NOTE: schema.sql is referenced in docs but not present in repo.
│       Use seed.sql to understand the DB schema.
│
├── uploads/
│   └── drivers/                  # Driver license / profile photo uploads (.gitkeep)
│
├── logs/
│   └── error.log                 # API error log (.gitkeep)
│
├── .htaccess                     # URL rewriting (Apache mod_rewrite)
├── index.php                     # Entry point — all route definitions + auth routes inline
└── docs/                         # Backend-side copy of docs (may be out of date)
```

---

## Android App (Kotlin, Android Studio)

```
MBPTODABookingApp/
├── app/
│   └── src/
│       └── main/
│           ├── java/com/example/mbptodabookingapp/
│           │   ├── PTODAApplication.kt           # App singleton (Retrofit init)
│           │   ├── MainActivity.kt               # Auth router (login state → role home)
│           │   │
│           │   ├── data/
│           │   │   ├── api/
│           │   │   │   ├── ApiClient.kt           # Retrofit singleton + auth interceptor
│           │   │   │   ├── ApiService.kt          # Retrofit interface (all endpoints)
│           │   │   │   └── ApiResponse.kt         # Generic response wrapper
│           │   │   ├── local/
│           │   │   │   └── PrefsManager.kt        # SharedPreferences (JWT, user role)
│           │   │   ├── models/
│           │   │   │   ├── AuthModels.kt          # LoginRequest, RegisterRequest, AuthResponse
│           │   │   │   ├── Booking.kt             # Booking data class (⚠️ coords typed String, not Double)
│           │   │   │   ├── User.kt                # User data class
│           │   │   │   ├── DriverModels.kt        # LocationUpdate, DriverRequest, etc.
│           │   │   │   ├── AdminModels.kt         # AdminUser, PendingDriver, etc.
│           │   │   │   └── FcmModels.kt           # FcmTokenRequest
│           │   │   └── repository/
│           │   │       ├── BaseRepository.kt      # safeApiCall wrapper
│           │   │       ├── AuthRepository.kt      # login, register, fcmToken sync
│           │   │       ├── BookingRepository.kt   # createBooking, getBookings, getById
│           │   │       ├── UserRepository.kt      # updateLocation, updateFcmToken
│           │   │       └── AdminRepository.kt     # getAllUsers, getBookings, driver approval
│           │   │
│           │   ├── services/
│           │   │   └── PTODAFirebaseMessagingService.kt  # FCM push handler
│           │   │
│           │   ├── ui/
│           │   │   ├── auth/
│           │   │   │   ├── LoginActivity.kt
│           │   │   │   ├── RegisterActivity.kt
│           │   │   │   └── AuthViewModel.kt
│           │   │   ├── passenger/
│           │   │   │   ├── PassengerHomeActivity.kt
│           │   │   │   ├── BookRideActivity.kt       # Full map + tap-to-place + reverse geocoding
│           │   │   │   ├── RideStatusActivity.kt     # Polls GET /bookings/{id} every 5s
│           │   │   │   ├── RideHistoryFragment.kt
│           │   │   │   └── PassengerViewModel.kt
│           │   │   ├── driver/
│           │   │   │   ├── DriverHomeActivity.kt
│           │   │   │   ├── DriverDashboardFragment.kt
│           │   │   │   ├── DriverRequestsFragment.kt
│           │   │   │   ├── DriverStatusFragment.kt   # Hardcoded "online" — no toggle yet
│           │   │   │   ├── RideRequestActivity.kt    # Accept / Reject (uses Intent extras for booking data)
│           │   │   │   ├── ActiveRideActivity.kt     # Complete ride only — no Start Ride yet
│           │   │   │   ├── RideRequestsAdapter.kt
│           │   │   │   └── DriverViewModel.kt
│           │   │   ├── admin/
│           │   │   │   ├── AdminDashboardActivity.kt
│           │   │   │   ├── ManageUsersActivity.kt
│           │   │   │   ├── UsersAdapter.kt
│           │   │   │   ├── PendingDriversAdapter.kt
│           │   │   │   └── AdminViewModel.kt
│           │   │   └── theme/
│           │   │       ├── Color.kt
│           │   │       ├── Theme.kt
│           │   │       └── Type.kt
│           │   │
│           │   └── utils/
│           │       ├── Constants.kt               # BASE_URL + all URL variants
│           │       └── Resource.kt                # Sealed class: Loading/Success/Error
│           │
│           ├── res/
│           │   ├── layout/                        # 18 XML layout files
│           │   ├── drawable/                      # Icons, backgrounds
│           │   ├── values/
│           │   │   ├── strings.xml
│           │   │   ├── colors.xml
│           │   │   └── themes.xml
│           │   ├── menu/                          # Toolbar/overflow menus
│           │   └── xml/
│           │       ├── network_security_config.xml  # Allows HTTP to 192.168.0.101
│           │       └── backup_rules.xml
│           │
│           └── AndroidManifest.xml                # ⚠️ Maps API key hardcoded here
│
├── google-services.json                           # Firebase config (in repo — should be gitignored)
├── build.gradle.kts                               # Project-level Gradle
├── settings.gradle.kts
├── gradle.properties
└── docs/                                          # Project documentation
```

---

## Database — Table Overview

| Table | Purpose |
|-------|---------|
| `users` | All users: passengers, drivers, admin. Columns: id, name, email, password, role, is_active |
| `driver_info` | Extended driver details: user_id (FK), license_number, vehicle_number, status (pending/approved/rejected), current_lat, current_lng |
| `bookings` | All ride records. status ENUM: `requested`, `accepted`, `in_progress`*, `completed`, `cancelled`* |
| `booking_logs` | Status change history per booking (write-only — no read endpoint exists) |
| `fcm_tokens` | FCM device tokens per user |

> `*` = ENUM value exists in DB but no API route can set it — these states are currently unreachable.

> `driver_info.is_online` column does **not** exist yet — required for the Driver Online/Offline Toggle feature.

---

## API Base URL (Local Development)

```
http://10.0.2.2/ptoda_booking_api/          (Android Emulator → localhost)
http://192.168.0.101/ptoda_booking_api/      (Physical device → PC LAN IP — current active)
http://localhost/ptoda_booking_api/          (Direct browser/Postman on PC)
```

---

## Known Structural Issues

| Issue | Detail |
|-------|--------|
| `routes/api.php` missing | Listed in old docs but was never created; routing is inline in `index.php` |
| `PassengerController.php` missing | Listed in old docs but was never created; passenger booking routes handled by `BookingController` |
| `AuthController.php` missing | Auth routes (`/auth/register`, `/auth/login`) are handled inline in `index.php`, not in a dedicated controller |
| `database/schema.sql` missing | Referenced in README setup instructions but file is absent; use `seed.sql` for schema reference |
| `check_admin.php` + `fix_admin.php` in web root | Debug utilities that expose admin password hash and allow password reset without authentication — **must be deleted** |
