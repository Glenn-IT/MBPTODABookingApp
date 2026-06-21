# MBPTODABookingApp — Lackings & Needed Improvements Audit

**Date:** 2026-06-21 (updated from 2026-06-09)
**Based on:** Full static analysis of Android Kotlin source + PHP backend (`ptoda_booking_api/`)
**Branch:** master

---

## Summary

| Category | Total Items | Critical | High | Medium | Low |
|----------|-------------|----------|------|--------|-----|
| Immediate Security (must fix now) | 3 | 3 | — | — | — |
| Incomplete Features | 7 | 2 | — | 4 | 1 |
| Missing Features | 7 | 3 | — | 3 | 1 |
| Security Gaps | 9 | 2 | 2 | 5 | — |
| Bugs (Runtime) | 7 | 1 | 3 | 2 | 1 |
| Architecture Issues | 7 | — | 1 | 4 | 2 |
| UI/UX Pending Work | 4 | — | — | 3 | 1 |
| Testing Gaps | 4 | 2 | — | 1 | 1 |
| Production Readiness | 4 | 2 | — | 2 | — |
| **Total** | **56** | **15** | **6** | **24** | **6** |

---

## 0. IMMEDIATE — Fix Before Any Other Work

These three issues are active threats on the current machine right now.

### 0.1 Delete `check_admin.php` and `fix_admin.php`
**Severity:** CRITICAL
**Files:** `C:\xampp\htdocs\ptoda_booking_api\check_admin.php`, `fix_admin.php`

`check_admin.php` dumps the admin account's password hash to any HTTP caller with zero authentication.
`fix_admin.php` unconditionally resets the admin password to `admin123` for any HTTP caller on the same LAN.

**What to do:** Delete both files immediately. They were debugging utilities that were never removed.

---

### 0.2 Replace the JWT Secret Placeholder
**Severity:** CRITICAL
**File:** `config/config.php:6`

```php
define('JWT_SECRET', 'CHANGE_THIS_TO_A_LONG_RANDOM_SECRET_KEY');
```

This is the literal placeholder. Any attacker who reads the config file (it may be in git history) can forge valid JWTs for any user ID — including admin role.

**What to do:** Replace with a random 256-bit hex string. Move it to a `.env` file outside the web root, add `.env` to `.gitignore`, and reference it via `getenv()`.

---

### 0.3 Set the Real FCM Server Key
**Severity:** CRITICAL
**File:** `config/config.php:11`

```php
define('FCM_SERVER_KEY', 'YOUR_FCM_SERVER_KEY_HERE');
```

Every single push notification in the app has silently failed since day one. Passengers never receive "driver accepted" alerts. Drivers never receive new booking notifications via FCM.

**What to do:** Get the server key from Firebase Console → Project Settings → Cloud Messaging and set it here.

---

## 1. Incomplete Features

### 1.1 Full Ride Booking Flow — NOT TESTED ON DEVICE
**Severity:** Critical

The passenger → driver → complete ride flow has never been tested end-to-end on a physical device. The API was Postman-tested, but the Android app flow (booking → status polling → driver accept → complete) has no confirmed test result. FCM notifications during this flow have also never worked (see 0.3 above).

**What to do:** Run the full flow on two physical devices simultaneously after fixing the FCM key.

---

### 1.2 Maps API Cloud Console Enable — Manually Required
**Severity:** Medium

"Enable Maps SDK for Android" in Google Cloud Console may not have been confirmed. If the Maps SDK is not enabled, map tiles will show errors at runtime.

**What to do:** Go to `console.cloud.google.com` → APIs & Services → Enable "Maps SDK for Android".

---

### 1.3 Integration Test — Register + Login on Physical Device
**Severity:** Medium

Register and login are confirmed working via Postman but a confirmed manual test on a physical device has not been recorded for all three role types.

**What to do:** Install APK on phone, log in with admin, passenger, and driver test accounts, verify correct role routing.

