package com.example.fishapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.fishapp.ui.components.AquaTopBar
import com.example.fishapp.ui.theme.*
import com.example.fishapp.viewmodel.FishViewModel

@Composable
fun PondDetailScreen(
    navController: NavHostController,
    viewModel: FishViewModel,
    onBack: () -> Unit
) {
    // Retrieve the active selected pond from the shared state context
    val pond = viewModel.selectedPond

    Scaffold(
        topBar = {
            AquaTopBar(
                title = pond?.name ?: "Pond Details",
                subtitle = "Verification & Audit Metrics",
                onBack = onBack
            )
        },
        containerColor = AquaBackground
    ) { innerPadding ->
        if (pond == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No pond data found.", color = TextSecondary)
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
                // --- 1. GEO-TAGGED IMAGE VIEW HEADER ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(pond.image_url),
                        contentDescription = "Registered Pond Visual File",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // --- 2. LIVE LOCATION AUDIT CARD ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location Pin",
                            tint = BrandGreen,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("GPS Coordinates", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                            Text("Latitude: ${pond.latitude}°", color = TextSecondary, fontSize = 12.sp)
                            Text("Longitude: ${pond.longitude}°", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }

                // --- 3. PHYSICAL METRICS LOGS ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Total Calculated Area", fontWeight = FontWeight.Medium, color = TextPrimary)
                            }
                            Text("${pond.estimated_area} sq ft", fontWeight = FontWeight.Bold, color = BrandGreen)
                        }

                        Divider(color = Color.Gray.copy(alpha = 0.2f))

                        // --- 4. BIOLOGICAL ECOSYSTEM TRACKER ---
                        Text("Cultivated Stock Profile", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)

                        // Maps individual list elements out dynamically using a clean text display layout
                        pond.fish_species.forEach { species ->
                            Text("• $species", color = BrandGreen, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        }
    }
}