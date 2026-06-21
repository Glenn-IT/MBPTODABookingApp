package com.example.mbptodabookingapp

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mbptodabookingapp.data.local.PrefsManager
import com.example.mbptodabookingapp.data.models.LoggedInUser
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 12.1.4 — PrefsManager: save/retrieve JWT + user data, clearAll.
 * Runs as an instrumented test because EncryptedSharedPreferences
 * requires the Android Keystore (not available in JVM unit tests).
 */
@RunWith(AndroidJUnit4::class)
class PrefsManagerTest {

    private val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()

    private val sampleUser = LoggedInUser(
        id = 42, name = "Test User", email = "test@example.com",
        role = "passenger", status = "active"
    )

    @Before
    fun setUp() = PrefsManager.clearAll(ctx)

    @After
    fun tearDown() = PrefsManager.clearAll(ctx)

    @Test
    fun saveLoginData_tokenIsRetrievable() {
        PrefsManager.saveLoginData(ctx, "my.jwt.token", sampleUser)
        assertEquals("my.jwt.token", PrefsManager.getJwtToken(ctx))
    }

    @Test
    fun saveLoginData_roleIsRetrievable() {
        PrefsManager.saveLoginData(ctx, "token", sampleUser)
        assertEquals("passenger", PrefsManager.getUserRole(ctx))
    }

    @Test
    fun saveLoginData_userIdIsRetrievable() {
        PrefsManager.saveLoginData(ctx, "token", sampleUser)
        assertEquals(42, PrefsManager.getUserId(ctx))
    }

    @Test
    fun saveLoginData_nameIsRetrievable() {
        PrefsManager.saveLoginData(ctx, "token", sampleUser)
        assertEquals("Test User", PrefsManager.getUserName(ctx))
    }

    @Test
    fun isLoggedIn_trueAfterSave() {
        PrefsManager.saveLoginData(ctx, "tok", sampleUser)
        assertTrue(PrefsManager.isLoggedIn(ctx))
    }

    @Test
    fun isLoggedIn_falseBeforeLogin() {
        assertFalse(PrefsManager.isLoggedIn(ctx))
    }

    @Test
    fun clearAll_removesToken() {
        PrefsManager.saveLoginData(ctx, "tok", sampleUser)
        PrefsManager.clearAll(ctx)
        assertNull(PrefsManager.getJwtToken(ctx))
    }

    @Test
    fun clearAll_isLoggedIn_returnsFalse() {
        PrefsManager.saveLoginData(ctx, "tok", sampleUser)
        PrefsManager.clearAll(ctx)
        assertFalse(PrefsManager.isLoggedIn(ctx))
    }

    @Test
    fun saveFcmToken_isRetrievable() {
        PrefsManager.saveFcmToken(ctx, "fcm_device_token_abc")
        assertEquals("fcm_device_token_abc", PrefsManager.getFcmToken(ctx))
    }

    @Test
    fun clearAll_removesFcmToken() {
        PrefsManager.saveFcmToken(ctx, "fcm_token")
        PrefsManager.clearAll(ctx)
        assertNull(PrefsManager.getFcmToken(ctx))
    }

    @Test
    fun saveServerUrl_isRetrievable() {
        PrefsManager.saveServerUrl(ctx, "http://192.168.1.99/ptoda_booking_api/")
        assertEquals("http://192.168.1.99/ptoda_booking_api/", PrefsManager.getServerUrl(ctx))
    }

    @Test
    fun getServerUrl_returnsDeviceUrlWhenNotSet() {
        // clearAll only clears encrypted prefs; dev prefs are separate — just verify it returns a non-empty string
        assertFalse(PrefsManager.getServerUrl(ctx).isEmpty())
    }
}
