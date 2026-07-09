package com.example.fishapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
    // FIXED: Uses your exact existing ViewModel fetch function signature
    LaunchedEffect(Unit) {
        viewModel.fetchFarmerPonds()
    }

    // FIXED: Uses your exact existing ViewModel state observations
    val pondsList by viewModel.pondsList
    val isPondsLoading by viewModel.isPondsLoading

    // DYNAMIC LOCAL HOST ROUTE: Change to your local Wi-Fi IP address if testing on a physical phone
    val backendHostAddress = "192.168.0.176:8000"

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
                                latitude = pond.latitude ?: 0.0,
                                longitude = pond.longitude ?: 0.0,
                                estimatedArea = pond.estimated_area ?: 0.0,
                                verified = pond.verified,
                                pondId = pond.id,
                                backendHost = backendHostAddress
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PondCard(
    name: String,
    latitude: Double,
    longitude: Double,
    estimatedArea: Double,
    verified: Boolean,
    pondId: Int,
    backendHost: String
) {
    val context = LocalContext.current

    // Set up secure Coil picture loader intersecting Endpoint 6 binary streams[cite: 1]
    val pondImagePainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data("http://$backendHost/ponds/$pondId/image") // Fetches explicit binary stream target routes[cite: 1]
            .addHeader("Authorization", com.example.fishapp.api.RetrofitClient.getAuthToken() ?: "")
            .crossfade(true)
            .build()
    )

    AquaCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Geo-tagged image container space[cite: 1]
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE2E8F0)), // Fallback Slate-Gray placeholder background
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = pondImagePainter,
                    contentDescription = "Pond Asset Snap",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.width(16.dp))

            // Metadata data processing tags column[cite: 1]
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                Text(
                    text = "Area: ${String.format("%.1f", estimatedArea)} sq ft",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PinDrop,
                        contentDescription = "Coordinates Label",
                        tint = BrandGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${String.format("%.4f", latitude)}, ${String.format("%.4f", longitude)}",
                        fontSize = 11.sp,
                        color = BrandGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // Dynamic Compliance Audit verification badge system[cite: 1]
            val badgeColor = if (verified) BrandGreen else Color.Gray
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = badgeColor.copy(alpha = 0.1f)
            ) {
                Text(
                    text = if (verified) "VERIFIED" else "PENDING",
                    color = badgeColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}