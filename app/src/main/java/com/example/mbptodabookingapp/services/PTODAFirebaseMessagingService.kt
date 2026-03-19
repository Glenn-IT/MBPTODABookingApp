package com.example.mbptodabookingapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.mbptodabookingapp.MainActivity
import com.example.mbptodabookingapp.R
import com.example.mbptodabookingapp.data.api.ApiClient
import com.example.mbptodabookingapp.data.local.PrefsManager
import com.example.mbptodabookingapp.data.models.FcmTokenRequest
import com.example.mbptodabookingapp.ui.driver.DriverHomeActivity
import com.example.mbptodabookingapp.ui.passenger.PassengerHomeActivity
import com.example.mbptodabookingapp.ui.passenger.RideStatusActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Handles incoming FCM push notifications and token refresh events.
 *
 * See: docs/api/FCM.md · docs/flows/AUTH_FLOW.md
 * See: DEVELOPMENT_CHECKLIST.md → 4.9.3 / 4.9.5
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
     * Called when a push notification is received while the app is in the foreground,
     * or for data-only messages in any app state.
     *
     * Supports both:
     *  - notification payload (title/body from Firebase Console or PHP helper)
     *  - data payload (booking_id, type) for routing on tap
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Prefer notification payload; fall back to data payload keys
        val title    = message.notification?.title ?: message.data["title"] ?: getString(R.string.app_name)
        val body     = message.notification?.body  ?: message.data["body"]  ?: ""
        val bookingId = message.data["booking_id"]?.toIntOrNull()

        showNotification(title, body, bookingId)
    }

    /**
     * Builds and displays a system notification.
     * Tapping the notification opens the role-appropriate screen:
     *  - driver  → DriverHomeActivity (see new requests)
     *  - passenger with bookingId → RideStatusActivity
     *  - passenger without bookingId → PassengerHomeActivity
     *  - unknown → MainActivity (auth router)
     */
    private fun showNotification(title: String, body: String, bookingId: Int? = null) {
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

        // Route tap to the correct screen based on user role
        val role = PrefsManager.getUserRole(applicationContext) ?: ""
        val targetIntent: Intent = when (role) {
            "driver" -> Intent(this, DriverHomeActivity::class.java)
            "passenger" -> if (bookingId != null) {
                Intent(this, RideStatusActivity::class.java)
                    .putExtra(RideStatusActivity.EXTRA_BOOKING_ID, bookingId)
            } else {
                Intent(this, PassengerHomeActivity::class.java)
            }
            else -> Intent(this, MainActivity::class.java)
        }.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            targetIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
