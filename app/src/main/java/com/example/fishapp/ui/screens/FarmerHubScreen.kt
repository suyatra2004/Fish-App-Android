package com.example.fishapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Waves
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
fun FarmerHubScreen(
    navController: NavHostController,
    viewModel: FishViewModel,
    onManagePonds: () -> Unit,
    onReportDisease: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Dialog state control to toggle exit prompt visibility
    var showExitDialog by remember { mutableStateOf(false) }

    // FIXED: Intercepts device physical back gestures/buttons to show the exit pop-up
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
                        viewModel.logoutUser() // Clear active login session interceptors securely
                        (context as? android.app.Activity)?.finishAffinity() // Gracefully completely close application task stack
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AquaBackground)
    ) {
        // --- FIXED: Upgraded Header to house the Back Arrow Alert trigger alongside Logout ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(BrandGreen, BrandGreenDark)))
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 20.dp) // Adjusted horizontal padding to balance icon alignment
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // FIXED: Embedded high-contrast back navigation button targeting our exit dialog prompt
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
                        text = "Farmer Hub",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Select a service to get started",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                // Seamless Logout Action hook button
                IconButton(
                    onClick = {
                        viewModel.logoutUser() // Purges active user profile state caching maps
                        navController.navigate(Screen.LoginSelection.route) {
                            popUpTo(0) { inclusive = true } // Completely destroys cross-workspace backstack history tracking links
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Sign Out Farmer",
                        tint = Color.White
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SectionLabel("AVAILABLE SERVICES")

            // 1. Manage Ponds Card
            ServiceCard(
                title = "My Ponds",
                description = "Monitor growth, pH levels, and inventory.",
                icon = Icons.Default.Waves,
                accentColor = BrandGreen,
                onClick = onManagePonds
            )

            // 2. Disease Reporting Card
            ServiceCard(
                title = "Disease Reporting",
                description = "Report outbreaks and get instant expert aid.",
                icon = Icons.Default.HealthAndSafety,
                accentColor = Color(0xFFE74C3C), // Red for medical/emergency
                onClick = onReportDisease
            )
        }
    }
}

@Composable
fun ServiceCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    AquaCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(accentColor.copy(alpha = 0.1f), androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                Text(description, fontSize = 13.sp, color = TextSecondary)
            }
        }
    }
}