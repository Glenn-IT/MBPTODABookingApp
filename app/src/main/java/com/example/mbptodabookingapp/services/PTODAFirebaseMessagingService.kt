package com.example.mbptodabookingapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.mbptodabookingapp.R
import com.example.mbptodabookingapp.data.api.ApiClient
import com.example.mbptodabookingapp.data.local.PrefsManager
import com.example.mbptodabookingapp.data.models.FcmTokenRequest
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Handles incoming FCM push notifications and token refresh events.
 *
 * See: docs/api/FCM.md · docs/flows/AUTH_FLOW.md
 * See: DEVELOPMENT_CHECKLIST.md → 4.9.3
 *
 * Registered in AndroidManifest.xml under the MESSAGING_EVENT intent filter.
 */
class PTODAFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Called when FCM issues a new device token (on install, reinstall, or token refresh).
     * Saves the token locally and syncs it to the backend if the user is already logged in.
     *
     * See: docs/api/FCM.md → Full Token Lifecycle
     * See: docs/models/FCM_TOKEN.md
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Always save locally first — used on next login if not logged in yet
        PrefsManager.saveFcmToken(applicationContext, token)

        // Sync to backend only if the user is already authenticated
        if (PrefsManager.isLoggedIn(applicationContext)) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    ApiClient.instance.updateFcmToken(FcmTokenRequest(token))
                } catch (_: Exception) {
                    // Non-critical — token will be synced on next login
                }
            }
        }
    }

    /**
     * Called when a push notification is received while the app is in foreground.
     * Displays the notification using the system NotificationManager.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: getString(R.string.app_name)
        val body  = message.notification?.body  ?: ""
        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "ptoda_channel"
        val manager   = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "PTODA Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}


