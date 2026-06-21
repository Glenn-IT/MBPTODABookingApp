package com.example.mbptodabookingapp.data.models

/**
 * Models for driver-specific endpoints.
 * See: docs/api/DRIVER.md · docs/models/DRIVER_INFO.md
 */

/** Request body for PUT /driver/location */
data class UpdateLocationRequest(
    val lat: Double,
    val lng: Double
)

/** Request body for PUT /driver/status */
data class UpdateDriverStatusRequest(
    val is_online: Boolean
)

/**
 * Driver GPS position — used when displaying driver on the map.
 * See: docs/models/DRIVER_INFO.md
 */
data class DriverLocation(
    val user_id: Int,
    val current_lat: Double?,
    val current_lng: Double?
)

