# MBPTODABookingApp — Project Audit

**Date:** 2026-06-21 (updated from 2026-06-09)
**Auditor:** Claude Code (Sonnet 4.6)
**Branch:** master

---

## 1. Project Overview

| Property | Value |
|----------|-------|
| App Name | PTODA Booking App |
| Package | `com.example.mbptodabookingapp` |
| Language | Kotlin |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 (Android 16) |
| Architecture | MVVM + Repository |
| Build System | Gradle KTS + `libs.versions.toml` |
| Backend | PHP + XAMPP + MySQL (local) |
| Backend Path | `C:\xampp\htdocs\ptoda_booking_api\` |

---

## 2. File & Module Inventory

### 2.1 Source Files (Kotlin / Android)

| Category | Count |
|----------|-------|
| Kotlin files (total) | 44 |
| Activities | 11 |
| Fragments | 4 |
| ViewModels | 4 |
| Repositories | 5 |
| Data models | 8 |
| Layouts (XML) | 18 |

### 2.2 Module Structure

```
app/src/main/java/com/example/mbptodabookingapp/
├── PTODAApplication.kt
├── MainActivity.kt
├── data/
│   ├── api/            ApiClient, ApiService, ApiResponse
│   ├── local/          PrefsManager
│   ├── models/         User, AuthModels, Booking, DriverModels, AdminModels, FcmModels
│   └── repository/     Base, Auth, Booking, User, Admin
├── services/
│   └── PTODAFirebaseMessagingService.kt
├── ui/
│   ├── auth/           LoginActivity, RegisterActivity, AuthViewModel
│   ├── passenger/      PassengerHomeActivity, BookRideActivity, RideStatusActivity,
│   │                   RideHistoryFragment, PassengerViewModel
│   ├── driver/         DriverHomeActivity, DriverDashboardFragment,
│   │                   DriverRequestsFragment, DriverStatusFragment,
│   │                   RideRequestActivity, ActiveRideActivity,
│   │                   RideRequestsAdapter, DriverViewModel
│   ├── admin/          AdminDashboardActivity, ManageUsersActivity,
│   │                   UsersAdapter, PendingDriversAdapter, AdminViewModel
│   └── theme/          Color, Theme, Type
└── utils/
    ├── Constants.kt
    └── Resource.kt
```

### 2.3 PHP Backend Source Files

```
C:\xampp\htdocs\ptoda_booking_api\
├── index.php                     # Entry point + all route definitions
├── config/
│   ├── config.php                # JWT secret, FCM key, app constants
│   └── database.php              # PDO connection singleton
├── helpers/
│   ├── JWT.php                   # HS256 encode/decode
│   ├── Response.php              # json_response() helper
│   └── FCM.php                   # Firebase push sender (cURL)
├── middleware/
│   └── AuthMiddleware.php        # Bearer token verifier
├── models/
│   ├── User.php
│   ├── Booking.php
│   └── Admin.php
├── controllers/
│   ├── BookingController.php
│   ├── DriverController.php
│   └── AdminController.php
├── database/
│   └── seed.sql
├── uploads/drivers/
└── logs/
```

> **Note:** `routes/api.php` and `PassengerController.php` listed in `PROJECT_STRUCTURE.md` do not exist. Auth routes are handled inline in `index.php`.

---

## 3. Dependencies

### 3.1 AndroidX / Jetpack

| Library | Version |
|---------|---------|
| core-ktx | 1.18.0 |
| lifecycle-viewmodel-ktx | 2.10.0 |
| lifecycle-livedata-ktx | 2.10.0 |
| activity-compose | 1.13.0 |
| recyclerview | 1.3.2 |

### 3.2 Compose BOM

`2024.09.00` — ui, material3, ui-tooling-preview

### 3.3 Firebase BOM

`33.13.0` — analytics, auth, firestore, messaging (FCM)

### 3.4 Networking

| Library | Version |
|---------|---------|
| Retrofit | 2.9.0 |
| Retrofit Gson Converter | 2.9.0 |
| OkHttp Logging Interceptor | 4.12.0 |

### 3.5 Maps & Location

| Library | Version |
|---------|---------|
| play-services-maps | 18.2.0 |
| play-services-location | 21.2.0 |

### 3.6 Coroutines

`kotlinx-coroutines-android` 1.7.3

---

## 4. API Surface

**Backend base URL (active):** `http://192.168.0.101/ptoda_booking_api/`

