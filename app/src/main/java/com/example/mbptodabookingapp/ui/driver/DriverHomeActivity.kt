package com.example.mbptodabookingapp.ui.driver

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mbptodabookingapp.R
import com.example.mbptodabookingapp.data.models.Booking
import com.example.mbptodabookingapp.databinding.ActivityDriverHomeBinding
import com.example.mbptodabookingapp.ui.auth.AuthViewModel
import com.example.mbptodabookingapp.ui.auth.LoginActivity
import com.example.mbptodabookingapp.utils.Resource
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

/** Driver home — map + list of pending ride requests. */
class DriverHomeActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityDriverHomeBinding
    private lateinit var viewModel: DriverViewModel
    private lateinit var adapter: RideRequestsAdapter
    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        viewModel = ViewModelProvider(this)[DriverViewModel::class.java]

        setupRecyclerView()
        setupMap()
        observeViewModel()

        binding.btnRefresh.setOnClickListener { viewModel.fetchRequests() }
        viewModel.fetchRequests()
    }

    private fun setupRecyclerView() {
        adapter = RideRequestsAdapter { booking ->
            startActivity(
                Intent(this, RideRequestActivity::class.java)
                    .putExtra(RideRequestActivity.EXTRA_BOOKING_ID, booking.id)
            )
        }
        binding.rvRequests.layoutManager = LinearLayoutManager(this)
        binding.rvRequests.adapter       = adapter
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            googleMap?.isMyLocationEnabled = true
            LocationServices.getFusedLocationProviderClient(this).lastLocation
                .addOnSuccessListener { loc ->
                    loc?.let {
                        googleMap?.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 14f)
                        )
                        viewModel.updateLocation(it.latitude, it.longitude)
                    }
                }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchRequests()
    }

    private fun observeViewModel() {
        viewModel.requests.observe(this) { state ->
            when (state) {
                is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val list = state.data ?: emptyList()
                    adapter.submitList(list)
                    if (list.isEmpty()) Toast.makeText(this, "No pending requests.", Toast.LENGTH_SHORT).show()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_logout) {
            ViewModelProvider(this)[AuthViewModel::class.java].logout()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

