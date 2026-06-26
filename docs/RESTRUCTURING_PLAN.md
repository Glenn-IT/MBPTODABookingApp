# MBPTODA System Restructuring Plan

## Overview

Restructure the current monolithic app into a **single Android Studio project with multiple modules**, plus a separate PHP web admin panel.

| Module / System | Type | Audience |
|---|---|---|
| `:core` | Android Library module | Shared by both apps |
| `:app-passenger` | Android App module | Passengers booking rides |
| `:app-driver` | Android App module | Drivers accepting rides |
| `MBPTODAAdmin/` | PHP Web App (separate) | Admin managing users, drivers, bookings |

**One Android Studio project. One `settings.gradle.kts`. Two APKs.**  
The PHP admin panel lives in XAMPP's `htdocs/` as a separate web project.

---

## Why Multi-Module (not two separate projects)

| | Multi-Module (chosen) | Two Separate Projects |
|---|---|---|
| Shared code updates | Change once in `:core`, both apps get it | Must update in two places |
| Gradle dependency management | One `libs.versions.toml` | Two separate version files |
| Android Studio windows | One window, switch run config | Two windows |
| Build | Gradle handles module dependencies | Manual copy/paste |
| Risk of code drift | Low | High |

---

## Current State

Single `app` module (`MBPTODABookingApp`) with all three roles:
- `ui/passenger/` — Passenger screens
- `ui/driver/` — Driver screens
- `ui/admin/` — Admin screens (being replaced by PHP web)
- `data/` — Shared API, models, repositories, local storage
- `services/` — FCM push notifications
- `utils/` — Constants, Resource wrapper
- `MainActivity.kt` — Role-router that redirects based on user type

**Problems with the current structure:**
- Admin features in a mobile app are hard to use and maintain
- Passenger and Driver flows are unrelated but share one APK
- Both roles download code they will never use
- Role-checking logic in `MainActivity` adds unnecessary complexity

---

## Target Project Structure

```
MBPTODABookingApp/                          ← Same root folder, same git repo
├── settings.gradle.kts                     ← Declares all 3 modules
├── build.gradle.kts                        ← Root-level build config
├── gradle/
│   └── libs.versions.toml                  ← Single version catalog for all modules
│
├── core/                                   ← :core Android Library module
│   └── src/main/java/com/mbptoda/core/
│       ├── data/
│       │   ├── api/
│       │   │   ├── ApiClient.kt
│       │   │   ├── ApiService.kt
│       │   │   └── ApiResponse.kt
│       │   ├── models/
│       │   │   ├── User.kt
│       │   │   ├── AuthModels.kt
│       │   │   ├── Booking.kt
│       │   │   ├── DriverModels.kt
│       │   │   └── FcmModels.kt
│       │   ├── repository/
│       │   │   ├── BaseRepository.kt
│       │   │   ├── AuthRepository.kt
│       │   │   ├── UserRepository.kt
│       │   │   └── BookingRepository.kt
│       │   └── local/
│       │       └── PrefsManager.kt
│       ├── services/
│       │   └── PTODAFirebaseMessagingService.kt
│       └── utils/
│           ├── Constants.kt
│           └── Resource.kt
│
├── app-passenger/                          ← :app-passenger Android App module
│   └── src/main/java/com/mbptoda/passenger/
│       ├── PassengerApplication.kt
│       ├── ui/
│       │   ├── auth/
│       │   │   ├── LoginActivity.kt
│       │   │   ├── RegisterActivity.kt
│       │   │   └── AuthViewModel.kt
│       │   ├── home/
│       │   │   ├── PassengerHomeActivity.kt
│       │   │   └── PassengerViewModel.kt
│       │   ├── booking/
│       │   │   ├── BookRideActivity.kt
│       │   │   └── RideStatusActivity.kt
│       │   ├── history/
│       │   │   └── RideHistoryFragment.kt
│       │   └── theme/
│       │       ├── Theme.kt
│       │       ├── Color.kt
│       │       └── Type.kt
│       └── AndroidManifest.xml
│
├── app-driver/                             ← :app-driver Android App module
│   └── src/main/java/com/mbptoda/driver/
│       ├── DriverApplication.kt
│       ├── ui/
│       │   ├── auth/
│       │   │   ├── LoginActivity.kt
│       │   │   ├── RegisterActivity.kt
│       │   │   └── AuthViewModel.kt
│       │   ├── home/
│       │   │   ├── DriverHomeActivity.kt
│       │   │   └── DriverViewModel.kt
│       │   ├── dashboard/
│       │   │   └── DriverDashboardFragment.kt
│       │   ├── requests/
│       │   │   ├── DriverRequestsFragment.kt
│       │   │   ├── RideRequestActivity.kt
│       │   │   └── RideRequestsAdapter.kt
│       │   ├── status/
│       │   │   └── DriverStatusFragment.kt
│       │   ├── ride/
│       │   │   └── ActiveRideActivity.kt
│       │   └── theme/
│       │       ├── Theme.kt
│       │       ├── Color.kt
│       │       └── Type.kt
│       └── AndroidManifest.xml
│
├── app/                                    ← Original module (kept temporarily, then removed)
└── docs/
```