---

### 1.4 Admin User Management — Not Tested from Android
**Severity:** Medium

Admin endpoints were tested via Postman, but the `AdminDashboardActivity` + `ManageUsersActivity` flow (approve driver, deactivate user, delete user) has not been tested from the app itself.

**What to do:** Log in as `admin@ptoda.local`, run through all admin actions in the app.

---

### 1.5 Phase 6 Driver Screens — Manual Tests Pending
**Severity:** Medium

Phase 6 code is complete and builds, but manual test items are unchecked:
- Active Ride banner behavior after app close/reopen
- Status tab completed count matching dashboard count
- Map location updates working on device
- Resume Ride → `ActiveRideActivity` with correct data

**What to do:** Run Phase 6 manual test checklist on a physical device with driver + passenger accounts.

---

### 1.6 Estimated Fare Display — Static Placeholder
**Severity:** Low

`tvEstimatedFare` in `activity_book_ride.xml` shows "Calculating…" with no backend fare calculation and no logic to populate it.

**What to do:** Either implement fare calculation (distance × rate formula) or replace with "Fare: To be determined" until the backend supports it.

---

### 1.7 `booking_logs` Table — Write-Only Audit Trail
**Severity:** Low

`Booking::logStatusChange()` (`Booking.php:129-142`) writes status changes to `booking_logs` but no endpoint ever reads from it. The audit trail accumulates silently.

**What to do:** Either add `GET /admin/bookings/{id}/logs` or accept the table as future-only and document that decision.

---

## 2. Missing Features

### 2.1 Passenger Cancel Ride — No Backend, No UI
**Severity:** Critical

`BookingStatus.CANCELLED` exists in `Constants.kt`. `RideHistoryFragment.kt:133` displays it. But:
- No `POST /bookings/{id}/cancel` route in `index.php`
- No Cancel button in `activity_ride_status.xml`
- No `ApiService.kt` call for cancel
- The `cancelled` ENUM value in the DB is unreachable at runtime

**What to do:**
1. Add `POST /bookings/{id}/cancel` in PHP — set status to `cancelled`, verify caller is the passenger owner, only allow when status is `requested`
2. Add Cancel button in `RideStatusActivity` (visible only while status = `requested`)
3. Add `cancelBooking(id)` to `BookingRepository` and `PassengerViewModel`

---

### 2.2 In-Progress Status Transition — No Route, No UI
**Severity:** Critical

The `accepted → in_progress → completed` lifecycle is in the DB ENUM but only `completed` is triggerable:
- No `POST /driver/start/{id}` endpoint
- `ActiveRideActivity` has only a Complete button, no "Start Ride" action
- Status jumps from `accepted` directly to `completed`

**What to do:** Add `POST /driver/start/{id}` PHP endpoint. Add "Start Ride" button in `ActiveRideActivity`. Transition: `accepted → in_progress` on start, `in_progress → completed` on complete.

---

### 2.3 Forgot Password / Account Recovery
**Severity:** Critical

No password reset flow exists in the app or API:
- No `POST /auth/forgot-password` or `POST /auth/reset-password` endpoints
- String resources for "Forgot Password" defined but no `TextView` in `activity_login.xml` and no handler in `LoginActivity.kt`

**What to do:** Add a Forgot Password link to the login screen. Implement an admin-side password reset at minimum, or email-based OTP via Firebase Auth.

---

### 2.4 Passenger Cannot See Driver Info After Accept
**Severity:** Medium

`activity_ride_status.xml` shows only status, booking ID, pickup, and dropoff. When a driver accepts, `GET /bookings/{id}` JOIN already returns `driver_name` and `driver_email`, but:
- `Booking.kt` data class has no `driver_name` / `driver_email` fields — Gson silently drops them
- No layout views display this information to the passenger

