package com.example.fishapp.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import coil.compose.rememberAsyncImagePainter
import com.example.fishapp.ui.components.*
import com.example.fishapp.ui.theme.*
import com.example.fishapp.viewmodel.FishViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    var watermarkedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isProcessingLocation by remember { mutableStateOf(false) }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    // NEW UTILITY STATE: Handles network loading indicator overlays
    var isRegisteringPond by remember { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedBorderColor = BrandGreen,
        unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f),
        focusedLabelColor = BrandGreen,
        unfocusedLabelColor = TextSecondary
    )

    // Photo picker configuration interacting with the GPS map engine handles
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
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
                                sourceUri = uri,
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

    // Multi-Permission runtime authorization verification
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            Toast.makeText(context, "Location rights are mandatory for geo-tagged pond registration.", Toast.LENGTH_LONG).show()
        }
    }

    if (isSuccess) {
        SuccessView(onReturn = onBack)
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

                // --- LIVE GEO-TAGGED GALLERY CONTROLLER BOX ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = !isRegisteringPond) { // Disable changes during network calls
                            val fineLoc = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                            val coarseLoc = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                            if (fineLoc == PackageManager.PERMISSION_GRANTED || coarseLoc == PackageManager.PERMISSION_GRANTED) {
                                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            } else {
                                permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
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
                            contentDescription = "Watermarked Pond Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Upload Geo-Tagged Image", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                            Text("Required for verification audit", fontSize = 11.sp, color = TextSecondary)
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

                // --- PIPELINE COMPLIANT UPLOAD SUBMIT BUTTON (WITH LOADING VISUALS) ---
                Button(
                    onClick = {
                        val areaValue = area.toDoubleOrNull()
                        val speciesList = fishType.split(",").map { it.trim() }.filter { it.isNotEmpty() }

                        if (pondName.isBlank() || areaValue == null || speciesList.isEmpty() || watermarkedImageUri == null || latitude == null || longitude == null) {
                            Toast.makeText(context, "Please provide complete details and select a valid image.", Toast.LENGTH_LONG).show()
                        } else {
                            // Turn loading animations ON
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