---

## Gradle Configuration

### `settings.gradle.kts`
```kotlin
pluginManagement { ... }

dependencyResolutionManagement { ... }

rootProject.name = "MBPTODABookingApp"

include(":core")
include(":app-passenger")
include(":app-driver")
// include(":app") ← comment out original once migration is done
```

### `core/build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.mbptoda.core"
    compileSdk = 36
    defaultConfig { minSdk = 24 }
}

dependencies {
    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    // Architecture
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.coroutines.android)

    // Security
    implementation(libs.security.crypto)

    // Gson
    implementation(libs.gson)
}
```

### `app-passenger/build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.mbptoda.passenger"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.mbptoda.passenger"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(project(":core"))        // ← pulls in all shared code

    // UI
    implementation(libs.material)
    implementation(libs.recyclerview)
    implementation(libs.viewpager2)
    implementation(libs.appcompat)

    // Maps & Location
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    // Firebase Analytics (app-level only)
    implementation(libs.firebase.analytics)
}
```

### `app-driver/build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.mbptoda.driver"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.mbptoda.driver"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(project(":core"))        // ← pulls in all shared code

    // UI
    implementation(libs.material)
    implementation(libs.recyclerview)
    implementation(libs.viewpager2)
    implementation(libs.appcompat)

    // Maps & Location
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    // Firebase Analytics
    implementation(libs.firebase.analytics)
}
```

---

## What Goes Where

### `:core` module — Shared by both apps

| File (current `app/`) | Destination in `:core` | Notes |
|---|---|---|
| `data/api/ApiClient.kt` | `data/api/ApiClient.kt` | No changes needed |
| `data/api/ApiService.kt` | `data/api/ApiService.kt` | No changes needed |
| `data/api/ApiResponse.kt` | `data/api/ApiResponse.kt` | No changes needed |
| `data/models/User.kt` | `data/models/User.kt` | No changes needed |
| `data/models/AuthModels.kt` | `data/models/AuthModels.kt` | No changes needed |
| `data/models/Booking.kt` | `data/models/Booking.kt` | No changes needed |
| `data/models/DriverModels.kt` | `data/models/DriverModels.kt` | No changes needed |
| `data/models/FcmModels.kt` | `data/models/FcmModels.kt` | No changes needed |
| `data/models/AdminModels.kt` | ~~Not migrated~~ | Admin is PHP web now |
| `data/repository/BaseRepository.kt` | `data/repository/BaseRepository.kt` | No changes needed |
| `data/repository/AuthRepository.kt` | `data/repository/AuthRepository.kt` | No changes needed |
| `data/repository/UserRepository.kt` | `data/repository/UserRepository.kt` | No changes needed |
| `data/repository/BookingRepository.kt` | `data/repository/BookingRepository.kt` | No changes needed |
| `data/repository/AdminRepository.kt` | ~~Not migrated~~ | Admin is PHP web now |
| `data/local/PrefsManager.kt` | `data/local/PrefsManager.kt` | No changes needed |
| `services/PTODAFirebaseMessagingService.kt` | `services/PTODAFirebaseMessagingService.kt` | No changes needed |
| `utils/Constants.kt` | `utils/Constants.kt` | No changes needed |
| `utils/Resource.kt` | `utils/Resource.kt` | No changes needed |

### `:app-passenger` module

| File (current `app/`) | Destination in `:app-passenger` |
|---|---|
| `ui/auth/LoginActivity.kt` | `ui/auth/LoginActivity.kt` |
| `ui/auth/RegisterActivity.kt` | `ui/auth/RegisterActivity.kt` |
| `ui/auth/AuthViewModel.kt` | `ui/auth/AuthViewModel.kt` |
| `ui/passenger/PassengerHomeActivity.kt` | `ui/home/PassengerHomeActivity.kt` |
| `ui/passenger/PassengerViewModel.kt` | `ui/home/PassengerViewModel.kt` |
| `ui/passenger/BookRideActivity.kt` | `ui/booking/BookRideActivity.kt` |
| `ui/passenger/RideStatusActivity.kt` | `ui/booking/RideStatusActivity.kt` |
| `ui/passenger/RideHistoryFragment.kt` | `ui/history/RideHistoryFragment.kt` |
| `ui/theme/` | `ui/theme/` |
| `PTODAApplication.kt` | `PassengerApplication.kt` (rename) |

**Layouts to move to `app-passenger/src/main/res/layout/`:**
- `activity_login.xml`
- `activity_register.xml`
- `activity_passenger_home.xml`
- `activity_book_ride.xml`
- `activity_ride_status.xml`
- `fragment_ride_history.xml`
- `item_booking_history.xml`

**AndroidManifest.xml:**
```xml
<application android:name=".PassengerApplication" ...>
    <activity android:name=".ui.auth.LoginActivity" android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>
    <activity android:name=".ui.auth.RegisterActivity" />
    <activity android:name=".ui.home.PassengerHomeActivity" />
    <activity android:name=".ui.booking.BookRideActivity" />
    <activity android:name=".ui.booking.RideStatusActivity" />

    <service android:name="com.mbptoda.core.services.PTODAFirebaseMessagingService"
        android:exported="false">
        <intent-filter>
            <action android:name="com.google.firebase.MESSAGING_EVENT" />
        </intent-filter>
    </service>
