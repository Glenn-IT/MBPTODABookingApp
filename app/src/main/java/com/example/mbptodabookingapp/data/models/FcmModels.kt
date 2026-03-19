package com.example.mbptodabookingapp.data.models

/**
 * Request body for PUT /user/fcm-token.
 * See: docs/api/FCM.md · docs/models/FCM_TOKEN.md
 */
data class FcmTokenRequest(
    val token: String
)

