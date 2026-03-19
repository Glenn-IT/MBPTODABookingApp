package com.example.mbptodabookingapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mbptodabookingapp.data.local.PrefsManager
import com.example.mbptodabookingapp.ui.admin.AdminDashboardActivity
import com.example.mbptodabookingapp.ui.auth.LoginActivity
import com.example.mbptodabookingapp.ui.driver.DriverHomeActivity
import com.example.mbptodabookingapp.ui.passenger.PassengerHomeActivity
import com.example.mbptodabookingapp.utils.UserRole

/**
 * Launch router — checks auth state and redirects to the correct screen.
 * No UI is shown here; it finishes immediately after redirecting.
 *
 * See: docs/flows/AUTH_FLOW.md → Android — Role-Based Navigation After Login
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (PrefsManager.isLoggedIn(this)) {
            navigateToHome()
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }

    private fun navigateToHome() {
        val intent = when (PrefsManager.getUserRole(this)) {
            UserRole.PASSENGER -> Intent(this, PassengerHomeActivity::class.java)
            UserRole.DRIVER    -> Intent(this, DriverHomeActivity::class.java)
            UserRole.ADMIN     -> Intent(this, AdminDashboardActivity::class.java)
            else               -> Intent(this, LoginActivity::class.java)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}