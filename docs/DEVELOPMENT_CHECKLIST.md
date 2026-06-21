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

- [!] **7.1.1** **Delete `check_admin.php` from web root** — exposes admin password hash to any LAN caller with zero auth
  - File: `C:\xampp\htdocs\ptoda_booking_api\check_admin.php`

- [!] **7.1.2** **Delete `fix_admin.php` from web root** — resets admin password to `admin123` for any LAN HTTP caller
  - File: `C:\xampp\htdocs\ptoda_booking_api\fix_admin.php`

- [!] **7.1.3** **Set a real JWT secret** in `config/config.php:6`
  - Replace `'CHANGE_THIS_TO_A_LONG_RANDOM_SECRET_KEY'` with a real random 256-bit hex string
  - Until fixed: every JWT in the system is forgeable by anyone who reads the config

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

- [!] **7.2.1** **Fix `Booking.kt` coordinate types** — `pickup_lat`, `pickup_lng`, `dropoff_lat`, `dropoff_lng` are declared `String` but MySQL returns JSON numbers. Gson produces `null`, then `.toDouble()` throws `NullPointerException`.
  - File: `data/models/Booking.kt:28-32`
  - Fix: Change all four fields from `String` to `Double`
  - Also: Remove all `.toDouble()` call sites in `ActiveRideActivity.kt:89-90` and `RideRequestActivity.kt:99-103`

- [ ] **7.2.2** **Add `passenger_name` to `Booking.kt`** — `GET /driver/requests` returns `passenger_name` in JSON but the data class has no field for it. Gson drops it silently. Driver sees blank where passenger name should appear.
  - Fix: Add `val passenger_name: String? = null` to `Booking.kt`
  - Also: Display it in `item_ride_request.xml` via `RideRequestsAdapter`

- [ ] **7.2.3** **Add driver info fields to `Booking.kt`** — `GET /bookings/{id}` returns `driver_name` and `driver_email` (from JOIN) but `Booking.kt` has no such fields. Passenger cannot see who accepted their ride.
  - Fix: Add `val driver_name: String? = null`, `val driver_email: String? = null` to `Booking.kt`
  - Also: Add `TextView` widgets to `activity_ride_status.xml` and populate them in `RideStatusActivity`

### 7.3 Logic Bugs

- [ ] **7.3.1** **Fix `ManageUsersActivity` success feedback** — admin actions (approve/deactivate/delete) succeed silently. The `actionState` observer only handles `Resource.Error`.
  - File: `ManageUsersActivity.kt:105-108`
  - Fix: Add a success `Toast` in the `Resource.Success` branch of the observer

- [ ] **7.3.2** **Fix `rejectRide()` ownership check** — any authenticated driver can reject any `requested` booking. No check verifies the rejecting driver was associated with the request.
  - File: `DriverController.php:67-81`
  - Fix: Decide and document whether rejection is open to any driver (broadcast model) or restricted. If restricted, add a check.

- [ ] **7.3.3** **Fix polling reads stale state** in `RideStatusActivity` — `pollRunnable` checks terminal status from the previous ViewModel value before the API response arrives, scheduling one extra unnecessary poll after a terminal status.
  - File: `RideStatusActivity.kt:33-43`
  - Fix: Move the terminal-status check inside the booking observer (after the response updates the ViewModel), not before the next poll is posted.

- [ ] **7.3.4** **Require `license_no` + `vehicle_no` for driver registration** — currently accepted as empty strings (`?? ''`). A driver can register with blank license/vehicle fields.
  - File: `AuthController.php:59-63`
  - Fix: Add validation: if `role === 'driver'` and `license_no` or `vehicle_no` is empty, return 422.

### 7.4 Security — High Priority (but not instant threats)

- [ ] **7.4.1** **Set `HttpLoggingInterceptor` to `NONE` for release builds**
  - File: `data/api/ApiClient.kt:65-67`
  - Fix:
    ```kotlin
    val level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
    ```

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

### 8.1 Passenger Cancel Ride

