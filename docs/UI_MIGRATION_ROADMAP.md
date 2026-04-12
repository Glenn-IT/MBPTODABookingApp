# UI/UX Migration Roadmap
## MBPTODABookingApp ← PTODA_Prototype

> **Started:** April 12, 2026
> **Goal:** Transform `MBPTODABookingApp` into a polished, production-ready app by applying
> the UI/UX design patterns from `PTODA_Prototype` — **without breaking any existing backend logic.**
>
> **Rule:** Each phase is completed, tested, and confirmed before proceeding to the next.

---

## 📊 Progress Tracker

| Phase | Name | Status | Date Completed |
|---|---|---|---|
| Phase 0 | Analysis & Feasibility | ✅ Done | 2026-04-12 |
| Phase 1 | Resource Setup (colors, fonts) | ⬜ Pending | — |
| Phase 2 | Login Screen Upgrade | ⬜ Pending | — |
| Phase 3 | Register Screen Polish | ⬜ Pending | — |
| Phase 4 | Booking Screen Card UI | ⬜ Pending | — |
| Phase 5 | Passenger BottomNav Shell | ⬜ Pending | — |
| Phase 6 | Driver BottomNav Shell | ⬜ Pending | — |
| Phase 7 | Admin Tabs + ViewPager2 | ⬜ Pending | — |
| Phase 8 | List Item Card Redesign | ⬜ Pending | — |
| Phase 9 | Global Theme Consistency | ⬜ Pending | — |
| Phase 10 | End-to-End Testing | ⬜ Pending | — |

---

## 📐 Phase 0 — Analysis & Feasibility ✅

**Status:** Complete
**Output:** `docs/FEASIBILITY_REPORT.md`

### What was done:
- Cloned and inspected all layouts in `PTODA_Prototype`
- Mapped every screen between both projects
- Identified critical view IDs that must not be changed
- Documented prototype screens that contain hardcoded/fake data
- Confirmed color palettes are nearly identical (`#1565C0` primary)
- Confirmed ViewBinding vs DataBinding compatibility

---

## 🎨 Phase 1 — Resource Setup

**Status:** ⬜ Pending
**Risk:** 🟢 Zero
**Files to edit:** `app/src/main/res/values/colors.xml`

### Goal:
Merge the prototype's color tokens into the main project so all future layout changes
can reference them correctly.

### Changes:
- Add `grey_text` alias (`#757575`) — used extensively in prototype layouts
- Align `colorAccent` (main `#FF8F00` → prototype `#FF6F00`) — optional, minor

### Checklist:
- [ ] Add `grey_text` to `colors.xml`
- [ ] Verify no existing views break with the alias added
- [ ] Build passes ✅

---

## 🔐 Phase 2 — Login Screen Upgrade

**Status:** ⬜ Pending
**Risk:** 🟢 Low
**Files to edit:** `app/src/main/res/layout/activity_login.xml`
**Files NOT to touch:** `LoginActivity.kt`, `AuthViewModel.kt`, `AuthRepository.kt`

### Goal:
Apply the prototype's visual branding (emoji logo, subtitle, typography) to the login screen
while keeping all Material components and existing view IDs intact.

### Prototype elements to bring over:
- 🚐 PTODA emoji + large bold title
- `"Mobile Booking System"` subtitle in grey
- Wider padding and centered vertical layout
- Consistent spacing between elements

### Protected IDs (must stay unchanged):
`etEmail`, `etPassword`, `tilEmail`, `tilPassword`, `btnLogin`, `progressBar`, `tvRegister`

### Checklist:
- [ ] Update `activity_login.xml` layout
- [ ] Verify all ViewBinding references still compile
- [ ] Test: Enter email + password → Login → correct role navigation
- [ ] Test: Empty fields → validation errors show correctly
- [ ] Build passes + manual test ✅

---

## 📝 Phase 3 — Register Screen Polish

**Status:** ⬜ Pending
**Risk:** 🟢 Low
**Files to edit:** `app/src/main/res/layout/activity_register.xml`
**Files NOT to touch:** `RegisterActivity.kt`, `AuthViewModel.kt`

### Goal:
Apply consistent spacing, typography, and section headers to match the style established in Phase 2.

### Changes:
- Add section label above role selector
- Consistent padding and margin values
- Match button style to updated login screen

### Protected IDs:
`etName`, `etEmail`, `etPassword`, `tilLicenseNo`, `tilVehicleNo`, `btnRegister`, `rgRole`, `rbPassenger`, `rbDriver`, `progressBar`, `tvLogin`

### Checklist:
- [ ] Update `activity_register.xml`
- [ ] Test: Passenger registration flow end-to-end
- [ ] Test: Driver registration shows/hides license + vehicle fields
- [ ] Build passes + manual test ✅

