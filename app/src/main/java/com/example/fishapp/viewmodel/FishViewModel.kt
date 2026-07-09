package com.example.fishapp.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fishapp.api.*
import com.example.fishapp.model.PredictionHistoryItem
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

// Keeps your excellent UI State machine perfectly intact
sealed class PredictionUiState {
    object Idle : PredictionUiState()
    object Loading : PredictionUiState()
    data class Success(val data: FishPredictionResponse) : PredictionUiState()
    data class Error(val message: String) : PredictionUiState()
}

// Added separate authentication state tracking to complement your prediction flow
sealed class AuthUiState {
    object Unauthenticated : AuthUiState()
    object Loading : AuthUiState()
    object Authenticated : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class FishViewModel : ViewModel() {

    // ==========================================
    // EXISTING STATES (Preserved)
    // ==========================================
    private val _uiState = mutableStateOf<PredictionUiState>(PredictionUiState.Idle)
    val uiState: State<PredictionUiState> = _uiState

    private val _selectedImageUri = mutableStateOf<Uri?>(null)
    val selectedImageUri: State<Uri?> = _selectedImageUri

    // ==========================================
    // NEW AUTHENTICATION STATES
    // ==========================================
    private val _authUriState = mutableStateOf<AuthUiState>(AuthUiState.Unauthenticated)
    val authUriState: State<AuthUiState> = _authUriState

    // ==========================================
    // STEP 3: FARMER POND STATES
    // ==========================================
    private val _pondsList = mutableStateOf<List<PondResponse>>(emptyList())
    val pondsList: State<List<PondResponse>> = _pondsList

    private val _isPondsLoading = mutableStateOf(false)
    val isPondsLoading: State<Boolean> = _isPondsLoading

    private val _pondsErrorMessage = mutableStateOf<String?>(null)
    val pondsErrorMessage: State<String?> = _pondsErrorMessage

    // ==========================================
    // STEP 4: ADMIN / EXPERT REPORT STATES
    // ==========================================
    private val _reportsList = mutableStateOf<List<ReportResponse>>(emptyList())
    val reportsList: State<List<ReportResponse>> = _reportsList

    private val _isReportsLoading = mutableStateOf(false)
    val isReportsLoading: State<Boolean> = _isReportsLoading

    private val _reportsErrorMessage = mutableStateOf<String?>(null)
    val reportsErrorMessage: State<String?> = _reportsErrorMessage

    // ==========================================
    // NEW: CONSUMER SCAN PREDICTION HISTORY STATES
    // ==========================================
    private val _historyList = mutableStateOf<List<PredictionHistoryItem>>(emptyList())
    val historyList: State<List<PredictionHistoryItem>> = _historyList

    private val _isHistoryLoading = mutableStateOf(false)
    val isHistoryLoading: State<Boolean> = _isHistoryLoading

    private val _historyErrorMessage = mutableStateOf<String?>(null)
    val historyErrorMessage: State<String?> = _historyErrorMessage

    // For single item inspection detailed caching
    private val _currentDetailItem = mutableStateOf<PredictionHistoryItem?>(null)
    val currentDetailItem: State<PredictionHistoryItem?> = _currentDetailItem

    private val _isDetailLoading = mutableStateOf(false)
    val isDetailLoading: State<Boolean> = _isDetailLoading


    // ==========================================
    // EXISTING FUNCTIONS (Preserved)
    // ==========================================
    fun onImageSelected(uri: Uri?) {
        _selectedImageUri.value = uri
        _uiState.value = PredictionUiState.Idle
    }

    fun resetState() {
        _selectedImageUri.value = null
        _uiState.value = PredictionUiState.Idle
    }