**What to do:**
1. Add `driver_name`, `driver_email`, `driver_vehicle_number` fields to `Booking.kt`
2. Add corresponding `TextView` widgets to `activity_ride_status.xml`
3. Populate them in `RideStatusActivity` when status becomes `accepted`

---

### 2.5 Driver Online/Offline Toggle
**Severity:** Medium

`DriverStatusFragment` shows hardcoded "You are online" text but:
- No toggle widget
- No `PUT /driver/status` endpoint
- No `is_online` column in `driver_info` table
- A driver cannot go offline to stop receiving new requests

**What to do:**
1. Add `is_online TINYINT(1) DEFAULT 1` column to `driver_info` via migration
2. Add `PUT /driver/status` endpoint
3. Add toggle switch in `DriverStatusFragment`
4. Filter `GET /driver/requests` to only return bookings to online drivers

---

### 2.6 Ride Rating / Feedback
**Severity:** Medium

After a ride completes, passengers cannot rate drivers. No `ratings` table, no `POST /bookings/{id}/rate` endpoint, no rating dialog in the passenger app.

**What to do:** Add `ratings` table (`booking_id`, `driver_id`, `passenger_id`, `rating`, `comment`). Add a rating dialog shown in `RideStatusActivity` when status transitions to `completed`.

---

### 2.7 No Profile View / Edit
**Severity:** Low

Users (passenger and driver) have no way to view or edit their profile information. No `GET /user/profile` or `PUT /user/profile` endpoint exists.

**What to do:** Add a Profile tab or settings screen. Implement at minimum a read-only profile view showing name, email, and (for drivers) vehicle number.

---

## 3. Security Gaps

### 3.1 Google Maps API Key Exposed in APK
**Severity:** Critical
**File:** `app/src/main/AndroidManifest.xml:28`

Key `AIzaSyD4we-oa3wABCTt2iKZEyOnzoF-5pfb6mk` is in plaintext and extractable from any compiled APK via `apktool`.

**What to do:**
1. Move the key to `local.properties` (already gitignored)
2. Reference it in `app/build.gradle.kts` via `manifestPlaceholders["MAPS_API_KEY"] = ...`
3. Use `${MAPS_API_KEY}` in `AndroidManifest.xml`
4. Restrict the key in Google Cloud Console to your app's SHA-1 fingerprint and package name

---

### 3.2 JWT Stored in Plain SharedPreferences
**Severity:** Critical
**File:** `data/local/PrefsManager.kt`

JWT stored unencrypted; readable on rooted devices or via ADB backup.

**What to do:** Replace `SharedPreferences` with `EncryptedSharedPreferences` (Jetpack Security library).

```kotlin
// build.gradle.kts
implementation("androidx.security:security-crypto:1.1.0-alpha06")

// PrefsManager.kt
val prefs = EncryptedSharedPreferences.create(
    context, "secure_prefs",
    MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

---

### 3.3 Full Body Logging in All Build Types
**Severity:** High
**File:** `data/api/ApiClient.kt:65-67`

`HttpLoggingInterceptor` is set to `BODY` level unconditionally. JWT tokens and plaintext passwords are logged to Logcat in release builds.

**What to do:**
```kotlin
val level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
```

---

### 3.4 All API Traffic is Plaintext HTTP
**Severity:** High
**File:** `utils/Constants.kt`, `res/xml/network_security_config.xml`

JWT tokens, passwords, and GPS coordinates travel over HTTP on the local network. Anyone on the same Wi-Fi can intercept credentials.

**What to do:** Set up HTTPS on the backend (self-signed cert for LAN or Let's Encrypt for VPS). Update base URL to `https://`. Remove the cleartext exception in `network_security_config.xml`.

---

### 3.5 Login Leaks Driver Account Existence
**Severity:** Medium
**File:** `AuthController.php:91-99`

When a driver's credentials are correct but account is pending approval, error message `"Your driver account is pending admin approval."` reveals the email is a registered driver account.

