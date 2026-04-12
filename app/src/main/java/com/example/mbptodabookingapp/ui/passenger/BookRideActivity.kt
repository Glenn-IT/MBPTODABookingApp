package com.example.mbptodabookingapp.ui.passenger

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.mbptodabookingapp.R
import com.example.mbptodabookingapp.data.models.BookingRequest
import com.example.mbptodabookingapp.databinding.ActivityBookRideBinding
import com.example.mbptodabookingapp.utils.Resource
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Passenger picks pickup and dropoff on the map (tap-to-place-marker),
 * or enters them manually, then submits a ride request.
 *
 * See: docs/flows/BOOKING_FLOW.md → Step 1–2
 * See: docs/api/BOOKINGS.md → POST /bookings
 * See: DEVELOPMENT_CHECKLIST.md → 4.10.6
 */
class BookRideActivity : AppCompatActivity(), OnMapReadyCallback {

    // Map interaction mode — which marker the next map tap will place
    private enum class MapMode { NONE, PICKUP, DROPOFF }

    private lateinit var binding: ActivityBookRideBinding
    private lateinit var viewModel: PassengerViewModel
    private var googleMap: GoogleMap? = null
    private var mapMode   = MapMode.NONE
    private var pickupMarker:  Marker? = null
    private var dropoffMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookRideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProvider(this)[PassengerViewModel::class.java]

        // Safe cast — map is hidden while Google Maps API is not activated.
        // Change back to: "as SupportMapFragment" once the API key is active.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        // Mode toggles — tapping the active button deactivates it (NONE)
        binding.btnModePickup.setOnClickListener {
            setMapMode(if (mapMode == MapMode.PICKUP) MapMode.NONE else MapMode.PICKUP)
        }
        binding.btnModeDropoff.setOnClickListener {
            setMapMode(if (mapMode == MapMode.DROPOFF) MapMode.NONE else MapMode.DROPOFF)
        }

        binding.btnUseCurrentLocation.setOnClickListener { fillCurrentLocation() }
        binding.btnRequestRide.setOnClickListener { submitBooking() }

        // Sample data — fills all 6 fields so booking can be tested without Maps API
        binding.btnUseSampleData.setOnClickListener { fillSampleData() }

