# PTODA Booking System — Documentation Index

**Last updated:** 2026-06-21

---

## Files in This Folder

| File | Purpose | When to use |
|------|---------|-------------|
| **`DEVELOPMENT_CHECKLIST.md`** | Master phase-by-phase task checklist covering all phases (setup, API, Android, bugs, missing features, security, production) | Track what's done and what's next |
| **`API_REFERENCE.md`** | All API endpoints — request/response shapes, error codes, Kotlin `ApiService` signatures, Postman quick reference | When adding/changing endpoints or debugging network calls |
| **`DATA_MODELS.md`** | MySQL table schemas + Kotlin data classes for all tables (users, driver_info, bookings, booking_logs, fcm_tokens) | When changing DB columns or Kotlin models |
| **`FLOWS.md`** | Step-by-step flows: Auth (register/login), Booking lifecycle, Driver Approval, Android network+FCM+Maps setup | Understanding how screens and API calls connect |
| **`BUGS_AND_FIXES.md`** | Living log of every bug encountered — root cause + fix applied + prevention tip (BUG-001 through BUG-017) | When a new bug is fixed — add it here |
| **`UI_MIGRATION_ROADMAP.md`** | UI/UX upgrade plan (Phases 0–10). Phases 0–6 done, Phases 7–10 pending | When working on UI phases 7–10 |
| **`audit.md`** | Full system audit (security findings, bugs, DB gaps, architecture issues) — updated 2026-06-21 | Reference for current system state |
| **`audit_lackings.md`** | Backlog of missing features, security gaps, architecture issues with effort/impact — updated 2026-06-21 | Prioritizing what to work on next |
| **`PROJECT_STRUCTURE.md`** | Actual file tree for both PHP backend and Android app, with known structural gaps noted | Understanding where files are |
| **`README.md`** | Quick API setup guide, endpoint summary table, test accounts, known issues | New developer onboarding |

---

## API Base URLs

| Context | URL |
|---------|-----|
| PC browser / Postman | `http://localhost/ptoda_booking_api/` |
| Android Emulator | `http://10.0.2.2/ptoda_booking_api/` |
| Physical device (same Wi-Fi) | `http://192.168.0.101/ptoda_booking_api/` ✅ active |

---

## Current Priority (2026-06-21)

**Do these first (Phase 7 — Critical Fixes):**

| # | Action | Why |
|---|--------|-----|
| 1 | Delete `check_admin.php` + `fix_admin.php` from web root | Anyone on LAN can reset admin password right now |
| 2 | Set real `JWT_SECRET` in `config/config.php` | All JWTs are currently forgeable |
| 3 | Set real `FCM_SERVER_KEY` + migrate FCM to HTTP v1 | Push notifications have never worked; Legacy API is shut down |
| 4 | Fix `Booking.kt` coordinates `String → Double` | App will crash on map screens with real booking data |

See `DEVELOPMENT_CHECKLIST.md` Phase 7 for the full list.

---

## What Was Removed / Compiled

Previously the docs had 23 files including `api/`, `models/`, and `flows/` subfolders. These were compiled into 3 files and the subfolders were deleted:

| Old (13 files) | Compiled into |
|----------------|---------------|
| `api/AUTH.md`, `api/BOOKINGS.md`, `api/DRIVER.md`, `api/ADMIN.md`, `api/FCM.md` | `API_REFERENCE.md` |
| `models/USER.md`, `models/BOOKING.md`, `models/DRIVER_INFO.md`, `models/FCM_TOKEN.md` | `DATA_MODELS.md` |
| `flows/AUTH_FLOW.md`, `flows/BOOKING_FLOW.md`, `flows/DRIVER_APPROVAL_FLOW.md`, `flows/ANDROID_SETUP.md` | `FLOWS.md` |

Deleted (not useful as project docs):
- `PROMPT_TEMPLATES.md` — AI prompting templates for Copilot
- `FEASIBILITY_REPORT.md` — UI migration analysis (Phases 0–6 are done; analysis is now historical)
- `Mobile-Based Tricycle Booking System (PTODA) – MVP Development Roadmap.md` — original draft roadmap, fully superseded by the checklist
