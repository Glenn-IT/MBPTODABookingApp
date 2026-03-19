package com.example.mbptodabookingapp.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mbptodabookingapp.data.api.ApiClient
import com.example.mbptodabookingapp.data.models.AdminUser
import com.example.mbptodabookingapp.data.models.Booking
import com.example.mbptodabookingapp.data.models.PendingDriver
import com.example.mbptodabookingapp.data.repository.AdminRepository
import com.example.mbptodabookingapp.utils.Resource
import kotlinx.coroutines.launch

/** ViewModel for admin screens. See: docs/api/ADMIN.md */
class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AdminRepository(ApiClient.instance)

    private val _users = MutableLiveData<Resource<List<AdminUser>>>()
    val users: LiveData<Resource<List<AdminUser>>> = _users

    private val _pendingDrivers = MutableLiveData<Resource<List<PendingDriver>>>()
    val pendingDrivers: LiveData<Resource<List<PendingDriver>>> = _pendingDrivers

    private val _bookings = MutableLiveData<Resource<List<Booking>>>()
    val bookings: LiveData<Resource<List<Booking>>> = _bookings

    private val _actionState = MutableLiveData<Resource<Unit>>()
    val actionState: LiveData<Resource<Unit>> = _actionState

    fun fetchUsers()          { _users.value = Resource.Loading;          viewModelScope.launch { _users.value          = repo.getAllUsers() } }
    fun fetchPendingDrivers() { _pendingDrivers.value = Resource.Loading; viewModelScope.launch { _pendingDrivers.value = repo.getPendingDrivers() } }
    fun fetchBookings()       { _bookings.value = Resource.Loading;       viewModelScope.launch { _bookings.value       = repo.getAllBookings() } }

    fun approveDriver(id: Int)  { viewModelScope.launch { _actionState.value = repo.approveDriver(id);  fetchPendingDrivers() } }
    fun rejectDriver(id: Int)   { viewModelScope.launch { _actionState.value = repo.rejectDriver(id);   fetchPendingDrivers() } }
    fun activateUser(id: Int)   { viewModelScope.launch { _actionState.value = repo.activateUser(id);   fetchUsers() } }
    fun deactivateUser(id: Int) { viewModelScope.launch { _actionState.value = repo.deactivateUser(id); fetchUsers() } }
    fun deleteUser(id: Int)     { viewModelScope.launch { _actionState.value = repo.deleteUser(id);     fetchUsers() } }
}

