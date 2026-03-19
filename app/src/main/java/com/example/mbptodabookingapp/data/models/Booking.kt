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
    val driver_id: Int?,            // null until a driver accepts
    val pickup_address: String,
    val pickup_lat: String,         // returned as String from MySQL DECIMAL
    val pickup_lng: String,
    val dropoff_address: String,
    val dropoff_lat: String,
    val dropoff_lng: String,
    val status: String,             // see BookingStatus constants in Constants.kt
    val created_at: String,
    val updated_at: String?
) {
    /** Convenience: parse lat/lng strings to Double for Google Maps use. */
    fun pickupLatLng()  = Pair(pickup_lat.toDouble(),  pickup_lng.toDouble())
    fun dropoffLatLng() = Pair(dropoff_lat.toDouble(), dropoff_lng.toDouble())
}

data class CreateBookingResponse(
    val booking_id: Int
)