| Endpoint | Method | Auth | Role | Description |
|----------|--------|------|------|-------------|
| `/auth/register` | POST | No | — | Register passenger or driver |
| `/auth/login` | POST | No | — | Authenticate, returns JWT |
| `/bookings` | POST | Yes | Passenger | Create ride request |
| `/bookings` | GET | Yes | Role-filtered | List bookings |
| `/bookings/{id}` | GET | Yes | Owner/Admin | Get single booking |
| `/passenger/history` | GET | Yes | Passenger | Passenger ride history (duplicate of GET /bookings) |
| `/driver/requests` | GET | Yes | Driver | Pending ride requests |
| `/driver/accept/{id}` | POST | Yes | Driver | Accept ride |
| `/driver/reject/{id}` | POST | Yes | Driver | Reject ride |
| `/driver/complete/{id}` | POST | Yes | Driver | Complete ride |
| `/driver/location` | PUT | Yes | Driver | Update GPS coordinates |
| `/user/fcm-token` | PUT | Yes | Any | Register/refresh FCM token |
| `/admin/users` | GET | Yes | Admin | All registered users |
| `/admin/drivers/pending` | GET | Yes | Admin | Pending driver approvals |
| `/admin/bookings` | GET | Yes | Admin | All bookings |
| `/admin/driver/approve/{id}` | PUT | Yes | Admin | Approve driver |
| `/admin/driver/reject/{id}` | PUT | Yes | Admin | Reject driver |
| `/admin/user/activate/{id}` | PUT | Yes | Admin | Activate user |
| `/admin/user/deactivate/{id}` | PUT | Yes | Admin | Deactivate user |
| `/admin/user/{id}` | DELETE | Yes | Admin | Delete user permanently |

**Missing endpoints (exist in schema/Android code but no PHP route):**

| Endpoint | Needed By |
|----------|-----------|
| `POST /bookings/{id}/cancel` | Passenger cancel flow; `BookingStatus.CANCELLED` exists in Android but no route |
| `POST /driver/start/{id}` | `in_progress` status transition; ENUM value exists in DB but unreachable |

**Authentication:** Bearer JWT — stored in `PrefsManager`, injected by `ApiClient` interceptor. 401 clears session.

---

## 5. Navigation Structure

```
MainActivity (auth router)
├── Not logged in → LoginActivity → RegisterActivity
└── Logged in
    ├── PASSENGER → PassengerHomeActivity
    │   ├── Map + FAB
    │   ├── BookRideActivity → RideStatusActivity
    │   └── RideHistoryFragment
    ├── DRIVER → DriverHomeActivity
    │   ├── DriverDashboardFragment (map)
    │   ├── DriverRequestsFragment → RideRequestActivity
    │   └── DriverStatusFragment → ActiveRideActivity
    └── ADMIN → AdminDashboardActivity
        └── ManageUsersActivity
```

Navigation is Activity-based (no Navigation Component / NavGraph).

---

## 6. Permissions Declared

| Permission | Purpose |
|-----------|---------|
| `INTERNET` | API calls |
| `ACCESS_FINE_LOCATION` | GPS for map/booking |
| `ACCESS_COARSE_LOCATION` | Fallback location |
| `POST_NOTIFICATIONS` | FCM (Android 13+) |

---

## 7. Security Findings

### 7.1 Critical