**What to do:** Use a generic `"Invalid email or password."` for all failed login scenarios.

---

### 3.6 CORS Wildcard
**Severity:** Medium
**File:** `index.php:7`

`Access-Control-Allow-Origin: *` permits any web origin to call the API.

**What to do:** Restrict to specific origin before production deployment.

---

### 3.7 Error Details Exposed to Callers
**Severity:** Medium
**File:** `index.php:22`, `config/database.php:28`

`$e->getMessage()` is returned in JSON responses in all environments. Comments say "Remove in production" but `APP_ENV` is never checked.

**What to do:** Gate on `APP_ENV`: return full message in development, generic `"Server error"` in production.

---

### 3.8 No Rate Limiting on Login
**Severity:** Medium
**File:** `index.php`

Brute-force login attempts are unlimited. No IP throttling exists anywhere.

**What to do:** Add rate limiting — at minimum limit `/auth/login` to 5 attempts per minute per IP, implemented in `index.php` or Apache `.htaccess`.

---

### 3.9 No Token Refresh — Aggressive 401 Logout
**Severity:** Medium
**File:** `data/api/ApiClient.kt`

When a JWT expires, the interceptor clears session data and forces re-login. No refresh token mechanism exists.

**What to do:** Implement `POST /auth/refresh`. Store a long-lived refresh token. On 401, attempt a token refresh before clearing the session.

---

## 4. Bugs (Runtime)

### 4.1 Coordinate Fields Typed as `String` → NPE Crash
**Severity:** Critical
**Files:** `data/models/Booking.kt:28-32`, `ActiveRideActivity.kt:89-90`, `RideRequestActivity.kt:99-103`

`pickup_lat`, `pickup_lng`, `dropoff_lat`, `dropoff_lng` are declared `String` in the data class. MySQL `DECIMAL(10,7)` values serialize as JSON numbers (e.g., `14.5995`). Gson cannot coerce a JSON number to `String` — fields become `null`. `.toDouble()` calls on null throw `NullPointerException` at runtime.

**Fix:** Change all four coordinate fields in `Booking.kt` to `Double`. Remove all `.toDouble()` call sites in `ActiveRideActivity.kt` and `RideRequestActivity.kt`.

---

### 4.2 `GET /bookings/{id}` Returns 403 for Drivers on Unassigned Bookings
**Severity:** High
**File:** `BookingController.php:58-63`

Ownership check is `$booking['driver_id'] == $auth['user_id']`. When a booking is still `requested`, `driver_id` is `NULL`, so the check fails and every driver gets 403. A workaround exists in `RideRequestActivity.kt` (uses Intent extras instead of calling this endpoint), but the PHP gate is fundamentally wrong.

**Fix:** Allow drivers to read `requested` bookings (where `driver_id` is NULL) — or explicitly document that `RideRequestActivity` must always use Intent extras and never call this endpoint.

---

### 4.3 `rejectRide()` Has No Ownership Check
**Severity:** High
**File:** `DriverController.php:79`

Any authenticated driver can reject any `requested` booking — not just their own. No check verifies the calling driver was associated with the request.

**Fix:** Add ownership or assignment check before allowing rejection.

---

### 4.4 `passenger_name` Dropped by Gson
**Severity:** High
**Files:** `Booking.kt`, `RideRequestsAdapter`

`Booking::getPendingRequests()` (`Booking.php:82`) returns `passenger_name` in the JSON, but `Booking.kt` has no such field. Gson silently drops it. The `RideRequestsAdapter` displays no passenger name.

**Fix:** Add `passenger_name: String?` to `Booking.kt`. Display it in `item_ride_request.xml`.

---

### 4.5 Polling Reads Stale State After Terminal Status
**Severity:** Medium
**File:** `RideStatusActivity.kt:33-43`

`pollRunnable` checks the terminal status from the previous ViewModel value before the API response arrives, causing one unnecessary extra poll after the ride completes.

