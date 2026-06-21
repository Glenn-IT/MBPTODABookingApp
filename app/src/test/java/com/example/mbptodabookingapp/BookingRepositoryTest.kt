package com.example.mbptodabookingapp

import com.example.mbptodabookingapp.data.api.ApiResponse
import com.example.mbptodabookingapp.data.api.ApiService
import com.example.mbptodabookingapp.data.models.BookingRequest
import com.example.mbptodabookingapp.data.models.CreateBookingResponse
import com.example.mbptodabookingapp.data.repository.BookingRepository
import com.example.mbptodabookingapp.utils.Resource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

/** 12.1.2 — BookingRepository createBooking and cancelBooking success/error paths */
class BookingRepositoryTest {

    private val api: ApiService = mockk()
    private val repo            = BookingRepository(api)

    private val request = BookingRequest(
        pickup_address  = "123 Main St",  pickup_lat  = 14.5995, pickup_lng  = 120.9842,
        dropoff_address = "456 Market St", dropoff_lat = 14.5900, dropoff_lng = 120.9800,
    )

    // ── createBooking ─────────────────────────────────────────────────────────

    @Test
    fun `createBooking returns Success with booking id on API success`() = runTest {
        coEvery { api.createBooking(any()) } returns ApiResponse(
            success = true, data = CreateBookingResponse(booking_id = 7), message = "Ride request created."
        )

        val result = repo.createBooking(request)

        assertTrue(result is Resource.Success)
        assertEquals(7, (result as Resource.Success).data)
    }

    @Test
    fun `createBooking returns Error when API success=false`() = runTest {
        coEvery { api.createBooking(any()) } returns ApiResponse(
            success = false, data = null, message = "Field 'pickup_address' is required."
        )

        val result = repo.createBooking(request)

        assertTrue(result is Resource.Error)
        assertEquals("Field 'pickup_address' is required.", (result as Resource.Error).message)
    }

    @Test
    fun `createBooking returns friendly error on IOException`() = runTest {
        coEvery { api.createBooking(any()) } throws IOException("timeout")

        val result = repo.createBooking(request)

        assertTrue(result is Resource.Error)
        assertTrue((result as Resource.Error).message.contains("No internet connection"))
    }

    @Test
    fun `createBooking returns generic error on unexpected exception`() = runTest {
        coEvery { api.createBooking(any()) } throws RuntimeException("unexpected")

        val result = repo.createBooking(request)

        assertTrue(result is Resource.Error)
        assertFalse((result as Resource.Error).message.isEmpty())
    }

    // ── cancelBooking ─────────────────────────────────────────────────────────

    @Test
    fun `cancelBooking returns Success on API success`() = runTest {
        coEvery { api.cancelBooking(5) } returns ApiResponse(
            success = true, data = Unit, message = "Booking cancelled."
        )

        val result = repo.cancelBooking(5)

        assertTrue(result is Resource.Success)
    }

    @Test
    fun `cancelBooking returns Error when booking is not cancellable`() = runTest {
        coEvery { api.cancelBooking(5) } returns ApiResponse(
            success = false, data = null, message = "Only 'requested' bookings can be cancelled."
        )

        val result = repo.cancelBooking(5)

        assertTrue(result is Resource.Error)
        assertEquals("Only 'requested' bookings can be cancelled.", (result as Resource.Error).message)
    }

    @Test
    fun `cancelBooking returns Error on network failure`() = runTest {
        coEvery { api.cancelBooking(any()) } throws IOException("no network")

        val result = repo.cancelBooking(3)

        assertTrue(result is Resource.Error)
        assertTrue((result as Resource.Error).message.contains("No internet connection"))
    }

    // ── acceptRide / rejectRide ───────────────────────────────────────────────

    @Test
    fun `acceptRide returns Success on API success`() = runTest {
        coEvery { api.acceptRide(1) } returns ApiResponse(success = true, data = Unit, message = "Accepted.")

        val result = repo.acceptRide(1)

        assertTrue(result is Resource.Success)
    }

    @Test
    fun `rejectRide returns Error when booking already accepted`() = runTest {
        coEvery { api.rejectRide(1) } returns ApiResponse(
            success = false, data = null, message = "Booking is already accepted."
        )

        val result = repo.rejectRide(1)

        assertTrue(result is Resource.Error)
    }
}
