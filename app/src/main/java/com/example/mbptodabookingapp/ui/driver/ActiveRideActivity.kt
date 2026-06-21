package com.example.mbptodabookingapp.ui.driver

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.mbptodabookingapp.R
import com.example.mbptodabookingapp.databinding.ActivityActiveRideBinding
import com.example.mbptodabookingapp.utils.Resource
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

/**
 * Shows the accepted/active ride on a map so the driver can navigate to the
 * passenger and then mark the ride as complete.
 *
 * Reachable from:
 *  - RideRequestActivity after accepting (direct Intent)
 *  - DriverDashboardFragment → "Resume Ride" button on the Active Ride banner
 *
 * Phase 6 Active-Ride fix — UI Migration Roadmap
 */
class ActiveRideActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        const val EXTRA_BOOKING_ID = "booking_id"
    }

    private lateinit var binding: ActivityActiveRideBinding
    private lateinit var viewModel: DriverViewModel
    private var bookingId: Int = -1
    private var googleMap: GoogleMap? = null
    private var lastAction = "" // "start" or "complete"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActiveRideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bookingId = intent.getIntExtra(EXTRA_BOOKING_ID, -1)
        viewModel = ViewModelProvider(this)[DriverViewModel::class.java]

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (bookingId != -1) viewModel.fetchBooking(bookingId)

        binding.btnStartRide.setOnClickListener {
            if (bookingId != -1) { lastAction = "start"; viewModel.startRide(bookingId) }
        }
        binding.btnCompleteRide.setOnClickListener {
            if (bookingId != -1) { lastAction = "complete"; viewModel.completeRide(bookingId) }
        }

        observeViewModel()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onMapReady(map: GoogleMap) { googleMap = map }

    private fun observeViewModel() {
        viewModel.booking.observe(this) { state ->
            if (state is Resource.Success) {
                val b = state.data ?: return@observe

                // Booking ID + status badge
                binding.tvBookingId.text = getString(R.string.label_booking_id, b.id)
                binding.tvStatus.text = when (b.status) {
                    "in_progress" -> getString(R.string.active_ride_status_in_progress)
                    else          -> getString(R.string.active_ride_status_accepted)
                }

                // Addresses
                binding.tvPassengerPickup.text  = b.pickup_address
                binding.tvPassengerDropoff.text = b.dropoff_address

                // Show the right action button based on status
                binding.btnStartRide.visibility    = if (b.status == "accepted")     View.VISIBLE else View.GONE
                binding.btnCompleteRide.visibility = if (b.status == "in_progress")  View.VISIBLE else View.GONE

                // Map markers
                val pickup  = LatLng(b.pickup_lat,  b.pickup_lng)
                val dropoff = LatLng(b.dropoff_lat, b.dropoff_lng)
                googleMap?.addMarker(MarkerOptions().position(pickup).title("Pickup"))
                googleMap?.addMarker(
                    MarkerOptions().position(dropoff).title("Dropoff")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                )
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(pickup, 14f))
            }
        }

        viewModel.actionState.observe(this) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.progressBar.visibility    = View.VISIBLE
                    binding.btnStartRide.isEnabled    = false
                    binding.btnCompleteRide.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility    = View.GONE
                    binding.btnStartRide.isEnabled    = true
                    binding.btnCompleteRide.isEnabled = true
                    if (lastAction == "start") {
                        Toast.makeText(this, "Ride started!", Toast.LENGTH_SHORT).show()
                        viewModel.fetchBooking(bookingId) // refreshes buttons to show Complete
                    } else {
                        Toast.makeText(this, getString(R.string.active_ride_complete_success), Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, DriverHomeActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        })
                        finish()
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility    = View.GONE
                    binding.btnStartRide.isEnabled    = true
                    binding.btnCompleteRide.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}



