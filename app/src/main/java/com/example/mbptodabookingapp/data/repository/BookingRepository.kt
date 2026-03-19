package com.example.mbptodabookingapp.data.repository

import com.example.mbptodabookingapp.data.api.ApiService
import com.example.mbptodabookingapp.data.models.Booking
import com.example.mbptodabookingapp.data.models.BookingRequest
import com.example.mbptodabookingapp.utils.Resource

/**
 * Handles all booking-related API calls for passengers and drivers.
 *
 * See: docs/api/BOOKINGS.md
 * See: docs/api/DRIVER.md
 * See: docs/flows/BOOKING_FLOW.md
 *
 * Usage in ViewModel:
 *   private val repo = BookingRepository(ApiClient.instance)
 */
class BookingRepository(
    private val api: ApiService
) : BaseRepository() {

    // ── Passenger ─────────────────────────────────────────────────────────────

    /**
     * Create a new ride request. Role: passenger.
     * Initial booking status is set to 'requested' by the API.
     *
     * See: docs/api/BOOKINGS.md → POST /bookings
     *
     * @return [Resource.Success] with the new booking_id.
     */
    suspend fun createBooking(request: BookingRequest): Resource<Int> {
        return try {
            val response = api.createBooking(request)
            if (response.success && response.data != null) {
                Resource.Success(response.data.booking_id)
            } else {
                Resource.Error(response.message)
            }
        } catch (e: Exception) {
            Resource.Error(parseApiError(e))
        }
    }

    /**
     * Get bookings filtered by the caller's JWT role:
     *   - passenger → only their own bookings
     *   - driver    → only bookings assigned to them
     *   - admin     → all bookings
     *
     * See: docs/api/BOOKINGS.md → GET /bookings
     */
    suspend fun getBookings(): Resource<List<Booking>> {
        return try {
            val response = api.getBookings()
            if (response.success && response.data != null) {
                Resource.Success(response.data)
            } else {
                Resource.Error(response.message)
            }
        } catch (e: Exception) {
            Resource.Error(parseApiError(e))
        }
    }

    /**
     * Get a single booking by ID.
     * Used by [RideStatusActivity] to poll for status updates every 5 seconds.
     *
     * See: docs/api/BOOKINGS.md → GET /bookings/{id}
     * See: docs/flows/BOOKING_FLOW.md → Step 5 (polling)
     */
    suspend fun getBookingById(id: Int): Resource<Booking> {
        return try {
            val response = api.getBookingById(id)
            if (response.success && response.data != null) {
                Resource.Success(response.data)
            } else {
                Resource.Error(response.message)
            }
        } catch (e: Exception) {
            Resource.Error(parseApiError(e))
        }
    }

    /**
     * Get the full ride history for the logged-in passenger.
     *
     * See: docs/api/BOOKINGS.md → GET /passenger/history
     */
    suspend fun getPassengerHistory(): Resource<List<Booking>> {
        return try {
            val response = api.getPassengerHistory()
            if (response.success && response.data != null) {
                Resource.Success(response.data)
            } else {
                Resource.Error(response.message)
            }
        } catch (e: Exception) {
            Resource.Error(parseApiError(e))
        }
    }

    // ── Driver ────────────────────────────────────────────────────────────────

    /**
     * Get all pending (unassigned) ride requests. Role: driver.
     * Polled by [DriverHomeActivity] to show new incoming requests.
     *
     * See: docs/api/DRIVER.md → GET /driver/requests
     */
    suspend fun getDriverRequests(): Resource<List<Booking>> {
        return try {
            val response = api.getDriverRequests()
            if (response.success && response.data != null) {
                Resource.Success(response.data)
            } else {
                Resource.Error(response.message)
            }
        } catch (e: Exception) {
            Resource.Error(parseApiError(e))
        }
    }

    /**
     * Accept a pending booking. Sets status → 'accepted', assigns driver_id.
     * The API also sends an FCM push notification to the passenger.
     *
     * See: docs/api/DRIVER.md → POST /driver/accept/{booking_id}
     * See: docs/flows/BOOKING_FLOW.md → Step 3a
     */
    suspend fun acceptRide(bookingId: Int): Resource<Unit> {
        return try {
            val response = api.acceptRide(bookingId)
            if (response.success) Resource.Success(Unit) else Resource.Error(response.message)
        } catch (e: Exception) {
            Resource.Error(parseApiError(e))
        }
    }

    /**
     * Reject a pending booking. Sets status → 'rejected'.
     *
     * See: docs/api/DRIVER.md → POST /driver/reject/{booking_id}
     * See: docs/flows/BOOKING_FLOW.md → Step 3b
     */
    suspend fun rejectRide(bookingId: Int): Resource<Unit> {
        return try {
            val response = api.rejectRide(bookingId)
            if (response.success) Resource.Success(Unit) else Resource.Error(response.message)
        } catch (e: Exception) {
            Resource.Error(parseApiError(e))
        }
    }

    /**
     * Mark an active ride as completed. Sets status → 'completed'.
     * The API also sends an FCM push notification to the passenger.
     *
     * See: docs/api/DRIVER.md → POST /driver/complete/{booking_id}
     * See: docs/flows/BOOKING_FLOW.md → Step 4
     */
    suspend fun completeRide(bookingId: Int): Resource<Unit> {
        return try {
            val response = api.completeRide(bookingId)
            if (response.success) Resource.Success(Unit) else Resource.Error(response.message)
        } catch (e: Exception) {
            Resource.Error(parseApiError(e))
        }
    }
}

