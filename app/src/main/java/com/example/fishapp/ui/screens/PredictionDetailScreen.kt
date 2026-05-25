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
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.fishapp.ui.components.AquaCard
import com.example.fishapp.ui.components.AquaTopBar
import com.example.fishapp.ui.theme.*
import com.example.fishapp.viewmodel.FishViewModel

@Composable
fun PredictionDetailScreen(
    navController: NavHostController,
    viewModel: FishViewModel
) {
    val context = LocalContext.current
    val item = viewModel.currentDetailItem.value

    Scaffold(
        topBar = {
            AquaTopBar(
                title = "Scan Report Detail",
                subtitle = "Archived AI pipeline calculation nodes",
                onBack = { navController.popBackStack() }
            )
        },
        containerColor = AquaBackground
    ) { paddingValues ->
        if (item == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Report context unavailable.", color = TextSecondary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Endpoint 5 Image Handshake: Loads the secure binary image stream dynamically using your server address
                val authenticatedImagePainter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(context)
                        .data("http://10.0.2.2:8000/predictions/${item.id}/image") // Maps image path stream endpoint
                        .crossfade(true)
                        .build()
                )

                Image(
                    painter = authenticatedImagePainter,
                    contentDescription = "Historical scanned image object stream",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                AquaCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        //  NEW FIXED LINE:
                        Text("Classification Node Metrics", fontWeight = FontWeight.Bold, color = BrandGreen, fontSize = 16.sp)
                        HorizontalDivider(color = BrandGreen.copy(alpha = 0.1f))

                        DetailRow("Report Reference ID", "#${item.id}")
                        DetailRow("Identified Species Class", item.species)
                        DetailRow("Species Classifier Confidence", item.species_confidence_percent)
                        DetailRow("YOLO Detection Accuracy", item.yolo_confidence_percent)
                        DetailRow("File Name Registry", item.filename)
                        DetailRow("Timestamp Logged", item.created_at.take(19).replace("T", " "))
                    }
                }

                val isHealthy = item.disease_status.contains("HEALTHY", ignoreCase = true) || item.disease_status.contains("FRESH", ignoreCase = true)
                val accentColor = if (isHealthy) BrandGreen else Color.Red

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(accentColor.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("DISEASE ANALYSIS EVALUATION VERDICT", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(item.disease_status.uppercase(), fontSize = 22.sp, color = accentColor, fontWeight = FontWeight.Black)
                        Text("Confidence Score Accuracy: ${item.disease_confidence_percent}", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(value, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}