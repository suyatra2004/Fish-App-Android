package com.example.fishapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.fishapp.ui.components.*
import com.example.fishapp.ui.theme.*

@Composable
fun AddPondScreen(onBack: () -> Unit) {
    var pondName by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var fishType by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedBorderColor = BrandGreen,
        unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f),
        focusedLabelColor = BrandGreen,
        unfocusedLabelColor = TextSecondary
    )

    if (isSuccess) {
        SuccessView(onReturn = onBack) // Reusing the same SuccessView from earlier
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AquaBackground)
                .verticalScroll(rememberScrollState())
        ) {
            AquaTopBar(
                title = "Add New Pond",
                subtitle = "Register a new water body",
                onBack = onBack
            )

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                SectionLabel("POND SPECIFICATIONS")

                OutlinedTextField(
                    value = pondName,
                    onValueChange = { pondName = it },
                    label = { Text("Pond Name (e.g. North Wing A)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("Total Area (in Acres/Hectares)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = fishType,
                    onValueChange = { fishType = it },
                    label = { Text("Primary Fish Species (e.g. Rohu)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { isSuccess = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    shape = MaterialTheme.shapes.medium,
                    enabled = pondName.isNotEmpty() && area.isNotEmpty()
                ) {
                    Text("Register Pond", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}