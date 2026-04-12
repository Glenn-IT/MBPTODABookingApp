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
        viewModel.requests.observe(viewLifecycleOwner) { state ->
            if (state is Resource.Success) {
                val list = state.data
                val pending   = list.count { it.status == "requested" }
                val completed = list.count { it.status == "completed" }
                binding.tvPendingCount.text  = pending.toString()
                binding.tvAcceptedCount.text = completed.toString()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

