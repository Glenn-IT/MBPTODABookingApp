package com.example.mbptodabookingapp.ui.driver

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mbptodabookingapp.data.api.ApiClient
import com.example.mbptodabookingapp.data.models.Booking
import com.example.mbptodabookingapp.data.repository.BookingRepository
import com.example.mbptodabookingapp.data.repository.UserRepository
import com.example.mbptodabookingapp.utils.Resource
import kotlinx.coroutines.launch

/** ViewModel for all driver screens. See: docs/api/DRIVER.md */
class DriverViewModel(application: Application) : AndroidViewModel(application) {

    private val bookingRepo = BookingRepository(ApiClient.instance)
    private val userRepo    = UserRepository(ApiClient.instance)

    private val _requests = MutableLiveData<Resource<List<Booking>>>()
    val requests: LiveData<Resource<List<Booking>>> = _requests

    /**
     * All bookings assigned to this driver (accepted, in_progress, completed).
     * Populated by fetchDriverBookings() → GET /bookings (role-filtered).
     * Used by DriverDashboardFragment to surface the "Active Ride" banner.
     */
    private val _driverBookings = MutableLiveData<Resource<List<Booking>>>()
    val driverBookings: LiveData<Resource<List<Booking>>> = _driverBookings

    private val _booking = MutableLiveData<Resource<Booking>>()
    val booking: LiveData<Resource<Booking>> = _booking

    private val _actionState = MutableLiveData<Resource<Unit>>()
    val actionState: LiveData<Resource<Unit>> = _actionState

    fun fetchRequests() {
        viewModelScope.launch { _requests.value = bookingRepo.getDriverRequests() }
    }

    /**
     * Fetches ALL bookings assigned to this driver (role-filtered by the API).
     * Called on every DriverHomeActivity resume so the dashboard stays current.
     */
    fun fetchDriverBookings() {
        viewModelScope.launch { _driverBookings.value = bookingRepo.getBookings() }
    }

    fun fetchBooking(id: Int) {
        viewModelScope.launch { _booking.value = bookingRepo.getBookingById(id) }
    }

    fun acceptRide(bookingId: Int) {
        _actionState.value = Resource.Loading
        viewModelScope.launch { _actionState.value = bookingRepo.acceptRide(bookingId) }
    }

    fun rejectRide(bookingId: Int) {
        _actionState.value = Resource.Loading
        viewModelScope.launch { _actionState.value = bookingRepo.rejectRide(bookingId) }
    }

    fun completeRide(bookingId: Int) {
        _actionState.value = Resource.Loading
        viewModelScope.launch { _actionState.value = bookingRepo.completeRide(bookingId) }
    }

    fun updateLocation(lat: Double, lng: Double) {
        viewModelScope.launch { userRepo.updateLocation(lat, lng) }
    }
}

