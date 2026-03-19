package com.example.mbptodabookingapp.data.models

/**
 * Models for admin-specific endpoints.
 * See: docs/api/ADMIN.md
 */

/** Full user row returned by GET /admin/users */
data class AdminUser(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,       // "passenger" | "driver" | "admin"
    val status: String,     // "active" | "inactive"
    val created_at: String
)

/** Driver row returned by GET /admin/drivers/pending */
data class PendingDriver(
    val id: Int,            // users.id (not driver_info.id)
    val name: String,
    val email: String,
    val created_at: String,
    val license_no: String?,
    val vehicle_no: String?,
    val approval_status: String  // always "pending" in this list
)

