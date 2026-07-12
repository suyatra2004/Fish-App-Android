package com.example.fishapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.fishapp.navigation.Screen
import com.example.fishapp.ui.components.AquaTopBar
import com.example.fishapp.ui.theme.AquaBackground
import com.example.fishapp.ui.theme.BrandGreen

@Composable
fun DiseaseMenuScreen(
    navController: NavHostController,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            AquaTopBar(
                title = "Disease Reporting",
                subtitle = "Select an operation action",
                onBack = onBack
            )
        },
        containerColor = AquaBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // Option 1: Create New Form Record
            Button(
                onClick = { navController.navigate(Screen.DiseaseReport.route) },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
            ) {
                Icon(Icons.Default.Assignment, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Write New Report")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Option 2: Inspect historic logs stack feed
            OutlinedButton(
                onClick = { navController.navigate(Screen.DiseaseHistoryList.route) },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandGreen)
            ) {
                Icon(Icons.Default.History, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Check Previous Reports")
            }
        }
    }
}