    // ==========================================
    // MODIFIED PREDICTION LOGIC (Secured)
    // ==========================================
    fun uploadImage(imagePart: MultipartBody.Part) {
        viewModelScope.launch {
            _uiState.value = PredictionUiState.Loading
            try {
                // The updated RetrofitClient automatically attaches the token interceptor!
                val response = RetrofitClient.instance.getPrediction(imagePart, "")
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = PredictionUiState.Success(response.body()!!)
                    fetchPredictionHistory() // Refresh the user's history list instantly after making a new scan!
                } else {
                    if (response.code() == 401) {
                        _uiState.value = PredictionUiState.Error("Session expired. Please log in again.")
                        logoutUser() // Force drop session if server rejects due to invalid token
                    } else {
                        _uiState.value = PredictionUiState.Error("Server error: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = PredictionUiState.Error("Connection failed: ${e.localizedMessage}")
            }
        }
    }

    // ==========================================
    // NEW FEATURE OPERATIONS (Authentication)
    // ==========================================

    /**
     * Authenticates credentials with the backend and handles token handshakes
     */
    fun loginUser(username: String, password: String) {
        viewModelScope.launch {
            _authUriState.value = AuthUiState.Loading
            try {
                val response = RetrofitClient.instance.login(LoginRequest(username, password))
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.access_token

                    // Update our interceptor so all future calls attach this token
                    RetrofitClient.setAuthToken(token)

                    _authUriState.value = AuthUiState.Authenticated
                } else {
                    _authUriState.value = AuthUiState.Error("Login failed: Invalid credentials")
                }
            } catch (e: Exception) {
                _authUriState.value = AuthUiState.Error("Connection failed: Check if server is running on Wi-Fi")
            }
        }
    }

    // ==========================================
    // EXTENDED REGISTRATION PIPELINES (API Integrated)
    // ==========================================

    /**
     * Executes a network transaction to create a consumer profile in the backend database
     */
    fun registerConsumerAccount(username: String, password: String, fullName: String, phone: String) {
        viewModelScope.launch {
            _authUriState.value = AuthUiState.Loading
            try {
                val payload = ConsumerRegisterRequest(
                    username = username,
                    password = password,
                    full_name = fullName.ifBlank { null },
                    phone_number = phone.ifBlank { null }
                )
                val response = RetrofitClient.instance.registerConsumer(payload)
                if (response.isSuccessful) {
                    // Auto-login immediately following a successful registration profile creation
                    loginUser(username, password)
                } else {
                    _authUriState.value = AuthUiState.Error("Registration rejected: Account already exists.")
                }
            } catch (e: Exception) {
                _authUriState.value = AuthUiState.Error("Server unreachable. Verify backend host state.")
            }
        }
    }

    /**
     * Routes management profile creation to either the farmer or admin endpoint depending on the selected role
     */
    fun registerManagementAccount(username: String, password: String, fullName: String, phone: String, role: String) {
        viewModelScope.launch {
            _authUriState.value = AuthUiState.Loading
            try {
                val payload = FarmerRegisterRequest(
                    username = username,
                    password = password,
                    full_name = fullName.ifBlank { null },
                    phone_number = phone.ifBlank { "0000000000" } // Fallback as phone number is required for managers
                )

                // Select matching endpoint execution route cleanly
                val response = if (role.equals("Farmer", ignoreCase = true)) {
                    RetrofitClient.instance.registerFarmer(payload)
                } else {
                    RetrofitClient.instance.registerAdmin(payload)
                }

                if (response.isSuccessful) {
                    // Auto-login immediately following a successful registration profile creation
                    loginUser(username, password)
                } else {
                    _authUriState.value = AuthUiState.Error("Registration failed: Role criteria not met.")
                }
            } catch (e: Exception) {
                _authUriState.value = AuthUiState.Error("Network error: Registration pipeline timeout.")
            }
        }
    }

    // ==========================================
    // STEP 3 FEATURE OPERATIONS (Farmer Pond API Tracker)
    // ==========================================

    /**
     * Queries your friend's backend database endpoints to retrieve the farmer's active assets list
     */
    fun fetchFarmerPonds() {
        viewModelScope.launch {
            _isPondsLoading.value = true
            _pondsErrorMessage.value = null
            try {
                val response = RetrofitClient.instance.listPonds("")
                if (response.isSuccessful && response.body() != null) {
                    _pondsList.value = response.body()!!
                } else {
                    if (response.code() == 401) {
                        _pondsErrorMessage.value = "Session expired. Please re-authenticate."
                        logoutUser()
                    } else {
                        _pondsErrorMessage.value = "Unable to fetch assets (Error code: ${response.code()})"
                    }
                }
            } catch (e: Exception) {
                _pondsErrorMessage.value = "Server connection lost. Verify backend host state."
            } finally {
                _isPondsLoading.value = false
            }
        }
    }

    // ==========================================
    // STEP 4 FEATURE OPERATIONS (Admin Outbreak Streamer)
    // ==========================================

    /**
     * Queries your friend's global endpoint to gather all submitted farm risk assessments
     */
    fun fetchAllReports() {
        viewModelScope.launch {
            _isReportsLoading.value = true
            _reportsErrorMessage.value = null
            try {
                val response = RetrofitClient.instance.listReports("")
                if (response.isSuccessful && response.body() != null) {
                    _reportsList.value = response.body()!!
                } else {
                    if (response.code() == 401) {
                        _reportsErrorMessage.value = "Session expired. Admin access unauthorized."
                        logoutUser()
                    } else {
                        _reportsErrorMessage.value = "Failed to load reports pipeline (Code: ${response.code()})"
                    }
                }
            } catch (e: Exception) {
                _reportsErrorMessage.value = "Network timeout. Expert dashboard cannot reach backend server."
            } finally {
                _isReportsLoading.value = false
            }
        }
    }

    // ==========================================
    // NEW CONSUMER OPERATIONS (Prediction History Handshaking)
    // ==========================================

    /**
     * API Endpoint 3 Implementation: Queries background server for historical user scans
     */
    fun fetchPredictionHistory() {
        viewModelScope.launch {
            _isHistoryLoading.value = true
            _historyErrorMessage.value = null
            try {
                val response = RetrofitClient.instance.getPredictionHistory("")
                if (response.isSuccessful && response.body() != null) {
                    _historyList.value = response.body()!!
                } else {
                    if (response.code() == 401) {
                        _historyErrorMessage.value = "Session expired. Please login again."
                        logoutUser()
                    } else {
                        _historyErrorMessage.value = "Failed to load records feed (Code: ${response.code()})"
                    }
                }
            } catch (e: Exception) {
                _historyErrorMessage.value = "Cannot reach server pipeline streams."
            } finally {
                _isHistoryLoading.value = false
            }
        }
    }

    /**
     * API Endpoint 4 Implementation: Fetches a single scan's unredacted property metrics
     */
    fun fetchPredictionDetail(predictionId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isDetailLoading.value = true
            try {
                val response = RetrofitClient.instance.getPredictionDetail(predictionId, "")
                if (response.isSuccessful && response.body() != null) {
                    _currentDetailItem.value = response.body()!!
                    onSuccess() // Execute layout scene transition callback seamlessly
                }
            } catch (e: Exception) {
                // Background debugging streams go here
            } finally {
                _isDetailLoading.value = false
            }
        }
    }