| # | Issue | File | Risk |
|---|-------|------|------|
| S1 | **`check_admin.php` + `fix_admin.php` in web root with zero auth** | `ptoda_booking_api/check_admin.php`, `fix_admin.php` | `check_admin.php` dumps admin password hash; `fix_admin.php` resets admin password to `admin123` for any LAN caller. DELETE IMMEDIATELY. |
| S2 | JWT secret is the literal placeholder string `CHANGE_THIS_TO_A_LONG_RANDOM_SECRET_KEY` | `config/config.php:6` | Any attacker reading this file can forge valid JWTs for any user ID and role. |
| S3 | FCM server key is `YOUR_FCM_SERVER_KEY_HERE` | `config/config.php:11` | Every push notification has silently failed since day one. |
| S4 | Google Maps API key hardcoded in Manifest | `AndroidManifest.xml:28` | Key extractable from any compiled APK. |
| S5 | JWT stored in plain `SharedPreferences` | `data/local/PrefsManager.kt` | Readable on rooted devices; use `EncryptedSharedPreferences`. |

### 7.2 High

| # | Issue | File | Risk |
|---|-------|------|------|
| S6 | All traffic is plaintext HTTP | `utils/Constants.kt`, `network_security_config.xml` | JWT tokens, passwords, GPS coordinates travel unencrypted on Wi-Fi. |
| S7 | `HttpLoggingInterceptor` at `BODY` level in all build types | `data/api/ApiClient.kt:65-67` | Full request/response bodies (including JWT + passwords) logged to Logcat in production. |

### 7.3 Medium

| # | Issue | File | Risk |
|---|-------|------|------|
| S8 | Login reveals driver account existence on pending-approval accounts | `AuthController.php:91-99` | Error message leaks whether an email is a registered driver account. |
| S9 | CORS wildcard `*` | `index.php:7` | Any web origin can make API requests. |
| S10 | `$e->getMessage()` returned to callers in all environments | `index.php:22`, `database.php:28` | Internal error details exposed to API consumers. |
| S11 | No rate limiting on `/auth/login` | `index.php` | Brute-force login is unlimited. |
| S12 | No refresh token — 401 forcibly logs out | `data/api/ApiClient.kt` | Expiring JWT mid-session breaks active bookings. |

---

## 8. Architecture & Code Quality

### 8.1 Issues

| # | Issue | Severity | Status |
|---|-------|----------|--------|
| A1 | Booking coordinates typed as `String` instead of `Double` | **Critical** | OPEN — will cause NPE at runtime on map screens |
| A2 | ~~`PassengerViewModel` not found~~ | ~~Medium~~ | ✅ RESOLVED — file exists and is fully wired |
| A3 | ~~Map integration incomplete in `BookRideActivity`~~ | ~~Medium~~ | ✅ RESOLVED — full map with tap-to-place and reverse geocoding |
| A4 | `RideRequestsAdapter` shows no passenger name despite API returning it | Medium | OPEN — `passenger_name` missing from `Booking.kt` data class |
| A5 | `RideStatusActivity` shows no driver info after ride accepted | Medium | OPEN — `driver_name` dropped by Gson; no layout views for it |
| A6 | `GET /bookings/{id}` returns 403 for drivers on `requested` bookings | Medium | OPEN — `driver_id` is NULL pre-accept; ownership check in `BookingController.php:58-63` fails |
| A7 | `DriverController::rejectRide()` has no ownership check | Medium | OPEN — any driver can reject any booking |
| A8 | Polling in `RideStatusActivity` reads stale state pre-response | Low | OPEN — extra poll after terminal status |
| A9 | `Admin::getAllDrivers()` is dead code | Low | OPEN — defined in `Admin.php:17`, never called |
| A10 | `booking_logs` table is write-only | Low | OPEN — written but no endpoint reads it |
| A11 | GoogleMap methods called without null checks | Low | OPEN — NPE risk on rotation |
| A12 | No Navigation Component used | Low | OPEN — manual Intent navigation |

### 8.2 Build Config

| # | Issue | Notes |
|---|-------|-------|
| B1 | Base URL hardcoded to physical device IP | Should vary by `buildType` (debug/release/emulator) |
| B2 | AGP version 9.1.0 | Verify compatibility with target SDK 36 |

---

## 9. Bugs (Runtime)

