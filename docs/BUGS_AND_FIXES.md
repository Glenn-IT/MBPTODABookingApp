# PTODA Booking System — Bugs, Mistakes & Fixes Log

> This document tracks every mistake, issue, or bug encountered during development — including the cause and the fix applied. Use this as a living document; add entries as they happen.

---

## How to Add an Entry

```
### [BUG-XXX] Short title
- **Date:** YYYY-MM-DD
- **Phase:** Phase N — Section name
- **File(s) affected:** `path/to/file.php` or `FileName.kt`
- **Description:** What went wrong.
- **Root cause:** Why it happened.
- **Fix applied:** What was changed to resolve it.
- **Prevention tip:** How to avoid this in the future.
```

---

## Log Entries

---

### [BUG-001] Project folder not accessible via XAMPP

- **Date:** _(fill in when encountered)_
- **Phase:** Phase 1 — Environment Setup
- **File(s) affected:** `httpd-vhosts.conf` / XAMPP config
- **Description:** Opening `http://localhost/ptoda_booking_api/` returns 403 Forbidden or 404 Not Found.
- **Root cause:** XAMPP Apache does not have read permissions on the project folder, or `mod_rewrite` is not enabled.
- **Fix applied:**
  1. Ensure the folder is inside `C:\xampp\htdocs\`.
  2. In `C:\xampp\apache\conf\httpd.conf`, confirm `mod_rewrite` is uncommented: `LoadModule rewrite_module modules/mod_rewrite.so`
  3. Set `AllowOverride All` for the `htdocs` directory block.
  4. Restart Apache in XAMPP Control Panel.
- **Prevention tip:** Always enable `mod_rewrite` and `AllowOverride All` before starting API development with `.htaccess` rewrites.

---

### [BUG-002] Android Emulator cannot reach localhost API

- **Date:** _(fill in when encountered)_
- **Phase:** Phase 5 — Integration Testing
- **File(s) affected:** `utils/Constants.kt`
- **Description:** API calls from Android Emulator return `Connection refused` or timeout.
- **Root cause:** The Android Emulator's `localhost` (127.0.0.1) refers to the emulator itself, not the development PC.
- **Fix applied:** Changed base URL in `Constants.kt` from `http://localhost/ptoda_booking_api/` to `http://10.0.2.2/ptoda_booking_api/`.
- **Prevention tip:** Always use `10.0.2.2` for emulator → host machine communication. For physical devices, use the PC's local network IP (e.g., `192.168.1.x`).

---

### [BUG-003] JWT token not sent with API requests

- **Date:** _(fill in when encountered)_
- **Phase:** Phase 4 — Android App (Networking Layer)
- **File(s) affected:** `data/api/ApiClient.kt`
- **Description:** Protected API endpoints return `401 Unauthorized` even after login.
- **Root cause:** JWT token was saved in `PrefsManager` but never attached to outgoing Retrofit requests.
- **Fix applied:** Added an OkHttp `Interceptor` in `ApiClient.kt` that reads the token from `PrefsManager` and injects the `Authorization: Bearer <token>` header on every request.
- **Prevention tip:** Set up the auth interceptor in `ApiClient` before building any screen that calls protected endpoints.

---

### [BUG-004] PHP API returns HTML error page instead of JSON

- **Date:** _(fill in when encountered)_
- **Phase:** Phase 3 — PHP REST API
- **File(s) affected:** `index.php`, `controllers/*.php`
- **Description:** Android app receives a Gson parse error; the API response body is an HTML Apache/PHP error page.
- **Root cause:** A PHP fatal error or uncaught exception caused Apache to return an HTML error page. `Content-Type: application/json` header was not set before any output.
- **Fix applied:**
  1. Added `header('Content-Type: application/json');` at the very top of `index.php`.
  2. Added a global `set_exception_handler` and `set_error_handler` in `index.php` to catch all errors and return a JSON error response.
- **Prevention tip:** Always set JSON content-type header first. Use a global error handler so no raw PHP errors ever leak as HTML to the client.

---

### [BUG-005] `google-services.json` missing or in wrong location

