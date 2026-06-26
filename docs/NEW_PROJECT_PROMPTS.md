# New Project Starter Prompts

Use these prompts when creating each new project in a fresh Android Studio / code editor session.
The current repo (`MBPTODABookingApp`) serves as the reference implementation.

---

## Passenger App — `MBPTODAPassengerApp`

```
I'm building a new Android app called MBPTODAPassengerApp for passengers to book tricycle rides.

Reference repo: C:\Users\GLENN\AndroidStudioProjects\MBPTODABookingApp
Restructuring plan: C:\Users\GLENN\AndroidStudioProjects\MBPTODABookingApp\docs\RESTRUCTURING_PLAN.md

The reference repo is a working monolithic app (Passenger + Driver + Admin in one).
We are extracting only the Passenger side into this new standalone project.

Before writing any code:
1. Read the restructuring plan doc
2. Read the following files from the reference repo so you understand the existing implementation:
   - data/api/ApiClient.kt, ApiService.kt, ApiResponse.kt
   - data/models/ (all files)
   - data/repository/AuthRepository.kt, BookingRepository.kt, BaseRepository.kt
   - data/local/PrefsManager.kt
   - utils/Constants.kt, Resource.kt
   - ui/auth/LoginActivity.kt, RegisterActivity.kt, AuthViewModel.kt
   - ui/passenger/ (all files)

Then implement the passenger app using:
- Package: com.mbptoda.passenger
- Min SDK: 24, Target SDK: 36
- Same tech stack: Retrofit, Firebase FCM, Google Maps, EncryptedSharedPreferences, MVVM + Repository
- Direct to LoginActivity on launch (no role-router)
- The backend is PHP + MySQL running on local XAMPP — base URL comes from PrefsManager

Match the existing logic exactly. Do not redesign or add features not already in the reference.
```

---

## Driver App — `MBPTODADriverApp`

```
I'm building a new Android app called MBPTODADriverApp for tricycle drivers to receive and manage ride requests.

Reference repo: C:\Users\GLENN\AndroidStudioProjects\MBPTODABookingApp
Restructuring plan: C:\Users\GLENN\AndroidStudioProjects\MBPTODABookingApp\docs\RESTRUCTURING_PLAN.md

The reference repo is a working monolithic app (Passenger + Driver + Admin in one).
We are extracting only the Driver side into this new standalone project.

Before writing any code:
1. Read the restructuring plan doc
2. Read the following files from the reference repo so you understand the existing implementation:
   - data/api/ApiClient.kt, ApiService.kt, ApiResponse.kt
   - data/models/ (all files)
   - data/repository/AuthRepository.kt, BookingRepository.kt, BaseRepository.kt
   - data/local/PrefsManager.kt
   - utils/Constants.kt, Resource.kt
   - ui/auth/LoginActivity.kt, RegisterActivity.kt, AuthViewModel.kt
   - ui/driver/ (all files)
   - services/PTODAFirebaseMessagingService.kt

Then implement the driver app using:
- Package: com.mbptoda.driver
- Min SDK: 24, Target SDK: 36
- Same tech stack: Retrofit, Firebase FCM, Google Maps, EncryptedSharedPreferences, MVVM + Repository
- Direct to LoginActivity on launch (no role-router)
- FCM must handle NEW_RIDE_REQUEST and BOOKING_CANCELLED notifications
- The backend is PHP + MySQL running on local XAMPP — base URL comes from PrefsManager

Match the existing logic exactly. Do not redesign or add features not already in the reference.
```

---

## Admin Web App — `MBPTODAAdmin`

```
I'm building a PHP web admin dashboard called MBPTODAAdmin for managing a tricycle booking system.

Reference repo (Android monolithic app): C:\Users\GLENN\AndroidStudioProjects\MBPTODABookingApp
Restructuring plan: C:\Users\GLENN\AndroidStudioProjects\MBPTODABookingApp\docs\RESTRUCTURING_PLAN.md

Before writing any code:
1. Read the restructuring plan doc (especially Phase 4 — Admin Web App section)
2. Read these files from the reference repo to understand what the admin currently does:
   - ui/admin/ (all files — AdminDashboardActivity, ManageUsersActivity, AdminViewModel, AdminRepository, all adapters)
   - data/models/AdminModels.kt
   - data/api/ApiService.kt (look for admin-related endpoints)
   - utils/Constants.kt (admin statuses and roles)

The admin panel will be placed in XAMPP htdocs/MBPTODAAdmin/ and connects directly
to the same MySQL database the Android apps use via PDO.

Build the following pages:
- index.php — Admin login with $_SESSION auth
- dashboard.php — Stats: total users, drivers, bookings, revenue
- users.php — List all passengers, search, deactivate
- drivers.php — Pending driver approvals (approve/reject), list of active drivers
- bookings.php — All bookings with filter by status and date range
- includes/auth.php — Session check included on every protected page
- includes/db.php — PDO connection
- includes/header.php / footer.php — Shared nav and layout
- config.php — DB credentials and base URL

Use Bootstrap 5 for the UI. Keep it functional and clean — no need for a complex frontend framework.
Auth is $_SESSION only — no JWT. Admins have a separate admins table in the database.

Match the feature set of the existing Android admin screens exactly. Do not add features not already present.
```

---

## How to Use These Prompts

1. Create the new project in Android Studio (or open a folder in VS Code for the PHP admin)
2. Open Claude Code in that new project directory
3. Paste the relevant prompt above as your first message
4. Claude will read the reference repo and restructuring plan before writing any code

> The reference repo path (`C:\Users\GLENN\AndroidStudioProjects\MBPTODABookingApp`) must remain
> accessible on your machine while working on the new projects.

---

*Created: 2026-06-26*
