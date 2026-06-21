package com.example.mbptodabookingapp.utils

import com.example.mbptodabookingapp.BuildConfig

/**
 * API base URL constants.
 * BASE_URL is set per build type in app/build.gradle.kts via buildConfigField.
 * Override at runtime with the ⚙ Server dialog in LoginActivity.
 */
object Constants {
    // Reference constants — used in comments and the ⚙ Server dialog hints
    const val BASE_URL_DEVICE   = "http://10.240.57.14/ptoda_booking_api/"
    const val BASE_URL_EMULATOR = "http://10.0.2.2/ptoda_booking_api/"

    // Injected at compile time: debug → device URL, release → production HTTPS URL
    // (10.4): For release, ensure network_security_config.xml has no cleartext exception
    //         and APP_ENV=production is set in the PHP .env file.
    val BASE_URL: String = BuildConfig.BASE_URL
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

