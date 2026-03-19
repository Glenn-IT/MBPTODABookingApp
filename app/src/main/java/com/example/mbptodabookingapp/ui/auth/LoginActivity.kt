package com.example.mbptodabookingapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.mbptodabookingapp.databinding.ActivityLoginBinding
import com.example.mbptodabookingapp.ui.admin.AdminDashboardActivity
import com.example.mbptodabookingapp.ui.driver.DriverHomeActivity
import com.example.mbptodabookingapp.ui.passenger.PassengerHomeActivity
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

        observeViewModel()
    }

    private fun attemptLogin() {
        val email    = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (email.isEmpty())    { binding.tilEmail.error    = "Email is required";    return }
        if (password.isEmpty()) { binding.tilPassword.error = "Password is required"; return }

        binding.tilEmail.error    = null
        binding.tilPassword.error = null
        viewModel.login(email, password)
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

