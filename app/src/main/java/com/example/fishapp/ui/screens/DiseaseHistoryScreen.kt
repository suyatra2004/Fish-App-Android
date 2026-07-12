package com.example.fishapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.fishapp.api.ReportResponse
import com.example.fishapp.navigation.Screen
import com.example.fishapp.ui.components.AquaCard
import com.example.fishapp.ui.components.AquaTopBar
import com.example.fishapp.ui.theme.*
import com.example.fishapp.viewmodel.FishViewModel

@Composable
fun DiseaseHistoryScreen(
    navController: NavHostController,
    viewModel: FishViewModel,
    onBack: () -> Unit
) {
    // Fetches live historical reports logs from API on launch
    LaunchedEffect(Unit) {
        viewModel.fetchAllReports()
    }

    val reportsList by viewModel.reportsList
    val isLoading by viewModel.isReportsLoading

    Scaffold(
        topBar = {
            AquaTopBar(
                title = "History Records",
                subtitle = "Submitted Outbreak Reports Log",
                onBack = onBack
            )
        },
        containerColor = AquaBackground
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandGreen)
            }
        } else if (reportsList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No submitted reports found.", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reportsList) { report ->
                    HistoryReportCard(
                        report = report,
                        onClick = {
                            viewModel.setSelectedReport(report)
                            navController.navigate(Screen.DiseaseDetail.route)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryReportCard(report: ReportResponse, onClick: () -> Unit) {
    AquaCard(
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(report.report_name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                Text("Pond Name: ${report.pond_name}", color = TextSecondary, fontSize = 13.sp)
                Text("Date: ${if(report.created_at.length >= 10) report.created_at.take(10) else report.created_at}", color = Color.Gray, fontSize = 11.sp)
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = (if (report.verified) BrandGreen else Color.Gray).copy(alpha = 0.1f)
            ) {
                Text(
                    text = if (report.verified) "RESOLVED" else "UNDER REVIEW",
                    color = if (report.verified) BrandGreen else Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}