package com.example.mbptodabookingapp.data.api

/**
 * Generic wrapper that matches every PHP API response shape:
 *   { "success": true/false, "data": <T>, "message": "..." }
 *
 * See: docs/flows/ANDROID_SETUP.md → ApiResponse Wrapper
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String
)