---

## 🚗 Phase 4 — Booking Screen Card UI

**Status:** ⬜ Pending
**Risk:** 🟡 Low-Medium
**Files to edit:** `app/src/main/res/layout/activity_book_ride.xml`
**Files NOT to touch:** `BookRideActivity.kt`, `PassengerViewModel.kt`, `BookingRepository.kt`

### Goal:
Borrow the prototype's card-based booking form aesthetic (estimated fare card, cleaner labels)
while keeping the full map + coordinate fields intact.

### Prototype elements to bring over:
- `MaterialCardView` wrapper around booking form sections
- Estimated Fare display row (styled card at bottom of form)
- Section header typography for "Pickup" / "Dropoff" labels

### What to NOT touch:
- Map `FragmentContainerView` (`@+id/map`)
- Tap-mode toggle buttons (`btnModePickup`, `btnModeDropoff`)
- All coordinate input fields (`etPickupLat`, `etPickupLng`, `etDropoffLat`, `etDropoffLng`)
- `btnRequestRide`, `btnUseCurrentLocation`, `progressBar`

### Checklist:
- [ ] Wrap form sections in `MaterialCardView`
- [ ] Add fare display card (static UI — wire up to data later)
- [ ] All existing IDs verified intact
- [ ] Test: Full booking flow (tap map → fill form → submit → ride status screen)
- [ ] Build passes + manual test ✅

---

## 👤 Phase 5 — Passenger BottomNav Shell

**Status:** ⬜ Pending
**Risk:** 🟡 Medium
**Files to create:**
- `app/src/main/res/menu/menu_passenger.xml`
- `app/src/main/java/.../ui/passenger/PassengerDashboardFragment.kt`
- `app/src/main/java/.../ui/passenger/RideHistoryFragment.kt`
**Files to edit:**
- `app/src/main/res/layout/activity_passenger_home.xml`
- `PassengerHomeActivity.kt`

### Goal:
Introduce a `BottomNavigationView` to the passenger home screen with tabs:
`Dashboard` | `Book Ride` | `History`

### Architecture approach:
- Keep the map in `PassengerHomeActivity` as-is
- Add `BottomNavigationView` at the bottom
- Create lightweight Fragment shells for Dashboard and History tabs
- Booking tab navigates to `BookRideActivity` (no change to booking flow)

### Checklist:
- [ ] Create `menu_passenger.xml` with 3 items
- [ ] Add `BottomNavigationView` + `FrameLayout` to `activity_passenger_home.xml`
- [ ] Create `PassengerDashboardFragment` (welcome card + quick stats)
- [ ] Create `RideHistoryFragment` (wraps existing history API call)
- [ ] Connect fragments in `PassengerHomeActivity`
- [ ] Test: Tab switching works
- [ ] Test: Book Ride tab still navigates correctly
- [ ] Test: History tab loads real data from API
- [ ] Build passes + manual test ✅

---

## 🏍️ Phase 6 — Driver BottomNav Shell

**Status:** ⬜ Pending
**Risk:** 🟡 Medium
**Files to create:**
- `app/src/main/res/menu/menu_driver.xml`
- `app/src/main/java/.../ui/driver/DriverDashboardFragment.kt`
- `app/src/main/java/.../ui/driver/DriverRequestsFragment.kt`
**Files to edit:**
- `app/src/main/res/layout/activity_driver_home.xml`
- `DriverHomeActivity.kt`

### Goal:
Introduce `BottomNavigationView` to the driver screen with tabs:
`Dashboard` | `Ride Requests` | `Status`

### Architecture approach:
- Move `rvRequests` RecyclerView + `btnRefresh` into `DriverRequestsFragment`
- Map stays in the activity layer
- Dashboard tab shows a status card (online/offline toggle)

### Checklist:
- [ ] Create `menu_driver.xml` with 3 items
- [ ] Create `DriverDashboardFragment` (status card)
- [ ] Create `DriverRequestsFragment` (moves existing RecyclerView + logic)
- [ ] Update `DriverHomeActivity` to use BottomNav
- [ ] Test: Ride requests load correctly in new fragment
- [ ] Test: Accept / Reject actions still work
- [ ] Test: Map still updates driver location
- [ ] Build passes + manual test ✅

---

## 🛡️ Phase 7 — Admin Tabs + ViewPager2

**Status:** ⬜ Pending
**Risk:** 🟡 Medium
**Files to create:**
- `app/src/main/java/.../ui/admin/AdminPagerAdapter.kt`
- `app/src/main/java/.../ui/admin/AdminStatsFragment.kt`
- `app/src/main/java/.../ui/admin/AdminBookingsFragment.kt`
**Files to edit:**
- `app/src/main/res/layout/activity_admin_dashboard.xml`
- `AdminDashboardActivity.kt`

