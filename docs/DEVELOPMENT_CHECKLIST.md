# PTODA Booking System — Development Checklist

**Last updated:** 2026-06-21
**Branch:** master

> **Status Legend:**
> - `[x]` — Done
> - `[~]` — In progress / partially done
> - `[ ]` — Not started
> - `[!]` — Urgent / blocking

---

## Phase 1 — Environment Setup ✅

- [x] **1.1** Install and start XAMPP (Apache + MySQL running)
- [x] **1.2** Create project folder `C:\xampp\htdocs\ptoda_booking_api\`
- [x] **1.3** Create MySQL database `ptoda_db` via phpMyAdmin
- [x] **1.4** Set up Android Studio with a new Kotlin project (`MBPTODABookingApp`)
- [x] **1.5** Configure `local.properties` (SDK path) — not committed
- [x] **1.6** Add `google-services.json` to the Android `app/` folder
- [x] **1.7** Create Firebase project and link to Android app

---

## Phase 2 — Database Schema ✅

- [x] **2.1** `users` table — id, name, email, password, role, status, timestamps
- [x] **2.2** `driver_info` table — user_id FK, license_no, vehicle_no, approval_status, current_lat, current_lng
- [x] **2.3** `bookings` table — id, passenger_id, driver_id, pickup/dropoff address+lat+lng, status ENUM, timestamps
- [x] **2.4** `booking_logs` table — booking_id FK, old_status, new_status, changed_at
- [x] **2.5** `fcm_tokens` table — user_id FK, token, updated_at (UNIQUE on user_id)
- [x] **2.6** Seed data in `database/seed.sql` — test passengers, drivers, admin, sample bookings
- [ ] **2.7** Add `is_online TINYINT(1) DEFAULT 1` column to `driver_info` *(required for online/offline toggle — Phase 11)*
- [ ] **2.8** Verify `ON DELETE CASCADE` is set on all FK constraints (driver_info, fcm_tokens, booking_logs → users/bookings)

> **Note:** `database/schema.sql` does not exist in the repo. `seed.sql` contains both CREATE TABLE and INSERT statements and is the only DB setup file.

---

## Phase 3 — PHP REST API ✅ (core done, fixes needed)

### 3.1 Core Infrastructure ✅

- [x] **3.1.1** `config/database.php` — PDO connection (singleton, ERRMODE_EXCEPTION, FETCH_ASSOC)
- [x] **3.1.2** `config/config.php` — JWT_SECRET, FCM_SERVER_KEY, role/status constants
- [x] **3.1.3** `index.php` — entry point + all route definitions (regex-based dispatcher)
- [x] **3.1.4** `.htaccess` — `RewriteBase /ptoda_booking_api/` mod_rewrite routing
- [x] **3.1.5** `helpers/Response.php` — standardized `success()`, `error()`, `unauthorized()`, `forbidden()`, `notFound()`
- [x] **3.1.6** `helpers/JWT.php` — HS256 encode/decode with `hash_equals()` timing-safe verification + expiry check
- [x] **3.1.7** `helpers/FCM.php` — push notification via cURL (single + multi-device)
- [x] **3.1.8** `middleware/AuthMiddleware.php` — Bearer token validation + role whitelist check

### 3.2 Models ✅

- [x] **3.2.1** `models/User.php` — create, findByEmail, findById, emailExists, updateFCMToken, getFCMToken, updateStatus, getAll
- [x] **3.2.2** `models/Booking.php` — create, findById (with passenger/driver JOIN), updateStatus, getPendingRequests, getByPassenger, getByDriver, getAll, logStatusChange
- [x] **3.2.3** `models/Admin.php` — getPendingDrivers, approveDriver, rejectDriver, deactivateUser, activateUser, deleteUser

### 3.3 Controllers ✅

- [x] **3.3.1** `controllers/AuthController.php`
  - [x] `POST /auth/register` — validates fields, hashes password, inserts user + driver_info if driver
  - [x] `POST /auth/login` — verifies password, checks account status + driver approval, returns JWT
- [x] **3.3.2** `controllers/BookingController.php`
  - [x] `POST /bookings` — passenger creates ride request
  - [x] `GET /bookings` — role-filtered list (passenger/driver/admin)
  - [x] `GET /bookings/{id}` — single booking with passenger+driver JOIN
  - [x] `GET /passenger/history` — alias of GET /bookings (same method, role-filtered)
- [x] **3.3.3** `controllers/DriverController.php`
  - [x] `GET /driver/requests` — all `status=requested` bookings
  - [x] `POST /driver/accept/{id}` — sets accepted + driver_id, sends FCM push to passenger
  - [x] `POST /driver/reject/{id}` — sets rejected status
  - [x] `POST /driver/complete/{id}` — checks ownership, sets completed, sends FCM push to passenger
  - [x] `PUT /driver/location` — updates current_lat/lng in driver_info
- [x] **3.3.4** `controllers/AdminController.php`
  - [x] `GET /admin/users` — all users
  - [x] `GET /admin/drivers/pending` — drivers with approval_status=pending
  - [x] `GET /admin/bookings` — all bookings with passenger+driver names
  - [x] `PUT /admin/driver/approve/{id}` — sets approval_status=approved
  - [x] `PUT /admin/driver/reject/{id}` — sets approval_status=rejected
  - [x] `PUT /admin/user/deactivate/{id}` — sets status=inactive
  - [x] `PUT /admin/user/activate/{id}` — sets status=active
  - [x] `DELETE /admin/user/{id}` — permanently deletes user (cascades to related tables)

### 3.4 API Postman Testing ✅

- [x] **3.4.1** `POST /auth/register` (passenger + driver)
- [x] **3.4.2** `POST /auth/login` (all roles)
- [x] **3.4.3** Protected routes with/without Bearer token
- [x] **3.4.4** Full booking flow: create → accept → complete via Postman
- [x] **3.4.5** Admin driver approval flow
- [x] **3.4.6** Admin user activate / deactivate / delete

**Test accounts (seed.sql, password = `password` for all):**

| Email | Role | Status |
|-------|------|--------|
| `juan@test.com` | passenger | active |
| `maria@test.com` | passenger | active |
| `pedro@test.com` | driver | approved |
| `jose@test.com` | driver | pending |

> Admin account not in seed — create manually via phpMyAdmin or add to seed.sql.

**Working URLs:**

| Context | URL |
|---------|-----|
| Postman / browser on PC | `http://localhost/ptoda_booking_api/` |
| Android Emulator | `http://10.0.2.2/ptoda_booking_api/` |
| Physical device | Set via ⚙ Server dialog in the app — see `README.md → Changing the server URL` |

