package com.example.mbptodabookingapp.data.repository

import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

/**
 * Base class for all repositories.
 * Provides a shared [parseApiError] helper that converts exceptions into
 * human-readable error messages for the UI.
 *
 * Error cases handled:
 *  - [HttpException]  → 4xx/5xx: reads the JSON "message" field from the PHP error body
 *  - [IOException]    → network failure (no internet, timeout, DNS error)
 *  - Everything else  → raw exception message as fallback
 */
abstract class BaseRepository {

    /**
     * Parses a [Throwable] thrown by a Retrofit call into a user-friendly string.
     *
     * The PHP API returns errors as:
     *   { "success": false, "data": null, "message": "Descriptive error." }
     *
     * This function extracts the "message" field from that body, or falls back
     * to a generic string if the body is unparseable.
     */
    protected fun parseApiError(throwable: Throwable): String {
        return when (throwable) {
            is HttpException -> {
                try {
                    val errorBody = throwable.response()?.errorBody()?.string().orEmpty()
                    JSONObject(errorBody).optString("message", "Server error (${throwable.code()})")
                } catch (_: Exception) {
                    "Server error (${throwable.code()})"
                }
            }
            is IOException -> "No internet connection. Please check your network."
            else -> throwable.message ?: "An unexpected error occurred."
        }
    }
}