### Goal:
Replace the single-scroll admin screen with a `TabLayout + ViewPager2` layout:
`Overview` | `Bookings` | `Users`

### Architecture approach:
- Move existing stats cards into `AdminStatsFragment`
- Move booking list into `AdminBookingsFragment`
- `ManageUsersActivity` stays — Users tab can launch it OR become a fragment

### Checklist:
- [ ] Create `AdminPagerAdapter`
- [ ] Create `AdminStatsFragment` (moves existing `tvUserCount`, `tvPendingCount`, `tvBookingCount`)
- [ ] Create `AdminBookingsFragment`
- [ ] Update `activity_admin_dashboard.xml` to `TabLayout + ViewPager2`
- [ ] Verify all `AdminViewModel` observers still fire correctly
- [ ] Test: Stats load on Overview tab
- [ ] Test: Manage Users still accessible
- [ ] Build passes + manual test ✅

---

## 📦 Phase 8 — List Item Card Redesign

**Status:** ⬜ Pending
**Risk:** 🟢 Low
**Files to edit:**
- `app/src/main/res/layout/item_ride_request.xml`
- `app/src/main/res/layout/item_user.xml`
- `app/src/main/res/layout/item_pending_driver.xml`

### Goal:
Wrap all RecyclerView item layouts in `MaterialCardView` with consistent elevation and corners.
Apply prototype's card style (8dp corner radius, 4dp elevation, `#F5F5F5` background rows).

### Checklist:
- [ ] Update `item_ride_request.xml` with card wrapper
- [ ] Update `item_user.xml` with card wrapper
- [ ] Update `item_pending_driver.xml` with card wrapper
- [ ] Verify adapters compile — no ID changes needed
- [ ] Build passes + visual check ✅

---

## 🎨 Phase 9 — Global Theme Consistency

**Status:** ⬜ Pending
**Risk:** 🟢 Low
**Files to edit:**
- `app/src/main/res/values/themes.xml`
- `app/src/main/res/values/colors.xml`
- `app/src/main/res/values/styles.xml` *(if exists)*

### Goal:
Ensure consistent look across all screens: typography scale, button corner radius,
toolbar style, input field style.

### Changes:
- Set global `ShapeAppearance` for buttons (8dp corners)
- Apply `TextAppearance.MaterialComponents` headline styles
- Ensure all Toolbars use `@color/colorPrimary` background consistently
- Final color audit between all screens

### Checklist:
- [ ] Update `themes.xml` with shape + typography attributes
- [ ] Visual audit: all screens match design intent
- [ ] Dark mode check (if applicable)
- [ ] Build passes ✅

---

## 🧪 Phase 10 — End-to-End Testing

**Status:** ⬜ Pending
**Risk:** 🟢 Low (if all prior phases passed)

### Test Matrix:

| Test Case | Role | Expected Result | Pass? |
|---|---|---|---|
| Login with valid credentials (passenger) | Passenger | → `PassengerHomeActivity` | ⬜ |
| Login with valid credentials (driver) | Driver | → `DriverHomeActivity` | ⬜ |
| Login with valid credentials (admin) | Admin | → `AdminDashboardActivity` | ⬜ |
| Login with wrong password | Any | Error toast shown | ⬜ |
| Register new passenger | — | Account created → login | ⬜ |
| Register new driver | — | Account created, pending approval | ⬜ |
| Book a ride (passenger) | Passenger | Booking submitted → status screen | ⬜ |
| View ride history | Passenger | Real data loads from API | ⬜ |
| View ride requests | Driver | Pending requests load from API | ⬜ |
| Accept ride request | Driver | Status updates to accepted | ⬜ |
| Complete ride | Driver | Status updates to completed | ⬜ |
| View admin stats | Admin | User + booking counts load | ⬜ |
| Approve pending driver | Admin | Driver status → approved | ⬜ |
| Manage users | Admin | User list loads, actions work | ⬜ |

### Checklist:
- [ ] All test cases pass on physical device
- [ ] No crashes on any screen
- [ ] API calls succeed on Wi-Fi (`192.168.0.100`)
- [ ] FCM notifications still fire correctly
- [ ] App is production-ready ✅

---

## 📁 Related Documents

| Document | Purpose |
|---|---|
| `FEASIBILITY_REPORT.md` | Full analysis of both repos, risks, and decisions |
| `docs/flows/AUTH_FLOW.md` | Login & register flow documentation |
| `docs/flows/BOOKING_FLOW.md` | Full ride lifecycle documentation |
| `docs/api/AUTH.md` | API contract for login/register |
| `docs/BUGS_AND_FIXES.md` | Log any bugs found during migration here |

---

_Last updated: 2026-04-12_