</application>
```

**FCM notifications received:**
- `BOOKING_ACCEPTED` — Driver accepted the ride
- `BOOKING_REJECTED` — Driver rejected the ride
- `DRIVER_ARRIVING` — Driver is near pickup point

### `:app-driver` module

| File (current `app/`) | Destination in `:app-driver` |
|---|---|
| `ui/auth/LoginActivity.kt` | `ui/auth/LoginActivity.kt` |
| `ui/auth/RegisterActivity.kt` | `ui/auth/RegisterActivity.kt` |
| `ui/auth/AuthViewModel.kt` | `ui/auth/AuthViewModel.kt` |
| `ui/driver/DriverHomeActivity.kt` | `ui/home/DriverHomeActivity.kt` |
| `ui/driver/DriverViewModel.kt` | `ui/home/DriverViewModel.kt` |
| `ui/driver/DriverDashboardFragment.kt` | `ui/dashboard/DriverDashboardFragment.kt` |
| `ui/driver/DriverRequestsFragment.kt` | `ui/requests/DriverRequestsFragment.kt` |
| `ui/driver/RideRequestActivity.kt` | `ui/requests/RideRequestActivity.kt` |
| `ui/driver/RideRequestsAdapter.kt` | `ui/requests/RideRequestsAdapter.kt` |
| `ui/driver/DriverStatusFragment.kt` | `ui/status/DriverStatusFragment.kt` |
| `ui/driver/ActiveRideActivity.kt` | `ui/ride/ActiveRideActivity.kt` |
| `ui/theme/` | `ui/theme/` |
| `PTODAApplication.kt` | `DriverApplication.kt` (rename) |

**Layouts to move to `app-driver/src/main/res/layout/`:**
- `activity_login.xml`
- `activity_register.xml`
- `activity_driver_home.xml`
- `activity_ride_request.xml`
- `activity_active_ride.xml`
- `fragment_driver_dashboard.xml`
- `fragment_driver_requests.xml`
- `fragment_driver_status.xml`
- `item_ride_request.xml`

**AndroidManifest.xml:**
```xml
<application android:name=".DriverApplication" ...>
    <activity android:name=".ui.auth.LoginActivity" android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>
    <activity android:name=".ui.auth.RegisterActivity" />
    <activity android:name=".ui.home.DriverHomeActivity" />
    <activity android:name=".ui.requests.RideRequestActivity" />
    <activity android:name=".ui.ride.ActiveRideActivity" />

    <service android:name="com.mbptoda.core.services.PTODAFirebaseMessagingService"
        android:exported="false">
        <intent-filter>
            <action android:name="com.google.firebase.MESSAGING_EVENT" />
        </intent-filter>
    </service>
</application>
```

**FCM notifications received:**
- `NEW_RIDE_REQUEST` — New passenger booking available
- `BOOKING_CANCELLED` — Passenger cancelled before driver arrived

---

## Phase 4 — Admin Web App (`MBPTODAAdmin`)

Completely replaces `ui/admin/` with a PHP web dashboard. Lives in XAMPP `htdocs/MBPTODAAdmin/`.

### Project structure
```
htdocs/MBPTODAAdmin/
├── config.php                  ← DB credentials, base URL
├── index.php                   ← Admin login page
├── dashboard.php               ← Overview: users, drivers, bookings, revenue
├── users.php                   ← List passengers, search, deactivate
├── drivers.php                 ← Approve/reject drivers, view active drivers
├── bookings.php                ← All bookings with status filter and date range
├── includes/
│   ├── auth.php                ← Session check — included at top of every page
│   ├── db.php                  ← PDO connection (reuse from existing backend)
│   ├── header.php              ← Nav sidebar, page header
│   └── footer.php
└── assets/
    ├── css/
    │   └── admin.css
    └── js/
        └── admin.js
