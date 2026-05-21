package com.example.fishapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.fishapp.navigation.Screen
import com.example.fishapp.ui.components.*
import com.example.fishapp.ui.theme.*

@Composable
fun DiseaseReportScreen(navController: NavHostController, onBack: () -> Unit) {
    val context = LocalContext.current
    var isSubmitted by remember { mutableStateOf(false) }
    var pondName by remember { mutableStateOf("") }
    var symptoms by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // 1. Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            navController.navigate(Screen.CameraScanner.route)
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. Gallery Photo Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedBorderColor = BrandGreen,
        unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f),
        focusedLabelColor = BrandGreen,
        unfocusedLabelColor = TextSecondary,
        cursorColor = BrandGreen
    )

    if (isSubmitted) {
        // This calls the function defined at the bottom of this file
        SuccessView(onReturn = onBack)
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AquaBackground)
                .verticalScroll(rememberScrollState())
        ) {
            AquaTopBar(
                title = "Report Outbreak",
                subtitle = "Expert help for your fish health",
                onBack = onBack
            )

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                SectionLabel("INCIDENT DETAILS")

                OutlinedTextField(
                    value = pondName,
                    onValueChange = { pondName = it },
                    label = { Text("Pond Name/Location") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    singleLine = true
                )

                OutlinedTextField(
                    value = symptoms,
                    onValueChange = { symptoms = it },
                    label = { Text("Observation/Symptoms") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    placeholder = { Text("Describe symptoms...") },
                    colors = textFieldColors
                )

                SectionLabel("UPLOAD EVIDENCE")

                DashedUploadBox(
                    icon = { Icon(Icons.Default.BugReport, contentDescription = null, tint = BrandGreen) },
                    title = if (selectedImageUri == null) "Attach Photo" else "Photo Attached ✅",
                    subtitle = if (selectedImageUri == null) "Clear photo of the sick fish" else "Click to change photo",
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

                if (selectedImageUri != null) {
                    Text(
                        text = "Evidence photo ready for upload",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrandGreen
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (pondName.isNotEmpty() && symptoms.isNotEmpty()) {
                            isSubmitted = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    shape = MaterialTheme.shapes.medium,
                    enabled = pondName.isNotEmpty() && symptoms.isNotEmpty()
                ) {
                    Text("Submit Urgent Report", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun SuccessView(onReturn: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = BrandGreen.copy(alpha = 0.1f)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = BrandGreen,
                modifier = Modifier.padding(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Report Submitted!",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Your report has been logged. An expert will review the data shortly.",
            fontSize = 15.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onReturn,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Back to Hub", fontWeight = FontWeight.Bold)
        }
    }
}