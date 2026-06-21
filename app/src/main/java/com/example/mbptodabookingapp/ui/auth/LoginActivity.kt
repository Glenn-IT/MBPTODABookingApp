package com.example.mbptodabookingapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.mbptodabookingapp.R
import com.example.mbptodabookingapp.data.local.PrefsManager
import com.example.mbptodabookingapp.databinding.ActivityLoginBinding
import com.example.mbptodabookingapp.ui.admin.AdminDashboardActivity
import com.example.mbptodabookingapp.ui.driver.DriverHomeActivity
import com.example.mbptodabookingapp.ui.passenger.PassengerHomeActivity
import com.example.mbptodabookingapp.utils.Constants
import com.example.mbptodabookingapp.utils.Resource
import com.example.mbptodabookingapp.utils.UserRole

/**
 * Login screen — collects email/password, calls AuthViewModel.login(),
 * then navigates to the role-appropriate home screen on success.
 *
 * See: docs/api/AUTH.md → POST /auth/login
 * See: docs/flows/AUTH_FLOW.md → Login Flow · 4.5.4
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        binding.btnLogin.setOnClickListener { attemptLogin() }
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.tvForgotPassword.setOnClickListener { showForgotPasswordDialog() }
        binding.tvServerConfig.setOnClickListener { showServerConfigDialog() }

        observeViewModel()
    }

    private fun attemptLogin() {
        val email    = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (email.isEmpty()) { binding.tilEmail.error = "Email is required"; return }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Invalid email address"; return
        }
        if (password.isEmpty()) { binding.tilPassword.error = "Password is required"; return }

        binding.tilEmail.error    = null
        binding.tilPassword.error = null
        viewModel.login(email, password)
    }

    private fun showForgotPasswordDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.forgot_password))
            .setMessage("Please contact your PTODA admin to reset your password.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled     = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled     = true
                    navigateToHome(state.data?.user?.role ?: "")
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled     = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showServerConfigDialog() {
        val currentUrl = PrefsManager.getServerUrl(this)
        val input = EditText(this).apply {
            setText(currentUrl)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            selectAll()
            setPadding(48, 24, 48, 24)
        }
        AlertDialog.Builder(this)
            .setTitle("Server URL")
            .setMessage("Enter the base URL of your XAMPP server.\nExample: http://10.240.57.14/ptoda_booking_api/")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                var url = input.text.toString().trim()
                if (url.isNotEmpty()) {
                    if (!url.endsWith("/")) url += "/"
                    PrefsManager.saveServerUrl(this, url)
                    Toast.makeText(this, "Saved: $url", Toast.LENGTH_LONG).show()
                }
            }
            .setNeutralButton("Reset Default") { _, _ ->
                PrefsManager.saveServerUrl(this, Constants.BASE_URL_DEVICE)
                Toast.makeText(this, "Reset to ${Constants.BASE_URL_DEVICE}", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /** Navigate to the correct home screen based on JWT role. */
    private fun navigateToHome(role: String) {
        val intent = when (role) {
            UserRole.PASSENGER -> Intent(this, PassengerHomeActivity::class.java)
            UserRole.DRIVER    -> Intent(this, DriverHomeActivity::class.java)
            UserRole.ADMIN     -> Intent(this, AdminDashboardActivity::class.java)
            else               -> Intent(this, LoginActivity::class.java)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}

