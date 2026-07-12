package com.example.fishapp.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.fishapp.ui.components.*
import com.example.fishapp.ui.theme.*
import com.example.fishapp.viewmodel.FishViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun AddPondScreen(
    onBack: () -> Unit,
    viewModel: FishViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Form Field States
    var pondName by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var fishType by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }

    // Media & Geolocation processing states
    var tempCameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var watermarkedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isProcessingLocation by remember { mutableStateOf(false) }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    var isRegisteringPond by remember { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedBorderColor = BrandGreen,
        unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f),
        focusedLabelColor = BrandGreen,
        unfocusedLabelColor = TextSecondary
    )

    // Helper function to spin up a secure file provider URI for the camera capture intent
    fun createTempImageUri(): Uri {
        val tempFile = File.createTempFile("camera_capture_", ".jpg", context.cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider", // Make sure this matches your AndroidManifest provider authorities rule!
            tempFile
        )
    }

    // FIXED: Swapped gallery picker contract out entirely for the TakePicture Camera contract execution loop
    val cameraCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            val capturedUri = tempCameraImageUri
            if (success && capturedUri != null) {
                isProcessingLocation = true
                scope.launch(Dispatchers.IO) {
                    val hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    val hasCoarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

                    if (hasFineLocation || hasCoarseLocation) {
                        val location = com.example.fishapp.utils.LocationWatermarkEngine.getCurrentLocation(context)
                        if (location != null) {
                            latitude = location.latitude
                            longitude = location.longitude

                            val addressText = com.example.fishapp.utils.LocationWatermarkEngine.getReadableAddress(context, location.latitude, location.longitude)
                            val stampedUri = com.example.fishapp.utils.LocationWatermarkEngine.addGpsWatermark(
                                context = context,
                                sourceUri = capturedUri,
                                lat = location.latitude,
                                lng = location.longitude,
                                addressText = addressText
                            )
                            withContext(Dispatchers.Main) {
                                watermarkedImageUri = stampedUri
                                isProcessingLocation = false
                                Toast.makeText(context, "GPS metadata stamped successfully!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                isProcessingLocation = false
                                Toast.makeText(context, "GPS signal unavailable. Check device location rules.", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            isProcessingLocation = false
                            Toast.makeText(context, "Location permission rejected. Standalone watermark failed.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    )

    // Multi-Permission runtime authorization verification supporting both Camera and Location checks
    val runtimePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] == true
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (cameraGranted && locationGranted) {
            val freshUri = createTempImageUri()
            tempCameraImageUri = freshUri
            cameraCaptureLauncher.launch(freshUri)
        } else {
            Toast.makeText(context, "Camera and Location permissions are mandatory to capture real-time geo-tagged assets.", Toast.LENGTH_LONG).show()
        }
    }

    if (isSuccess) {
        AlertDialog(
            onDismissRequest = { onBack() },
            confirmButton = {
                Button(
                    onClick = { onBack() },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                ) {
                    Text("OK")
                }
            },
            title = { Text("Registration Successful", fontWeight = FontWeight.Bold) },
            text = { Text("Your new pond water body asset has been created and logged successfully in the verification audit system.") },
            shape = RoundedCornerShape(12.dp),
            containerColor = Color.White
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AquaBackground)
                .verticalScroll(rememberScrollState())
        ) {
            AquaTopBar(
                title = "Add New Pond",
                subtitle = "Register a new water body",
                onBack = onBack
            )

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                SectionLabel("POND SPECIFICATIONS")

                // --- LIVE CAMERA CONTROLLER BOX ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = !isRegisteringPond) {
                            val cameraCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            val fineLoc = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                            val coarseLoc = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)

                            if (cameraCheck == PackageManager.PERMISSION_GRANTED && (fineLoc == PackageManager.PERMISSION_GRANTED || coarseLoc == PackageManager.PERMISSION_GRANTED)) {
                                val freshUri = createTempImageUri()
                                tempCameraImageUri = freshUri
                                cameraCaptureLauncher.launch(freshUri)
                            } else {
                                runtimePermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.CAMERA,
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isProcessingLocation) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = BrandGreen)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Stamping GPS Watermark...", color = TextSecondary, fontSize = 12.sp)
                        }
                    } else if (watermarkedImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(watermarkedImageUri),
                            contentDescription = "Watermarked Camera Captured Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Click Geo-Tagged Photo via Camera", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                            Text("Required for live verification audit", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }

                // --- ACTIVE GPS ACQUISITION METADATA BADGE ---
                if (latitude != null && longitude != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BrandGreen.copy(alpha = 0.06f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Done, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("GPS Lock Secured", fontWeight = FontWeight.Bold, color = BrandGreen, fontSize = 12.sp)
                                Text("Lat: $latitude • Long: $longitude", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // --- FORM TEXT INPUT FIELDS ---
                OutlinedTextField(
                    value = pondName,
                    onValueChange = { pondName = it },
                    label = { Text("Pond Name (e.g. North Wing A)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    enabled = !isRegisteringPond
                )

                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("Total Area (numeric sq ft value)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    enabled = !isRegisteringPond
                )

                OutlinedTextField(
                    value = fishType,
                    onValueChange = { fishType = it },
                    label = { Text("Primary Fish Species (e.g. Rohu, Catla)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    enabled = !isRegisteringPond
                )

                Spacer(modifier = Modifier.height(12.dp))

                // --- SUBMIT BUTTON ---
                Button(
                    onClick = {
                        val areaValue = area.toDoubleOrNull()
                        val speciesList = fishType.split(",").map { it.trim() }.filter { it.isNotEmpty() }

                        if (pondName.isBlank() || areaValue == null || speciesList.isEmpty() || watermarkedImageUri == null || latitude == null || longitude == null) {
                            Toast.makeText(context, "Please click a photo and complete all specifications fields.", Toast.LENGTH_LONG).show()
                        } else {
                            isRegisteringPond = true

                            viewModel.uploadNewPond(
                                context = context,
                                name = pondName,
                                lat = latitude!!,
                                lng = longitude!!,
                                area = areaValue,
                                speciesList = speciesList,
                                imageUri = watermarkedImageUri!!,
                                onSuccess = {
                                    isRegisteringPond = false
                                    isSuccess = true
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    shape = MaterialTheme.shapes.medium,
                    enabled = pondName.isNotEmpty() && area.isNotEmpty() && fishType.isNotEmpty() && watermarkedImageUri != null && !isRegisteringPond
                ) {
                    if (isRegisteringPond) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Registering Pond, please wait...", style = MaterialTheme.typography.titleMedium.copy(color = Color.White))
                        }
                    } else {
                        Text("Register Pond", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}