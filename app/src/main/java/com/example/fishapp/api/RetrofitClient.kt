package com.example.fishapp.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // IMPORTANT: Verify this IP address matches your friend's current IP
    private const val BASE_URL = "http://192.168.0.176:8000/"

    // Variable to temporarily hold the JWT token string securely in memory
    private var authToken: String? = null

    // Helper function to update the token globally once a user logs in successfully
    fun setAuthToken(token: String?) {
        // Pre-format the token with "Bearer " as expected by the backend architecture
        authToken = if (token != null && !token.startsWith("Bearer ")) {
            "Bearer $token"
        } else {
            token
        }
    }

    // ADDED: Public helper function so our UI screens can read the pre-formatted Bearer token safely
    fun getAuthToken(): String? {
        return authToken
    }

    // Clears the token out of memory when the user logs out
    fun clearAuthToken() {
        authToken = null
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        // Dynamic Header Interceptor: Automatically appends the JWT token to any request if it exists
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()

            // If an auth token is saved in memory, inject it straight into the HTTP Headers
            authToken?.let { token ->
                requestBuilder.header("Authorization", token)
            }

            chain.proceed(requestBuilder.build())
        }
        .connectTimeout(30, TimeUnit.SECONDS) // Keeps your excellent 30s timeout cushion
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val instance: FishDetectionApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FishDetectionApi::class.java)
    }
}