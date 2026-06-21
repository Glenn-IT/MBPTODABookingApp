# PTODA API Reference

**Base URL:** `http://192.168.0.101/ptoda_booking_api/` (physical device)
**Last updated:** 2026-06-21

All protected endpoints require: `Authorization: Bearer <jwt_token>`

All responses follow this shape:
```json
{ "success": bool, "message": "string", "data": any | null }
```

---

## Auth (no token required)

### POST `/auth/register`

**Body:**
```json
{
  "name": "string",
  "email": "string",
  "password": "string (min 6 chars)",
  "role": "passenger | driver",
  "license_no": "string (required if driver)",
  "vehicle_no": "string (required if driver)"
}
```
**Success:** `201 { user_id: int }`
**Errors:** `422` missing field / invalid role / short password · `409` email taken

---

### POST `/auth/login`

**Body:** `{ email, password }`

**Success `200`:**
```json
{
  "token": "eyJ...",
  "user": { "id": 5, "name": "...", "email": "...", "role": "passenger", "status": "active" }
}
```
**Errors:** `422` missing fields · `401` wrong credentials · `403` account inactive / driver not yet approved

**JWT payload:** `{ user_id, role, email, iat, exp }` — expires in 7 days.

---

## Bookings

### POST `/bookings` — Role: passenger

**Body:**
```json
{
  "pickup_address": "string",
  "pickup_lat": 14.5995,
  "pickup_lng": 120.975,
  "dropoff_address": "string",
  "dropoff_lat": 14.5833,
  "dropoff_lng": 120.9797
}
```
**Success:** `201 { booking_id: 10 }`

---

### GET `/bookings` — Role: any (role-filtered)

Returns all bookings visible to the caller:
- Passenger → their own bookings only
- Driver → bookings assigned to them
- Admin → all bookings

**Success:** `200 [ ...Booking ]`

---

### GET `/bookings/{id}` — Role: owner or admin

Returns single booking with passenger and driver JOIN fields (`passenger_name`, `passenger_email`, `driver_name`, `driver_email`).

**Errors:** `404` not found · `403` not your booking

> ⚠️ Known bug: returns `403` for drivers on `requested` bookings where `driver_id` is `NULL`. Workaround: use Intent extras in `RideRequestActivity` instead of calling this endpoint.

---

### GET `/passenger/history` — Role: passenger

Alias of `GET /bookings`. Returns the same role-filtered result.

---

### POST `/bookings/{id}/cancel` — Role: passenger ❌ NOT IMPLEMENTED

Not yet built. See `DEVELOPMENT_CHECKLIST.md` Phase 8.1.

---

## Driver

All driver endpoints require `role = driver` and `approval_status = approved`.

### GET `/driver/requests` — Role: driver

Returns all bookings with `status = requested` (unassigned). Includes `passenger_name`.

---

### POST `/driver/accept/{booking_id}` — Role: driver

Sets status to `accepted`, assigns `driver_id`. Sends FCM push to passenger.
**Errors:** `404` not found · `409` already accepted by someone else

---

### POST `/driver/reject/{booking_id}` — Role: driver

Sets status to `rejected`.
> ⚠️ No ownership check — any driver can currently reject any requested booking.

---

### POST `/driver/start/{booking_id}` — Role: driver ❌ NOT IMPLEMENTED

Not yet built. See `DEVELOPMENT_CHECKLIST.md` Phase 8.2. Sets `accepted → in_progress`.

---

### POST `/driver/complete/{booking_id}` — Role: driver (must be assigned driver)

Checks `driver_id == auth.user_id`. Sets status to `completed`. Sends FCM push to passenger.
**Errors:** `403` not assigned driver · `409` wrong status

---

### PUT `/driver/location` — Role: driver

**Body:** `{ "lat": 14.5995, "lng": 120.975 }`

Updates `driver_info.current_lat` / `current_lng`.

---

### PUT `/driver/status` — Role: driver ❌ NOT IMPLEMENTED

Not yet built. Will toggle `driver_info.is_online`. See Phase 8.3.

---

## User (any authenticated role)

### PUT `/user/fcm-token`

**Body:** `{ "token": "FCM_DEVICE_TOKEN_STRING" }`

Upserts the FCM token for the authenticated user in the `fcm_tokens` table.

---

## Admin

All admin endpoints require `role = admin`.

### GET `/admin/users`
Returns all users: `[ { id, name, email, role, status, created_at } ]`

### GET `/admin/drivers/pending`
Returns drivers with `approval_status = pending`, including `license_no`, `vehicle_no`.

### GET `/admin/bookings`
Returns all bookings with `passenger_name` and `driver_name` from JOIN.

