package com.example.mbptodabookingapp.data.repository

import com.example.mbptodabookingapp.data.api.ApiService
import com.example.mbptodabookingapp.data.models.AdminUser
import com.example.mbptodabookingapp.data.models.Booking
import com.example.mbptodabookingapp.data.models.PendingDriver
import com.example.mbptodabookingapp.utils.Resource

/**
 * Handles all admin-panel API calls: user management, driver approval, and booking overview.
 *
 * See: docs/api/ADMIN.md
 * See: docs/flows/DRIVER_APPROVAL_FLOW.md
 *
 * Usage in ViewModel:
 *   private val repo = AdminRepository(ApiClient.instance)
 */
class AdminRepository(
    private val api: ApiService
) : BaseRepository() {

    // ── Users ─────────────────────────────────────────────────────────────────

    /**
     * Get all registered users (all roles).
     * See: docs/api/ADMIN.md → GET /admin/users
     */
    suspend fun getAllUsers(): Resource<List<AdminUser>> {
        return try {
            val response = api.getAllUsers()
            if (response.success && response.data != null) {
                Resource.Success(response.data)
            } else {
                Resource.Error(response.message)
            }
        } catch (e: Exception) {
            Resource.Error(parseApiError(e))
        }
    }

    /**
     * Activate a deactivated user account. Sets users.status = 'active'.
     * See: docs/api/ADMIN.md → PUT /admin/user/activate/{id}
     */
    suspend fun activateUser(userId: Int): Resource<Unit> {
        return try {
            val response = api.activateUser(userId)
            if (response.success) Resource.Success(Unit) else Resource.Error(response.message)
        } catch (e: Exception) {
            Resource.Error(parseApiError(e))
        }
    }

    /**
     * Deactivate a user account. Sets users.status = 'inactive'.
     * The user will be blocked at login until re-activated.
     * See: docs/api/ADMIN.md → PUT /admin/user/deactivate/{id}
     */
    suspend fun deactivateUser(userId: Int): Resource<Unit> {
        return try {
            val response = api.deactivateUser(userId)
            if (response.success) Resource.Success(Unit) else Resource.Error(response.message)
        } catch (e: Exception) {
            Resource.Error(parseApiError(e))
        }
    }

    /**
     * Permanently delete a user and all related records.
     * ⚠️ Destructive — irreversible. Use only for test cleanup or GDPR removal.
     * See: docs/api/ADMIN.md → DELETE /admin/user/{id}
     */
    suspend fun deleteUser(userId: Int): Resource<Unit> {
        return try {
            val response = api.deleteUser(userId)
            if (response.success) Resource.Success(Unit) else Resource.Error(response.message)
        } catch (e: Exception) {
            Resource.Error(parseApiError(e))
        }
    }

    // ── Driver Approval ───────────────────────────────────────────────────────

    /**
     * Get all drivers with approval_status = 'pending'.
     * See: docs/api/ADMIN.md → GET /admin/drivers/pending
     * See: docs/flows/DRIVER_APPROVAL_FLOW.md → Step 3
     */
    suspend fun getPendingDrivers(): Resource<List<PendingDriver>> {
        return try {
            val response = api.getPendingDrivers()
            if (response.success && response.data != null) {
                Resource.Success(response.data)
            } else {
                Resource.Error(response.message)
            }
        } catch (e: Exception) {
            Resource.Error(parseApiError(e))
        }
    }

    /**
     * Approve a pending driver. Sets driver_info.approval_status = 'approved'.
     * Driver can now log in and accept rides.
     * See: docs/api/ADMIN.md → PUT /admin/driver/approve/{id}
     * See: docs/flows/DRIVER_APPROVAL_FLOW.md → Step 4a
     */
    suspend fun approveDriver(driverId: Int): Resource<Unit> {
        return try {
            val response = api.approveDriver(driverId)
            if (response.success) Resource.Success(Unit) else Resource.Error(response.message)
        } catch (e: Exception) {
            Resource.Error(parseApiError(e))
        }
    }

    /**
     * Reject a pending driver. Sets driver_info.approval_status = 'rejected'.
     * Driver will be blocked at login with a rejection message.
     * See: docs/api/ADMIN.md → PUT /admin/driver/reject/{id}
     * See: docs/flows/DRIVER_APPROVAL_FLOW.md → Step 4b
     */
    suspend fun rejectDriver(driverId: Int): Resource<Unit> {
        return try {
            val response = api.rejectDriver(driverId)
            if (response.success) Resource.Success(Unit) else Resource.Error(response.message)
        } catch (e: Exception) {
            Resource.Error(parseApiError(e))
        }
    }

    // ── Bookings ──────────────────────────────────────────────────────────────

    /**
     * Get all bookings in the system regardless of status.
     * See: docs/api/ADMIN.md → GET /admin/bookings
     */
    suspend fun getAllBookings(): Resource<List<Booking>> {
        return try {
            val response = api.getAllBookings()
            if (response.success && response.data != null) {
                Resource.Success(response.data)
            } else {
                Resource.Error(response.message)
            }
        } catch (e: Exception) {
            Resource.Error(parseApiError(e))
        }
    }
}

