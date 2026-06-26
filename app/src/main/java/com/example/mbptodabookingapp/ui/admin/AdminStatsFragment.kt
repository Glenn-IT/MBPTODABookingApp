package com.example.mbptodabookingapp.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mbptodabookingapp.databinding.FragmentAdminStatsBinding
import com.example.mbptodabookingapp.utils.Resource

class AdminStatsFragment : Fragment() {

    private var _binding: FragmentAdminStatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AdminViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[AdminViewModel::class.java]

        observeViewModel()
        loadStats()

        binding.btnManageUsers.setOnClickListener {
            startActivity(Intent(requireContext(), ManageUsersActivity::class.java))
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
        viewModel.users.observe(viewLifecycleOwner) { state ->
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
        viewModel.pendingDrivers.observe(viewLifecycleOwner) { state ->
            if (state is Resource.Success)
                binding.tvPendingCount.text = state.data?.size.toString()
        }
        viewModel.bookings.observe(viewLifecycleOwner) { state ->
            if (state is Resource.Success)
                binding.tvBookingCount.text = state.data?.size.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
