package com.example.mbptodabookingapp

import android.app.Application
import com.example.mbptodabookingapp.data.api.ApiClient

/**
 * Custom Application class.
 * Initialises ApiClient with the application context so the auth interceptor
 * can read the JWT token from SharedPreferences on every request.
 *
 * See: docs/flows/ANDROID_SETUP.md → Application Class — Init ApiClient
 */
class PTODAApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.init(this)
    }
}

