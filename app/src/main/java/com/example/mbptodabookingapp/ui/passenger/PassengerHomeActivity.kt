package com.example.mbptodabookingapp.ui.passenger

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.mbptodabookingapp.R
import com.example.mbptodabookingapp.data.local.PrefsManager
import com.example.mbptodabookingapp.databinding.ActivityPassengerHomeBinding
import com.example.mbptodabookingapp.ui.auth.AuthViewModel
import com.example.mbptodabookingapp.ui.auth.LoginActivity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

/** Passenger home — map + FAB on Dashboard tab, ride history on History tab. */
class PassengerHomeActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityPassengerHomeBinding
    private var googleMap: GoogleMap? = null
    private val LOCATION_REQUEST     = 1001
    private val NOTIFICATION_REQUEST = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPassengerHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Hello, ${PrefsManager.getUserName(this) ?: "Passenger"}"

        // Safe cast — maps API may not be active yet
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        // FAB still works as a quick shortcut on Dashboard
        binding.fabBookRide.setOnClickListener {
            startActivity(Intent(this, BookRideActivity::class.java))
        }

        setupBottomNav()
        requestNotificationPermission()
    }

    // ── BottomNav ─────────────────────────────────────────────────────────────

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_dashboard -> {
                    // Show map + FAB, hide fragment overlay
                    binding.fragmentContainer.visibility = View.GONE
                    binding.fabBookRide.visibility       = View.VISIBLE
                    true
                }

                R.id.nav_book_ride -> {
                    // Navigate to booking screen — don't change tab selection
                    startActivity(Intent(this, BookRideActivity::class.java))
                    false   // return false = don't highlight this tab
                }

                R.id.nav_history -> {
                    // Show history fragment, hide FAB
                    binding.fabBookRide.visibility       = View.GONE
                    binding.fragmentContainer.visibility = View.VISIBLE
                    showHistoryFragment()
                    true
                }

                else -> false
            }
        }
    }

    private fun showHistoryFragment() {
        // Only add if not already added (avoids stacking on repeated taps)
        if (supportFragmentManager.findFragmentByTag("history") == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, RideHistoryFragment(), "history")
                .commit()
        }
    }

    // ── Map (existing logic unchanged) ───────────────────────────────────────

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST
            )
        }
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return

        googleMap?.isMyLocationEnabled = true
        LocationServices.getFusedLocationProviderClient(this).lastLocation
            .addOnSuccessListener { loc ->
                loc?.let {
                    googleMap?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15f)
                    )
                }
            }
    }

    /** Request POST_NOTIFICATIONS permission on Android 13+. */
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

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_REQUEST -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation()
                } else {
                    Toast.makeText(this, "Location permission needed for map.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ── Options menu (logout) — unchanged ────────────────────────────────────

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

