package com.example.mbptodabookingapp.ui.driver

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mbptodabookingapp.databinding.FragmentDriverRequestsBinding
import com.example.mbptodabookingapp.utils.Resource

/**
 * Driver Requests Fragment — shown when the "Requests" BottomNav tab is selected.
 * Hosts the RideRequestsAdapter RecyclerView and Refresh button.
 * Shares the DriverViewModel with the parent DriverHomeActivity.
 *
 * Phase 6 — UI Migration Roadmap
 */
class DriverRequestsFragment : Fragment() {

    private var _binding: FragmentDriverRequestsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DriverViewModel
    private lateinit var adapter: RideRequestsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDriverRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Share ViewModel with parent activity
        viewModel = ViewModelProvider(requireActivity())[DriverViewModel::class.java]

        setupRecyclerView()
        observeViewModel()

        binding.btnRefresh.setOnClickListener { viewModel.fetchRequests() }
    }

    private fun setupRecyclerView() {
        adapter = RideRequestsAdapter { booking ->
            startActivity(
                Intent(requireContext(), RideRequestActivity::class.java)
                    .putExtra(RideRequestActivity.EXTRA_BOOKING_ID,       booking.id)
                    .putExtra(RideRequestActivity.EXTRA_PICKUP_ADDRESS,   booking.pickup_address)
                    .putExtra(RideRequestActivity.EXTRA_DROPOFF_ADDRESS,  booking.dropoff_address)
                    .putExtra(RideRequestActivity.EXTRA_PICKUP_LAT,       booking.pickup_lat)
                    .putExtra(RideRequestActivity.EXTRA_PICKUP_LNG,       booking.pickup_lng)
                    .putExtra(RideRequestActivity.EXTRA_DROPOFF_LAT,      booking.dropoff_lat)
                    .putExtra(RideRequestActivity.EXTRA_DROPOFF_LNG,      booking.dropoff_lng)
            )
        }
        binding.rvRequests.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRequests.adapter       = adapter
    }

    private fun observeViewModel() {
        viewModel.requests.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val list = state.data
                    adapter.submitList(list)
                    if (list.isEmpty()) {
                        Toast.makeText(requireContext(), "No pending requests.", Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

