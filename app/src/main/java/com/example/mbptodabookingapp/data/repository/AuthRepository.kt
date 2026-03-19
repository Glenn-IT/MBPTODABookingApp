package com.example.mbptodabookingapp.data.repository

import android.content.Context
import com.example.mbptodabookingapp.data.api.ApiService
import com.example.mbptodabookingapp.data.local.PrefsManager
import com.example.mbptodabookingapp.data.models.FcmTokenRequest
import com.example.mbptodabookingapp.data.models.LoginRequest
import com.example.mbptodabookingapp.data.models.LoginResponse
import com.example.mbptodabookingapp.data.models.RegisterRequest
import com.example.mbptodabookingapp.utils.Resource

/**
 * Handles all authentication-related API calls: register, login, and logout.
 *
 * See: docs/api/AUTH.md
 * See: docs/flows/AUTH_FLOW.md
 *
 * Usage in ViewModel:
 *   private val repo = AuthRepository(ApiClient.instance, context)
 */
class AuthRepository(
    private val api: ApiService,
    private val context: Context
) : BaseRepository() {

    /**
     * Register a new passenger or driver account.
     *
     * For drivers, [request] must include [RegisterRequest.license_no] and
     * [RegisterRequest.vehicle_no]. The newly created driver account starts
     * with approval_status = 'pending' and cannot log in until an admin approves.
     *
     * See: docs/api/AUTH.md → POST /auth/register
     *
     * @return [Resource.Success] with the new user_id on success.
     *         [Resource.Error] with the API message on failure (422, 409, 500).
     */
    suspend fun register(request: RegisterRequest): Resource<Int> {
        return try {
            val response = api.register(request)
            if (response.success && response.data != null) {
                Resource.Success(response.data["user_id"] ?: 0)
            } else {
                Resource.Error(response.message)
            }
        } catch (e: Exception) {
            Resource.Error(parseApiError(e))
        }
    }

    /**
     * Authenticate a user and persist the JWT + user info to SharedPreferences.
     *
     * On success, [PrefsManager.saveLoginData] is called automatically so the
     * auth interceptor in [ApiClient] will include the token in all future requests.
     *
     * See: docs/api/AUTH.md → POST /auth/login
     * See: docs/flows/AUTH_FLOW.md → Login Flow
     *
     * @return [Resource.Success] with [LoginResponse] (token + user).
     *         [Resource.Error] with the API message on failure (401, 403, 422).
     */
    suspend fun login(request: LoginRequest): Resource<LoginResponse> {
        return try {
            val response = api.login(request)
            if (response.success && response.data != null) {
                // Persist token + user — ApiClient interceptor reads this on every request
                PrefsManager.saveLoginData(context, response.data.token, response.data.user)
                // Sync FCM token that may have been stored before login (4.9 — token lifecycle)
                syncFcmTokenIfAvailable()
                Resource.Success(response.data)
            } else {
                Resource.Error(response.message)
            }
        } catch (e: Exception) {
            Resource.Error(parseApiError(e))
        }
    }

    /**
     * Registers the locally stored FCM device token with the backend after login.
     *
     * FCM may issue a token (via onNewToken) before the user ever logs in.
     * Calling this right after login ensures the server always has a valid token
     * so it can send push notifications to this device.
     *
     * See: docs/api/FCM.md → Full Token Lifecycle · 4.9.3 / 4.9.5
     */
    private suspend fun syncFcmTokenIfAvailable() {
        val fcmToken = PrefsManager.getFcmToken(context) ?: return
        try {
            api.updateFcmToken(FcmTokenRequest(fcmToken))
        } catch (_: Exception) {
            // Non-critical — notifications won't work until onNewToken fires again,
            // but the rest of the app is fully functional.
        }
    }

    /**
     * Clear all locally stored credentials (JWT, user info, FCM token).
     * Call this on explicit logout or when the [ApiClient] receives a 401.
     *
     * See: docs/flows/AUTH_FLOW.md → Token Expiry & Refresh
     */
    fun logout() {
        PrefsManager.clearAll(context)
    }
}

