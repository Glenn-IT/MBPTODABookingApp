package com.example.mbptodabookingapp.data.models

/**
 * Booking request and response models.
 * See: docs/models/BOOKING.md · docs/api/BOOKINGS.md
 *
 * Status lifecycle:
 *   requested → accepted → in_progress → completed
 *                       ↘ rejected
 *   requested ↘ cancelled  (future)
 */

data class BookingRequest(
    val pickup_address: String,
    val pickup_lat: Double,
    val pickup_lng: Double,
    val dropoff_address: String,
    val dropoff_lat: Double,
    val dropoff_lng: Double
)

data class Booking(
    val id: Int,
    val passenger_id: Int,
    val driver_id: Int?,
    val pickup_address: String,
    val pickup_lat: Double,
    val pickup_lng: Double,
    val dropoff_address: String,
    val dropoff_lat: Double,
    val dropoff_lng: Double,
    val status: String,
    val created_at: String,
    val updated_at: String?,
    val passenger_name: String? = null,
    val driver_name: String?    = null,
    val driver_email: String?   = null
) {
    fun pickupLatLng()  = Pair(pickup_lat,  pickup_lng)
    fun dropoffLatLng() = Pair(dropoff_lat, dropoff_lng)
}

data class CreateBookingResponse(
    val booking_id: Int
)

