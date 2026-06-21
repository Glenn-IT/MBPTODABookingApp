# PTODA Booking API

PHP REST API for the Mobile-Based Tricycle Booking System (PTODA).

_Last updated: 2026-06-21_

---

## Requirements

- PHP 7.4+
- MySQL 5.7+
- Apache with `mod_rewrite` enabled
- XAMPP (for local development)

## Local Setup

1. Copy the `ptoda_booking_api/` folder to `C:\xampp\htdocs\ptoda_booking_api\`
2. Start Apache and MySQL in XAMPP Control Panel
3. Open `http://localhost/phpmyadmin` and create a database named `ptoda_db`
4. Import `database/seed.sql` to create tables and seed test data
5. Edit `config/config.php` and set:
   - `JWT_SECRET` — a random 256-bit hex string (never use the placeholder)
   - `FCM_SERVER_KEY` — your Firebase project's Server Key from Firebase Console → Project Settings → Cloud Messaging
6. Test the API at `http://localhost/ptoda_booking_api/`

> ⚠️ **Before running:** Delete `check_admin.php` and `fix_admin.php` from the web root. They allow anyone on your network to read and reset the admin password without authentication.

> ⚠️ `database/schema.sql` is referenced in older docs but does not exist in the repo. Use `seed.sql` to set up the database.

---

## API Base URL

```
http://localhost/ptoda_booking_api/          (PC browser / Postman)
http://10.0.2.2/ptoda_booking_api/           (Android Emulator)
http://192.168.0.101/ptoda_booking_api/      (Physical device — use your PC's LAN IP)
```

---

## Authentication

All protected endpoints require a `Bearer` token in the `Authorization` header:

```
Authorization: Bearer <jwt_token>
```

---

## Endpoints Summary

### Auth (no token required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Register new user (passenger or driver) |
| POST | `/auth/login` | Login, returns JWT token |

### Passenger

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/bookings` | Yes | Create new ride booking |
| GET | `/bookings` | Yes | List own bookings (role-filtered) |
| GET | `/bookings/{id}` | Yes | Get single booking by ID |
| GET | `/passenger/history` | Yes | Passenger ride history |
| ❌ | `/bookings/{id}/cancel` | — | **NOT IMPLEMENTED** — no cancel route exists |

### Driver

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/driver/requests` | Yes | Pending ride requests |
| POST | `/driver/accept/{booking_id}` | Yes | Accept a ride (`requested → accepted`) |
| POST | `/driver/reject/{booking_id}` | Yes | Reject a ride |
| ❌ | `/driver/start/{booking_id}` | — | **NOT IMPLEMENTED** — no start route exists (`in_progress` status unreachable) |
| POST | `/driver/complete/{booking_id}` | Yes | Complete a ride (`accepted → completed`) |
| PUT | `/driver/location` | Yes | Update driver GPS coordinates |

### User (any authenticated role)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| PUT | `/user/fcm-token` | Yes | Register or refresh FCM push token |

### Admin

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/admin/users` | Yes | List all registered users |
| GET | `/admin/drivers/pending` | Yes | List drivers pending approval |
| GET | `/admin/bookings` | Yes | List all bookings |
| PUT | `/admin/driver/approve/{id}` | Yes | Approve driver account |
| PUT | `/admin/driver/reject/{id}` | Yes | Reject driver account |
| PUT | `/admin/user/activate/{id}` | Yes | Re-enable a deactivated user |
| PUT | `/admin/user/deactivate/{id}` | Yes | Deactivate a user |
| DELETE | `/admin/user/{id}` | Yes | Permanently delete a user |

---

## Test Accounts (after running seed.sql)

| Role | Email | Password |
|------|-------|----------|
| Admin | `admin@ptoda.local` | `admin123` |
| Passenger | `passenger@ptoda.local` | `password123` |
| Driver (approved) | `driver@ptoda.local` | `password123` |

---

## Known Issues

| Issue | Detail |
|-------|--------|
| Push notifications broken | `FCM_SERVER_KEY` in `config/config.php` is a placeholder — must be set to a real key |
| All JWTs forgeable | `JWT_SECRET` in `config/config.php` is a placeholder — must be replaced immediately |
| `cancelled` status unreachable | No `/cancel` route exists; `BookingStatus.CANCELLED` in the Android app has no backend counterpart |
| `in_progress` status unreachable | No `/driver/start` route exists; the `in_progress` DB ENUM value cannot be set |
| `check_admin.php` security risk | Dumps admin password hash with no auth — delete this file |
| `fix_admin.php` security risk | Resets admin password to `admin123` for any LAN caller — delete this file |

---

## Project Docs

- [`PROJECT_STRUCTURE.md`](PROJECT_STRUCTURE.md) — Full project structure (Android + PHP)
- [`audit.md`](audit.md) — Current system audit (security, bugs, architecture)
- [`audit_lackings.md`](audit_lackings.md) — Missing features and improvement backlog
