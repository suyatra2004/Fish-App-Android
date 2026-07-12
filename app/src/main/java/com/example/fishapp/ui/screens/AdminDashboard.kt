package com.example.fishapp.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.fishapp.api.AdminPondResponse // FIXED: Sourced from api package
import com.example.fishapp.api.AdminReportResponse // FIXED: Sourced from api package
import com.example.fishapp.ui.components.AquaCard
import com.example.fishapp.ui.theme.*
import com.example.fishapp.viewmodel.FishViewModel

@OptIn(ExperimentalMaterial3Api::class) // FIXED: Required for Material 3 TopAppBar layout containers
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

    // Refresh metrics on dashboard mount
    LaunchedEffect(selectedTab) {
        if (selectedTab == 0) {
            viewModel.fetchAllAdminPonds()
        } else {
            viewModel.fetchAllAdminReports()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar( // FIXED: Replaced SmallTopAppBar with standard Material 3 TopAppBar
                title = {
                    Column {
                        Text("Admin Control Center", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                        Text("Moderation & Verification System", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.logoutUser()
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandGreen) // FIXED: Correct Material 3 color builder function
            )
        },
        containerColor = AquaBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // --- TAB SELECTOR COMPONENT ---
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

            // --- MAIN LIST FEED OVERLAY ---
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
    }
}

@Composable
fun AdminPondCard(
    pond: AdminPondResponse,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val context = LocalContext.current
    AquaCard {
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
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val context = LocalContext.current
    AquaCard {
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