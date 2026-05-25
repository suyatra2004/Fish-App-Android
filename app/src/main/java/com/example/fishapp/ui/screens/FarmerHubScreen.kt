package com.example.fishapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
fun FarmerHubScreen(
    navController: NavHostController,      // ADDED: For wiping navigation memory maps
    viewModel: FishViewModel,              // ADDED: For calling safe session token drop rules
    onManagePonds: () -> Unit,
    onReportDisease: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AquaBackground)
    ) {
        // --- FIXED: Upgraded Header to house the high-contrast Logout Action button ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(BrandGreen, BrandGreenDark)))
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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

                // ADDED: Seamless Logout Action hook button
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