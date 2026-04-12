package com.example.mbptodabookingapp.ui.driver

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mbptodabookingapp.R
import com.example.mbptodabookingapp.data.local.PrefsManager
import com.example.mbptodabookingapp.data.models.Booking
import com.example.mbptodabookingapp.databinding.FragmentDriverDashboardBinding
import com.example.mbptodabookingapp.utils.Resource

/**
 * Driver Dashboard — shown when the Dashboard BottomNav tab is selected.
 * Displays:
 *  1. Active Ride Banner — appears when there is a booking with status
 *     "accepted" or "in_progress" assigned to this driver. Tapping it
 *     opens ActiveRideActivity so the driver can resume managing the ride.
 *  2. Welcome card with driver name + online indicator dot.
 *  3. Live stats card — pending requests count + completed rides count.
 *  4. Tips card.
 *
 * Shares DriverViewModel with the parent DriverHomeActivity — no extra API calls.
 *
 * Phase 6 / Active-Ride fix — UI Migration Roadmap
 */
class DriverDashboardFragment : Fragment() {

    private var _binding: FragmentDriverDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DriverViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDriverDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[DriverViewModel::class.java]

        val name = PrefsManager.getUserName(requireContext()) ?: "Driver"
        binding.tvWelcome.text = getString(R.string.driver_welcome, name)

        observeViewModel()
    }

    private fun observeViewModel() {
        // Pending count — GET /driver/requests only has status='requested' bookings ✓
        viewModel.requests.observe(viewLifecycleOwner) { state ->
            if (state is Resource.Success) {
                binding.tvDashPendingCount.text = state.data.count { it.status == "requested" }.toString()
            }
        }

        // Active banner + completed count — GET /bookings (role-filtered, all statuses)
        // IMPORTANT: do NOT read completed count from viewModel.requests — it never has them
        viewModel.driverBookings.observe(viewLifecycleOwner) { state ->
            if (state is Resource.Success) {
                val list = state.data
                val active    = list.firstOrNull { it.status == "accepted" || it.status == "in_progress" }
                val completed = list.count { it.status == "completed" }
                bindActiveRideBanner(active)
                binding.tvDashCompletedCount.text = completed.toString()
            }
        }
    }

    private fun bindActiveRideBanner(active: Booking?) {
        if (active == null) {
            binding.cardActiveRide.visibility = View.GONE
            return
        }

        binding.cardActiveRide.visibility = View.VISIBLE
        binding.tvActiveBannerBookingId.text = getString(R.string.label_booking_id, active.id)
        binding.tvActiveBannerPickup.text    = active.pickup_address
        binding.tvActiveBannerDropoff.text   = active.dropoff_address
        binding.tvActiveRideStatus.text      = when (active.status) {
            "in_progress" -> getString(R.string.active_ride_status_in_progress)
            else          -> getString(R.string.active_ride_status_accepted)
        }

        binding.btnResumeRide.setOnClickListener {
            startActivity(
                Intent(requireContext(), ActiveRideActivity::class.java)
                    .putExtra(ActiveRideActivity.EXTRA_BOOKING_ID, active.id)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}