**Fix:** Check terminal status inside the API response observer, not before posting the next poll.

---

### 4.6 `ManageUsersActivity` Shows No Success Feedback
**Severity:** Medium
**File:** `ManageUsersActivity.kt:105-108`

The `actionState` observer handles only `Resource.Error`. On success, the list silently refreshes via LiveData but no toast or snackbar is shown to the admin.

**Fix:** Add a success toast in the observer for `Resource.Success`.

---

### 4.7 `/user/fcm-token` Route Uses Inline Logic Instead of Controller
**Severity:** Low
**File:** `index.php:100-109`

This route loads `AuthMiddleware` and `User` inline unlike every other route that delegates to a controller. Fragile and architecturally inconsistent.

**Fix:** Move to a `UserController::updateFcmToken()` method.

---

## 5. Architecture Issues

### 5.1 Booking Coordinate Types Are `String` Instead of `Double`
**Severity:** High (see also Bug 4.1 — same root cause)
**File:** `data/models/Booking.kt`

Covered in Bug 4.1. The fix here is a data-model change that also closes the runtime crash.

---

### 5.2 Polling Instead of FCM Data Messages
**Severity:** Medium
**File:** `ui/passenger/RideStatusActivity.kt`

`RideStatusActivity` polls `GET /bookings/{id}` every 5 seconds. This burns battery and creates continuous server load. FCM is already wired in the backend — when a driver accepts/completes, a push is sent — but the Android `PTODAFirebaseMessagingService` doesn't update the booking status observable.

**What to do:** Have `PTODAFirebaseMessagingService` post a local broadcast or update a shared LiveData when a booking-status FCM message arrives. `RideStatusActivity` observes this instead of polling.

---

### 5.3 No Navigation Component — Back-Stack Issues
**Severity:** Medium

Navigation is done entirely via `startActivity()` with manual Intent flags. No deep link support. Back stack is managed inconsistently. Type-unsafe Intent string keys cause issues (see BUG-016 in the old bug log).

---

### 5.4 Hardcoded Base URL — No BuildType Switching
**Severity:** Medium
**File:** `utils/Constants.kt`

Four base URL constants exist but the active one is a hardcoded assignment. Switching environments requires a code edit + rebuild.

**What to do:**
```kotlin
// app/build.gradle.kts
buildTypes {
    debug {
        buildConfigField("String", "BASE_URL", "\"http://10.0.2.2/ptoda_booking_api/\"")
    }
    release {
        buildConfigField("String", "BASE_URL", "\"https://your-production-domain.com/api/\"")
    }
}
```

---

### 5.5 `Admin::getAllDrivers()` Dead Code
**Severity:** Low
**File:** `Admin.php:17`

Defined but never called by any controller or route. Either add a route for it or delete it.

---

### 5.6 GoogleMap Null Safety
**Severity:** Low
**File:** Multiple Activity files

`GoogleMap.animateCamera()` and marker operations called without null checks. Risk of NPE on screen rotation.

**What to do:** Guard all map operations with `map?.let { ... }`.

---

### 5.7 Fragment Lifecycle Management
**Severity:** Low
**File:** `ui/passenger/PassengerHomeActivity.kt`

`RideHistoryFragment` is added lazily. `findFragmentByTag()` check prevents duplicate creation, but `show`/`hide` lifecycle management is absent — observers may fire stale updates.

**What to do:** Use `show`/`hide` for tab-switching fragments so they are created once and reused properly.

---

## 6. UI/UX Pending Work

### 6.1 Admin Tabs + ViewPager2 — Phase 7 Not Started
**Severity:** Medium

`AdminDashboardActivity` is a single-scroll screen. Roadmap planned `TabLayout + ViewPager2` with Overview, Bookings, and Users tabs.