- **Date:** 2026-03-21
- **Phase:** Phase 4 — Firebase Cloud Messaging / Build
- **File(s) affected:** `app/google-services.json`
- **Description:** Android build fails with `Execution failed for task ':app:processDebugGoogleServices'. File google-services.json is missing.`
- **Root cause:** `google-services.json` was placed in the project root (`MBPTODABookingApp/`) instead of the `app/` module directory. The Google Services Gradle plugin only searches inside `app/`.
- **Fix applied:** Copied `google-services.json` from project root into `app/google-services.json`. Also updated `AndroidManifest.xml` to use the real Maps API key from the same file (`AIzaSyD4we-oa3wABCTt2iKZEyOnzoF-5pfb6mk`) replacing the `YOUR_MAPS_API_KEY` placeholder.
- **Prevention tip:** Firebase always requires `google-services.json` to be inside the `app/` directory, not the project root. When adding Firebase to a project via Android Studio, the assistant will place it correctly — but if added manually, always double-check the path.

---

### [BUG-006] FCM token not updated after app reinstall

- **Date:** _(fill in when encountered)_
- **Phase:** Phase 4 — Firebase Cloud Messaging
- **File(s) affected:** `services/PTODAFirebaseMessagingService.kt`
- **Description:** Push notifications stop working after reinstalling the app on the same device.
- **Root cause:** FCM generates a new device token on reinstall, but the old token remains stored in the database.
- **Fix applied:** Implemented `onNewToken()` callback in `PTODAFirebaseMessagingService.kt` to automatically `PUT /user/fcm-token` to the API whenever a new token is issued.
- **Prevention tip:** Always implement `onNewToken` and sync the token to your backend immediately.

---

### [BUG-007] MySQL PDO connection fails silently

- **Date:** _(fill in when encountered)_
- **Phase:** Phase 3 — PHP REST API (Core Setup)
- **File(s) affected:** `config/database.php`
- **Description:** API returns empty responses or crashes with no useful message; MySQL is running.
- **Root cause:** PDO connection was missing `PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION`, so errors were suppressed.
- **Fix applied:** Added error mode to PDO options array:
  ```php
  $options = [
      PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
      PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
  ];
  ```
- **Prevention tip:** Always enable `ERRMODE_EXCEPTION` on PDO connections during development so errors surface immediately.

---

### [BUG-008] CORS error — Android WebView or future web client blocked

- **Date:** _(fill in when encountered)_
- **Phase:** Phase 3 — PHP REST API
- **File(s) affected:** `index.php`
- **Description:** Cross-Origin requests blocked by browser/client with `Access-Control-Allow-Origin` error.
- **Root cause:** No CORS headers set in the PHP API.
- **Fix applied:** Added CORS headers at the top of `index.php`:
  ```php
  header("Access-Control-Allow-Origin: *");
  header("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS");
  header("Access-Control-Allow-Headers: Content-Type, Authorization");
  if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') { http_response_code(200); exit(); }
  ```
- **Prevention tip:** Add CORS headers early in development. Restrict `Allow-Origin` to specific domains before going to production.

---

### [BUG-009] Booking status not updating in real-time on passenger screen

- **Date:** _(fill in when encountered)_
- **Phase:** Phase 4 — Passenger Screens
- **File(s) affected:** `ui/passenger/RideStatusActivity.kt`
- **Description:** Passenger sees stale booking status; it doesn't update when driver accepts the ride.
- **Root cause:** The app only fetches status once on screen load with no polling or push mechanism implemented yet.
- **Fix applied:** Implemented a short-interval polling mechanism (every 5 seconds via `Handler.postDelayed`) in `RideStatusActivity.kt` that calls `GET /bookings/{id}` until status is `completed` or `cancelled`.
- **Prevention tip:** For MVP, polling is acceptable. Plan to replace with WebSocket or FCM data messages in a later phase.

---

### [BUG-010] Driver approval not checked on login

