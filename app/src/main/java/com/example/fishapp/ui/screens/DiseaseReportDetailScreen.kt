package com.example.fishapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.fishapp.ui.components.AquaTopBar
import com.example.fishapp.ui.theme.*
import com.example.fishapp.viewmodel.FishViewModel

@Composable
fun DiseaseReportDetailScreen(
    viewModel: FishViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val report = viewModel.selectedReport
    val backendHostAddress =  com.example.fishapp.api.RetrofitClient.BACKEND_HOST


    val rawToken = com.example.fishapp.api.RetrofitClient.getAuthToken() ?: ""
    val authHeaderValue = if (rawToken.startsWith("Bearer ", ignoreCase = true)) rawToken else "Bearer $rawToken"

    // FIXED: Maps photo path variants directly to the backend spec matching relative path configurations
    val imageTargetData = when {
        report == null -> ""
        report.photo_url.startsWith("http://") || report.photo_url.startsWith("https://") -> report.photo_url
        report.photo_url.startsWith("/") -> "http://$backendHostAddress${report.photo_url}"
        // Re-routes standalone IDs or relative terms directly to the photo path definition
        else -> "http://$backendHostAddress/admin/reports/${report.id}/photo"
    }

    // Set up secure Coil picture loader targeting backend binary image endpoints
    val imagePainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(imageTargetData)
            .addHeader("Authorization", authHeaderValue)
            .crossfade(true)
            .build()
    )

    Scaffold(
        topBar = {
            AquaTopBar(
                title = report?.report_name ?: "Report Detail",
                subtitle = "Incident Log Audit Record",
                onBack = onBack
            )
        },
        containerColor = AquaBackground
    ) { innerPadding ->
        if (report == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No incident report data found.", color = TextSecondary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. DYNAMIC OUTBREAK SNAPSHOT HEADER
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE2E8F0))
                ) {
                    Image(
                        painter = imagePainter,
                        contentDescription = "Disease Evidence Snap",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // 2. METADATA SPECIFICATIONS CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Target Reservoir: ${report.pond_name}", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                        Text("Pond Registered ID reference: #${report.pond_id}", color = TextSecondary, fontSize = 13.sp)
                        Text("Created Date: ${if(report.created_at.length >= 19) report.created_at.replace("T", " ").take(19) else report.created_at}", color = Color.Gray, fontSize = 12.sp)
                    }
                }

                // 3. CLINICAL SYMPTOMS AUDIT CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Observed Pathological Symptoms", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray.copy(alpha = 0.2f))
                        Text(report.symptoms, color = TextSecondary, fontSize = 13.sp, lineHeight = 20.sp)
                    }
                }
            }
        }
    }
}