- [ ] **8.1.1** Add `POST /bookings/{id}/cancel` route to `index.php`
- [ ] **8.1.2** Add `cancelBooking(int $bookingId)` method to `BookingController.php`
  - Auth: passenger only
  - Validate: caller is the booking's passenger_id
  - Validate: current status is `requested` (cannot cancel once accepted)
  - Action: set status to `cancelled`, log to booking_logs
- [ ] **8.1.3** Add `cancelBooking(id)` to `ApiService.kt` (Kotlin)
- [ ] **8.1.4** Add `cancelBooking(id)` to `BookingRepository.kt`
- [ ] **8.1.5** Test via Postman: cancel a `requested` booking, verify status = `cancelled`

### 8.2 Driver Start Ride (`in_progress` transition)

- [ ] **8.2.1** Add `POST /driver/start/{id}` route to `index.php`
- [ ] **8.2.2** Add `startRide(int $bookingId)` method to `DriverController.php`
  - Auth: driver only
  - Validate: caller is booking's `driver_id`
  - Validate: current status is `accepted`
  - Action: set status to `in_progress`, log to booking_logs
  - Optional: FCM push to passenger — "Driver has started your ride"
- [ ] **8.2.3** Add `startRide(id)` to `ApiService.kt`
- [ ] **8.2.4** Add `startRide(id)` to `BookingRepository.kt` (or `DriverViewModel.kt`)
- [ ] **8.2.5** Test via Postman: start an `accepted` booking, verify status = `in_progress`

### 8.3 Driver Online/Offline Toggle

- [ ] **8.3.1** Add `is_online TINYINT(1) DEFAULT 1` column to `driver_info` table (run ALTER TABLE or update seed.sql)
- [ ] **8.3.2** Add `PUT /driver/status` route to `index.php`
- [ ] **8.3.3** Add `updateOnlineStatus(int $driverId, bool $isOnline)` to `DriverController.php`
- [ ] **8.3.4** Update `GET /driver/requests` — filter to only return bookings to online drivers OR leave open (decide)
- [ ] **8.3.5** Add `updateDriverStatus(isOnline: Boolean)` to `ApiService.kt`
- [ ] **8.3.6** Test via Postman: toggle offline, verify driver doesn't receive new requests (if filtering added)

### 8.4 Minor API Cleanup

- [ ] **8.4.1** Fix `BookingController.php:58-63` — the ownership check `$booking['driver_id'] == $auth['user_id']` returns 403 for drivers viewing `requested` bookings where `driver_id` is NULL. Either allow drivers to view `requested` bookings, or document that `RideRequestActivity` must always use Intent extras.
- [ ] **8.4.2** Move `PUT /user/fcm-token` logic from inline `index.php:100-109` into a `UserController.php` method for consistency.
- [ ] **8.4.3** Add `GET /admin/bookings/{id}/logs` if you want the `booking_logs` audit trail to be readable (currently write-only).

---

## Phase 9 — Missing Android Features

### 9.1 Passenger Cancel Ride UI

- [ ] **9.1.1** Add a Cancel button to `activity_ride_status.xml` (visible only when status = `requested`)
- [ ] **9.1.2** Add `cancelBooking(id)` call in `RideStatusActivity.kt` (show confirmation dialog first)
- [ ] **9.1.3** On success: show toast + finish activity (or update status display to `cancelled`)
- [ ] **9.1.4** Test: passenger cancels a `requested` booking; status updates correctly

### 9.2 Driver Start Ride UI

- [ ] **9.2.1** Add a "Start Ride" button to `activity_active_ride.xml` (visible when status = `accepted`)
- [ ] **9.2.2** Wire "Start Ride" → `POST /driver/start/{id}` in `ActiveRideActivity.kt`
- [ ] **9.2.3** On success: hide "Start Ride", show "Complete Ride" (for `in_progress` status)
- [ ] **9.2.4** Test: full flow `accepted → in_progress → completed`

### 9.3 Passenger Sees Driver Info After Accept