- **Date:** _(fill in when encountered)_
- **Phase:** Phase 3 — PHP REST API (Auth)
- **File(s) affected:** `controllers/AuthController.php`
- **Description:** A newly registered driver can log in and accept rides before an admin approves their account.
- **Root cause:** `AuthController` login logic did not check `driver_info.approval_status` for driver-role users.
- **Fix applied:** Added a check in `AuthController@login`: if `role === 'driver'` and `approval_status !== 'approved'`, return `403` with message `"Your driver account is pending admin approval."`.
- **Prevention tip:** Always enforce role-specific business rules during authentication, not just at the feature endpoint level.

---

### [BUG-011] Admin approval flow incomplete — no pending list or reject endpoint

- **Date:** 2026-03-17
- **Phase:** Phase 3 — PHP REST API (Admin)
- **File(s) affected:** `controllers/AdminController.php`, `models/Admin.php`, `index.php`
- **Description:** Admin had no way to see which drivers are waiting for approval, and there was no endpoint to reject a driver — only `PUT /admin/driver/approve/{id}` existed.
- **Root cause:** During initial API build, the approval workflow was only partially implemented. The `approve` action was added but the `GET /admin/drivers/pending` listing endpoint and `PUT /admin/driver/reject/{id}` action were overlooked.
- **Fix applied:**
  1. Added `getPendingDrivers()` to `Admin.php` — queries `driver_info` for `approval_status = 'pending'`.
  2. Added `rejectDriver()` to `Admin.php` — sets `approval_status = 'rejected'`.
  3. Added `getPendingDrivers()` and `rejectDriver()` methods to `AdminController.php`.
  4. Registered two new routes in `index.php`:
     - `GET /admin/drivers/pending` → `AdminController::getPendingDrivers()`
     - `PUT /admin/driver/reject/{id}` → `AdminController::rejectDriver()`
- **Prevention tip:** When designing an approval workflow, always plan all three actions together: **list** (who needs action), **approve**, and **reject**. Check the full workflow end-to-end before marking a feature complete.

---

### [BUG-012] No activate endpoint — deactivated users could never be re-enabled

- **Date:** 2026-03-17
- **Phase:** Phase 3 — PHP REST API (Admin)
- **File(s) affected:** `controllers/AdminController.php`, `models/Admin.php`, `index.php`
- **Description:** `PUT /admin/user/deactivate/{id}` existed but there was no counterpart to re-enable a user. Once deactivated, a user account was permanently locked with no recovery path via the API.
- **Root cause:** Only half of the status-toggle workflow was implemented.
- **Fix applied:** Added `activateUser()` to `Admin.php` and `activateUser()` action to `AdminController.php`. Registered route `PUT /admin/user/activate/{id}` in `index.php`. Also tightened `deactivateUser()` to only match `status = 'active'` rows so `rowCount() === 0` correctly returns 404 when user is already inactive.
- **Prevention tip:** Whenever you add a "disable" action, always implement the "enable" counterpart at the same time.

---

### [BUG-013] No delete user endpoint — no way to permanently remove accounts

- **Date:** 2026-03-17
- **Phase:** Phase 3 — PHP REST API (Admin)
- **File(s) affected:** `controllers/AdminController.php`, `models/Admin.php`, `index.php`
- **Description:** There was no `DELETE /admin/user/{id}` endpoint, making it impossible to permanently remove test accounts or comply with data-removal requests.
- **Root cause:** Delete (the D in CRUD) was never implemented for the user resource.
- **Fix applied:** Added `deleteUser()` to `Admin.php` (issues `DELETE FROM users WHERE id = :id`). Added `deleteUser()` action to `AdminController.php` with a pre-flight `findById` check. Registered route `DELETE /admin/user/{id}` in `index.php`.
- **Prevention tip:** Always review full CRUD coverage (Create, Read, Update, Delete) for every resource before marking a feature complete. Related records (driver_info, fcm_tokens) must be covered by `ON DELETE CASCADE` in the schema to avoid orphaned rows.

---

### [BUG-014] Apache `.htaccess` `mod_rewrite` not routing sub-paths — all non-root API routes return 404

