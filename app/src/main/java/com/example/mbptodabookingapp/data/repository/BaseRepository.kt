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
                val bodyMessage = try {
                    val body = throwable.response()?.errorBody()?.string().orEmpty()
                    JSONObject(body).optString("message", "").takeIf { it.isNotEmpty() }
                } catch (_: Exception) { null }

                bodyMessage ?: when (throwable.code()) {
                    401  -> "Session expired. Please log in again."
                    403  -> "You don't have permission to do that."
                    404  -> "The requested resource was not found."
                    409  -> "This action conflicts with existing data."
                    422  -> "Please check your input and try again."
                    429  -> "Too many attempts. Please wait a moment."
                    500  -> "Something went wrong. Please try again later."
                    else -> "Server error (${throwable.code()}). Please try again."
                }
            }
            is IOException -> "No internet connection. Please check your network."
            else -> throwable.message ?: "An unexpected error occurred."
        }
    }
}