| # | Bug | File | Impact |
|---|-----|------|--------|
| BUG-1 | Coordinate fields `String` — Gson produces `null` from JSON numbers, then `.toDouble()` throws NPE | `Booking.kt:28-32`, `ActiveRideActivity.kt:89-90`, `RideRequestActivity.kt:99-103` | **App crash** on map screens with real booking data |
| BUG-2 | `GET /bookings/{id}` returns 403 for drivers on unassigned bookings | `BookingController.php:58-63` | Workaround in place (Intent extras), but underlying gate is broken |
| BUG-3 | `DriverController::rejectRide()` has no ownership check | `DriverController.php:79` | Any driver can reject any other driver's booking |
| BUG-4 | Polling checks terminal status before API response arrives | `RideStatusActivity.kt:33-43` | One extra unnecessary poll after ride completes |
| BUG-5 | `ManageUsersActivity` shows no success feedback on admin actions | `ManageUsersActivity.kt:105-108` | Silent success; admin has no confirmation |
| BUG-6 | `passenger_name` from API silently dropped by Gson | `Booking.kt`, `RideRequestsAdapter` | Passenger name never shows in driver request list |
| BUG-7 | `PUT /user/fcm-token` uses inline `require_once` instead of controller | `index.php:100-109` | Inconsistent architecture; fragile if file moves |

---

## 10. Database Schema Gaps

| Gap | Detail |
|-----|--------|
| `driver_info.is_online` column missing | Required for Driver Online/Offline Toggle feature; not in schema |
| `in_progress` status unreachable | ENUM value in `bookings.status` but no route sets it |
| `cancelled` status unreachable | ENUM value exists, `BookingStatus.CANCELLED` in Android, but no `/cancel` route |
| `booking_logs` table write-only | Accumulated audit trail but no read endpoint |
| `Admin::getAllDrivers()` dead code | `Admin.php:17`, never called |

---

## 11. Testing Coverage

| Type | Status |
|------|--------|
| Unit tests | None |
| Instrumented tests | None |
| PHP unit tests | None |
| Test dependencies present | Yes (JUnit 4.13.2, Espresso 3.7.0) |

---

## 12. Recommendations (Priority Order)

| Priority | Action | Reason |
|----------|--------|--------|
| P0 | Delete `check_admin.php` and `fix_admin.php` from web root | Anyone on LAN can reset admin password to `admin123` right now |
| P1 | Replace `JWT_SECRET` placeholder in `config/config.php` | All JWTs are currently forgeable |
| P2 | Set real FCM Server Key in `config/config.php` | Push notifications have never worked |
| P3 | Fix `Booking.kt` coordinates from `String` → `Double`; remove `.toDouble()` call sites | App will crash on map screens with live data |
| P4 | Move Maps API key to `local.properties` + `BuildConfig` | Key exposed in every APK build |
| P5 | Set `HttpLoggingInterceptor` to `NONE` for release builds | JWT + passwords logged to Logcat in production |
| P6 | Add `POST /bookings/{id}/cancel` endpoint + Android Cancel button | Passenger cannot cancel a ride |
| P7 | Add `POST /driver/start/{id}` endpoint + "Start Ride" button | `in_progress` status is unreachable |
| P8 | Add `passenger_name` and `driver_name` fields to `Booking.kt` | Driver sees no passenger name; passenger sees no driver info |
| P9 | Fix `BookingController.php:58-63` ownership check to allow drivers on `requested` bookings | Underlying gate is broken even though workaround exists |
| P10 | Add ownership check in `DriverController::rejectRide()` | Any driver can reject another driver's booking |
| P11 | Migrate `PrefsManager` to `EncryptedSharedPreferences` | JWT readable on rooted devices |
| P12 | Add `is_online` column to `driver_info` | Prerequisite for Online/Offline toggle |
| P13 | Enable TLS on backend, update base URL to `https://` | All traffic currently unencrypted |

---

*Updated by Claude Code on 2026-06-21. Based on full static analysis of Android source + PHP backend source.*
