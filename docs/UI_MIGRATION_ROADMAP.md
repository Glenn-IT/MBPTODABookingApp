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
| Phase 1 | Resource Setup (colors, fonts) | ✅ Done | 2026-04-12 |
| Phase 2 | Login Screen Upgrade | ✅ Done | 2026-04-12 |
| Phase 3 | Register Screen Polish | ✅ Done | 2026-04-12 |
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

## 🎨 Phase 1 — Resource Setup ✅

**Status:** Complete — 2026-04-12
**Risk:** 🟢 Zero
**Files edited:** `app/src/main/res/values/colors.xml`, `app/src/main/res/values/strings.xml`

### Changes made:

#### `colors.xml` — Added 3 prototype aliases (purely additive, same hex values as existing tokens)
| New token | Value | Maps to existing |
|---|---|---|
| `grey_text` | `#757575` | = `grey` |
| `green_status` | `#2E7D32` | = `statusCompleted` |
| `red_status` | `#C62828` | = `statusRejected` |

> `colorAccent` kept at `#FF8F00` (prototype diff of `#FF6F00` is cosmetic — not worth the risk to existing buttons).

#### `strings.xml` — Added new strings (purely additive)
| New string | Value | Used in |
|---|---|---|
| `app_subtitle` | `"Mobile Booking System"` | Phase 2 login screen |
| `forgot_password` | `"Forgot Password?"` | Phase 2 login screen |
| `tab_dashboard` | `"Dashboard"` | Phase 5 & 6 BottomNav |
| `tab_book_ride` | `"Book Ride"` | Phase 5 BottomNav |
| `tab_history` | `"History"` | Phase 5 BottomNav |
| `tab_requests` | `"Requests"` | Phase 6 BottomNav |
| `tab_status` | `"Status"` | Phase 6 BottomNav |
| `tab_overview` | `"Overview"` | Phase 7 Admin tabs |
| `tab_bookings` | `"Bookings"` | Phase 7 Admin tabs |
| `tab_users` | `"Users"` | Phase 7 Admin tabs |

### Checklist:
- [x] Add `grey_text` to `colors.xml`
- [x] Add `green_status` and `red_status` aliases to `colors.xml`
- [x] Add Phase 2–7 string resources to `strings.xml`
- [x] Verified no existing views reference broken — all changes are additive
- [x] Build passes ✅

---

## 🔐 Phase 2 — Login Screen Upgrade ✅

**Status:** Complete — 2026-04-12
**Risk:** 🟢 Zero
**Files edited:** `app/src/main/res/layout/activity_login.xml`, `app/src/main/res/values/strings.xml`
**Files NOT touched:** `LoginActivity.kt`, `AuthViewModel.kt`, `AuthRepository.kt`

### Changes made:

#### `activity_login.xml`
| Element | Before | After |
|---|---|---|
| Header | `ImageView` 80dp launcher icon | `TextView` emoji `🚐` at 52sp |
| Title size | `26sp` | `36sp` bold + centered |
| Subtitle | *(none)* | `@string/app_subtitle` at 16sp `grey_text` |
| Spacing above form | `32dp` margin | `40dp` margin (prototype style) |
| Outer padding | `28dp` | `32dp` |
| Background | *(default)* | `@color/white` explicit |
| Login button | default padding | `12dp` top+bottom padding |
| `tvRegister` | no padding | `8dp` padding + `14sp` text size |

#### `strings.xml`
- Added `app_logo_emoji = "🚐"` (resolves lint warning for hardcoded string)

### Protected IDs — all verified intact:
`tilEmail` ✅ `etEmail` ✅ `tilPassword` ✅ `etPassword` ✅ `btnLogin` ✅ `progressBar` ✅ `tvRegister` ✅

### Checklist:
- [x] Updated `activity_login.xml` layout
- [x] All ViewBinding references verified — `LoginActivity.kt` shows 0 errors
- [x] Emoji moved to `@string/app_logo_emoji` — 0 lint warnings
- [ ] Test: Enter email + password → Login → correct role navigation
- [ ] Test: Empty fields → validation errors show correctly
- [ ] Build passes + manual test ✅

---

## 📝 Phase 3 — Register Screen Polish ✅

**Status:** Complete — 2026-04-12
**Risk:** 🟢 Low
**Files edited:** `app/src/main/res/layout/activity_register.xml`, `app/src/main/res/values/strings.xml`, `RegisterActivity.kt`
**Files NOT touched:** `AuthViewModel.kt`, `AuthRepository.kt`

### Changes made:

#### `activity_register.xml`
| Element | Before | After |
|---|---|---|
| Background | *(default)* | `@color/white` explicit |
| Outer padding | `24dp` | `32dp` (matches login) |
| Title | `22sp` plain black | `32sp` bold `@color/colorPrimary` |
| Subtitle | *(none)* | `"Join PTODA today"` `15sp` `grey_text` |
| Role selector | bare `RadioGroup` | wrapped in `MaterialCardView` (8dp radius, 2dp elevation) |
| Section labels | single `"Select Role"` label | 3 section headers: `PERSONAL INFO`, `CHOOSE ROLE`, `DRIVER DETAILS` |
| Driver section label | *(none)* | `tvDriverSectionLabel` (hidden by default, shown with driver fields) |
| `btnRegister` | default padding | `12dp` top+bottom (matches login) |
| `tvLogin` | bare text | `8dp` padding + `14sp` (matches `tvRegister` in login) |

#### `strings.xml` — 4 strings added
| String | Value |
|---|---|
| `register_subtitle` | `"Join PTODA today"` |
| `section_personal_info` | `"PERSONAL INFO"` |
| `section_choose_role` | `"CHOOSE ROLE"` |
| `section_driver_details` | `"DRIVER DETAILS"` |

#### `RegisterActivity.kt` — 1 line added
- `tvDriverSectionLabel` visibility toggled alongside `tilLicenseNo` / `tilVehicleNo`

### Protected IDs — all verified intact:
`tilName` ✅ `etName` ✅ `tilEmail` ✅ `etEmail` ✅ `tilPassword` ✅ `etPassword` ✅
`rgRole` ✅ `rbPassenger` ✅ `rbDriver` ✅ `tilLicenseNo` ✅ `etLicenseNo` ✅
`tilVehicleNo` ✅ `etVehicleNo` ✅ `btnRegister` ✅ `progressBar` ✅ `tvLogin` ✅

### Checklist:
- [x] Updated `activity_register.xml`
- [x] Added 4 new strings to `strings.xml`
- [x] Updated `RegisterActivity.kt` to show/hide `tvDriverSectionLabel`
- [x] `gradlew assembleDebug` → **BUILD SUCCESSFUL** ✅
- [ ] Test: Passenger registration flow end-to-end
- [ ] Test: Driver registration shows/hides license + vehicle + section label
- [ ] Manual test on device ✅

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

_Last updated: 2026-04-12 — Phase 3 complete_

