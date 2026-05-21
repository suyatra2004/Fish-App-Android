package com.example.fishapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Refresh
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
import com.example.fishapp.ui.components.*
import com.example.fishapp.ui.theme.*
import com.example.fishapp.viewmodel.FishViewModel

@Composable
fun AdminDashboard(
    onBack: () -> Unit,
    viewModel: FishViewModel // FIXED: Wired up shared state parameter to pull real-time database feeds
) {
    // Automatically query the global report pipeline stream from the server upon launching
    LaunchedEffect(Unit) {
        viewModel.fetchAllReports()
    }

    // Observe active live state metrics from the central engine
    val reportsList by viewModel.reportsList
    val isReportsLoading by viewModel.isReportsLoading
    val reportsErrorMessage by viewModel.reportsErrorMessage

    Scaffold(
        containerColor = Color(0xFFF8FAFC) // Light Gray-Blue background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Reusable Top bar with transparently placed manual sync trigger
            Box(modifier = Modifier.fillMaxWidth()) {
                AquaTopBar(
                    title = "Expert Console",
                    subtitle = "Regional aquaculture monitoring",
                    onBack = onBack
                )

                IconButton(
                    onClick = { viewModel.fetchAllReports() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 12.dp, end = 12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Sync Data", tint = Color.White)
                }
            }

            // DYNAMIC LAYOUT LOADING CONTROLLER BASED ON NETWORK STATUS
            if (isReportsLoading) {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandGreen)
                }
            } else if (!reportsErrorMessage.isNullOrEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f).padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorCard(message = reportsErrorMessage ?: "Unknown Error Fetching Incidents")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Executive Summary Panel calculated directly from backend metrics
                    item {
                        SectionLabel("System Health")
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatChip(
                                value = String.format("%02d", reportsList.size),
                                label = "New Reports",
                                valueColor = Color(0xFFE74C3C)
                            )
                            StatChip(
                                value = "Active",
                                label = "Pipeline State",
                                valueColor = BrandGreen
                            )
                        }
                    }

                    // 2. Urgent Alerts Stream
                    item { SectionLabel("Active Outbreaks") }

                    if (reportsList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No active disease outbreak alerts filed across the district.", color = TextSecondary, fontSize = 14.sp)
                            }
                        }
                    } else {
                        // Iterates dynamically over active entries matching database responses
                        items(reportsList) { report ->
                            OutbreakCard(
                                title = report.report_name,
                                symptoms = report.symptoms,
                                pondName = report.pond_name,
                                isVerified = report.verified
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OutbreakCard(title: String, symptoms: String, pondName: String, isVerified: Boolean) {
    AquaCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFFE74C3C).copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Assignment, contentDescription = null, tint = Color(0xFFE74C3C))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                Text("Pond: $pondName • Symptoms: $symptoms", fontSize = 12.sp, color = TextSecondary, maxLines = 1)
            }

            // Dynamic Verification Badge display
            if (!isVerified) {
                Text("URGENT", color = Color(0xFFE74C3C), fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 0.5.sp)
            } else {
                Text("RESOLVED", color = BrandGreen, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 0.5.sp)
            }
        }
    }
}