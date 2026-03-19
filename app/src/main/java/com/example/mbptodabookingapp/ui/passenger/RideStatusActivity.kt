package com.example.mbptodabookingapp.ui.passenger

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.mbptodabookingapp.data.models.Booking
import com.example.mbptodabookingapp.databinding.ActivityRideStatusBinding
import com.example.mbptodabookingapp.utils.BookingStatus
import com.example.mbptodabookingapp.utils.Resource

/**
 * Polls GET /bookings/{id} every 5 seconds and updates the UI until a terminal status is reached.
 * See: docs/flows/BOOKING_FLOW.md → Step 5
 * See: docs/api/BOOKINGS.md → Polling Pattern
 */
class RideStatusActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BOOKING_ID = "booking_id"
    }

    private lateinit var binding: ActivityRideStatusBinding
    private lateinit var viewModel: PassengerViewModel
    private var bookingId: Int = -1

    private val handler   = Handler(Looper.getMainLooper())
    private val pollDelay = 5_000L

    private val pollRunnable = object : Runnable {
        override fun run() {
            if (bookingId != -1) {
                viewModel.fetchBooking(bookingId)
                val currentStatus = (viewModel.booking.value as? Resource.Success)?.data?.status
                if (currentStatus == null || !BookingStatus.isTerminal(currentStatus)) {
                    handler.postDelayed(this, pollDelay)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRideStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bookingId = intent.getIntExtra(EXTRA_BOOKING_ID, -1)
        viewModel = ViewModelProvider(this)[PassengerViewModel::class.java]

        if (bookingId == -1) { Toast.makeText(this, "Invalid booking.", Toast.LENGTH_SHORT).show(); finish(); return }

        observeViewModel()
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }

    override fun onResume() {
        super.onResume()
        handler.post(pollRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(pollRunnable)
    }

    private fun observeViewModel() {
        viewModel.booking.observe(this) { state ->
            when (state) {
                is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    state.data?.let { updateUI(it) }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun updateUI(booking: Booking) {
        binding.tvBookingId.text = "Booking #${booking.id}"
        binding.tvPickup.text    = booking.pickup_address
        binding.tvDropoff.text   = booking.dropoff_address
        binding.tvStatus.text    = booking.status.uppercase().replace("_", " ")

        val color = when (booking.status) {
            BookingStatus.REQUESTED   -> getColor(com.example.mbptodabookingapp.R.color.statusRequested)
            BookingStatus.ACCEPTED    -> getColor(com.example.mbptodabookingapp.R.color.statusAccepted)
            BookingStatus.IN_PROGRESS -> getColor(com.example.mbptodabookingapp.R.color.statusInProgress)
            BookingStatus.COMPLETED   -> getColor(com.example.mbptodabookingapp.R.color.statusCompleted)
            BookingStatus.REJECTED,
            BookingStatus.CANCELLED   -> getColor(com.example.mbptodabookingapp.R.color.statusRejected)
            else -> getColor(com.example.mbptodabookingapp.R.color.grey)
        }
        binding.tvStatus.setTextColor(color)

        if (BookingStatus.isTerminal(booking.status)) {
            handler.removeCallbacks(pollRunnable)
            binding.tvPolling.visibility = View.GONE
        }
    }
}

