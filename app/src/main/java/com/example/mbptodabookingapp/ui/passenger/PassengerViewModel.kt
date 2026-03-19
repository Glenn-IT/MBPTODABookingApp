package com.example.mbptodabookingapp.ui.passenger

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mbptodabookingapp.data.api.ApiClient
import com.example.mbptodabookingapp.data.models.Booking
import com.example.mbptodabookingapp.data.models.BookingRequest
import com.example.mbptodabookingapp.data.repository.BookingRepository
import com.example.mbptodabookingapp.utils.Resource
import kotlinx.coroutines.launch

/**
 * ViewModel for all passenger screens.
 * See: docs/api/BOOKINGS.md · docs/flows/BOOKING_FLOW.md
 */
class PassengerViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = BookingRepository(ApiClient.instance)

    // ── Create booking ────────────────────────────────────────────────────────

    private val _createState = MutableLiveData<Resource<Int>>()
    val createState: LiveData<Resource<Int>> = _createState

    fun createBooking(request: BookingRequest) {
        _createState.value = Resource.Loading
        viewModelScope.launch {
            _createState.value = repo.createBooking(request)
        }
    }

    // ── Single booking (for status polling) ───────────────────────────────────

    private val _booking = MutableLiveData<Resource<Booking>>()
    val booking: LiveData<Resource<Booking>> = _booking

    /**
     * Fetch the current state of a booking.
     * Called every 5 s from RideStatusActivity until status is terminal.
     * See: docs/flows/BOOKING_FLOW.md → Step 5 (polling)
     */
    fun fetchBooking(id: Int) {
        viewModelScope.launch {
            _booking.value = repo.getBookingById(id)
        }
    }

    // ── History ───────────────────────────────────────────────────────────────

    private val _history = MutableLiveData<Resource<List<Booking>>>()
    val history: LiveData<Resource<List<Booking>>> = _history

    fun fetchHistory() {
        _history.value = Resource.Loading
        viewModelScope.launch {
            _history.value = repo.getPassengerHistory()
        }
    }
}