---

## Phase 4 — Android App ✅ (core done, fixes needed)

### 4.1 Networking Layer ✅

- [x] **4.1.1** `data/api/ApiClient.kt` — Retrofit + OkHttp auth interceptor (injects Bearer token, 401 clears session); base URL is read from `PrefsManager` on every call and Retrofit rebuilds if it changes
- [x] **4.1.2** `data/api/ApiService.kt` — all Retrofit interface methods
- [x] **4.1.3** `data/api/ApiResponse.kt` — generic `ApiResponse<T>` wrapper
- [x] **4.1.4** `data/local/PrefsManager.kt` — JWT token, user role, FCM token, and server URL via SharedPreferences (server URL stored in separate `ptoda_dev_prefs` file so it survives logout)

### 4.2 Data Models ✅

- [x] **4.2.1** `data/models/AuthModels.kt` — LoginRequest, RegisterRequest, LoginResponse, UserResponse
- [x] **4.2.2** `data/models/Booking.kt` — Booking data class
- [x] **4.2.3** `data/models/DriverModels.kt` — LocationUpdate, driver-specific models
- [x] **4.2.4** `data/models/AdminModels.kt` — AdminUser, PendingDriver
- [x] **4.2.5** `data/models/FcmModels.kt` — FcmTokenRequest

### 4.3 Repositories ✅

- [x] **4.3.1** `data/repository/BaseRepository.kt` — `safeApiCall` coroutine wrapper
- [x] **4.3.2** `data/repository/AuthRepository.kt` — login, register, fcmToken sync after login
- [x] **4.3.3** `data/repository/BookingRepository.kt` — createBooking, getBookings, getById
- [x] **4.3.4** `data/repository/UserRepository.kt` — updateLocation, updateFcmToken
- [x] **4.3.5** `data/repository/AdminRepository.kt` — getAllUsers, getBookings, driver approval

### 4.4 Auth Screens ✅

- [x] **4.4.1** `LoginActivity.kt` + `activity_login.xml` — includes ⚙ Server dialog for runtime URL changes (tap "⚙ Server" at bottom of login screen)
- [x] **4.4.2** `RegisterActivity.kt` + `activity_register.xml` (role selector shows/hides driver fields)
- [x] **4.4.3** `AuthViewModel.kt` — login/register LiveData flow
- [x] **4.4.4** `MainActivity.kt` — auth router: checks JWT + role, navigates to correct home screen

### 4.5 Passenger Screens ✅

- [x] **4.5.1** `PassengerHomeActivity.kt` — BottomNav shell + Google Map + FAB
- [x] **4.5.2** `BookRideActivity.kt` — full map with tap-to-place markers, reverse geocoding, current location button
- [x] **4.5.3** `RideStatusActivity.kt` — polls `GET /bookings/{id}` every 5s, stops on terminal status
- [x] **4.5.4** `RideHistoryFragment.kt` — ride history list
- [x] **4.5.5** `PassengerViewModel.kt`