**Files to create:** `AdminPagerAdapter.kt`, `AdminStatsFragment.kt`, `AdminBookingsFragment.kt`
**Files to edit:** `activity_admin_dashboard.xml`, `AdminDashboardActivity.kt`

---

### 6.2 RecyclerView Item Card Redesign — Phase 8 Not Started
**Severity:** Medium

`item_ride_request.xml`, `item_user.xml`, and `item_pending_driver.xml` are not wrapped in `MaterialCardView`. All other screens (Phases 2–6) use card-based layouts. These are visually inconsistent.

**What to do:** Wrap all three item layouts in `MaterialCardView` with 8dp corner radius and 4dp elevation.

---

### 6.3 Global Theme Consistency — Phase 9 Not Started
**Severity:** Medium

No global `ShapeAppearance` for buttons, no consistent `TextAppearance` hierarchy, Toolbar styles differ screen-to-screen.

**What to do:** Update `themes.xml` with button shape (8dp corners), headline typography scale, and toolbar color attributes.

---

### 6.4 End-to-End Manual Test Matrix — Phase 10 Not Started
**Severity:** Low

The full 14-item test matrix (login, register, book ride, accept, complete, admin actions) has not been executed. Zero test cases are marked as passed.

---

## 7. Testing Gaps

### 7.1 Zero Unit Tests (Android)
**Severity:** Critical

No unit test files exist. JUnit 4 dependency present but unused.

**High-value tests to write first:**
- `AuthViewModel` — login success, login failure, empty field validation
- `BookingRepository` — create booking success/error paths
- `Resource` sealed class behavior

---

### 7.2 Zero Instrumented Tests
**Severity:** Critical

No Espresso test files exist. No UI behavior is automatically verified.

**High-value tests to write first:**
- Login flow: enter credentials → correct navigation by role
- Register flow: driver fields show/hide on role selection

---

### 7.3 No API Contract Tests (PHP)
**Severity:** Medium

No PHPUnit tests. All PHP testing is manual via Postman. Any PHP change can silently break an endpoint.

**What to do:** Add PHPUnit tests for `AuthController`, `BookingController`, `DriverController`.

---

### 7.4 No CI/CD Pipeline
**Severity:** Low

No GitHub Actions or similar. No automated build verification on commit.

**What to do:** Add a GitHub Actions workflow that runs `./gradlew assembleDebug` and `./gradlew test` on every push.

---

## 8. Production Readiness

### 8.1 No Production Deployment Plan
**Severity:** Critical

Backend runs on XAMPP on a local Windows PC. App cannot be distributed outside the local Wi-Fi network.

**What to do:** Plan VPS deployment (DigitalOcean/Linode) with Nginx + PHP-FPM + MySQL. Set up a domain name and Let's Encrypt HTTPS certificate.

---

### 8.2 No Input Validation on Android Side
**Severity:** Critical

Client-side validation (empty fields, email format, password length, coordinate range) is missing. All validation relies on the backend returning error messages.

**What to do:** Add `TextInputLayout` error display for all registration and login fields. Validate before API calls.

---

### 8.3 Secrets in Config File — No `.env` Approach
**Severity:** Medium

JWT secret and FCM key are in `config/config.php`. If this file ends up in git history, credentials are exposed.

**What to do:** Move secrets to a `.env` file outside the web root. Add `.env` to `.gitignore`. Provide `.env.example` as a template.

---

### 8.4 No User-Friendly Error Messages
**Severity:** Medium

API errors surface as raw backend strings. No mapping from HTTP status codes to localized, friendly messages.

**What to do:** Create an error mapping in `BaseRepository` or a dedicated `ErrorHandler`. Map 401, 403, 404, 422, 500 to friendly strings in `strings.xml`.

---

## 9. Quick Wins (Easiest to Fix First)

