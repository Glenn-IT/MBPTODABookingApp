package com.example.mbptodabookingapp.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.mbptodabookingapp.data.models.LoggedInUser

object PrefsManager {

    private const val PREFS_NAME     = "ptoda_prefs"
    private const val KEY_TOKEN      = "jwt_token"
    private const val KEY_USER_ID    = "user_id"
    private const val KEY_ROLE       = "user_role"
    private const val KEY_NAME       = "user_name"
    private const val KEY_FCM        = "fcm_token"

    // Separate prefs file for dev config — not cleared on logout, not sensitive
    private const val DEV_PREFS_NAME = "ptoda_dev_prefs"
    private const val KEY_SERVER_URL = "server_url"

    // ── Encrypted prefs (JWT, user info, FCM token) ───────────────────────────

    private fun getPrefs(context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Keystore unavailable (emulator quirk, corrupted key) — fall back to plain prefs
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    // ── Login / Logout ────────────────────────────────────────────────────────

    fun saveLoginData(context: Context, token: String, user: LoggedInUser) {
        getPrefs(context).edit {
            putString(KEY_TOKEN,  token)
            putInt(KEY_USER_ID,   user.id)
            putString(KEY_ROLE,   user.role)
            putString(KEY_NAME,   user.name)
        }
    }

    fun clearAll(context: Context) {
        getPrefs(context).edit { clear() }
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    fun getJwtToken(context: Context): String? =
        getPrefs(context).getString(KEY_TOKEN, null)

    fun getUserRole(context: Context): String? =
        getPrefs(context).getString(KEY_ROLE, null)

    fun getUserId(context: Context): Int =
        getPrefs(context).getInt(KEY_USER_ID, -1)

    fun getUserName(context: Context): String? =
        getPrefs(context).getString(KEY_NAME, null)

    fun isLoggedIn(context: Context): Boolean = getJwtToken(context) != null

    // ── FCM Token ─────────────────────────────────────────────────────────────

    fun saveFcmToken(context: Context, token: String) {
        getPrefs(context).edit { putString(KEY_FCM, token) }
    }

    fun getFcmToken(context: Context): String? =
        getPrefs(context).getString(KEY_FCM, null)

    // ── Dev / Server Config (plain prefs — not sensitive) ─────────────────────

    fun getServerUrl(context: Context): String =
        context.getSharedPreferences(DEV_PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SERVER_URL, null)
            ?: com.example.mbptodabookingapp.utils.Constants.BASE_URL_DEVICE

    fun saveServerUrl(context: Context, url: String) {
        context.getSharedPreferences(DEV_PREFS_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_SERVER_URL, url)
        }
    }
}
