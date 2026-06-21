package com.example.mbptodabookingapp.data.api

import android.content.Context
import com.example.mbptodabookingapp.BuildConfig
import com.example.mbptodabookingapp.data.local.PrefsManager
import com.example.mbptodabookingapp.utils.Constants
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton Retrofit client.
 *
 * Must call [ApiClient.init] once from your Application class before any API call.
 *
 * Features:
 *  - Automatically attaches "Authorization: Bearer <token>" to every request.
 *  - Clears SharedPreferences on 401 so the user must log in again.
 *  - Logs all HTTP traffic in DEBUG (remove HttpLoggingInterceptor in production).
 *
 * See: docs/flows/ANDROID_SETUP.md → ApiClient
 * See: docs/flows/AUTH_FLOW.md → Authenticated Request Flow
 */
object ApiClient {

    private lateinit var appContext: Context

    /**
     * Call once in your Application.onCreate().
     * Stores the application context so the interceptor can read the JWT token.
     */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    // ── Interceptors ──────────────────────────────────────────────────────────

    /**
     * Reads the JWT token from SharedPreferences and injects it as a Bearer header.
     * On a 401 response, clears all stored credentials — the resulting HttpException
     * will propagate up through the Repository → ViewModel → Activity/Fragment,
     * which should then navigate the user back to LoginActivity.
     *
     * See: docs/flows/AUTH_FLOW.md → Token Expiry & Refresh
     */
    private val authInterceptor = Interceptor { chain ->
        val token = PrefsManager.getJwtToken(appContext)
        val request = chain.request().newBuilder()
            .apply { if (token != null) addHeader("Authorization", "Bearer $token") }
            .build()

        val response = chain.proceed(request)

        if (response.code == 401) {
            // Token expired or invalid — wipe local session.
            // The 401 HttpException will propagate to the ViewModel to trigger logout UI.
            PrefsManager.clearAll(appContext)
        }

        response
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG)
            HttpLoggingInterceptor.Level.BODY
        else
            HttpLoggingInterceptor.Level.NONE
    }

    // ── OkHttp + Retrofit ─────────────────────────────────────────────────────

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private var _currentBaseUrl: String = ""
    private var _instance: ApiService? = null

    /**
     * The Retrofit-generated [ApiService] instance.
     * Rebuilds automatically if the server URL was changed via the dev settings dialog.
     * Access all API calls via: ApiClient.instance.login(...), etc.
     */
    val instance: ApiService
        get() {
            val url = PrefsManager.getServerUrl(appContext)
            if (url != _currentBaseUrl || _instance == null) {
                _currentBaseUrl = url
                _instance = Retrofit.Builder()
                    .baseUrl(url)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ApiService::class.java)
            }
            return _instance!!
        }
}

