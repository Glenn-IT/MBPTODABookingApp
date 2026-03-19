package com.example.mbptodabookingapp.ui.driver

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.mbptodabookingapp.databinding.ActivityRideRequestBinding
import com.example.mbptodabookingapp.utils.Resource
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.mbptodabookingapp.R

/** Shows booking details and Accept/Reject buttons for a pending ride request. */
class RideRequestActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        const val EXTRA_BOOKING_ID = "booking_id"
    }

    private lateinit var binding: ActivityRideRequestBinding
    private lateinit var viewModel: DriverViewModel
    private var bookingId: Int = -1
    private var googleMap: GoogleMap? = null
    private var lastAction: String = "" // "accept" or "reject"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRideRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bookingId = intent.getIntExtra(EXTRA_BOOKING_ID, -1)
        viewModel = ViewModelProvider(this)[DriverViewModel::class.java]

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (bookingId != -1) viewModel.fetchBooking(bookingId)

        binding.btnAccept.setOnClickListener { lastAction = "accept"; viewModel.acceptRide(bookingId) }
        binding.btnReject.setOnClickListener { lastAction = "reject"; viewModel.rejectRide(bookingId) }

        observeViewModel()
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }

    override fun onMapReady(map: GoogleMap) { googleMap = map }

    private fun observeViewModel() {
        viewModel.booking.observe(this) { state ->
            if (state is Resource.Success) {
                val b = state.data ?: return@observe
                binding.tvBookingId.text = "Booking #${b.id}"
                binding.tvPickup.text    = "Pickup: ${b.pickup_address}"
                binding.tvDropoff.text   = "Dropoff: ${b.dropoff_address}"
                try {
                    val pickupLatLng = LatLng(b.pickup_lat.toDouble(), b.pickup_lng.toDouble())
                    googleMap?.addMarker(MarkerOptions().position(pickupLatLng).title("Pickup"))
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(pickupLatLng, 14f))
                } catch (_: Exception) { }
            }
        }

        viewModel.actionState.observe(this) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnAccept.isEnabled    = false
                    binding.btnReject.isEnabled    = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    if (lastAction == "accept") {
                        startActivity(
                            Intent(this, ActiveRideActivity::class.java)
                                .putExtra(ActiveRideActivity.EXTRA_BOOKING_ID, bookingId)
                        )
                    }
                    finish()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnAccept.isEnabled    = true
                    binding.btnReject.isEnabled    = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}




