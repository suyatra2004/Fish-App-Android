package com.example.fishapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.fishapp.ui.components.AquaTopBar
import com.example.fishapp.ui.theme.AquaBackground
import com.example.fishapp.ui.theme.BrandGreen
import com.example.fishapp.ui.theme.TextPrimary
import com.example.fishapp.ui.theme.TextSecondary
import com.example.fishapp.viewmodel.FishViewModel

@Composable
fun PondDetailScreen(
    navController: NavHostController,
    viewModel: FishViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val pond = viewModel.selectedPond

    // DYNAMIC LOCAL HOST ROUTE: Matches your dashboard setup perfectly
    val backendHostAddress = "192.168.0.176:8000"

    // Set up secure Coil picture loader intersecting explicit binary stream target routes
    val rawToken = com.example.fishapp.api.RetrofitClient.getAuthToken() ?: ""
    val authHeaderValue = if (rawToken.startsWith("Bearer ", ignoreCase = true)) rawToken else "Bearer $rawToken"

    val pondImagePainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(if (pond != null) "http://$backendHostAddress/ponds/${pond.id}/image" else "")
            .addHeader("Authorization", authHeaderValue) // Standardized token authentication wrapper string
            .crossfade(true)
            .build()
    )

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
                // --- 1. GEO-TAGGED IMAGE VIEW HEADER (WITH DYNAMIC SECURE STREAM LOADING) ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE2E8F0))
                ) {
                    Image(
                        painter = pondImagePainter,
                        contentDescription = "Registered Pond Visual File",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // --- TASK 2: DYNAMIC COMPLIANCE AUDIT VERIFICATION BADGE OVERLAY ---
                    val verifiedState = pond.verified
                    val badgeColor = if (verifiedState) BrandGreen else Color.Gray
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = badgeColor.copy(alpha = 0.9f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = if (verifiedState) "VERIFIED" else "PENDING",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // --- 2. LIVE LOCATION AUDIT CARD ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
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
                    shape = RoundedCornerShape(12.dp)
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
                            Text("${pond.estimated_area ?: 0.0} sq ft", fontWeight = FontWeight.Bold, color = BrandGreen)
                        }

                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

                        // --- 4. BIOLOGICAL ECOSYSTEM TRACKER ---
                        Text("Cultivated Stock Profile", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)

                        val speciesList = pond.fish_species ?: emptyList()

                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            speciesList.forEach { singleSpecies ->
                                Text(
                                    text = "• $singleSpecies",
                                    color = BrandGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}