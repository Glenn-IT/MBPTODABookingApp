package com.example.mbptodabookingapp.utils

/**
 * Generic wrapper that represents the state of an API operation.
 *
 * Returned by every Repository method and observed by ViewModels to
 * drive UI state (loading spinner, data display, error toast).
 *
 * Usage in ViewModel:
 *   when (result) {
 *       is Resource.Success -> { /* use result.data */ }
 *       is Resource.Error   -> { /* show result.message */ }
 *       is Resource.Loading -> { /* show spinner */ }
 *   }
 */
sealed class Resource<out T> {

    /** API call succeeded. [data] holds the result. */
    data class Success<T>(val data: T) : Resource<T>()

    /** API call failed. [message] is a user-friendly description of what went wrong. */
    data class Error(val message: String) : Resource<Nothing>()

    /** API call is in progress — UI should show a loading indicator. */
    object Loading : Resource<Nothing>()
}

