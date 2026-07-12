package com.example.fishapp.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.fishapp.api.AdminPondResponse
import com.example.fishapp.api.AdminReportResponse
import com.example.fishapp.ui.components.AquaCard
import com.example.fishapp.ui.theme.*
import com.example.fishapp.viewmodel.FishViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    navController: NavHostController,
    viewModel: FishViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0 = Ponds, 1 = Disease Reports

    val adminPonds by viewModel.adminPondsList
    val adminReports by viewModel.adminReportsList
    val isLoading by viewModel.isAdminLoading
    val errorMessage by viewModel.adminErrorMessage

    // Detail Pop-Up States
    var selectedPondDetail by remember { mutableStateOf<AdminPondResponse?>(null) }
    var selectedReportDetail by remember { mutableStateOf<AdminReportResponse?>(null) }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 0) {
            viewModel.fetchAllAdminPonds()
        } else {
            viewModel.fetchAllAdminReports()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Admin Control Center", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                        Text("Moderation & Verification System", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.logoutUser()
                        // FIXED: Safely navigates directly to the login path without popping the graph anchor line
                        navController.navigate("login_selection") {
                            popUpTo("login_selection") { inclusive = true }
                            launchSingleTop = true
                        }
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandGreen)
            )
        },
        containerColor = AquaBackground
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = BrandGreen
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Ponds Moderation", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Disease Reports", fontWeight = FontWeight.Bold) }
                    )
                }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrandGreen)
                    }
                } else if (errorMessage != null) {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(errorMessage!!, color = Color.Red, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (selectedTab == 0) {
                            if (adminPonds.isEmpty()) {
                                item { Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { Text("No ponds listed for review.", color = TextSecondary) } }
                            }
                            items(adminPonds) { pond ->
                                AdminPondCard(
                                    pond = pond,
                                    onClick = { selectedPondDetail = pond },
                                    onApprove = {
                                        viewModel.moderatePondStatus(pond.id, approve = true) {
                                            Toast.makeText(context, "Pond approved successfully!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onReject = {
                                        viewModel.moderatePondStatus(pond.id, approve = false) {
                                            Toast.makeText(context, "Pond unverified/rejected.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        } else {
                            if (adminReports.isEmpty()) {
                                item { Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { Text("No disease health alerts submitted.", color = TextSecondary) } }
                            }
                            items(adminReports) { report ->
                                AdminReportCard(
                                    report = report,
                                    onClick = { selectedReportDetail = report },
                                    onApprove = {
                                        viewModel.moderateReportStatus(report.id, approve = true) {
                                            Toast.makeText(context, "Report marked as Resolved!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onReject = {
                                        viewModel.moderateReportStatus(report.id, approve = false) {
                                            Toast.makeText(context, "Report status updated to Under Review.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // --- POND DETAILED DIALOG MODULE ---
            selectedPondDetail?.let { pond ->
                AdminPondDetailDialog(pond = pond, onDismiss = { selectedPondDetail = null })
            }

            // --- REPORT DETAILED DIALOG MODULE ---
            selectedReportDetail?.let { report ->
                AdminReportDetailDialog(report = report, onDismiss = { selectedReportDetail = null })
            }
        }
    }
}

@Composable
fun AdminPondCard(
    pond: AdminPondResponse,
    onClick: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val context = LocalContext.current
    AquaCard(modifier = Modifier.clickable { onClick() }) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(pond.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    Text("Owner: ${pond.owner_username}", color = TextSecondary, fontSize = 13.sp)
                    Text("Area: ${pond.estimated_area ?: 0.0} sq ft", color = TextSecondary, fontSize = 13.sp)
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = (if (pond.verified) BrandGreen else Color.Red).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = if (pond.verified) "VERIFIED" else "PENDING",
                        color = if (pond.verified) BrandGreen else Color.Red,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.clickable {
                        pond.owner_phone?.let { phone ->
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(intent)
                        }
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(pond.owner_phone ?: "No Phone", color = BrandGreen, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!pond.verified) {
                        Button(onClick = onApprove, colors = ButtonDefaults.buttonColors(containerColor = BrandGreen), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Approve", fontSize = 12.sp)
                        }
                    } else {
                        OutlinedButton(onClick = onReject, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reject", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminReportCard(
    report: AdminReportResponse,
    onClick: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val context = LocalContext.current
    AquaCard(modifier = Modifier.clickable { onClick() }) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(report.report_name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    Text("Pond Context: ${report.pond_name}", color = TextSecondary, fontSize = 13.sp)
                    Text("Symptoms: ${report.symptoms}", color = Color.DarkGray, fontSize = 13.sp, maxLines = 2)
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = (if (report.verified) BrandGreen else Color.Red).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = if (report.verified) "RESOLVED" else "PENDING",
                        color = if (report.verified) BrandGreen else Color.Red,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.clickable {
                        report.farmer_phone?.let { phone ->
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(intent)
                        }
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(report.farmer_phone ?: "No Phone", color = BrandGreen, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!report.verified) {
                        Button(onClick = onApprove, colors = ButtonDefaults.buttonColors(containerColor = BrandGreen), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Resolve", fontSize = 12.sp)
                        }
                    } else {
                        OutlinedButton(onClick = onReject, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Unverify", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// DETAILED INSPECTION MODAL DIALOG COMPOSABLES
// ==========================================

@Composable
fun AdminPondDetailDialog(pond: AdminPondResponse, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val backendHostAddress = com.example.fishapp.api.RetrofitClient.BACKEND_HOST

    val rawToken = com.example.fishapp.api.RetrofitClient.getAuthToken() ?: ""
    val authHeaderValue = if (rawToken.startsWith("Bearer ", ignoreCase = true)) rawToken else "Bearer $rawToken"

    // FIXED: Maps clean paths directly against the local network address host
    val imagePath = pond.image_url ?: ""
    val imageUrl = when {
        imagePath.startsWith("http://") || imagePath.startsWith("https://") -> imagePath
        imagePath.startsWith("/") -> "http://$backendHostAddress$imagePath"
        else -> "http://$backendHostAddress/$imagePath"
    }

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .addHeader("Authorization", authHeaderValue)
            .crossfade(true)
            .build()
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)) {
                Text("Close Layout")
            }
        },
        title = { Text(pond.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                ) {
                    Image(
                        painter = painter,
                        contentDescription = "Pond Geo-Tagged Image Asset",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Text("• Owner Username: ${pond.owner_username}", fontSize = 13.sp, color = Color.Black)
                Text("• Total Asset Area: ${pond.estimated_area ?: 0.0} sq ft", fontSize = 13.sp, color = Color.Black)
                Text("• Species Cultured: ${pond.fish_species.joinToString(", ")}", fontSize = 13.sp, color = Color.Black)
                Text("• GPS Coordinates: Lat ${pond.latitude ?: 0.0} / Lng ${pond.longitude ?: 0.0}", fontSize = 13.sp, color = Color.Black)
                Text("• Log Timestamp: ${pond.created_at.take(19).replace("T", " ")}", fontSize = 12.sp, color = Color.Gray)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
fun AdminReportDetailDialog(report: AdminReportResponse, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val backendHostAddress = com.example.fishapp.api.RetrofitClient.BACKEND_HOST

    val rawToken = com.example.fishapp.api.RetrofitClient.getAuthToken() ?: ""
    val authHeaderValue = if (rawToken.startsWith("Bearer ", ignoreCase = true)) rawToken else "Bearer $rawToken"

    // FIXED: Maps clean paths directly against the local network address host
    val photoPath = report.photo_url ?: ""
    val imageUrl = when {
        photoPath.startsWith("http://") || photoPath.startsWith("https://") -> photoPath
        photoPath.startsWith("/") -> "http://$backendHostAddress$photoPath"
        else -> "http://$backendHostAddress/$photoPath"
    }

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .addHeader("Authorization", authHeaderValue)
            .crossfade(true)
            .build()
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)) {
                Text("Dismiss View")
            }
        },
        title = { Text(report.report_name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                ) {
                    Image(
                        painter = painter,
                        contentDescription = "Outbreak Pathological Photo Asset",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Text("• Farmer Account: ${report.farmer_username}", fontSize = 13.sp, color = Color.Black)
                Text("• Target Pond Body: ${report.pond_name} (ID: #${report.pond_id})", fontSize = 13.sp, color = Color.Black)
                Text("• Reported Symptoms:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                Text(report.symptoms, fontSize = 13.sp, color = Color.DarkGray, lineHeight = 18.sp)
                Text("• Incident Date: ${report.created_at.take(19).replace("T", " ")}", fontSize = 12.sp, color = Color.Gray)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(14.dp)
    )
}