    // ==========================================
    // CLOSURE OPERATIONS
    // ==========================================

    /**
     * Clears current active user sessions out of memory gracefully
     */
    fun logoutUser() {
        _authUriState.value = AuthUiState.Unauthenticated
        _pondsList.value = emptyList()
        _reportsList.value = emptyList() // Clean up expert records cache
        _historyList.value = emptyList() // Purge user scanning profile tracking history cleanly
        _currentDetailItem.value = null  // Destroy active detail inspections context
        resetState()
        RetrofitClient.clearAuthToken()
    }

    /**
     * Packages multi-part properties and coordinates for storage via Endpoint 3
     */
    /**
     * Packages multi-part properties and coordinates for storage via Endpoint 3
     */
    fun uploadNewPond(
        context: Context,
        name: String,
        lat: Double,
        lng: Double,
        area: Double,
        speciesList: List<String>,
        imageUri: Uri,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val mediaType = "text/plain".toMediaTypeOrNull()
                val namePart = name.toRequestBody(mediaType)
                val latPart = lat.toString().toRequestBody(mediaType)
                val lngPart = lng.toString().toRequestBody(mediaType)
                val areaPart = area.toString().toRequestBody(mediaType)

                val speciesString = speciesList.joinToString(", ")
                val speciesPart = speciesString.toRequestBody(mediaType)

                val inputStream = context.contentResolver.openInputStream(imageUri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes != null) {
                    val imageMediaType = "image/jpeg".toMediaTypeOrNull()
                    val requestFile = bytes.toRequestBody(imageMediaType)
                    val imagePart = MultipartBody.Part.createFormData("geo_image", "pond_scan.jpg", requestFile)

                    val response = RetrofitClient.instance.createPond(
                        namePart, latPart, lngPart, areaPart, speciesPart, imagePart
                    )

                    if (response.isSuccessful) {
                        fetchFarmerPonds()
                        // FIXED: Changed from launch to withContext to cleanly switch thread contexts
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            onSuccess()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}