package com.example.fishapp.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

    // ADDED FOR TASK 2: Tracks the active clicked pond context for the details layout screen
    private val _selectedPond = mutableStateOf<PondResponse?>(null)
    val selectedPond: PondResponse? get() = _selectedPond.value

    fun setSelectedPond(pond: PondResponse?) {
        _selectedPond.value = pond
    }

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
                val response = RetrofitClient.instance.getPrediction(imagePart, "")
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = PredictionUiState.Success(response.body()!!)
                    fetchPredictionHistory()
                } else {
                    if (response.code() == 401) {
                        _uiState.value = PredictionUiState.Error("Session expired. Please log in again.")
                        logoutUser()
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
    fun loginUser(username: String, password: String) {
        viewModelScope.launch {
            _authUriState.value = AuthUiState.Loading
            try {
                val response = RetrofitClient.instance.login(LoginRequest(username, password))
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.access_token
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
                    loginUser(username, password)
                } else {
                    _authUriState.value = AuthUiState.Error("Registration rejected: Account already exists.")
                }
            } catch (e: Exception) {
                _authUriState.value = AuthUiState.Error("Server unreachable. Verify backend host state.")
            }
        }
    }

    fun registerManagementAccount(username: String, password: String, fullName: String, phone: String, role: String) {
        viewModelScope.launch {
            _authUriState.value = AuthUiState.Loading
            try {
                val payload = FarmerRegisterRequest(
                    username = username,
                    password = password,
                    full_name = fullName.ifBlank { null },
                    phone_number = phone.ifBlank { "0000000000" }
                )

                val response = if (role.equals("Farmer", ignoreCase = true)) {
                    RetrofitClient.instance.registerFarmer(payload)
                } else {
                    RetrofitClient.instance.registerAdmin(payload)
                }

                if (response.isSuccessful) {
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

    fun fetchPredictionDetail(predictionId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isDetailLoading.value = true
            try {
                val response = RetrofitClient.instance.getPredictionDetail(predictionId, "")
                if (response.isSuccessful && response.body() != null) {
                    _currentDetailItem.value = response.body()!!
                    onSuccess()
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
    fun logoutUser() {
        _authUriState.value = AuthUiState.Unauthenticated
        _pondsList.value = emptyList()
        _reportsList.value = emptyList()
        _historyList.value = emptyList()
        _currentDetailItem.value = null
        _selectedPond.value = null // Purge the active selected pond model cache cleanly
        resetState()
        RetrofitClient.clearAuthToken()
    }

    // ==========================================
    // POND UPLOAD PIPELINE
    // ==========================================
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