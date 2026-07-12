package com.example.fishapp.ui.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.fishapp.ui.components.AquaTopBar
import com.example.fishapp.ui.theme.*
import com.example.fishapp.viewmodel.FishViewModel
import com.example.fishapp.viewmodel.ReportUploadUiState

@Composable
fun DiseaseReportScreen(
    navController: NavHostController,
    viewModel: FishViewModel, // Accepting the injected shared state machine securely
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Form Input States
    var pondName by remember { mutableStateOf("") }
    var reportName by remember { mutableStateOf("") }
    var symptoms by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Observe live submission state parameters to show the loading wheel spinner overlay
    val uploadState by viewModel.reportUploadState

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    // Reset upload feedback machine flags cleanly when screen builds fresh
    LaunchedEffect(Unit) {
        viewModel.resetReportUploadState()
    }

    Scaffold(
        topBar = {
            AquaTopBar(
                title = "New Outbreak Report",
                subtitle = "Submit diagnostic alert to experts",
                onBack = onBack
            )
        },
        containerColor = AquaBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. POND NAME INPUT ---
            OutlinedTextField(
                value = pondName,
                onValueChange = { pondName = it },
                label = { Text("Pond Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            // --- 2. REPORT TITLE INPUT ---
            OutlinedTextField(
                value = reportName,
                onValueChange = { reportName = it },
                label = { Text("Report Title / Issue Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            // --- 3. PATHOLOGICAL SYMPTOMS INPUT ---
            OutlinedTextField(
                value = symptoms,
                onValueChange = { symptoms = it },
                label = { Text("Observed Symptoms / Behavior") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(8.dp)
            )

            // --- 4. IMAGE UPLOAD AREA WITH CONTENT SELECTION ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Selected Evidence Snapshot File",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Upload Evidence Picture", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- 5. ACTION BUTTON WITH LOADING SPIN LOGIC ---
            val isLoading = uploadState is ReportUploadUiState.Loading

            Button(
                onClick = {
                    if (pondName.isBlank() || reportName.isBlank() || symptoms.isBlank() || imageUri == null) {
                        Toast.makeText(context, "Please fulfill all inputs and upload an image.", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.uploadNewReport(
                            context = context,
                            pondName = pondName,
                            reportName = reportName,
                            symptoms = symptoms,
                            imageUri = imageUri!!,
                            onSuccess = {
                                Toast.makeText(context, "Report posted successfully!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack() // Smoothly return to the choice menu portal scene
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                enabled = !isLoading // Blocks double submission clicks while transmission runs
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Submit Report", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            // Error display warning message logs
            if (uploadState is ReportUploadUiState.Error) {
                Text(
                    text = (uploadState as ReportUploadUiState.Error).message,
                    color = Color.Red,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}