### 4.6 Driver Screens ✅

- [x] **4.6.1** `DriverHomeActivity.kt` — BottomNav shell + Google Map
- [x] **4.6.2** `DriverDashboardFragment.kt` — welcome + stats + Active Ride banner
- [x] **4.6.3** `DriverRequestsFragment.kt` — pending requests RecyclerView
- [x] **4.6.4** `DriverStatusFragment.kt` — online status card (hardcoded "online" — toggle not yet built)
- [x] **4.6.5** `RideRequestActivity.kt` — shows booking details from Intent extras, accept/reject buttons
- [x] **4.6.6** `ActiveRideActivity.kt` — shows accepted booking on map, Complete Ride button
- [x] **4.6.7** `RideRequestsAdapter.kt`
- [x] **4.6.8** `DriverViewModel.kt` — requests LiveData + driverBookings LiveData (for active ride banner)

### 4.7 Admin Screens ✅

- [x] **4.7.1** `AdminDashboardActivity.kt` — user/pending/booking count cards, refreshes on onResume
- [x] **4.7.2** `ManageUsersActivity.kt` — user list with approve/activate/deactivate/delete actions
- [x] **4.7.3** `UsersAdapter.kt`, `PendingDriversAdapter.kt`
- [x] **4.7.4** `AdminViewModel.kt`

### 4.8 Firebase Cloud Messaging ✅

- [x] **4.8.1** `PTODAFirebaseMessagingService.kt` — `onNewToken` syncs to API, `onMessageReceived` shows notification with role-aware tap routing
- [x] **4.8.2** Registered in `AndroidManifest.xml` with `POST_NOTIFICATIONS` permission
- [x] **4.8.3** FCM token synced to API after login in `AuthRepository`

### 4.9 Google Maps ✅

- [x] **4.9.1** `play-services-maps` + `play-services-location` in `build.gradle.kts`
- [x] **4.9.2** Maps API key added to `AndroidManifest.xml`
- [x] **4.9.3** `SupportMapFragment` in passenger and driver screens
- [x] **4.9.4** `ACCESS_FINE_LOCATION` + `ACCESS_COARSE_LOCATION` permission requests
- [x] **4.9.5** `FusedLocationProviderClient` shows current location on map
- [x] **4.9.6** Tap-to-set-marker with `MapMode` enum (PICKUP/DROPOFF) + reverse geocoding in `BookRideActivity`

---

## Phase 5 — Integration Testing (partial)

- [x] **5.1** XAMPP Apache + mod_rewrite confirmed working — `RewriteBase /ptoda_booking_api/` in `.htaccess`
- [x] **5.2** Physical device network config — `network_security_config.xml` + firewall rule on PC
- [~] **5.3** Register + login flow on physical device — API confirmed via Postman; Android manual test on device pending
- [ ] **5.4** Full ride booking flow on two physical devices (passenger + driver simultaneously)
- [ ] **5.5** FCM notification delivery test — **blocked until FCM key is set (Phase 7.1.3)**
- [ ] **5.6** Admin management actions tested from Android app (not just Postman)
- [ ] **5.7** Driver Active Ride banner: accept ride → close app → reopen → banner appears
- [ ] **5.8** RideStatusActivity polling: passenger sees status change within 5s of driver accepting

---

## Phase 6 — UI/UX Migration (Phases 7–10 pending)

| Sub-phase | Name | Status |
|-----------|------|--------|
| Phase 0 | Analysis & Feasibility | ✅ Done — 2026-04-12 |
| Phase 1 | Color + String Resources | ✅ Done — 2026-04-12 |
| Phase 2 | Login Screen Upgrade | ✅ Done — 2026-04-12 |
| Phase 3 | Register Screen Polish | ✅ Done — 2026-04-12 |
| Phase 4 | Booking Screen Card UI | ✅ Done — 2026-04-12 |
| Phase 5 | Passenger BottomNav Shell | ✅ Done — 2026-04-12 |
| Phase 6 | Driver BottomNav Shell | ✅ Done — 2026-04-12 |
| Phase 7 | Admin Tabs + ViewPager2 | ⬜ Pending |
| Phase 8 | RecyclerView Card Redesign | ⬜ Pending |
| Phase 9 | Global Theme Consistency | ⬜ Pending |
| Phase 10 | End-to-End Manual Test Matrix | ⬜ Pending |

### Phase 6 → Phase 7: Admin Tabs + ViewPager2

- [ ] **6.7.1** Create `AdminPagerAdapter.kt`
- [ ] **6.7.2** Create `AdminStatsFragment.kt` (moves stats cards)
- [ ] **6.7.3** Create `AdminBookingsFragment.kt` (bookings list)
- [ ] **6.7.4** Update `activity_admin_dashboard.xml` → `TabLayout + ViewPager2`
- [ ] **6.7.5** Update `AdminDashboardActivity.kt` — wire pager + verify observers
- [ ] **6.7.6** Test: Stats load on Overview tab, Manage Users still accessible

