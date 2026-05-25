package com.example.fishapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
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
import com.example.fishapp.ui.components.AquaCard
import com.example.fishapp.ui.components.AquaTopBar
import com.example.fishapp.ui.theme.*
import com.example.fishapp.viewmodel.FishViewModel

@Composable
fun PredictionHistoryListScreen(
    navController: NavHostController,
    viewModel: FishViewModel
) {
    val historyList by viewModel.historyList
    val isHistoryLoading by viewModel.isHistoryLoading

    // Ensure the feed is freshly synced from the server when opening this screen
    LaunchedEffect(Unit) {
        viewModel.fetchPredictionHistory()
    }

    Scaffold(
        topBar = {
            AquaTopBar(
                title = "All Scans History",
                subtitle = "Complete archive of your pipeline analyses",
                onBack = { navController.popBackStack() }
            )
        },
        containerColor = AquaBackground
    ) { paddingValues ->
        if (isHistoryLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandGreen)
            }
        } else if (historyList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(text = "No scanning history found.", color = TextSecondary, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Loops through EVERY item returned by Endpoint 3
                items(historyList) { item ->
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
                                Text(text = item.species, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(
                                    text = "${item.created_at.take(10)} • Match: ${item.species_confidence_percent}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
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
                }
            }
        }
    }
}