- **Date:** 2026-03-21
- **Phase:** Phase 3 — PHP Backend / Phase 5 — Integration Testing
- **File(s) affected:** `C:\xampp\htdocs\ptoda_booking_api\.htaccess`
- **Description:** Root endpoint `GET /ptoda_booking_api/` returned 200 correctly, but every sub-path (`/auth/login`, `/auth/register`, `/bookings`, etc.) returned Apache's native 404 HTML page — not a PHP-generated JSON response.
- **Root cause:** The `.htaccess` `RewriteRule` was missing `RewriteBase`. Without it, Apache resolves the rewrite target (`index.php`) relative to the document root, so requests to sub-paths fail before reaching PHP.
- **Fix applied:** Added `RewriteBase /ptoda_booking_api/` to `.htaccess` immediately after `RewriteEngine On`:
  ```apache
  Options -Indexes
  RewriteEngine On
  RewriteBase /ptoda_booking_api/

  RewriteCond %{REQUEST_FILENAME} !-f
  RewriteCond %{REQUEST_FILENAME} !-d
  RewriteRule ^(.*)$ index.php [QSA,L]
  ```
- **Prevention tip:** Whenever an Apache app lives in a subdirectory (not the document root), always add `RewriteBase /your-folder/`. Without it, `mod_rewrite` rewrites relative to `/` which breaks sub-path routing entirely.

---

### [BUG-015] Android app blocked from HTTP requests on physical device (cleartext traffic policy)

- **Date:** 2026-03-21
- **Phase:** Phase 4 — Networking / Phase 5 — Integration Testing
- **File(s) affected:** `app/src/main/res/xml/network_security_config.xml` _(new)_, `app/src/main/AndroidManifest.xml`
- **Description:** Android app silently fails all API calls on a physical device (Android 9+). No network error shown; requests never reach the server. The root cause is Android's default `CLEARTEXT_NOT_PERMITTED` policy for HTTP (non-HTTPS) traffic, which was introduced in Android 9 (API 28).
- **Root cause:** Android 9+ blocks all cleartext (HTTP) traffic by default unless explicitly allowed. Our PHP API runs on plain HTTP (`http://192.168.0.100/...`).
- **Fix applied:**
  1. Created `app/src/main/res/xml/network_security_config.xml` to permit cleartext traffic only to the local PC IP:
     ```xml
     <network-security-config>
         <domain-config cleartextTrafficPermitted="true">
             <domain includeSubdomains="false">192.168.0.100</domain>
         </domain-config>
     </network-security-config>
     ```
  2. Referenced it in `AndroidManifest.xml` on the `<application>` tag:
     `android:networkSecurityConfig="@xml/network_security_config"`
- **Prevention tip:** Always add a network security config when testing against a local HTTP server. In production, use HTTPS and remove this config.

---

### [BUG-016] `Constants.kt` `BASE_URL` pointing to Android emulator address on physical device

