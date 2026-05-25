package com.example.fishapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.fishapp.navigation.Screen
import com.example.fishapp.ui.components.*
import com.example.fishapp.ui.theme.*
import com.example.fishapp.viewmodel.FishViewModel

@Composable
fun AdminDashboard(
    navController: NavHostController,
    onBack: () -> Unit,
    viewModel: FishViewModel
) {
    val context = LocalContext.current

    // Automatically query the global report pipeline stream from the server upon launching
    LaunchedEffect(Unit) {
        viewModel.fetchAllReports()
    }

    // Observe active live state metrics from the central engine
    val reportsList by viewModel.reportsList
    val isReportsLoading by viewModel.isReportsLoading
    val reportsErrorMessage by viewModel.reportsErrorMessage

    // Dialog state control to toggle exit prompt visibility
    var showExitDialog by remember { mutableStateOf(false) }

    // FIXED: Intercepts device physical back gestures or keys to show the application exit dialog
    BackHandler(enabled = true) {
        showExitDialog = true
    }

    // SYSTEM EXIT CONFIRMATION DIALOG INTERFACE
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = {
                Text(text = "Exit Application", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(text = "Are you sure you want to exit AquaSense?", color = TextSecondary)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        viewModel.logoutUser() // Clear active login session token state maps securely
                        (context as? android.app.Activity)?.finishAffinity() // Force complete shutdown of application activity stack
                    }
                ) {
                    Text("Yes", color = BrandGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("No", color = TextSecondary)
                }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC) // Light Gray-Blue background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // --- FIXED: Upgraded Custom Header housing Back Arrow Alert trigger alongside Actions ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(BrandGreen, BrandGreenDark)))
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // FIXED: Embedded high-contrast back navigation button targeting our exit confirmation dialog
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Trigger Exit Dialog",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Expert Console",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Regional aquaculture monitoring",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    // Combined Actions Slot
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // 1. Data Sync Trigger
                        IconButton(onClick = { viewModel.fetchAllReports() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync Data",
                                tint = Color.White
                            )
                        }

                        // 2. Seamless Admin Session Revocation Trigger
                        IconButton(
                            onClick = {
                                viewModel.logoutUser() // Purges active token caches and in-memory streams
                                navController.navigate(Screen.LoginSelection.route) {
                                    popUpTo(0) { inclusive = true } // Erases total application backstack trail traces
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Sign Out Admin",
                                tint = Color.White
                            )
                        }
                    }
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