```

### Pages and features

| Page | Replaces Android Class | Features |
|---|---|---|
| `index.php` | `AdminDashboardActivity` (login part) | Session login |
| `dashboard.php` | `AdminStatsFragment` | Total users, drivers, bookings, revenue |
| `users.php` | `ManageUsersActivity` + `UsersAdapter` | List, search, deactivate passengers |
| `drivers.php` | `PendingDriversAdapter` | Approve/reject pending drivers, list active |
| `bookings.php` | `AdminBookingsFragment` + `AdminBookingsAdapter` | All bookings, filter by status/date |

### Auth strategy
- PHP `$_SESSION` — no JWT needed for server-rendered web
- `includes/auth.php` added at the top of every protected page:
```php
session_start();
if (!isset($_SESSION['admin_id'])) {
    header('Location: index.php');
    exit;
}
```
- Separate `admins` table in the database (not mixed with regular users)

---

## Android Studio — Run Configurations

After the restructure, you will have two run configurations in the same Android Studio window:

| Config name | Module | Output |
|---|---|---|
| `app-passenger` | `:app-passenger` | Passenger APK |
| `app-driver` | `:app-driver` | Driver APK |

Switch between them using the run config dropdown in the toolbar. Both build from the same window.

---

## Migration Phases

### Phase 1 — Set up Gradle multi-module structure
- Create `core/`, `app-passenger/`, `app-driver/` folders
- Update `settings.gradle.kts` to include all three modules
- Create `build.gradle.kts` for each module
- Verify project syncs in Android Studio with no errors (no code moved yet)

### Phase 2 — Populate `:core`
- Move all shared files from `app/` into `core/`
- Update package names from `com.example.mbptodabookingapp` to `com.mbptoda.core`
- Verify `:core` compiles on its own

### Phase 3 — Build `:app-passenger`
- Move passenger + auth files from `app/` into `app-passenger/`
- Add `implementation(project(":core"))` dependency
- Update package names to `com.mbptoda.passenger`
- Copy and adapt layouts
- Write `PassengerApplication.kt`
- Write `AndroidManifest.xml`
- Run on device/emulator — test: Login → Book Ride → Ride Status → History

### Phase 4 — Build `:app-driver`
- Move driver + auth files from `app/` into `app-driver/`
- Add `implementation(project(":core"))` dependency
- Update package names to `com.mbptoda.driver`
- Copy and adapt layouts
- Write `DriverApplication.kt`
- Write `AndroidManifest.xml`
- Run on device/emulator — test: Login → Receive Request → Accept → Active Ride

### Phase 5 — Build PHP Admin Panel
- Create `htdocs/MBPTODAAdmin/` in XAMPP
- Build pages using existing PHP backend endpoints
- Test: Login → Approve driver → View bookings

### Phase 6 — Clean up
- Remove or archive the original `app/` module
- Update `settings.gradle.kts` to exclude `:app`
- Update docs

---

## Package Naming Change

| Location | Old package | New package |
|---|---|---|
| `:core` | `com.example.mbptodabookingapp` | `com.mbptoda.core` |
| `:app-passenger` | `com.example.mbptodabookingapp` | `com.mbptoda.passenger` |
| `:app-driver` | `com.example.mbptodabookingapp` | `com.mbptoda.driver` |

> When moving files, update the `package` declaration at the top of each `.kt` file and fix all `import` statements that reference the old package name.

---

## Key Decisions

**Why keep everything in one git repo?**
Shared history, easier to track changes across modules, one place to open in Android Studio.

**Why session auth for admin instead of JWT?**
The admin panel is server-rendered PHP — `$_SESSION` is the natural fit. JWT adds complexity for no benefit in a web-only context.

**Do we need two separate Firebase projects?**
No. One Firebase project serves both apps. Each app gets its own `google-services.json` registered under separate package names (`com.mbptoda.passenger` and `com.mbptoda.driver`).

**What happens to `MainActivity.kt` (the role-router)?**
Deleted. Each app always starts at Login → its own home. No role-routing needed.

**What happens to the original `app/` module?**
Kept read-only during migration as a reference. Removed in Phase 6 once both new modules are verified.

---

*Created: 2026-06-26*  
*Updated: 2026-06-26 — Revised to multi-module approach (single Android Studio project)*  
*Status: Planning — not yet started*