- **Date:** 2026-03-21
- **Phase:** Phase 4 — Networking / Phase 5 — Integration Testing
- **File(s) affected:** `app/src/main/java/com/example/mbptodabookingapp/utils/Constants.kt`
- **Description:** App on physical phone cannot reach the API. The `BASE_URL` was set to `http://10.0.2.2/ptoda_booking_api/` which is the emulator loopback alias for the host PC — meaningless on a real device.
- **Root cause:** `BASE_URL` was left pointing to `BASE_URL_EMULATOR` after emulator development and was not switched before physical device testing.
- **Fix applied:** Changed active `BASE_URL` constant to `BASE_URL_DEVICE = "http://192.168.0.100/ptoda_booking_api/"` (PC's LAN IP on the shared Wi-Fi network). Also corrected the `BASE_URL_DEVICE` placeholder which had `192.168.1.x`.
- **Prevention tip:** Always update `BASE_URL` before switching between emulator and physical device. Consider using a Gradle `buildConfigField` or a runtime toggle to avoid this in future.

---

### [BUG-017] PHP `POST /auth/login` response missing `status` field — Android `LoggedInUser` parse failure

- **Date:** 2026-03-21
- **Phase:** Phase 3 — PHP Backend / Phase 4 — Auth Screens
- **File(s) affected:** `controllers/AuthController.php` (PHP), `data/models/User.kt` (Android)
- **Description:** Android `AuthRepository.login()` succeeds but the `LoggedInUser` object has a null/empty `status` field. Gson silently assigns null to a non-nullable Kotlin `String` property, potentially causing `NullPointerException` downstream when status is checked.
- **Root cause:** The PHP `AuthController::login()` response body included `id`, `name`, `email`, `role` in the `user` object but omitted `status`. The Android `LoggedInUser` data class declares `status: String` (non-nullable).
- **Fix applied:** Added `'status' => $user['status']` to the PHP login response user object:
  ```php
  'user' => [
      'id'     => $user['id'],
      'name'   => $user['name'],
      'email'  => $user['email'],
      'role'   => $user['role'],
      'status' => $user['status'],   // ← added
  ],
  ```
- **Prevention tip:** When writing a PHP API response, always cross-reference the matching Android data class field-by-field. Any non-nullable Kotlin field with no matching JSON key will silently become null at runtime (Gson bypasses Kotlin null safety).

---

_Last updated: 2026-03-21_
_Add new entries above this line as issues are discovered._

- **Date:** _(fill in when encountered)_
- **Phase:** Phase 4 — Firebase Cloud Messaging
- **File(s) affected:** `services/PTODAFirebaseMessagingService.kt`
- **Description:** Push notifications stop working after reinstalling the app on the same device.
- **Root cause:** FCM generates a new device token on reinstall, but the old token remains stored in the database.
- **Fix applied:** Implemented `onNewToken()` callback in `PTODAFirebaseMessagingService.kt` to automatically `PUT /user/fcm-token` to the API whenever a new token is issued.
- **Prevention tip:** Always implement `onNewToken` and sync the token to your backend immediately.

---

### [BUG-007] MySQL PDO connection fails silently

- **Date:** _(fill in when encountered)_
- **Phase:** Phase 3 — PHP REST API (Core Setup)
- **File(s) affected:** `config/database.php`
- **Description:** API returns empty responses or crashes with no useful message; MySQL is running.
- **Root cause:** PDO connection was missing `PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION`, so errors were suppressed.
- **Fix applied:** Added error mode to PDO options array:
  ```php
  $options = [
      PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
      PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
  ];
  ```
- **Prevention tip:** Always enable `ERRMODE_EXCEPTION` on PDO connections during development so errors surface immediately.

---

### [BUG-008] CORS error — Android WebView or future web client blocked

- **Date:** _(fill in when encountered)_
- **Phase:** Phase 3 — PHP REST API
- **File(s) affected:** `index.php`
- **Description:** Cross-Origin requests blocked by browser/client with `Access-Control-Allow-Origin` error.
- **Root cause:** No CORS headers set in the PHP API.
- **Fix applied:** Added CORS headers at the top of `index.php`:
  ```php
  header("Access-Control-Allow-Origin: *");
  header("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS");
  header("Access-Control-Allow-Headers: Content-Type, Authorization");
  if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') { http_response_code(200); exit(); }
  ```
- **Prevention tip:** Add CORS headers early in development. Restrict `Allow-Origin` to specific domains before going to production.

---

### [BUG-009] Booking status not updating in real-time on passenger screen

- **Date:** _(fill in when encountered)_
- **Phase:** Phase 4 — Passenger Screens
- **File(s) affected:** `ui/passenger/RideStatusActivity.kt`
- **Description:** Passenger sees stale booking status; it doesn't update when driver accepts the ride.
- **Root cause:** The app only fetches status once on screen load with no polling or push mechanism implemented yet.
- **Fix applied:** Implemented a short-interval polling mechanism (every 5 seconds via `Handler.postDelayed`) in `RideStatusActivity.kt` that calls `GET /bookings/{id}` until status is `completed` or `cancelled`.
- **Prevention tip:** For MVP, polling is acceptable. Plan to replace with WebSocket or FCM data messages in a later phase.

---

### [BUG-010] Driver approval not checked on login

- **Date:** _(fill in when encountered)_
- **Phase:** Phase 3 — PHP REST API (Auth)
- **File(s) affected:** `controllers/AuthController.php`
- **Description:** A newly registered driver can log in and accept rides before an admin approves their account.
- **Root cause:** `AuthController` login logic did not check `driver_info.approval_status` for driver-role users.
- **Fix applied:** Added a check in `AuthController@login`: if `role === 'driver'` and `approval_status !== 'approved'`, return `403` with message `"Your driver account is pending admin approval."`.
- **Prevention tip:** Always enforce role-specific business rules during authentication, not just at the feature endpoint level.

---

### [BUG-011] Admin approval flow incomplete — no pending list or reject endpoint

- **Date:** 2026-03-17
- **Phase:** Phase 3 — PHP REST API (Admin)
- **File(s) affected:** `controllers/AdminController.php`, `models/Admin.php`, `index.php`
- **Description:** Admin had no way to see which drivers are waiting for approval, and there was no endpoint to reject a driver — only `PUT /admin/driver/approve/{id}` existed.
- **Root cause:** During initial API build, the approval workflow was only partially implemented. The `approve` action was added but the `GET /admin/drivers/pending` listing endpoint and `PUT /admin/driver/reject/{id}` action were overlooked.
- **Fix applied:**
  1. Added `getPendingDrivers()` to `Admin.php` — queries `driver_info` for `approval_status = 'pending'`.
  2. Added `rejectDriver()` to `Admin.php` — sets `approval_status = 'rejected'`.
  3. Added `getPendingDrivers()` and `rejectDriver()` methods to `AdminController.php`.
  4. Registered two new routes in `index.php`:
     - `GET /admin/drivers/pending` → `AdminController::getPendingDrivers()`
     - `PUT /admin/driver/reject/{id}` → `AdminController::rejectDriver()`
- **Prevention tip:** When designing an approval workflow, always plan all three actions together: **list** (who needs action), **approve**, and **reject**. Check the full workflow end-to-end before marking a feature complete.

---

### [BUG-012] No activate endpoint — deactivated users could never be re-enabled

- **Date:** 2026-03-17
- **Phase:** Phase 3 — PHP REST API (Admin)
- **File(s) affected:** `controllers/AdminController.php`, `models/Admin.php`, `index.php`
- **Description:** `PUT /admin/user/deactivate/{id}` existed but there was no counterpart to re-enable a user. Once deactivated, a user account was permanently locked with no recovery path via the API.
- **Root cause:** Only half of the status-toggle workflow was implemented.
- **Fix applied:** Added `activateUser()` to `Admin.php` and `activateUser()` action to `AdminController.php`. Registered route `PUT /admin/user/activate/{id}` in `index.php`. Also tightened `deactivateUser()` to only match `status = 'active'` rows so `rowCount() === 0` correctly returns 404 when user is already inactive.
- **Prevention tip:** Whenever you add a "disable" action, always implement the "enable" counterpart at the same time.

---

### [BUG-013] No delete user endpoint — no way to permanently remove accounts

- **Date:** 2026-03-17
- **Phase:** Phase 3 — PHP REST API (Admin)
- **File(s) affected:** `controllers/AdminController.php`, `models/Admin.php`, `index.php`
- **Description:** There was no `DELETE /admin/user/{id}` endpoint, making it impossible to permanently remove test accounts or comply with data-removal requests.
- **Root cause:** Delete (the D in CRUD) was never implemented for the user resource.
- **Fix applied:** Added `deleteUser()` to `Admin.php` (issues `DELETE FROM users WHERE id = :id`). Added `deleteUser()` action to `AdminController.php` with a pre-flight `findById` check. Registered route `DELETE /admin/user/{id}` in `index.php`.
- **Prevention tip:** Always review full CRUD coverage (Create, Read, Update, Delete) for every resource before marking a feature complete. Related records (driver_info, fcm_tokens) must be covered by `ON DELETE CASCADE` in the schema to avoid orphaned rows.

---

### [BUG-014] Driver had no way to find an accepted booking after closing the app

- **Date:** 2026-04-12
- **Phase:** Phase 6 — Driver BottomNav Shell
- **File(s) affected:** `DriverDashboardFragment.kt`, `fragment_driver_dashboard.xml`, `DriverViewModel.kt`, `activity_active_ride.xml`, `ActiveRideActivity.kt`
- **Description:** Once a driver accepted a ride and the status changed to `"accepted"`, the booking disappeared from `GET /driver/requests` (which only returns `status = 'requested'` unassigned bookings). If the driver closed the app or pressed Back from `ActiveRideActivity`, there was no visible entry point on the dashboard or requests tab to resume managing the active ride. The passenger's status screen correctly showed "accepted" but the driver was effectively locked out.
- **Root cause:** `DriverDashboardFragment` only observed `viewModel.requests` (pending list), which excludes `accepted` / `in_progress` bookings. No "Active Ride" banner existed. `ActiveRideActivity` also had no back button.
- **Fix applied:**
  1. Added `fetchDriverBookings()` to `DriverViewModel` — calls `GET /bookings` (role-filtered), which returns all bookings assigned to the driver regardless of status.
  2. Added `driverBookings: LiveData` to `DriverViewModel`.
  3. Updated `DriverDashboardFragment` to observe `driverBookings`, detect any booking with `status = "accepted"` or `"in_progress"`, and show a prominent **Active Ride Banner** card at the top of the dashboard.
  4. The banner displays Booking ID, pickup address, dropoff address, and a status badge. Tapping **Resume Ride** launches `ActiveRideActivity` with the correct `booking_id`.
  5. `DriverHomeActivity` now calls `fetchDriverBookings()` in both `onCreate` and `onResume` so the banner is always current.
  6. `ActiveRideActivity` layout redesigned to be uniform with Phase 2–6 card style (Booking ID row, labeled pickup/dropoff, status badge, back-arrow navigation).
- **Prevention tip:** When an entity transitions to a new status and disappears from a role-filtered list, always ensure the next status is surfaced via a separate API call or a dedicated LiveData + UI element. Never leave a user with "accepted" data and no path forward.

---

### [BUG-015] Status tab "Rides Completed" count always showed 0

- **Date:** 2026-04-12
- **Phase:** Phase 6 — Driver BottomNav Shell
- **File(s) affected:** `DriverStatusFragment.kt`, `DriverDashboardFragment.kt`
- **Description:** The **Status** tab (`DriverStatusFragment`) always displayed `0` for "Rides Completed" even after a driver completed multiple rides. The **Dashboard** tab showed the correct count after the BUG-014 fix. The two tabs were inconsistent.
- **Root cause:** `DriverStatusFragment.observeViewModel()` counted completed bookings from `viewModel.requests`, which is backed by `GET /driver/requests`. That endpoint only ever returns bookings with `status = 'requested'` (unassigned, pending). Completed bookings are never in that list, so the filter `list.count { it.status == "completed" }` always returned `0`. Identical to the same mistake originally in `DriverDashboardFragment` before BUG-014 was fixed.
- **Fix applied:**
  1. **`DriverStatusFragment`** — split the observer into two:
     - `viewModel.requests` → `tvPendingCount` (pending count — correct source ✓)
     - `viewModel.driverBookings` → `tvAcceptedCount` (completed count — now uses `GET /bookings` role-filtered, which includes all statuses)
  2. **`DriverDashboardFragment`** — also had the same `driverBookings.observe` called twice (once for the active banner, once for completed count). Merged into a single observer that handles the banner, pending count, and completed count together — eliminating the double-registration.
- **Prevention tip:** Any count or display that involves a status *other than `requested`* must use `viewModel.driverBookings` (backed by `GET /bookings`). Only `viewModel.requests` (`GET /driver/requests`) should be used for pending/requested counts. Add a comment at the top of every observer block stating which endpoint backs it.

---

_Last updated: 2026-04-12_
_Add new entries above this line as issues are discovered._