### Phase 6 → Phase 8: RecyclerView Card Redesign

- [ ] **6.8.1** Wrap `item_ride_request.xml` in `MaterialCardView` (8dp radius, 4dp elevation)
- [ ] **6.8.2** Wrap `item_user.xml` in `MaterialCardView`
- [ ] **6.8.3** Wrap `item_pending_driver.xml` in `MaterialCardView`
- [ ] **6.8.4** Build passes + visual check ✅

### Phase 6 → Phase 9: Global Theme Consistency

- [ ] **6.9.1** Add global `ShapeAppearance` for buttons (8dp corners) to `themes.xml`
- [ ] **6.9.2** Apply `TextAppearance.MaterialComponents` headline hierarchy
- [ ] **6.9.3** Ensure all Toolbars use `@color/colorPrimary` consistently
- [ ] **6.9.4** Final color audit across all 11 screens

### Phase 6 → Phase 10: End-to-End Test Matrix

| Test Case | Expected Result | Pass? |
|-----------|-----------------|-------|
| Login (passenger) | → `PassengerHomeActivity` | ⬜ |
| Login (driver, approved) | → `DriverHomeActivity` | ⬜ |
| Login (admin) | → `AdminDashboardActivity` | ⬜ |
| Login (wrong password) | Error toast | ⬜ |
| Register (passenger) | Account created → login | ⬜ |
| Register (driver) | Account created, pending approval | ⬜ |
| Book a ride | Booking submitted → status screen | ⬜ |
| View ride history | Real data loads | ⬜ |
| View ride requests (driver) | Pending list loads | ⬜ |
| Accept ride request | Status → accepted | ⬜ |
| Complete ride | Status → completed | ⬜ |
| View admin stats | Counts load from API | ⬜ |
| Approve pending driver | Driver status → approved | ⬜ |
| Manage users (admin) | List loads, all actions work | ⬜ |

---

## Phase 7 — Critical Fixes (DO FIRST) 🔴

These are security threats or runtime crashes. Fix before any feature work.

### 7.1 Security — Immediate Threats

- [x] **7.1.1** **Delete `check_admin.php` from web root** — ✅ Deleted 2026-06-21

- [x] **7.1.2** **Delete `fix_admin.php` from web root** — ✅ Deleted 2026-06-21

- [x] **7.1.3** **Set a real JWT secret** in `config/config.php:6` — ✅ Set to 256-bit random hex 2026-06-21

- [!] **7.1.4** **Set the real FCM Server Key** in `config/config.php:11`
  - Replace `'YOUR_FCM_SERVER_KEY_HERE'` with the key from Firebase Console → Project Settings → Cloud Messaging
  - Until fixed: every push notification silently fails (driver never gets new booking alert, passenger never gets "driver accepted" alert)