        setMapMode(MapMode.NONE)   // initialise button colours + hint text
        observeViewModel()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed(); return true
    }

    // ── Map ───────────────────────────────────────────────────────────────────

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            googleMap?.isMyLocationEnabled = true
        }

        // Tap-to-place-marker — behaviour depends on the active MapMode
        googleMap?.setOnMapClickListener { latLng ->
            when (mapMode) {
                MapMode.PICKUP  -> setPickupFromMap(latLng)
                MapMode.DROPOFF -> setDropoffFromMap(latLng)
                MapMode.NONE    ->
                    Toast.makeText(this, getString(R.string.map_tap_hint_none), Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ── Mode Toggle ───────────────────────────────────────────────────────────

    /**
     * Updates [mapMode], button highlight colours, and the hint text.
     *
     * Active button → filled primary blue + white text.
     * Inactive button → transparent background + primary text (outline style).
     */
    private fun setMapMode(mode: MapMode) {
        mapMode = mode
        val primaryColor = ContextCompat.getColor(this, R.color.colorPrimary)
        val activeBg     = ColorStateList.valueOf(primaryColor)
        val inactiveBg   = ColorStateList.valueOf(Color.TRANSPARENT)

        when (mode) {
            MapMode.PICKUP -> {
                binding.btnModePickup.backgroundTintList  = activeBg
                binding.btnModePickup.setTextColor(Color.WHITE)
                binding.btnModeDropoff.backgroundTintList = inactiveBg
                binding.btnModeDropoff.setTextColor(primaryColor)
                binding.tvMapHint.text = getString(R.string.map_tap_hint_pickup)
                binding.tvMapHint.setTextColor(primaryColor)
            }
            MapMode.DROPOFF -> {
                binding.btnModeDropoff.backgroundTintList = activeBg
                binding.btnModeDropoff.setTextColor(Color.WHITE)
                binding.btnModePickup.backgroundTintList  = inactiveBg
                binding.btnModePickup.setTextColor(primaryColor)
                binding.tvMapHint.text = getString(R.string.map_tap_hint_dropoff)
                binding.tvMapHint.setTextColor(ContextCompat.getColor(this, R.color.statusCompleted))
            }
            MapMode.NONE -> {
                binding.btnModePickup.backgroundTintList  = inactiveBg
                binding.btnModePickup.setTextColor(primaryColor)
                binding.btnModeDropoff.backgroundTintList = inactiveBg
                binding.btnModeDropoff.setTextColor(primaryColor)
                binding.tvMapHint.text = getString(R.string.map_tap_hint_none)
                binding.tvMapHint.setTextColor(ContextCompat.getColor(this, R.color.grey))
            }
        }
    }

    // ── Map Tap Handlers ──────────────────────────────────────────────────────

    /** Places / moves the red pickup marker and fills the pickup form fields. */
    private fun setPickupFromMap(latLng: LatLng) {
        pickupMarker?.remove()
        pickupMarker = googleMap?.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Pickup")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )
        binding.etPickupLat.setText(String.format("%.6f", latLng.latitude))
        binding.etPickupLng.setText(String.format("%.6f", latLng.longitude))
        binding.tilPickupLat.error  = null
        binding.tilPickupLng.error  = null
        reverseGeocode(latLng) { addr ->
            binding.etPickupAddress.setText(addr)
            binding.tilPickupAddress.error = null
        }
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    /** Places / moves the green dropoff marker and fills the dropoff form fields. */
    private fun setDropoffFromMap(latLng: LatLng) {
        dropoffMarker?.remove()
        dropoffMarker = googleMap?.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Dropoff")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )
        binding.etDropoffLat.setText(String.format("%.6f", latLng.latitude))
        binding.etDropoffLng.setText(String.format("%.6f", latLng.longitude))
        binding.tilDropoffLat.error  = null
        binding.tilDropoffLng.error  = null
        reverseGeocode(latLng) { addr ->
            binding.etDropoffAddress.setText(addr)
            binding.tilDropoffAddress.error = null
        }
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    // ── Reverse Geocoding ─────────────────────────────────────────────────────

    /**
     * Converts [latLng] to a human-readable address using the device's built-in Geocoder.
     * Falls back to a formatted coordinate string ("lat, lng") if geocoding fails.
     * Uses the async API on Android 13+ (API 33) and the coroutine-based sync API below.
     *
     * See: DEVELOPMENT_CHECKLIST.md → 4.10.6
     */
    private fun reverseGeocode(latLng: LatLng, onResult: (String) -> Unit) {
        val geocoder = Geocoder(this, Locale.getDefault())
        val fallback = "${String.format("%.5f", latLng.latitude)}, ${String.format("%.5f", latLng.longitude)}"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Async API (non-blocking, runs on its own thread)
            geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) { addresses ->
                val addr = addresses.firstOrNull()?.getAddressLine(0) ?: fallback
                runOnUiThread { onResult(addr) }
            }
        } else {
            // Legacy synchronous API — offloaded to IO dispatcher
            lifecycleScope.launch(Dispatchers.IO) {
                val addr = try {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                        ?.firstOrNull()?.getAddressLine(0) ?: fallback
                } catch (_: Exception) { fallback }
                withContext(Dispatchers.Main) { onResult(addr) }
            }
        }
    }

    // ── Current Location Fill ─────────────────────────────────────────────────

    /** Reads the device's last known location and calls [setPickupFromMap] with it. */
    private fun fillCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission not granted.", Toast.LENGTH_SHORT).show()
            return
        }
        LocationServices.getFusedLocationProviderClient(this).lastLocation
            .addOnSuccessListener { loc ->
                loc?.let { setPickupFromMap(LatLng(it.latitude, it.longitude)) }
                    ?: Toast.makeText(this, "Location unavailable. Try again.", Toast.LENGTH_SHORT).show()
            }
    }

    // ── Sample Data Fill ──────────────────────────────────────────────────────

    /** Fills all 6 form fields with sample data — for testing without Maps API. */
    private fun fillSampleData() {
        binding.etPickupAddress.setText("123 Sample St, Sample City")
        binding.etPickupLat.setText("37.422065")
        binding.etPickupLng.setText("-122.084089")
        binding.etDropoffAddress.setText("456 Example Ave, Example Town")
        binding.etDropoffLat.setText("37.427474")
        binding.etDropoffLng.setText("-122.085289")
    }

    // ── Booking Submission ────────────────────────────────────────────────────

    private fun submitBooking() {
        val pickupAddr  = binding.etPickupAddress.text.toString().trim()
        val pickupLat   = binding.etPickupLat.text.toString().toDoubleOrNull()
        val pickupLng   = binding.etPickupLng.text.toString().toDoubleOrNull()
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
                    binding.progressBar.visibility   = View.VISIBLE
                    binding.btnRequestRide.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility   = View.GONE
                    binding.btnRequestRide.isEnabled = true
                    val bookingId = state.data ?: return@observe
                    startActivity(
                        Intent(this, RideStatusActivity::class.java)
                            .putExtra(RideStatusActivity.EXTRA_BOOKING_ID, bookingId)
                    )
                    finish()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility   = View.GONE
                    binding.btnRequestRide.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
