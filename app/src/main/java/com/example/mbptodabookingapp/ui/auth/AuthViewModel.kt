package com.example.mbptodabookingapp.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mbptodabookingapp.data.api.ApiClient
import com.example.mbptodabookingapp.data.models.LoginRequest
import com.example.mbptodabookingapp.data.models.LoginResponse
import com.example.mbptodabookingapp.data.models.RegisterRequest
import com.example.mbptodabookingapp.data.repository.AuthRepository
import com.example.mbptodabookingapp.utils.Resource
import kotlinx.coroutines.launch

/**
 * ViewModel for LoginActivity and RegisterActivity.
 * Exposes LiveData for login/register state and calls AuthRepository.
 *
 * See: docs/api/AUTH.md · docs/flows/AUTH_FLOW.md
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AuthRepository(ApiClient.instance, application)

    // ── Login ─────────────────────────────────────────────────────────────────

    private val _loginState = MutableLiveData<Resource<LoginResponse>>()
    val loginState: LiveData<Resource<LoginResponse>> = _loginState

    fun login(email: String, password: String) {
        _loginState.value = Resource.Loading
        viewModelScope.launch {
            _loginState.value = repo.login(LoginRequest(email, password))
        }
    }

    // ── Register ──────────────────────────────────────────────────────────────

    private val _registerState = MutableLiveData<Resource<Int>>()
    val registerState: LiveData<Resource<Int>> = _registerState

    fun register(
        name: String,
        email: String,
        password: String,
        role: String,
        licenseNo: String? = null,
        vehicleNo: String? = null
    ) {
        _registerState.value = Resource.Loading
        viewModelScope.launch {
            _registerState.value = repo.register(
                RegisterRequest(name, email, password, role, licenseNo, vehicleNo)
            )
        }
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    fun logout() = repo.logout()
}