- [!] **7.1.5** **Migrate FCM to HTTP v1 API** in `helpers/FCM.php`
  - The Legacy FCM API (`fcm.googleapis.com/fcm/send`) was shut down by Google in June 2025. It is dead.
  - Must migrate to HTTP v1: `https://fcm.googleapis.com/v1/projects/{project_id}/messages:send`
  - HTTP v1 uses a Service Account OAuth2 Bearer token instead of the old server key
  - Steps:
    1. Go to Firebase Console → Project Settings → Service Accounts → Generate new private key → download JSON
    2. Use the JSON to get an OAuth2 access token (via Google's `googleapis` library or a manual JWT flow)
    3. Rewrite `FCM::send()` to POST to the v1 URL with `Authorization: Bearer {access_token}`
    4. Update the message payload format (v1 uses a different JSON structure)

### 7.2 Runtime Bugs — Will Crash the App

- [x] **7.2.1** **Fix `Booking.kt` coordinate types** — ✅ Changed String→Double; removed .toDouble() in ActiveRideActivity; RideRequestActivity now uses getDoubleExtra. 2026-06-21

- [x] **7.2.2** **Add `passenger_name` to `Booking.kt`** — ✅ Added field; adapter now shows address without redundant "Pickup:/Dropoff:" prefix. 2026-06-21

- [x] **7.2.3** **Add driver info fields to `Booking.kt`** — ✅ Added driver_name/driver_email fields; driver card added to activity_ride_status.xml; RideStatusActivity shows card when status is accepted/in_progress/completed. 2026-06-21

### 7.3 Logic Bugs

- [x] **7.3.1** **Fix `ManageUsersActivity` success feedback** — ✅ Added success Toast + auto-refresh of the active tab. 2026-06-21

- [ ] **7.3.2** **Fix `rejectRide()` ownership check** — any authenticated driver can reject any `requested` booking. No check verifies the rejecting driver was associated with the request.
  - File: `DriverController.php:67-81`
  - Fix: Decide and document whether rejection is open to any driver (broadcast model) or restricted. If restricted, add a check.

- [x] **7.3.3** **Fix polling reads stale state** in `RideStatusActivity` — ✅ pollRunnable now always reschedules; observer cancels via removeCallbacks when terminal. 2026-06-21

- [x] **7.3.4** **Require `license_no` + `vehicle_no` for driver registration** — ✅ Added 422 validation in AuthController.php. 2026-06-21

### 7.4 Security — High Priority (but not instant threats)

- [x] **7.4.1** **Set `HttpLoggingInterceptor` to `NONE` for release builds** — ✅ Gated on BuildConfig.DEBUG; also enabled buildConfig in build.gradle.kts. 2026-06-21

- [ ] **7.4.2** **Move Maps API key to `local.properties` + `BuildConfig`**
  - File: `AndroidManifest.xml:28` (key `AIzaSyD4we-...` is hardcoded)
  - Fix:
    1. Add key to `local.properties`: `MAPS_API_KEY=AIzaSyD4we-...`
    2. In `app/build.gradle.kts`: `manifestPlaceholders["MAPS_API_KEY"] = localProperties["MAPS_API_KEY"]`
    3. In `AndroidManifest.xml`: `android:value="${MAPS_API_KEY}"`
    4. Restrict key in Google Cloud Console to this app's SHA-1 + package name

- [ ] **7.4.3** **Gate error detail output on `APP_ENV`**
  - Files: `index.php:22`, `config/database.php:27`
  - Fix: Replace `$e->getMessage()` with a conditional — return full message only when `APP_ENV === 'development'`, generic `"Server error."` otherwise.

---

## Phase 8 — Missing API Endpoints

These endpoints are needed for the Android app to have a complete booking lifecycle.

### 8.1 Passenger Cancel Ride ✅

- [x] **8.1.1** Add `POST /bookings/{id}/cancel` route to `index.php`
- [x] **8.1.2** Add `cancelBooking(int $bookingId)` to `BookingController.php` — passenger only, requested status only, logs status change
- [x] **8.1.3** Add `cancelBooking(id)` to `ApiService.kt`
- [x] **8.1.4** Add `cancelBooking(id)` to `BookingRepository.kt`
- [ ] **8.1.5** Test via Postman: cancel a `requested` booking, verify status = `cancelled`

### 8.2 Driver Start Ride (`in_progress` transition) ✅

- [x] **8.2.1** Add `POST /driver/start/{id}` route to `index.php`
- [x] **8.2.2** Add `startRide(int $bookingId)` to `DriverController.php` — driver ownership + accepted-only guard + FCM push to passenger
- [x] **8.2.3** Add `startRide(id)` to `ApiService.kt`
- [x] **8.2.4** Add `startRide(id)` to `BookingRepository.kt` and `DriverViewModel.kt`
- [ ] **8.2.5** Test via Postman: start an `accepted` booking, verify status = `in_progress`

### 8.3 Driver Online/Offline Toggle ✅

- [x] **8.3.1** `is_online TINYINT(1) DEFAULT 1` added via ALTER TABLE (live DB) + schema.sql updated
- [x] **8.3.2** Add `PUT /driver/status` route to `index.php`
- [x] **8.3.3** Add `updateOnlineStatus()` to `DriverController.php`
- [ ] **8.3.4** Update `GET /driver/requests` — filter to online drivers only (decided: left open — requests are broadcast to all drivers regardless of status)
- [x] **8.3.5** Add `updateDriverStatus(isOnline: Boolean)` to `ApiService.kt`, `BookingRepository.kt`, `DriverViewModel.kt`
- [ ] **8.3.6** Test via Postman: toggle offline, call PUT /driver/status with is_online=false

### 8.4 Minor API Cleanup ✅

- [x] **8.4.1** Fixed `BookingController.php` — any driver can now view `requested` bookings (driver_id is NULL); ownership check updated
- [x] **8.4.2** Moved `PUT /user/fcm-token` to new `UserController.php`; index.php cleaned up
- [ ] **8.4.3** `GET /admin/bookings/{id}/logs` — deferred (audit trail is write-only; add if needed later)

---

## Phase 9 — Missing Android Features

### 9.1 Passenger Cancel Ride UI ✅

- [x] **9.1.1** Cancel button added to `activity_ride_status.xml` — shown only when status = `requested`
- [x] **9.1.2** `cancelBooking()` added to `PassengerViewModel`; confirmation AlertDialog in `RideStatusActivity`
- [x] **9.1.3** On success: toast + finish()
- [ ] **9.1.4** Test: passenger cancels a `requested` booking; status updates correctly

### 9.2 Driver Start Ride UI ✅

- [x] **9.2.1** "Start Ride" button added to `activity_active_ride.xml` (gone by default)
- [x] **9.2.2** Wired → `viewModel.startRide(bookingId)` in `ActiveRideActivity`; `lastAction` tracks start vs complete
- [x] **9.2.3** Status-driven visibility: `accepted` → Start visible, `in_progress` → Complete visible; on start success, `fetchBooking()` refreshes both
- [ ] **9.2.4** Test: full flow `accepted → in_progress → completed`

### 9.3 Passenger Sees Driver Info After Accept ✅

- [x] **9.3.1** Driver card (`cardDriverInfo`, `tvDriverName`, `tvDriverEmail`) added in Phase 7 (7.2.3)
- [x] **9.3.2** `RideStatusActivity` shows the card when status is `accepted`/`in_progress`/`completed` and `driver_name != null`
- [ ] **9.3.3** Test: passenger status screen shows driver name after driver accepts

### 9.4 Driver Online/Offline Toggle ✅

- [x] **9.4.1** `SwitchMaterial` (`switchOnline`) added to `fragment_driver_status.xml` alongside new IDs `tvOnlineTitle`, `tvOnlineBody`, `cardOnlineStatus`
- [x] **9.4.2** Toggle wired → `viewModel.updateDriverStatus(isChecked)` in `DriverStatusFragment`
- [x] **9.4.3** Card background + title + body text update instantly on toggle (blue = online, grey = offline)
- [ ] **9.4.4** Test: toggle offline → re-open app → verify state persists on server

### 9.5 Forgot Password ✅ (partial)

- [x] **9.5.1** Decision: admin-side reset only (no email OTP backend yet)
- [ ] **9.5.2** `POST /auth/forgot-password` — deferred (no email server configured)
- [x] **9.5.3** "Forgot Password?" `TextView` (`tvForgotPassword`) added to `activity_login.xml`
- [x] **9.5.4** Click shows AlertDialog: "Contact your PTODA admin to reset your password."

### 9.6 Input Validation ✅

- [x] **9.6.1** `LoginActivity` — empty email/password + email format check (Patterns.EMAIL_ADDRESS)
- [x] **9.6.2** `RegisterActivity` — name, email format, password ≥ 6 chars, license/vehicle for drivers
- [x] **9.6.3** `BookRideActivity` — all 6 fields validated before API call
- [ ] **9.6.4** Test: each validation shows an inline TIL error (not a Toast)

---

## Phase 10 — Security Hardening ✅

- [x] **10.1** **Migrate `PrefsManager` to `EncryptedSharedPreferences`**
  - Added `androidx.security:security-crypto:1.1.0-alpha06` to `build.gradle.kts`
  - `PrefsManager.kt` now uses `MasterKey.Builder` + `EncryptedSharedPreferences.create()` with AES256-GCM/SIV; falls back to plain prefs if keystore unavailable
  - Dev config (server URL) kept in plain `ptoda_dev_prefs` — not sensitive

- [x] **10.2** **Rate limiting on `/auth/login`** (PHP)
  - New `helpers/RateLimiter.php` — file-based (JSON in `storage/rate_limit.json`), 5 attempts/60s per IP
  - Returns HTTP 429 on breach; counter resets on successful login
  - Called at the start of `AuthController::login()`

- [x] **10.3** **Move PHP secrets to a `.env` file**
  - `config/config.php` now reads via `parse_ini_file()` → `JWT_SECRET`, `FCM_SERVER_KEY`, `APP_ENV`; falls back to `getenv()` for server-set env vars
  - `.env` created (gitignored); `.env.example` committed as template
  - `.gitignore` updated: removed old `config/config.php` exclusion, added `.env` and `storage/`

- [~] **10.4** **Enable HTTPS on the backend** (deferred — production-only)
  - Note added in `Constants.kt` → replace `http://` with `https://` domain and remove cleartext exception from `network_security_config.xml` when deploying
  - Local dev continues to use HTTP

- [x] **10.5** **Restrict CORS origin** in `index.php`
  - `Access-Control-Allow-Origin` is `*` when `APP_ENV=development`, production domain when `APP_ENV=production`
  - Config load moved before headers so `APP_ENV` constant is available
  - Error detail (`'error'` field) also gated on `APP_ENV !== 'production'`

- [x] **10.6** **Fix generic login error message for driver account existence leak**
  - Pending-driver path now returns `401 "Invalid email or password."` (same as wrong password) instead of a distinct `403 "pending approval"` message, preventing account enumeration

---

## Phase 11 — Production Readiness ✅

- [x] **11.1** **User-friendly error messages (Android)** — ✅ 2026-06-21
  - `BaseRepository.parseApiError()` now maps HTTP codes to friendly strings (401/403/404/409/422/429/500) when the API body has no parseable message
  - Error string resources added to `strings.xml` (error_unauthorized, error_forbidden, error_not_found, error_unprocessable, error_too_many_requests, error_server, error_network)

- [x] **11.2** **Switch base URL by build type** — ✅ 2026-06-21
  - `build.gradle.kts` → debug `buildConfigField BASE_URL` = device URL; release = production placeholder
  - `Constants.kt` → `BASE_URL = BuildConfig.BASE_URL` (compile-time injection; ⚙ Server dialog still overrides at runtime)

- [x] **11.3** **Remove debug files and TODO comments** — ✅ 2026-06-21
  - `check_admin.php` + `fix_admin.php` deleted in Phase 7
  - `BookingController.php:40` TODO replaced with a note referencing checklist 7.1.5 (FCM migration blocker)
  - `data_extraction_rules.xml` boilerplate TODO replaced with correct EncryptedSharedPreferences backup exclusion rule

- [~] **11.4** **VPS deployment plan** — (documentation only, deferred until deployment)
  - Choose VPS provider (DigitalOcean, Linode, etc.)
  - Install Nginx + PHP-FPM + MySQL
  - Set up domain name + Let's Encrypt SSL certificate
  - Migrate DB from XAMPP to remote MySQL
  - Update debug `buildConfigField BASE_URL` in `build.gradle.kts` to production HTTPS URL; remove cleartext exception from `network_security_config.xml`

- [x] **11.5** **CI/CD pipeline basics** — ✅ 2026-06-21
  - `.github/workflows/ci.yml` created: `assembleDebug` + `test` on push/PR to master; JDK 17 + Gradle cache
  - PHP syntax check job included but gated (`if: false`) — activate by committing PHP sources under `backend/` in this repo

---

## Phase 12 — Testing ✅

### 12.1 Unit Tests (Android) ✅ — BUILD SUCCESSFUL 2026-06-21

Dependencies added: `mockk:1.13.9`, `kotlinx-coroutines-test`, `androidx.arch.core:core-testing`

- [x] **12.1.1** `AuthRepository` login/register paths (AuthViewModel delegates directly to repo) — `AuthRepositoryTest.kt`
  - login success → `Resource.Success` with token + user
  - login wrong password → `Resource.Error` with API message
  - login network failure → friendly "No internet connection" message
  - login HTTP 429 → body message preferred over generic fallback
  - register success → `Resource.Success` with user_id
  - register duplicate email → `Resource.Error` with API message
  - *Input validation (empty fields) is in LoginActivity — tested in 12.2.1*
- [x] **12.1.2** `BookingRepository` — `BookingRepositoryTest.kt`
  - createBooking success → `Resource.Success(booking_id)`
  - createBooking API error → `Resource.Error(message)`
  - createBooking IOException → friendly error
  - cancelBooking success/error/network paths
  - acceptRide success; rejectRide API error
- [x] **12.1.3** `Resource` sealed class + `BookingStatus.isTerminal()` — `ResourceTest.kt`
  - 10 test cases covering all status values and terminal-state helper
- [x] **12.1.4** `PrefsManager` — moved to instrumented tests (requires Android Keystore) — see 12.2

### 12.2 Instrumented Tests (Android) ✅ — 2026-06-21

Run on device/emulator: `./gradlew connectedDebugAndroidTest`

- [x] **12.2.1** `LoginActivityTest.kt` — input validation (no live server needed)
  - Empty email → tilEmail error shown
  - Invalid email format → tilEmail error shown
  - Valid email + empty password → tilPassword error shown
  - *Full navigation test (login → home) requires live server — covered by manual E2E matrix (6.10)*
- [x] **12.2.2** `RegisterActivityTest.kt` — driver fields toggle
  - Driver fields hidden by default (passenger selected)
  - Driver fields visible when "Driver" radio selected
  - Driver fields hidden again when "Passenger" re-selected
  - Empty name → tilName error; driver TIL visible when driver selected
- [x] **12.1.4 / 12.2.3** `PrefsManagerTest.kt` — 12 test cases
  - saveLoginData: token, role, userId, name all retrievable
  - isLoggedIn true after save, false before login
  - clearAll removes token + FCM token → isLoggedIn false
  - saveServerUrl + getServerUrl round-trip

### 12.3 PHP API Contract Tests (PHPUnit) ✅ — 2026-06-21

Requires: XAMPP running + seed data (`database/seed.sql`) + `composer install`

Setup: `cd C:\xampp\htdocs\ptoda_booking_api && composer install && vendor/bin/phpunit --testdox`

- [x] **12.3.1** `POST /auth/register` — `tests/ApiContractTest.php`
  - 201 with valid passenger data (new unique email)
  - 422 on missing required fields
  - 409 on duplicate email (`juan@test.com`)
  - 422 for driver without license_no / vehicle_no
- [x] **12.3.2** `POST /auth/login`
  - 200 + JWT token on valid credentials (`juan@test.com`)
  - 401 on wrong password (same generic message as wrong password — 10.6)
  - 422 on missing password field
  - 401 for non-existent account (same message — anti-enumeration)
- [x] **12.3.3** Protected route enforcement
  - 401 without token
  - 403 passenger token on driver-only endpoint
  - 403 driver token on admin-only endpoint
- [x] **12.3.4** `POST /bookings`
  - 201 + booking_id with passenger token
  - 403 with driver token
- [x] **12.3.5** `POST /driver/accept/{id}`
  - 200 + status updated on first accept
  - 409 on second accept (already accepted)

---

## Known Issues Log (open bugs)

| ID | Severity | File | Description | Phase |
|----|----------|------|-------------|-------|
| BUG-017 | ✅ Fixed | `Booking.kt` | Coordinates changed String→Double; .toDouble() calls removed | 7.2.1 |
| BUG-018 | ✅ Fixed | `Booking.kt` | `passenger_name` field added | 7.2.2 |
| BUG-019 | ✅ Fixed | `Booking.kt` | `driver_name`/`driver_email` added; driver card shown in RideStatusActivity | 7.2.3 |
| BUG-020 | 🟠 High | `DriverController.php:79` | `rejectRide()` has no ownership check (broadcast model — any driver can reject) | 7.3.2 |
| BUG-021 | ✅ Fixed | `AuthController.php` | Driver registration now requires license_no + vehicle_no | 7.3.4 |
| BUG-022 | ✅ Fixed | `RideStatusActivity.kt` | pollRunnable always reschedules; observer cancels on terminal status | 7.3.3 |
| BUG-023 | ✅ Fixed | `ManageUsersActivity.kt` | Success toast + list auto-refresh added | 7.3.1 |
| BUG-024 | 🟡 Medium | `BookingController.php:58-63` | `GET /bookings/{id}` returns 403 for drivers on `requested` bookings (`driver_id` is NULL) | 8.4.1 |
| BUG-025 | ✅ Fixed | `ApiClient.kt` | Logging now NONE in release builds via BuildConfig.DEBUG | 7.4.1 |

---

## Fixed Bugs (from `BUGS_AND_FIXES.md`)

| ID | Description | Fixed |
|----|-------------|-------|
| BUG-001 | XAMPP 403/404 — mod_rewrite not enabled | ✅ Phase 3 |
| BUG-002 | Android emulator cannot reach localhost — use `10.0.2.2` | ✅ Phase 4 |
| BUG-003 | JWT not attached to Retrofit requests — OkHttp interceptor added | ✅ Phase 4 |
| BUG-004 | PHP returns HTML errors instead of JSON — global error handler + Content-Type header | ✅ Phase 3 |
| BUG-005 | `google-services.json` in wrong directory | ✅ Phase 4 |
| BUG-006 | FCM token not updated on reinstall — `onNewToken` sync added | ✅ Phase 4 |
| BUG-007 | PDO errors suppressed — `ERRMODE_EXCEPTION` added | ✅ Phase 3 |
| BUG-008 | CORS error — CORS headers + OPTIONS preflight handler added | ✅ Phase 3 |
| BUG-009 | Ride status not real-time — 5s polling added to `RideStatusActivity` | ✅ Phase 4 |
| BUG-010 | Driver approval not checked on login | ✅ Phase 3 |
| BUG-011 | Admin approval flow incomplete — added pending list + reject endpoint | ✅ Phase 3 |
| BUG-012 | No activate endpoint — deactivated users permanently locked | ✅ Phase 3 |
| BUG-013 | No delete user endpoint | ✅ Phase 3 |
| BUG-014 | Driver had no way to find accepted booking after closing app — Active Ride banner added | ✅ Phase 6 |
| BUG-015 | Status tab completed count always 0 — now uses `driverBookings` LiveData | ✅ Phase 6 |
| BUG-016 | Ride request detail blank — now populated from Intent extras, not API call | ✅ Phase 6 |
| BUG-017 | PHP login response missing `status` field — added to response | ✅ Phase 3 |

---

## Environment Reference

| Setting | Value |
|---------|-------|
| Backend path | `C:\xampp\htdocs\ptoda_booking_api\` |
| Database name | `ptoda_db` |
| PC LAN IP | `192.168.0.101` |
| Active `BASE_URL` | `http://192.168.0.101/ptoda_booking_api/` |
| JWT expiry | 7 days (config/config.php) |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 (Android 16) |
| AGP version | 9.1.0 |
| Kotlin | latest in libs.versions.toml |

---

_Last updated: 2026-06-21 — Full audit applied. Phases 7–12 added based on code review of PHP backend + Android source._
