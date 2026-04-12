package com.example.mbptodabookingapp.ui.driver

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.mbptodabookingapp.R
import com.example.mbptodabookingapp.databinding.ActivityDriverHomeBinding
import com.example.mbptodabookingapp.ui.auth.AuthViewModel
import com.example.mbptodabookingapp.ui.auth.LoginActivity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

/**
 * Driver home — Dashboard | Requests | Status tabs via BottomNavigationView.
 * Each tab loads a dedicated Fragment into fragmentContainer.
 * The map is always visible as the background layer.
 * Phase 6 — UI Migration Roadmap
 */
class DriverHomeActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityDriverHomeBinding
    private lateinit var viewModel: DriverViewModel
    private var googleMap: GoogleMap? = null
    private val NOTIFICATION_REQUEST = 2001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        viewModel = ViewModelProvider(this)[DriverViewModel::class.java]

        setupMap()
        setupBottomNav()

        // Pre-load ride requests so fragments have data immediately
        viewModel.fetchRequests()
        viewModel.fetchDriverBookings()

        requestNotificationPermission()
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchRequests()
        viewModel.fetchDriverBookings()
    }

    // ── BottomNav ─────────────────────────────────────────────────────────────

    private fun setupBottomNav() {
        // Default: Dashboard tab
        binding.bottomNav.selectedItemId = R.id.nav_map
        showFragment("dashboard", ::DriverDashboardFragment)

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_map      -> { showFragment("dashboard", ::DriverDashboardFragment); true }
                R.id.nav_requests -> { showFragment("requests",  ::DriverRequestsFragment);  true }
                R.id.nav_status   -> { showFragment("status",    ::DriverStatusFragment);    true }
                else              -> false
            }
        }
    }

    /**
     * Show [fragmentContainer], then add or replace the fragment with the given [tag].
     * The fragment is created lazily via [factory] only when not already present.
     */
    private fun showFragment(tag: String, factory: () -> androidx.fragment.app.Fragment) {
        binding.fragmentContainer.visibility = View.VISIBLE
        if (supportFragmentManager.findFragmentByTag(tag) == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, factory(), tag)
                .commit()
        } else {
            // Bring existing fragment to front by re-attaching if needed
            supportFragmentManager.findFragmentByTag(tag)?.let { frag ->
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, frag, tag)
                    .commit()
            }
        }
    }

    // ── Map — always visible as background ───────────────────────────────────

    private fun setupMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
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

    // ── Permissions ───────────────────────────────────────────────────────────

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_REQUEST
                )
            }
        }
    }

    // ── Options menu (logout) ─────────────────────────────────────────────────

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




