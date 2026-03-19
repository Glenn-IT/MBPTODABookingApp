package com.example.mbptodabookingapp.data.api

import com.example.mbptodabookingapp.data.models.AdminUser
import com.example.mbptodabookingapp.data.models.Booking
import com.example.mbptodabookingapp.data.models.BookingRequest
import com.example.mbptodabookingapp.data.models.CreateBookingResponse
import com.example.mbptodabookingapp.data.models.FcmTokenRequest
import com.example.mbptodabookingapp.data.models.LoginRequest
import com.example.mbptodabookingapp.data.models.LoginResponse
import com.example.mbptodabookingapp.data.models.PendingDriver
import com.example.mbptodabookingapp.data.models.RegisterRequest
import com.example.mbptodabookingapp.data.models.UpdateLocationRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Retrofit interface — defines every API endpoint.
 * All methods are suspend functions called from within a coroutine (ViewModel/Repository).
 *
 * See: docs/flows/ANDROID_SETUP.md → ApiService
 * See: docs/INDEX.md → All Endpoints at a Glance
 */
interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────────────────
    // See: docs/api/AUTH.md

    @POST("auth/register")
    suspend fun register(
        @Body body: RegisterRequest
    ): ApiResponse<Map<String, Int>>

    @POST("auth/login")
    suspend fun login(
        @Body body: LoginRequest
    ): ApiResponse<LoginResponse>

    // ── Bookings ──────────────────────────────────────────────────────────────
    // See: docs/api/BOOKINGS.md

    /** Create a new ride request. Role: passenger */
    @POST("bookings")
    suspend fun createBooking(
        @Body body: BookingRequest
    ): ApiResponse<CreateBookingResponse>

    /** List bookings — response is role-filtered by the API (passenger/driver/admin). */
    @GET("bookings")
    suspend fun getBookings(): ApiResponse<List<Booking>>

    /** Get a single booking by ID. */
    @GET("bookings/{id}")
    suspend fun getBookingById(
        @Path("id") id: Int
    ): ApiResponse<Booking>

    /** Passenger ride history. Role: passenger */
    @GET("passenger/history")
    suspend fun getPassengerHistory(): ApiResponse<List<Booking>>

    // ── Driver ────────────────────────────────────────────────────────────────
    // See: docs/api/DRIVER.md

    /** Get all pending (unassigned) booking requests. Role: driver */
    @GET("driver/requests")
    suspend fun getDriverRequests(): ApiResponse<List<Booking>>

    /** Accept a pending booking. Role: driver */
    @POST("driver/accept/{booking_id}")
    suspend fun acceptRide(
        @Path("booking_id") bookingId: Int
    ): ApiResponse<Unit>

    /** Reject a pending booking. Role: driver */
    @POST("driver/reject/{booking_id}")
    suspend fun rejectRide(
        @Path("booking_id") bookingId: Int
    ): ApiResponse<Unit>

    /** Mark an accepted/in-progress ride as completed. Role: driver */
    @POST("driver/complete/{booking_id}")
    suspend fun completeRide(
        @Path("booking_id") bookingId: Int
    ): ApiResponse<Unit>

    /** Update driver's current GPS coordinates. Role: driver */
    @PUT("driver/location")
    suspend fun updateLocation(
        @Body body: UpdateLocationRequest
    ): ApiResponse<Unit>

    // ── FCM ───────────────────────────────────────────────────────────────────
    // See: docs/api/FCM.md

    /** Register or refresh the FCM device token for the current user. Role: any */
    @PUT("user/fcm-token")
    suspend fun updateFcmToken(
        @Body body: FcmTokenRequest
    ): ApiResponse<Unit>

    // ── Admin ─────────────────────────────────────────────────────────────────
    // See: docs/api/ADMIN.md

    /** List all registered users. Role: admin */
    @GET("admin/users")
    suspend fun getAllUsers(): ApiResponse<List<AdminUser>>

    /** List drivers with approval_status = 'pending'. Role: admin */
    @GET("admin/drivers/pending")
    suspend fun getPendingDrivers(): ApiResponse<List<PendingDriver>>

    /** List all bookings in the system. Role: admin */
    @GET("admin/bookings")
    suspend fun getAllBookings(): ApiResponse<List<Booking>>

    /** Approve a pending driver account. Role: admin */
    @PUT("admin/driver/approve/{id}")
    suspend fun approveDriver(
        @Path("id") driverId: Int
    ): ApiResponse<Unit>

    /** Reject a pending driver account. Role: admin */
    @PUT("admin/driver/reject/{id}")
    suspend fun rejectDriver(
        @Path("id") driverId: Int
    ): ApiResponse<Unit>

    /** Re-activate a deactivated user. Role: admin */
    @PUT("admin/user/activate/{id}")
    suspend fun activateUser(
        @Path("id") userId: Int
    ): ApiResponse<Unit>

    /** Deactivate a user account. Role: admin */
    @PUT("admin/user/deactivate/{id}")
    suspend fun deactivateUser(
        @Path("id") userId: Int
    ): ApiResponse<Unit>

    /** Permanently delete a user and all related records. Role: admin */
    @DELETE("admin/user/{id}")
    suspend fun deleteUser(
        @Path("id") userId: Int
    ): ApiResponse<Unit>
}

