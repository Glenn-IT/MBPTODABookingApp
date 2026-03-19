package com.example.mbptodabookingapp.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.mbptodabookingapp.R
import com.example.mbptodabookingapp.databinding.ActivityAdminDashboardBinding
import com.example.mbptodabookingapp.ui.auth.AuthViewModel
import com.example.mbptodabookingapp.ui.auth.LoginActivity
import com.example.mbptodabookingapp.utils.Resource

/** Admin dashboard — shows counts for users, pending drivers, and bookings. */
class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var viewModel: AdminViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        viewModel = ViewModelProvider(this)[AdminViewModel::class.java]

        observeViewModel()
        loadStats()

        binding.btnManageUsers.setOnClickListener {
            startActivity(Intent(this, ManageUsersActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadStats()
    }

    private fun loadStats() {
        viewModel.fetchUsers()
        viewModel.fetchPendingDrivers()
        viewModel.fetchBookings()
    }

    private fun observeViewModel() {
        viewModel.users.observe(this) { state ->
            when (state) {
                is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvUserCount.text = state.data?.size.toString()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvUserCount.text = "!"
                }
            }
        }
        viewModel.pendingDrivers.observe(this) { state ->
            if (state is Resource.Success)
                binding.tvPendingCount.text = state.data?.size.toString()
        }
        viewModel.bookings.observe(this) { state ->
            if (state is Resource.Success)
                binding.tvBookingCount.text = state.data?.size.toString()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_logout) {
            ViewModelProvider(this)[AuthViewModel::class.java].logout()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

