package com.example.mbptodabookingapp.ui.passenger

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.mbptodabookingapp.R
import com.example.mbptodabookingapp.data.models.BookingRequest
import com.example.mbptodabookingapp.databinding.ActivityBookRideBinding
import com.example.mbptodabookingapp.utils.Resource
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

/** Passenger enters pickup/dropoff details and submits a ride request. */
class BookRideActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityBookRideBinding
    private lateinit var viewModel: PassengerViewModel
    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookRideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProvider(this)[PassengerViewModel::class.java]

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnUseCurrentLocation.setOnClickListener { fillCurrentLocation() }
        binding.btnRequestRide.setOnClickListener { submitBooking() }

        observeViewModel()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed(); return true
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            googleMap?.isMyLocationEnabled = true
        }
    }

    private fun fillCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission not granted.", Toast.LENGTH_SHORT).show()
            return
        }
        LocationServices.getFusedLocationProviderClient(this).lastLocation
            .addOnSuccessListener { loc ->
                loc?.let {
                    binding.etPickupLat.setText(it.latitude.toString())
                    binding.etPickupLng.setText(it.longitude.toString())
                    binding.etPickupAddress.setText("Current Location (${String.format("%.4f", it.latitude)}, ${String.format("%.4f", it.longitude)})")
                    googleMap?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15f)
                    )
                }
            }
    }

    private fun submitBooking() {
        val pickupAddr = binding.etPickupAddress.text.toString().trim()
        val pickupLat  = binding.etPickupLat.text.toString().toDoubleOrNull()
        val pickupLng  = binding.etPickupLng.text.toString().toDoubleOrNull()
        val dropoffAddr = binding.etDropoffAddress.text.toString().trim()
        val dropoffLat  = binding.etDropoffLat.text.toString().toDoubleOrNull()
        val dropoffLng  = binding.etDropoffLng.text.toString().toDoubleOrNull()

        if (pickupAddr.isEmpty())  { binding.tilPickupAddress.error  = "Required"; return }
        if (pickupLat == null)     { binding.tilPickupLat.error      = "Invalid";  return }
        if (pickupLng == null)     { binding.tilPickupLng.error      = "Invalid";  return }
        if (dropoffAddr.isEmpty()) { binding.tilDropoffAddress.error = "Required"; return }
        if (dropoffLat == null)    { binding.tilDropoffLat.error     = "Invalid";  return }
        if (dropoffLng == null)    { binding.tilDropoffLng.error     = "Invalid";  return }

        viewModel.createBooking(
            BookingRequest(pickupAddr, pickupLat, pickupLng, dropoffAddr, dropoffLat, dropoffLng)
        )
    }

    private fun observeViewModel() {
        viewModel.createState.observe(this) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.progressBar.visibility  = View.VISIBLE
                    binding.btnRequestRide.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility  = View.GONE
                    binding.btnRequestRide.isEnabled = true
                    val bookingId = state.data ?: return@observe
                    startActivity(
                        Intent(this, RideStatusActivity::class.java)
                            .putExtra(RideStatusActivity.EXTRA_BOOKING_ID, bookingId)
                    )
                    finish()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility  = View.GONE
                    binding.btnRequestRide.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