- [ ] **9.3.1** After fixing 7.2.3 (add `driver_name` to `Booking.kt`), add `TextView` for driver name + email in `activity_ride_status.xml`
- [ ] **9.3.2** Show driver info card in `RideStatusActivity` when booking status becomes `accepted`
- [ ] **9.3.3** Test: passenger status screen shows driver name after driver accepts

### 9.4 Driver Online/Offline Toggle

- [ ] **9.4.1** After Phase 8.3 (backend done), add toggle switch to `DriverStatusFragment`
- [ ] **9.4.2** Wire toggle → `PUT /driver/status` in `DriverViewModel.kt`
- [ ] **9.4.3** Persist online/offline state visually on toggle (update status text + dot color)
- [ ] **9.4.4** Test: toggle offline → re-open app → toggle state is correct

### 9.5 Forgot Password

- [ ] **9.5.1** Decide on reset flow: admin-side reset vs email OTP (Firebase Auth or custom)
- [ ] **9.5.2** Add `POST /auth/forgot-password` endpoint (PHP)
- [ ] **9.5.3** Add a "Forgot Password?" `TextView` to `activity_login.xml` (string resource already exists)
- [ ] **9.5.4** Add click handler in `LoginActivity.kt` → navigate to reset flow screen

### 9.6 Input Validation (Android Side)

- [ ] **9.6.1** `LoginActivity` — show `TextInputLayout` error if email or password is empty before API call
- [ ] **9.6.2** `RegisterActivity` — validate email format, password ≥ 6 chars, required driver fields
- [ ] **9.6.3** `BookRideActivity` — validate pickup + dropoff are set before allowing "Request Ride"
- [ ] **9.6.4** Test: each validation shows an inline error (not a Toast)

---

## Phase 10 — Security Hardening

- [ ] **10.1** **Migrate `PrefsManager` to `EncryptedSharedPreferences`**
  - Add `implementation("androidx.security:security-crypto:1.1.0-alpha06")` to `build.gradle.kts`
  - Replace `SharedPreferences` instantiation with `EncryptedSharedPreferences.create(...)` — same key/value API
  - File: `data/local/PrefsManager.kt`

- [ ] **10.2** **Rate limiting on `/auth/login`** (PHP)
  - Add IP-based rate limit: max 5 attempts per minute per IP
  - Options: in-memory via `$_SESSION` (simple), APCu cache, or Apache `.htaccess` mod_ratelimit
  - File: `index.php` or `.htaccess`

- [ ] **10.3** **Move PHP secrets to a `.env` file**
  - Install `vlucas/phpdotenv` via Composer (or use manual `parse_ini_file()`)
  - Move `JWT_SECRET` and `FCM_SERVER_KEY` out of `config/config.php` into `.env`
  - Add `.env` to `.gitignore`, provide `.env.example` template
  - Files: `config/config.php`, `.gitignore`, new `.env` + `.env.example`

- [ ] **10.4** **Enable HTTPS on the backend**
  - For local dev: skip or use a self-signed cert
  - For production: set up Let's Encrypt on the VPS
  - Update `BASE_URL` constants in `utils/Constants.kt` to `https://`
  - Remove cleartext exception in `res/xml/network_security_config.xml`

- [ ] **10.5** **Restrict CORS origin** in `index.php:7`
  - Change `Access-Control-Allow-Origin: *` to the specific production domain
  - Keep `*` for development only (gate on `APP_ENV`)

- [ ] **10.6** **Fix generic login error message for driver account existence leak**
  - File: `AuthController.php:97-99`
  - Both "pending approval" and "wrong password" should return the same generic message to prevent account enumeration

---

## Phase 11 — Production Readiness

- [ ] **11.1** **User-friendly error messages (Android)**
  - Create `ErrorHandler` or `BaseRepository` extension — map HTTP status codes to friendly strings
  - 401 → "Session expired, please log in again"
  - 403 → "You don't have permission to do that"
  - 404 → "Not found"
  - 422 → Display field-specific errors from API response
  - 500 → "Something went wrong. Please try again."
  - Populate `strings.xml` with all error strings

