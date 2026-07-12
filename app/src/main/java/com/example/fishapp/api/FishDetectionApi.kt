package com.example.fishapp.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import com.example.fishapp.model.PredictionHistoryItem

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
    val expires_in: Int, // in seconds
    val role: String // Added role field from docs
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

// ADDED FOR ADMIN REGISTRATION PIPELINE
data class AdminRegisterRequest(
    val username: String,
    val password: String,
    val full_name: String?,
    val phone_number: String?
)

data class UserResponse(
    val username: String,
    val full_name: String,
    val role: String,
    val disabled: Boolean,
    val phone_number: String?
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

// FIXED: Updated PondResponse structure to match the geo-tagged parameters
data class PondResponse(
    val id: Int,
    val name: String,
    val latitude: Double?,
    val longitude: Double?,
    val estimated_area: Double?,
    val fish_species: List<String>,
    val verified: Boolean,
    val created_at: String,
    val image_url: String?
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

// ADDED: Admin Specific Moderation Response Data Classes
data class AdminPondResponse(
    val id: Int,
    val name: String,
    val latitude: Double?,
    val longitude: Double?,
    val estimated_area: Double?,
    val fish_species: List<String>,
    val verified: Boolean,
    val created_at: String,
    val image_url: String?,
    val owner_username: String,
    val owner_phone: String?
)

data class AdminReportResponse(
    val id: Int,
    val report_name: String,
    val symptoms: String,
    val pond_id: Int,
    val pond_name: String,
    val created_at: String,
    val photo_url: String,
    val verified: Boolean,
    val farmer_username: String,
    val farmer_phone: String?
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
        @Body request: AdminRegisterRequest
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

    @GET("predictions")
    suspend fun getPredictionHistory(
        @Header("Authorization") bearerToken: String = ""
    ): Response<List<PredictionHistoryItem>>

    @GET("predictions/{prediction_id}")
    suspend fun getPredictionDetail(
        @Path("prediction_id") predictionId: Int,
        @Header("Authorization") bearerToken: String = ""
    ): Response<PredictionHistoryItem>

    @GET("predictions/{prediction_id}/image")
    suspend fun getPredictionImage(
        @Path("prediction_id") predictionId: Int,
        @Header("Authorization") bearerToken: String = ""
    ): Response<ResponseBody>


    // --- Farmer Operational Endpoints ---

    @Multipart
    @POST("ponds")
    suspend fun createPond(
        @Part("name") name: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("estimated_area") estimatedArea: RequestBody,
        @Part("fish_species") fishSpecies: RequestBody,
        @Part geoImage: MultipartBody.Part
    ): Response<PondResponse>

    @GET("ponds")
    suspend fun listPonds(
        @Header("Authorization") bearerToken: String = ""
    ): Response<List<PondResponse>>

    @GET("ponds/{pond_id}")
    suspend fun getPondDetail(
        @Path("pond_id") pondId: Int,
        @Header("Authorization") bearerToken: String = ""
    ): Response<PondResponse>

    @GET("ponds/{pond_id}/image")
    suspend fun getPondImage(
        @Path("pond_id") pondId: Int,
        @Header("Authorization") bearerToken: String = ""
    ): Response<ResponseBody>

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


    // --- ADDED: Admin Moderation Operational Endpoints ---

    @GET("admin/ponds")
    suspend fun getAllPonds(
        @Header("Authorization") bearerToken: String = ""
    ): Response<List<AdminPondResponse>>

    @GET("admin/ponds/{pond_id}/image")
    suspend fun getAdminPondImage(
        @Path("pond_id") pondId: Int,
        @Header("Authorization") bearerToken: String = ""
    ): Response<ResponseBody>

    @PATCH("admin/ponds/{pond_id}/verify")
    suspend fun verifyPond(
        @Path("pond_id") pondId: Int,
        @Query("verified") verified: Boolean,
        @Header("Authorization") bearerToken: String = ""
    ): Response<Map<String, Any>>

    @GET("admin/reports")
    suspend fun getAllReports(
        @Header("Authorization") bearerToken: String = ""
    ): Response<List<AdminReportResponse>>

    @GET("admin/reports/{report_id}/photo")
    suspend fun getAdminReportPhoto(
        @Path("report_id") reportId: Int,
        @Header("Authorization") bearerToken: String = ""
    ): Response<ResponseBody>

    @PATCH("admin/reports/{report_id}/verify")
    suspend fun verifyReport(
        @Path("report_id") reportId: Int,
        @Query("verified") verified: Boolean,
        @Header("Authorization") bearerToken: String = ""
    ): Response<Map<String, Any>>


    // --- System Status Endpoints ---

    @GET("health")
    suspend fun checkHealth(): Response<Map<String, Any>>
}