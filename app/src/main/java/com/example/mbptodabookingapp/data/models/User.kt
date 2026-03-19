package com.example.mbptodabookingapp.data.models

/**
 * User returned in login response and admin user lists.
 * NOTE: Password is never included in any API response.
 * See: docs/models/USER.md
 */
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,       // "passenger" | "driver" | "admin"
    val status: String,     // "active" | "inactive"
    val created_at: String
)

/**
 * Compact user info embedded inside LoginResponse and stored in SharedPreferences.
 * See: docs/models/USER.md · docs/api/AUTH.md
 */
data class LoggedInUser(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,   // "passenger" | "driver" | "admin"
    val status: String  // "active" | "inactive"
)