- [ ] **11.2** **Switch base URL by build type**
  - File: `app/build.gradle.kts`
    ```kotlin
    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2/ptoda_booking_api/\"")
        }
        release {
            buildConfigField("String", "BASE_URL", "\"https://your-production-domain.com/api/\"")
        }
    }
    ```
  - File: `utils/Constants.kt` — replace hardcoded URL with `BuildConfig.BASE_URL`

- [ ] **11.3** **Remove debug files and TODO comments**
  - Delete `check_admin.php` and `fix_admin.php` (also in Phase 7 — done first)
  - Search for `// TODO`, `// FIXME`, `// remove when API is active` across all files and resolve or remove

- [ ] **11.4** **VPS deployment plan**
  - Choose VPS provider (DigitalOcean, Linode, etc.)
  - Install Nginx + PHP-FPM + MySQL
  - Set up domain name + Let's Encrypt SSL certificate
  - Migrate DB from XAMPP to remote MySQL
  - Update `BASE_URL` to production URL in release build config

- [ ] **11.5** **CI/CD pipeline basics**
  - Add GitHub Actions workflow: `./gradlew assembleDebug` + `./gradlew test` on every push to master
  - Add PHP syntax check step: `find . -name "*.php" -exec php -l {} \;`

---

## Phase 12 — Testing

### 12.1 Unit Tests (Android)

- [ ] **12.1.1** `AuthViewModel` — login success, login failure, empty field validation
- [ ] **12.1.2** `BookingRepository` — createBooking success/error paths
- [ ] **12.1.3** `Resource` sealed class behavior
- [ ] **12.1.4** `PrefsManager` — save and retrieve JWT, clear session

### 12.2 Instrumented Tests (Android)

- [ ] **12.2.1** Login flow: enter credentials → verify correct role navigation
- [ ] **12.2.2** Register flow: driver fields show/hide on role selection
- [ ] **12.2.3** Booking form: empty pickup/dropoff shows validation error

### 12.3 PHP API Contract Tests (PHPUnit)

- [ ] **12.3.1** `POST /auth/register` — returns 201 with valid data, 422 on missing fields, 409 on duplicate email
- [ ] **12.3.2** `POST /auth/login` — returns JWT on valid creds, 401 on wrong password, 403 on inactive account
- [ ] **12.3.3** Protected route — returns 401 without token, 403 with wrong role
- [ ] **12.3.4** `POST /bookings` — returns 201 with passenger token, 403 with driver token
- [ ] **12.3.5** `POST /driver/accept/{id}` — returns 200 and updates status, 409 if already accepted

---

## Known Issues Log (open bugs)

| ID | Severity | File | Description | Phase |
|----|----------|------|-------------|-------|
| BUG-017 | 🔴 Critical | `Booking.kt:28-32` | Coordinates typed `String`, Gson returns `null`, `.toDouble()` throws NPE | 7.2.1 |
| BUG-018 | 🟠 High | `Booking.kt` | `passenger_name` silently dropped by Gson; not in data class | 7.2.2 |
| BUG-019 | 🟠 High | `Booking.kt` | `driver_name`, `driver_email` silently dropped; passenger never sees driver info | 7.2.3 |
| BUG-020 | 🟠 High | `DriverController.php:79` | `rejectRide()` has no ownership check | 7.3.2 |
| BUG-021 | 🟠 High | `AuthController.php:59-63` | Driver registration accepts empty license/vehicle | 7.3.4 |
| BUG-022 | 🟡 Medium | `RideStatusActivity.kt:33-43` | Polling reads stale state; one extra poll after terminal status | 7.3.3 |
| BUG-023 | 🟡 Medium | `ManageUsersActivity.kt:105-108` | Admin actions show no success feedback | 7.3.1 |
| BUG-024 | 🟡 Medium | `BookingController.php:58-63` | `GET /bookings/{id}` returns 403 for drivers on `requested` bookings (`driver_id` is NULL) | 8.4.1 |
| BUG-025 | 🟡 Medium | `ApiClient.kt:65-67` | `HttpLoggingInterceptor` at BODY level in all builds, logs JWT + passwords | 7.4.1 |

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
