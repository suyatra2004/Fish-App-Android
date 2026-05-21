package com.example.fishapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.fishapp.navigation.Screen
import com.example.fishapp.ui.components.*
import com.example.fishapp.ui.theme.*
import com.example.fishapp.viewmodel.FishViewModel

@Composable
fun FarmerDashboard(
    navController: NavHostController,
    onBack: () -> Unit,
    viewModel: FishViewModel
) {
    // Automatically trigger a live network fetch from the backend when the screen opens
    LaunchedEffect(Unit) {
        viewModel.fetchFarmerPonds()
    }

    // Observe state metrics from the repository engine
    val pondsList by viewModel.pondsList
    val isPondsLoading by viewModel.isPondsLoading
    val pondsErrorMessage by viewModel.pondsErrorMessage

    Scaffold(
        containerColor = AquaBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.AddPond.route)
                },
                containerColor = BrandGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Pond")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Reusable Header with manual sync action button
            Box(modifier = Modifier.fillMaxWidth()) {
                AquaTopBar(
                    title = "My Ponds",
                    subtitle = "Manage your aquaculture assets",
                    onBack = onBack
                )

                IconButton(
                    onClick = { viewModel.fetchFarmerPonds() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 12.dp, end = 12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Sync", tint = Color.White)
                }
            }

            // DYNAMIC LAYOUT HANDLING BASED ON API RESPONSE STATES
            if (isPondsLoading) {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandGreen)
                }
            } else if (!pondsErrorMessage.isNullOrEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f).padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorCard(message = pondsErrorMessage ?: "Unknown Data Fetch Error")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        SectionLabel("Overview")
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatChip(value = String.format("%02d", pondsList.size), label = "Total Ponds", valueColor = BrandGreen)
                            StatChip(value = "00", label = "Active Alerts", valueColor = BrandGreen)
                        }
                    }

                    item { SectionLabel("Active Ponds") }

                    if (pondsList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No registered ponds found. Tap + to create one!", color = TextSecondary, fontSize = 14.sp)
                            }
                        }
                    } else {
                        // Iterates smoothly over live database items
                        items(pondsList) { pond ->
                            PondCard(
                                name = pond.name,
                                phValue = pond.ph,          // Pass Double directly
                                temperature = pond.temperature // Pass Double directly
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PondCard(name: String, phValue: Double, temperature: Double) { // FIXED: Updated parameters to Double
    AquaCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(BrandGreen.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Waves, contentDescription = null, tint = BrandGreen)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, color = TextPrimary)
                // FIXED: String format used here to display decimal numbers nicely (e.g., 7.20 and 28.0)
                Text("pH: ${String.format("%.2f", phValue)} • Temp: ${String.format("%.1f", temperature)}°C", fontSize = 12.sp, color = TextSecondary)
            }
            StatusBadge(com.example.fishapp.model.ReportStatus.RESOLVED)
        }
    }
}