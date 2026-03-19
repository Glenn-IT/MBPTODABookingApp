package com.example.mbptodabookingapp.data.repository

import com.example.mbptodabookingapp.data.api.ApiService
import com.example.mbptodabookingapp.data.models.FcmTokenRequest
import com.example.mbptodabookingapp.data.models.UpdateLocationRequest
import com.example.mbptodabookingapp.utils.Resource

/**
 * Handles user-level API calls that apply across roles:
 *   - FCM token registration/refresh (all roles)
 *   - Driver GPS location update (driver only)
 *
 * See: docs/api/FCM.md
 * See: docs/api/DRIVER.md → PUT /driver/location
 * See: docs/models/FCM_TOKEN.md → Full Token Lifecycle
 *
 * Usage in ViewModel:
 *   private val repo = UserRepository(ApiClient.instance)
 */
class UserRepository(
    private val api: ApiService
) : BaseRepository() {

    /**
     * Register or refresh the FCM device token on the backend.
     *
     * Must be called:
     *  1. After a successful login (sync any locally saved token).
     *  2. Whenever [PTODAFirebaseMessagingService.onNewToken] fires.
     *
     * The API upserts the token — one row per user, latest token wins.
     *
     * See: docs/api/FCM.md → PUT /user/fcm-token
     * See: docs/models/FCM_TOKEN.md → Full Token Lifecycle
     */
    suspend fun updateFcmToken(token: String): Resource<Unit> {
        return try {
            val response = api.updateFcmToken(FcmTokenRequest(token))
            if (response.success) Resource.Success(Unit) else Resource.Error(response.message)
        } catch (e: Exception) {
            Resource.Error(parseApiError(e))
        }
    }

    /**
     * Update the driver's current GPS coordinates on the backend.
     *
     * Call this periodically from [FusedLocationProviderClient] callbacks
     * while the driver is marked as online. Stored in driver_info.current_lat/lng.
     *
     * See: docs/api/DRIVER.md → PUT /driver/location
     * See: docs/models/DRIVER_INFO.md → current_lat / current_lng columns
     */
    suspend fun updateLocation(lat: Double, lng: Double): Resource<Unit> {
        return try {
            val response = api.updateLocation(UpdateLocationRequest(lat, lng))
            if (response.success) Resource.Success(Unit) else Resource.Error(response.message)
        } catch (e: Exception) {
            Resource.Error(parseApiError(e))
        }
    }
}

