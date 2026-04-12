package com.example.mbptodabookingapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.mbptodabookingapp.databinding.ActivityRegisterBinding
import com.example.mbptodabookingapp.utils.Resource
import com.example.mbptodabookingapp.utils.UserRole

/**
 * Registration screen — collects user details, shows driver fields when role = driver.
 * On success redirects to LoginActivity (driver sees pending-approval message first).
 *
 * See: docs/api/AUTH.md → POST /auth/register
 * See: docs/flows/AUTH_FLOW.md → Registration Flow
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupRoleToggle()
        binding.btnRegister.setOnClickListener { attemptRegister() }
        binding.tvLogin.setOnClickListener { finish() }

        observeViewModel()
    }

    /** Show/hide driver-only fields when the role radio group changes. */
    private fun setupRoleToggle() {
        binding.rgRole.setOnCheckedChangeListener { _, checkedId ->
            val isDriver = checkedId == binding.rbDriver.id
            val vis = if (isDriver) View.VISIBLE else View.GONE
            // Section label added in Phase 3 — toggled alongside driver fields
            binding.tvDriverSectionLabel.visibility = vis
            binding.tilLicenseNo.visibility = vis
            binding.tilVehicleNo.visibility  = vis
        }
    }

    private fun attemptRegister() {
        val name     = binding.etName.text.toString().trim()
        val email    = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val isDriver = binding.rbDriver.isChecked
        val role     = if (isDriver) UserRole.DRIVER else UserRole.PASSENGER

        if (name.isEmpty())     { binding.tilName.error     = "Name is required";     return }
        if (email.isEmpty())    { binding.tilEmail.error    = "Email is required";    return }
        if (password.length < 6){ binding.tilPassword.error = "Min 6 characters";    return }

        var licenseNo: String? = null
        var vehicleNo: String? = null
        if (isDriver) {
            licenseNo = binding.etLicenseNo.text.toString().trim()
            vehicleNo = binding.etVehicleNo.text.toString().trim()
            if (licenseNo.isEmpty()) { binding.tilLicenseNo.error = "License No required"; return }
            if (vehicleNo.isEmpty()) { binding.tilVehicleNo.error = "Vehicle No required"; return }
        }

        listOf(binding.tilName, binding.tilEmail, binding.tilPassword,
               binding.tilLicenseNo, binding.tilVehicleNo).forEach { it.error = null }

        viewModel.register(name, email, password, role, licenseNo, vehicleNo)
    }

    private fun observeViewModel() {
        viewModel.registerState.observe(this) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnRegister.isEnabled  = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled  = true
                    val msg = if (binding.rbDriver.isChecked)
                        "Registration successful! Await admin approval before logging in."
                    else
                        "Registration successful! Please log in."
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    })
                    finish()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled  = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

