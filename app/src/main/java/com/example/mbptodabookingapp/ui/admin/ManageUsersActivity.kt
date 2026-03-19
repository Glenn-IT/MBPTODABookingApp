package com.example.mbptodabookingapp.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mbptodabookingapp.databinding.ActivityManageUsersBinding
import com.example.mbptodabookingapp.utils.Resource
import com.google.android.material.tabs.TabLayout

/**
 * Admin user management — Tab 0: Pending Drivers (approve/reject)
 *                         Tab 1: All Users (activate/deactivate/delete)
 */
class ManageUsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageUsersBinding
    private lateinit var viewModel: AdminViewModel

    private val usersAdapter = UsersAdapter(
        onActivate   = { u -> viewModel.activateUser(u.id) },
        onDeactivate = { u -> viewModel.deactivateUser(u.id) },
        onDelete     = { u ->
            AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Permanently delete ${u.name}?")
                .setPositiveButton("Delete") { _, _ -> viewModel.deleteUser(u.id) }
                .setNegativeButton("Cancel", null)
                .show()
        }
    )

    private val driversAdapter = PendingDriversAdapter(
        onApprove = { d -> viewModel.approveDriver(d.id) },
        onReject  = { d -> viewModel.rejectDriver(d.id) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProvider(this)[AdminViewModel::class.java]

        binding.rvList.layoutManager = LinearLayoutManager(this)

        setupTabs()
        observeViewModel()

        // Start on Pending Drivers tab
        viewModel.fetchPendingDrivers()
        binding.rvList.adapter = driversAdapter
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> { binding.rvList.adapter = driversAdapter; viewModel.fetchPendingDrivers() }
                    1 -> { binding.rvList.adapter = usersAdapter;   viewModel.fetchUsers() }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun observeViewModel() {
        viewModel.pendingDrivers.observe(this) { state ->
            when (state) {
                is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    driversAdapter.submitList(state.data ?: emptyList())
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.users.observe(this) { state ->
            when (state) {
                is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    usersAdapter.submitList(state.data ?: emptyList())
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.actionState.observe(this) { state ->
            if (state is Resource.Error)
                Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
        }
    }
}

