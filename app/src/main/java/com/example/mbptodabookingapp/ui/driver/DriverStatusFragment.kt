package com.example.mbptodabookingapp.ui.driver

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mbptodabookingapp.databinding.FragmentDriverStatusBinding
import com.example.mbptodabookingapp.utils.Resource

/**
 * Shows driver's online status + live counts of pending / completed rides.
 * Loaded in DriverHomeActivity when the Status BottomNav tab is selected.
 *
 * Uses the shared DriverViewModel from the parent activity.
 *
 * FIX (BUG-014 follow-up):
 *  - Pending count  → from viewModel.requests   (GET /driver/requests, status='requested')
 *  - Completed count → from viewModel.driverBookings (GET /bookings, role-filtered, all statuses)
 *  The old code read BOTH counts from viewModel.requests, which only ever contains
 *  'requested' bookings — so completed was always 0.
 */
class DriverStatusFragment : Fragment() {

    private var _binding: FragmentDriverStatusBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DriverViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDriverStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Share ViewModel with parent activity — no extra API calls
        viewModel = ViewModelProvider(requireActivity())[DriverViewModel::class.java]

        observeViewModel()
    }

    private fun observeViewModel() {
        // Pending count — requests list only has status='requested' bookings, correct source
        viewModel.requests.observe(viewLifecycleOwner) { state ->
            if (state is Resource.Success) {
                binding.tvPendingCount.text = state.data.count { it.status == "requested" }.toString()
            }
        }

        // Completed count — must come from driverBookings (GET /bookings, all statuses)
        // requests list NEVER contains completed bookings → always 0 if read from there
        viewModel.driverBookings.observe(viewLifecycleOwner) { state ->
            if (state is Resource.Success) {
                binding.tvAcceptedCount.text = state.data.count { it.status == "completed" }.toString()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



