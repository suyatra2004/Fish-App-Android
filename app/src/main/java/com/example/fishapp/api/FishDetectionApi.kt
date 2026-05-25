package com.example.fishapp.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import com.example.fishapp.model.PredictionHistoryItem // ADDED: Imports your new data model cleanly

// ==========================================
// 1. AUTHENTICATION & REQUEST DATA CLASSES
// ==========================================

data class LoginRequest(
    val username: String,
    val password: String
)

data class TokenResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int // in seconds
)

data class ConsumerRegisterRequest(
    val username: String,
    val password: String,
    val full_name: String?,
    val phone_number: String?
)

data class FarmerRegisterRequest(
    val username: String,
    val password: String,
    val full_name: String?,
    val phone_number: String // REQUIRED for farmers
)

data class UserResponse(
    val username: String,
    val full_name: String,
    val role: String,
    val disabled: Boolean,
    val phone_number: String?
)

data class PondCreateRequest(
    val name: String,
    val ph: Double,
    val temperature: Double,
    val pond_size: String?
)

// ==========================================
// 2. RETROFIT RESPONSE DATA CLASSES
// ==========================================

data class ClassProbability(
    val class_name: String,
    val probability: Double,
    val confidence_percent: String
)

data class FishPredictionResponse(
    val success: Boolean,
    val species: String,
    val species_confidence: Double,
    val species_confidence_percent: String,
    val disease_status: String,
    val disease_confidence: Double,
    val disease_confidence_percent: String,
    val yolo_confidence: Double,
    val yolo_confidence_percent: String,
    val is_valid_detection: Boolean,
    val all_class_probabilities: List<ClassProbability>,
    val message: String,
    val detection_count: Int,
    val prediction_id: Int
)

data class PondResponse(
    val id: Int,
    val name: String,
    val ph: Double,
    val temperature: Double,
    val pond_size: String?,
    val verified: Boolean,
    val created_at: String
)

data class ReportResponse(
    val id: Int,
    val report_name: String,
    val symptoms: String,
    val pond_id: Int,
    val pond_name: String,
    val created_at: String,
    val verified: Boolean,
    val photo_url: String
)

// ==========================================
// 3. COMPLETE ROUTING INTERFACE
// ==========================================

interface FishDetectionApi {

    // --- Public Authentication Endpoints ---

    @POST("consumer-register")
    suspend fun registerConsumer(
        @Body request: ConsumerRegisterRequest
    ): Response<UserResponse>

    @POST("farmer-register")
    suspend fun registerFarmer(
        @Body request: FarmerRegisterRequest
    ): Response<UserResponse>

    @POST("admin-register")
    suspend fun registerAdmin(
        @Body request: FarmerRegisterRequest
    ): Response<UserResponse>

    @POST("login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<TokenResponse>

    @POST("logout")
    suspend fun logout(
        @Header("Authorization") bearerToken: String = ""
    ): Response<Map<String, String>>


    // --- Consumer Operational Endpoints ---

    @Multipart
    @POST("predict")
    suspend fun getPrediction(
        @Part file: MultipartBody.Part,
        @Header("Authorization") bearerToken: String = ""
    ): Response<FishPredictionResponse>

    // FIXED: Updated response mapping format from individual response to our history tracking items
    @GET("predictions")
    suspend fun getPredictionHistory(
        @Header("Authorization") bearerToken: String = ""
    ): Response<List<PredictionHistoryItem>>

    // ADDED - Endpoint 4: Query a single detailed node report from user memory
    @GET("predictions/{prediction_id}")
    suspend fun getPredictionDetail(
        @Path("prediction_id") predictionId: Int,
        @Header("Authorization") bearerToken: String = ""
    ): Response<PredictionHistoryItem>

    // ADDED - Endpoint 5: Fetch unredacted historical image stream with secure header tokens
    @GET("predictions/{prediction_id}/image")
    suspend fun getPredictionImage(
        @Path("prediction_id") predictionId: Int,
        @Header("Authorization") bearerToken: String = ""
    ): Response<ResponseBody>


    // --- Farmer Operational Endpoints ---

    @POST("ponds")
    suspend fun createPond(
        @Body request: PondCreateRequest,
        @Header("Authorization") bearerToken: String = ""
    ): Response<PondResponse>

    @GET("ponds")
    suspend fun listPonds(
        @Header("Authorization") bearerToken: String = ""
    ): Response<List<PondResponse>>

    @Multipart
    @POST("reports")
    suspend fun createReport(
        @Part("pond_name") pondName: RequestBody,
        @Part("report_name") reportName: RequestBody,
        @Part("symptoms") symptoms: RequestBody,
        @Part photo: MultipartBody.Part,
        @Header("Authorization") bearerToken: String = ""
    ): Response<ReportResponse>

    @GET("reports")
    suspend fun listReports(
        @Header("Authorization") bearerToken: String = ""
    ): Response<List<ReportResponse>>


    // --- System Status Endpoints ---

    @GET("health")
    suspend fun checkHealth(): Response<Map<String, Any>>
}