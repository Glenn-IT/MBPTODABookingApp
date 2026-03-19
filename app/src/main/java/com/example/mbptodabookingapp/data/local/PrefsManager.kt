package com.example.mbptodabookingapp.data.local

import android.content.Context
import androidx.core.content.edit
import com.example.mbptodabookingapp.data.models.LoggedInUser

/**
 * Manages all SharedPreferences storage for the app.
 * Stores JWT token, user info, and FCM device token.
 *
 * See: docs/models/USER.md → SharedPreferences Storage
 *
 * Usage:
 *   PrefsManager.saveLoginData(context, token, user)
 *   PrefsManager.getJwtToken(context)
 *   PrefsManager.clearAll(context)  // on logout or 401
 */
object PrefsManager {

    private const val PREFS_NAME   = "ptoda_prefs"
    private const val KEY_TOKEN    = "jwt_token"
    private const val KEY_USER_ID  = "user_id"
    private const val KEY_ROLE     = "user_role"
    private const val KEY_NAME     = "user_name"
    private const val KEY_FCM      = "fcm_token"

    // ── Login / Logout ────────────────────────────────────────────────────────

    /**
     * Persist JWT token and user info after a successful login.
     * Call this immediately after POST /auth/login succeeds.
     */
    fun saveLoginData(context: Context, token: String, user: LoggedInUser) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_TOKEN,  token)
            putInt(KEY_USER_ID,   user.id)
            putString(KEY_ROLE,   user.role)
            putString(KEY_NAME,   user.name)
        }
    }

    /** Clear all stored data — call on logout or when a 401 is received. */
    fun clearAll(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit { clear() }
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    /** Returns the stored JWT token, or null if the user is not logged in. */
    fun getJwtToken(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TOKEN, null)

    /** Returns the stored user role ("passenger" | "driver" | "admin"), or null. */
    fun getUserRole(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ROLE, null)

    /** Returns the stored user ID, or -1 if not logged in. */
    fun getUserId(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_USER_ID, -1)

    /** Returns the stored display name, or null. */
    fun getUserName(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_NAME, null)

    /** Returns true if a JWT token exists (user is logged in). */
    fun isLoggedIn(context: Context): Boolean = getJwtToken(context) != null

    // ── FCM Token ─────────────────────────────────────────────────────────────

    /**
     * Save FCM device token locally.
     * Called from PTODAFirebaseMessagingService.onNewToken().
     * See: docs/api/FCM.md
     */
    fun saveFcmToken(context: Context, token: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_FCM, token)
        }
    }

    /** Returns the locally saved FCM token, or null if not yet received. */
    fun getFcmToken(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_FCM, null)
}




