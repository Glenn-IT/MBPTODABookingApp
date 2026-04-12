package com.example.mbptodabookingapp.ui.driver

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.mbptodabookingapp.R
import com.example.mbptodabookingapp.databinding.ActivityRideRequestBinding
import com.example.mbptodabookingapp.utils.Resource
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

/**
 * Shows booking details for a pending ride request so the driver can Accept or Reject.
 *
 * Data is passed directly from DriverRequestsFragment via Intent extras — the booking
 * object is already in memory from GET /driver/requests, so NO second API call is needed
 * to populate the UI.  Accept / Reject actions still go through DriverViewModel.
 *
 * Fix: previously called GET /bookings/{id} which the PHP API rejects for unassigned
 * (driver_id = null) bookings, leaving the screen blank.  See BUGS_AND_FIXES.md BUG-016.
 */
class RideRequestActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        const val EXTRA_BOOKING_ID      = "booking_id"
        const val EXTRA_PICKUP_ADDRESS  = "pickup_address"
        const val EXTRA_DROPOFF_ADDRESS = "dropoff_address"
        const val EXTRA_PICKUP_LAT      = "pickup_lat"
        const val EXTRA_PICKUP_LNG      = "pickup_lng"
        const val EXTRA_DROPOFF_LAT     = "dropoff_lat"
        const val EXTRA_DROPOFF_LNG     = "dropoff_lng"
    }

    private lateinit var binding: ActivityRideRequestBinding
    private lateinit var viewModel: DriverViewModel
    private var bookingId: Int = -1
    private var googleMap: GoogleMap? = null
    private var lastAction: String = "" // "accept" or "reject"

    // Stored from Intent so map markers can be added after onMapReady fires
    private var pickupLatLng:  LatLng? = null
    private var dropoffLatLng: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRideRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bookingId = intent.getIntExtra(EXTRA_BOOKING_ID, -1)
        viewModel = ViewModelProvider(this)[DriverViewModel::class.java]

        populateFromIntent()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnAccept.setOnClickListener { lastAction = "accept"; viewModel.acceptRide(bookingId) }
        binding.btnReject.setOnClickListener { lastAction = "reject"; viewModel.rejectRide(bookingId) }

        observeActionState()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    /**
     * Populates all UI fields immediately from Intent extras.
     * Also parses and caches lat/lng so they are available when the map is ready.
     */
    private fun populateFromIntent() {
        val pickupAddress  = intent.getStringExtra(EXTRA_PICKUP_ADDRESS)  ?: ""
        val dropoffAddress = intent.getStringExtra(EXTRA_DROPOFF_ADDRESS) ?: ""
        val pickupLat      = intent.getStringExtra(EXTRA_PICKUP_LAT)      ?: ""
        val pickupLng      = intent.getStringExtra(EXTRA_PICKUP_LNG)      ?: ""
        val dropoffLat     = intent.getStringExtra(EXTRA_DROPOFF_LAT)     ?: ""
        val dropoffLng     = intent.getStringExtra(EXTRA_DROPOFF_LNG)     ?: ""

        // Populate text fields immediately — no API round-trip needed
        binding.tvBookingId.text = getString(R.string.label_booking_id, bookingId)
        binding.tvPickup.text    = pickupAddress
        binding.tvDropoff.text   = dropoffAddress

        // Cache LatLng for onMapReady
        try {
            if (pickupLat.isNotEmpty() && pickupLng.isNotEmpty()) {
                pickupLatLng = LatLng(pickupLat.toDouble(), pickupLng.toDouble())
            }
            if (dropoffLat.isNotEmpty() && dropoffLng.isNotEmpty()) {
                dropoffLatLng = LatLng(dropoffLat.toDouble(), dropoffLng.toDouble())
            }
        } catch (_: NumberFormatException) { }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        // Add markers now that the map is ready — lat/lng already parsed from Intent
        pickupLatLng?.let { latLng ->
            googleMap?.addMarker(MarkerOptions().position(latLng).title("Pickup"))
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
        }
        dropoffLatLng?.let { latLng ->
            googleMap?.addMarker(
                MarkerOptions().position(latLng).title("Dropoff")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )
        }
    }

    /** Observes only the Accept / Reject action result — data display is Intent-driven. */
    private fun observeActionState() {
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






