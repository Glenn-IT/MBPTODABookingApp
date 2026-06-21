package com.example.mbptodabookingapp.utils

/**
 * API base URL constants.
 * Switch BASE_URL to the appropriate value depending on your setup.
 * See: docs/INDEX.md → API Base URL table
 */
object Constants {
    // Physical device → PC on current Wi-Fi (10.240.57.x network)
    const val BASE_URL_DEVICE   = "http://10.240.57.14/ptoda_booking_api/"

    // Android Emulator → XAMPP Apache (loopback alias for the host PC)
    const val BASE_URL_EMULATOR = "http://10.0.2.2/ptoda_booking_api/"

    // Active base URL — switch between BASE_URL_DEVICE and BASE_URL_EMULATOR
    const val BASE_URL = BASE_URL_DEVICE
}

/**
 * User role values — must match the PHP API's ENUM('passenger','driver','admin')
 * See: docs/models/USER.md
 */
object UserRole {
    const val PASSENGER = "passenger"
    const val DRIVER    = "driver"
    const val ADMIN     = "admin"
}

/**
 * User account status values — must match users.status ENUM
 * See: docs/models/USER.md
 */
object UserStatus {
    const val ACTIVE   = "active"
    const val INACTIVE = "inactive"
}

/**
 * Driver approval status values — must match driver_info.approval_status ENUM
 * See: docs/models/DRIVER_INFO.md
 */
object DriverApprovalStatus {
    const val PENDING  = "pending"
    const val APPROVED = "approved"
    const val REJECTED = "rejected"
}

/**
 * Booking status values — must match bookings.status ENUM
 * See: docs/models/BOOKING.md
 *
 * Lifecycle:
 *   requested → accepted → in_progress → completed
 *                       ↘ rejected
 *   requested ↘ cancelled  (future)
 */
object BookingStatus {
    const val REQUESTED   = "requested"
    const val ACCEPTED    = "accepted"
    const val IN_PROGRESS = "in_progress"
    const val COMPLETED   = "completed"
    const val CANCELLED   = "cancelled"
    const val REJECTED    = "rejected"

    /** Returns true if the booking has reached a terminal state (no further updates expected). */
    fun isTerminal(status: String): Boolean =
        status == COMPLETED || status == CANCELLED || status == REJECTED
}

