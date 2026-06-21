package com.example.mbptodabookingapp

import com.example.mbptodabookingapp.utils.BookingStatus
import com.example.mbptodabookingapp.utils.Resource
import org.junit.Assert.*
import org.junit.Test

/** 12.1.3 — Resource sealed class and BookingStatus terminal-state helper */
class ResourceTest {

    @Test
    fun `success wraps data`() {
        val r = Resource.Success(42)
        assertEquals(42, r.data)
    }

    @Test
    fun `success with null data is valid`() {
        val r = Resource.Success<String?>(null)
        assertNull(r.data)
    }

    @Test
    fun `error wraps message`() {
        val r = Resource.Error("Oops")
        assertEquals("Oops", r.message)
    }

    @Test
    fun `loading is the same singleton`() {
        assertSame(Resource.Loading, Resource.Loading)
    }

    @Test
    fun `isTerminal true for completed`() {
        assertTrue(BookingStatus.isTerminal(BookingStatus.COMPLETED))
    }

    @Test
    fun `isTerminal true for cancelled`() {
        assertTrue(BookingStatus.isTerminal(BookingStatus.CANCELLED))
    }

    @Test
    fun `isTerminal true for rejected`() {
        assertTrue(BookingStatus.isTerminal(BookingStatus.REJECTED))
    }

    @Test
    fun `isTerminal false for requested`() {
        assertFalse(BookingStatus.isTerminal(BookingStatus.REQUESTED))
    }

    @Test
    fun `isTerminal false for accepted`() {
        assertFalse(BookingStatus.isTerminal(BookingStatus.ACCEPTED))
    }

    @Test
    fun `isTerminal false for in_progress`() {
        assertFalse(BookingStatus.isTerminal(BookingStatus.IN_PROGRESS))
    }
}
