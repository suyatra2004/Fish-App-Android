package com.example.fishapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fishapp.ui.components.*
import com.example.fishapp.ui.theme.*

@Composable
fun FarmerHubScreen(
    onManagePonds: () -> Unit,
    onReportDisease: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AquaBackground)
    ) {
        // Aesthetic Header
        AquaTopBar(
            title = "Farmer Hub",
            subtitle = "Select a service to get started",
            onBack = onBack
        )

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
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(accentColor.copy(alpha = 0.1f), androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
                contentAlignment = androidx.compose.ui.Alignment.Center
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