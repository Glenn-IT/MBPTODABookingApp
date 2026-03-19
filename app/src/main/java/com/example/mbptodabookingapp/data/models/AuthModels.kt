package com.example.mbptodabookingapp.data.models

/**
 * Request/response models for POST /auth/register and POST /auth/login.
 * See: docs/api/AUTH.md
 */

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String,                 // "passenger" or "driver"
    val license_no: String? = null,   // required if role == "driver"
    val vehicle_no: String? = null    // required if role == "driver"
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: LoggedInUser   // embedded user info saved to SharedPreferences
)

