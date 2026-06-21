package com.example.mbptodabookingapp

import android.content.Context
import com.example.mbptodabookingapp.data.api.ApiResponse
import com.example.mbptodabookingapp.data.api.ApiService
import com.example.mbptodabookingapp.data.local.PrefsManager
import com.example.mbptodabookingapp.data.models.LoggedInUser
import com.example.mbptodabookingapp.data.models.LoginRequest
import com.example.mbptodabookingapp.data.models.LoginResponse
import com.example.mbptodabookingapp.data.models.RegisterRequest
import com.example.mbptodabookingapp.data.repository.AuthRepository
import com.example.mbptodabookingapp.utils.Resource
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * 12.1.1 — AuthRepository: login success/failure and register paths.
 * AuthViewModel is thin (just delegates to repo), so testing the repo covers
 * all ViewModel paths. Empty-field validation lives in LoginActivity and is
 * tested as an instrumented test in LoginActivityTest (12.2.1).
 */
class AuthRepositoryTest {

    private val api: ApiService   = mockk()
    private val context: Context  = mockk(relaxed = true)
    private val repo              = AuthRepository(api, context)

    private val sampleUser = LoggedInUser(
        id = 1, name = "Juan Dela Cruz", email = "juan@test.com",
        role = "passenger", status = "active"
    )

    @Before
    fun setUp() {
        mockkObject(PrefsManager)
        every { PrefsManager.saveLoginData(any(), any(), any()) } returns Unit
        every { PrefsManager.getFcmToken(any()) } returns null  // skip FCM sync
    }

    @After
    fun tearDown() = unmockkObject(PrefsManager)

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    fun `login returns Success with token and user on valid credentials`() = runTest {
        val expected = LoginResponse(token = "header.payload.sig", user = sampleUser)
        coEvery { api.login(LoginRequest("juan@test.com", "password")) } returns
            ApiResponse(success = true, data = expected, message = "Login successful.")

        val result = repo.login(LoginRequest("juan@test.com", "password"))

        assertTrue(result is Resource.Success)
        val data = (result as Resource.Success).data
        assertEquals("header.payload.sig", data.token)
        assertEquals("passenger", data.user.role)
        assertEquals("Juan Dela Cruz", data.user.name)
    }

    @Test
    fun `login returns Error with API message on wrong credentials`() = runTest {
        coEvery { api.login(any()) } returns ApiResponse(
            success = false, data = null, message = "Invalid email or password."
        )

        val result = repo.login(LoginRequest("bad@example.com", "wrong"))

        assertTrue(result is Resource.Error)
        assertEquals("Invalid email or password.", (result as Resource.Error).message)
    }

    @Test
    fun `login returns Error on network failure`() = runTest {
        coEvery { api.login(any()) } throws IOException("connection refused")

        val result = repo.login(LoginRequest("juan@test.com", "password"))

        assertTrue(result is Resource.Error)
        assertTrue((result as Resource.Error).message.contains("No internet connection"))
    }

    @Test
    fun `login returns Error on HTTP 429 rate limit`() = runTest {
        val httpEx = retrofit2.HttpException(
            retrofit2.Response.error<Any>(
                429,
                """{"success":false,"message":"Too many login attempts. Please try again in a minute."}"""
                    .toResponseBody("application/json".toMediaType())
            )
        )
        coEvery { api.login(any()) } throws httpEx

        val result = repo.login(LoginRequest("juan@test.com", "password"))

        assertTrue(result is Resource.Error)
        assertTrue((result as Resource.Error).message.contains("Too many"))
    }

    // ── register ──────────────────────────────────────────────────────────────

    @Test
    fun `register returns Success with user id on valid passenger data`() = runTest {
        coEvery { api.register(any()) } returns ApiResponse(
            success = true, data = mapOf("user_id" to 5), message = "Registration successful."
        )

        val result = repo.register(
            RegisterRequest("Test User", "new@test.com", "pass123", "passenger")
        )

        assertTrue(result is Resource.Success)
        assertEquals(5, (result as Resource.Success).data)
    }

    @Test
    fun `register returns Error on duplicate email`() = runTest {
        coEvery { api.register(any()) } returns ApiResponse(
            success = false, data = null, message = "Email is already registered."
        )

        val result = repo.register(
            RegisterRequest("Dup", "juan@test.com", "pass123", "passenger")
        )

        assertTrue(result is Resource.Error)
        assertEquals("Email is already registered.", (result as Resource.Error).message)
    }

    @Test
    fun `register returns Error on network failure`() = runTest {
        coEvery { api.register(any()) } throws IOException("no network")

        val result = repo.register(
            RegisterRequest("User", "user@test.com", "pass123", "passenger")
        )

        assertTrue(result is Resource.Error)
        assertTrue((result as Resource.Error).message.contains("No internet connection"))
    }
}