### PUT `/admin/driver/approve/{id}`
Sets `driver_info.approval_status = approved`.
**Errors:** `404` driver not found or already approved

### PUT `/admin/driver/reject/{id}`
Sets `driver_info.approval_status = rejected`.

### PUT `/admin/user/activate/{id}`
Sets `users.status = active` (only if currently `inactive`).
**Errors:** `404` user not found or already active

### PUT `/admin/user/deactivate/{id}`
Sets `users.status = inactive` (only if currently `active`).
**Errors:** `404` user not found or already inactive

### DELETE `/admin/user/{id}`
Permanently deletes the user. Related rows in `driver_info`, `fcm_tokens`, `booking_logs` are removed via `ON DELETE CASCADE`.

---

## Status Lifecycle

```
[requested] → [accepted] → [in_progress]* → [completed]
                         ↘ [rejected]
[requested] ↘ [cancelled]*

* = status exists in DB ENUM but no route can set it yet (see checklist Phase 8)
```

| Status | Set by | Route |
|--------|--------|-------|
| `requested` | System on booking create | `POST /bookings` |
| `accepted` | Driver | `POST /driver/accept/{id}` |
| `in_progress` | Driver | ❌ not implemented |
| `completed` | Driver | `POST /driver/complete/{id}` |
| `rejected` | Driver | `POST /driver/reject/{id}` |
| `cancelled` | Passenger | ❌ not implemented |

---

## Kotlin ApiService Reference

```kotlin
// Auth
@POST("auth/register") suspend fun register(@Body body: RegisterRequest): ApiResponse<Map<String, Int>>
@POST("auth/login")    suspend fun login(@Body body: LoginRequest): ApiResponse<LoginResponse>

// Bookings
@POST("bookings")            suspend fun createBooking(@Body body: BookingRequest): ApiResponse<CreateBookingResponse>
@GET("bookings")             suspend fun getBookings(): ApiResponse<List<Booking>>
@GET("bookings/{id}")        suspend fun getBookingById(@Path("id") id: Int): ApiResponse<Booking>
@GET("passenger/history")    suspend fun getPassengerHistory(): ApiResponse<List<Booking>>

// Driver
@GET("driver/requests")                    suspend fun getDriverRequests(): ApiResponse<List<Booking>>
@POST("driver/accept/{id}")                suspend fun acceptRide(@Path("id") id: Int): ApiResponse<Unit>
@POST("driver/reject/{id}")                suspend fun rejectRide(@Path("id") id: Int): ApiResponse<Unit>
@POST("driver/complete/{id}")              suspend fun completeRide(@Path("id") id: Int): ApiResponse<Unit>
@PUT("driver/location")                    suspend fun updateLocation(@Body body: UpdateLocationRequest): ApiResponse<Unit>

// User
@PUT("user/fcm-token") suspend fun updateFcmToken(@Body body: FcmTokenRequest): ApiResponse<Unit>

// Admin
@GET("admin/users")                        suspend fun getAllUsers(): ApiResponse<List<AdminUser>>
@GET("admin/drivers/pending")              suspend fun getPendingDrivers(): ApiResponse<List<PendingDriver>>
@GET("admin/bookings")                     suspend fun getAllBookings(): ApiResponse<List<Booking>>
@PUT("admin/driver/approve/{id}")          suspend fun approveDriver(@Path("id") id: Int): ApiResponse<Unit>
@PUT("admin/driver/reject/{id}")           suspend fun rejectDriver(@Path("id") id: Int): ApiResponse<Unit>
@PUT("admin/user/activate/{id}")           suspend fun activateUser(@Path("id") id: Int): ApiResponse<Unit>
@PUT("admin/user/deactivate/{id}")         suspend fun deactivateUser(@Path("id") id: Int): ApiResponse<Unit>
@DELETE("admin/user/{id}")                 suspend fun deleteUser(@Path("id") id: Int): ApiResponse<Unit>
```

---

## Postman Quick Reference

**Variables:** `{{base_url}}` = `http://localhost/ptoda_booking_api` · `{{passenger_token}}` · `{{driver_token}}` · `{{admin_token}}`

**Full booking flow test sequence:**
1. `POST /auth/login` as passenger → save token
2. `POST /auth/login` as approved driver → save token
3. `POST /bookings` with passenger token → save `booking_id`
4. `GET /driver/requests` with driver token → find booking
5. `POST /driver/accept/{booking_id}` with driver token
6. `POST /driver/complete/{booking_id}` with driver token
7. `GET /bookings/{booking_id}` with either token → verify `status: completed`
