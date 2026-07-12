package com.example.fishapp.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.fishapp.navigation.Screen
import com.example.fishapp.ui.components.*
import com.example.fishapp.ui.theme.*
import com.example.fishapp.viewmodel.FishViewModel
import com.example.fishapp.viewmodel.PredictionUiState
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@Composable
fun ConsumerHomeScreen(
    navController: NavHostController,
    onBack: () -> Unit,
    viewModel: FishViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState
    val selectedImageUri by viewModel.selectedImageUri

    // Real-time Prediction History States Hook
    val historyList by viewModel.historyList
    val isHistoryLoading by viewModel.isHistoryLoading

    // Automatically queries backend servers for previous scan results upon launching
    LaunchedEffect(Unit) {
        viewModel.fetchPredictionHistory()
    }

    // Dialog state control to toggle exit prompt visibility
    var showExitDialog by remember { mutableStateOf(false) }

    // Intercepts phone structural gestures or physical back keys to display confirmation pop-up
    BackHandler(enabled = true) {
        showExitDialog = true
    }

    // SYSTEM EXIT CONFIRMATION DIALOG INTERFACE
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = {
                Text(
                    text = "Exit Application",
                    color = Color.Black, // FIXED: High-contrast visibility toggle
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to exit AquaSense?",
                    color = Color.DarkGray // FIXED: Restores unreadable description text blocks
                )
            },
            containerColor = Color.White, // FIXED: Overrides theme collision bugs
            shape = RoundedCornerShape(14.dp),
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        viewModel.logoutUser() // Clear active login session interceptors securely
                        (context as? android.app.Activity)?.finishAffinity() // Force complete shutdown
                    }
                ) {
                    Text("Yes", color = BrandGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("No", color = Color.Gray) // FIXED: Replaced hidden color state references
                }
            }
        )
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            viewModel.onImageSelected(uri)
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            navController.navigate(Screen.CameraScanner.route)
        } else {
            Toast.makeText(context, "Camera access is needed to scan fish", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AquaBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // --- Header ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.horizontalGradient(listOf(BrandGreen, BrandGreenDark)),
                        RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .padding(24.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    // Top Actions Row containing Back (Exit App Prompt) and Logout Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showExitDialog = true }, modifier = Modifier.offset(x = (-12).dp)) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Exit Alert Handler", tint = Color.White)
                        }

                        // High-contrast Logout Action button linked to clean redirection
                        IconButton(onClick = {
                            viewModel.logoutUser() // Drops backend authorization tokens cleanly
                            // FIXED: Explicitly redirects graph down to matching root selection route constants cleanly
                            navController.navigate(Screen.LoginSelection.route) {
                                popUpTo(Screen.LoginSelection.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        }, modifier = Modifier.offset(x = 12.dp)) {
                            Icon(Icons.Default.Logout, contentDescription = "Sign Out User", tint = Color.White)
                        }
                    }

                    Column {
                        Text("Freshness Scanner", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Analyze fish from camera or gallery", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }

            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .offset(y = (-40).dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // --- Upload or Preview Area ---
                if (selectedImageUri == null) {
                    DashedUploadBox(
                        icon = { Icon(Icons.Default.Camera, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(32.dp)) },
                        title = "Start New Scan",
                        subtitle = "Instant species & freshness detection",
                        onCamera = {
                            val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                navController.navigate(Screen.CameraScanner.route)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        onGallery = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                } else {
                    ImagePreviewCard(
                        uri = selectedImageUri!!,
                        onReset = { viewModel.resetState() },
                        onPredict = {
                            val part = createMultipartBody(context, selectedImageUri!!)
                            if (part != null) {
                                viewModel.uploadImage(part)
                            } else {
                                Toast.makeText(context, "Error processing image", Toast.LENGTH_SHORT).show()
                            }
                        },
                        isLoading = uiState is PredictionUiState.Loading
                    )
                }

                // --- Result Interface ---
                when (val state = uiState) {
                    is PredictionUiState.Success -> {
                        ResultCard(state.data)
                    }
                    is PredictionUiState.Error -> {
                        ErrorCard(state.message)
                    }
                    is PredictionUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = BrandGreen)
                        }
                    }
                    else -> {}
                }

                // --- History Header Area ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionLabel("Recent History")
                    Text(
                        text = "View All",
                        color = BrandGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable {
                            navController.navigate(Screen.PredictionHistoryList.route)
                        }
                    )
                }

                // DYNAMIC PRODUCTION PIPELINE LIST FEED CONTROLLER
                if (isHistoryLoading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrandGreen)
                    }
                } else if (historyList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(text = "No scan histories found. Run a new scan above!", color = Color.Gray, fontSize = 13.sp)
                    }
                } else {
                    // Iterates dynamically to render up to the 3 most recent real database scan returns
                    historyList.take(3).forEach { item ->
                        AquaCard(
                            onClick = {
                                viewModel.fetchPredictionDetail(item.id) {
                                    navController.navigate("prediction_detail_screen")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(50.dp).background(BrandGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.History, contentDescription = null, tint = BrandGreen)
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = item.species, fontWeight = FontWeight.Bold, color = Color.Black)
                                    Text(
                                        text = "${item.created_at.take(10)} • Match: ${item.species_confidence_percent}",
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )
                                }

                                val isHealthy = item.disease_status.contains("HEALTHY", ignoreCase = true) || item.disease_status.contains("FRESH", ignoreCase = true)
                                val badgeColor = if (isHealthy) BrandGreen else Color.Red
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = badgeColor.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = item.disease_status.uppercase(),
                                        color = badgeColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ImagePreviewCard(uri: Uri, onReset: () -> Unit, onPredict: () -> Unit, isLoading: Boolean) {
    AquaCard {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = "Selected Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Reset")
                }
                Button(
                    onClick = onPredict,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Predict", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ResultCard(data: com.example.fishapp.api.FishPredictionResponse) {
    AquaCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("AI Analysis Engine Pipeline", modifier = Modifier.padding(bottom = 2.dp), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BrandGreen)
            HorizontalDivider(color = BrandGreen.copy(alpha = 0.15f))

            ResultRow("Identified Fish Class", data.species)
            ResultRow("Species Confidence Score", data.species_confidence_percent)
            ResultRow("YOLO Detection Accuracy", data.yolo_confidence_percent)
            ResultRow("System Status Message", data.message)

            val isHealthyResult = data.disease_status.contains("HEALTHY", ignoreCase = true) || data.disease_status.contains("FRESH", ignoreCase = true)
            val statusColor = if (isHealthyResult) BrandGreen else Color.Red

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .background(statusColor.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "HEALTH STATUS VERDICT:",
                        fontSize = 11.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = data.disease_status.uppercase(),
                        fontWeight = FontWeight.Black,
                        color = statusColor,
                        fontSize = 18.sp,
                        letterSpacing = 1.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ResultRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.DarkGray, fontSize = 13.sp, fontWeight = FontWeight.Normal)
        Text(value, fontWeight = FontWeight.SemiBold, color = Color.Black, fontSize = 13.sp)
    }
}

@Composable
fun ErrorCard(message: String) {
    AquaCard {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
            Text("⚠️", fontSize = 20.sp)
            Spacer(Modifier.width(12.dp))
            Text(message, color = Color.Red, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

private fun createMultipartBody(context: Context, uri: Uri): MultipartBody.Part? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        inputStream?.close()
        if (bytes != null) {
            val requestFile = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("file", "fish_scan_image.jpg", requestFile)
        } else null
    } catch (e: Exception) {
        null
    }
}