| # | Item | Effort | Impact |
|---|------|--------|--------|
| Q1 | Delete `check_admin.php` and `fix_admin.php` | 5 min | **Critical** — active security threat |
| Q2 | Replace JWT secret placeholder in `config.php` | 10 min | **Critical** — all JWTs currently forgeable |
| Q3 | Set real FCM Server Key in `config.php` | 10 min | **Critical** — push notifications have never worked |
| Q4 | Fix `Booking.kt` coordinates `String` → `Double`, remove `.toDouble()` | 1 hour | **Critical** — prevents NPE crash on map screens |
| Q5 | Set `HttpLoggingInterceptor` to `NONE` for release | 15 min | High — stops logging tokens in production |
| Q6 | Add `passenger_name` to `Booking.kt` + show in adapter | 30 min | High — driver sees blank where passenger name should be |
| Q7 | Add success toast in `ManageUsersActivity` observer | 15 min | Medium — admin gets no feedback on actions |
| Q8 | Move Maps API key to `local.properties` + `BuildConfig` | 1 hour | High — key exposed in every APK |
| Q9 | Migrate `PrefsManager` to `EncryptedSharedPreferences` | 2 hours | High — JWT readable on rooted devices |
| Q10 | Wrap 3 RecyclerView items in `MaterialCardView` (Phase 8) | 30 min | Medium — visual consistency |

---

## 10. Items Verified as RESOLVED (Removed From Tracking)

These were in the previous audit but have been confirmed as completed:

| Item | What was claimed | What is true now |
|------|-----------------|-----------------|
| A2: PassengerViewModel missing | "May not exist or be a dead reference" | `PassengerViewModel.kt` fully exists and is correctly wired |
| A3: Map integration incomplete | "Placeholder with weight=0" | Full map with tap-to-place, reverse geocoding, and current-location fill |
| 2.7: No logout in Driver/Passenger Home | "No logout button visible" | Both `DriverHomeActivity.kt:142-155` and `PassengerHomeActivity.kt:157-170` have logout via options menu |
| 2.7: `AdminDashboardActivity.onResume` missing `fetchStats` | "Does not call fetchStats" | `AdminDashboardActivity.kt:40-42` has `override fun onResume() { loadStats() }` |
| 4.4: PassengerViewModel reference conflict | "May crash at runtime" | PassengerViewModel is fully implemented; no conflict |
| 4.7: RideHistoryFragment added multiple times | "No duplicate prevention" | `PassengerHomeActivity.kt:88-95` checks `findFragmentByTag("history") != null` before adding |

---

## 11. Recommended Completion Order

```
This session (security emergencies)
├── Q1  Delete check_admin.php + fix_admin.php      [immediate security]
├── Q2  Replace JWT_SECRET placeholder               [immediate security]
└── Q3  Set real FCM Server Key                     [immediate security]

This week (critical bugs)
├── Q4  Fix Booking.kt coordinate types             [prevents runtime crash]
├── Q5  Set logging NONE for release                [security]
├── Q6  Add passenger_name to Booking.kt            [missing data]
└── Q8  Move Maps API key to BuildConfig            [security]

Short-term (missing features)
├── 2.1 Passenger cancel ride                       [missing feature]
├── 2.2 In-progress status transition               [missing feature]
├── 2.4 Passenger sees driver info after accept     [missing feature]
├── 4.3 Fix rejectRide() ownership check            [bug]
└── 4.2 Fix BookingController 403 for drivers       [bug]

Before release
├── 2.3 Forgot password flow                        [missing feature]
├── 2.5 Driver online/offline toggle                [missing feature]
├── 3.2 EncryptedSharedPreferences                  [security]
├── 3.4 Enable HTTPS                                [security]
├── 8.1 VPS deployment plan                         [production]
├── 8.3 .env for PHP secrets                        [security]
├── 7.1 + 7.2 Unit + UI tests                       [testing]
└── 6.1–6.3 UI consistency phases                   [UX]
```

---

*Updated by Claude Code on 2026-06-21. Cross-referenced against full PHP backend source and Android Kotlin source tree.*
