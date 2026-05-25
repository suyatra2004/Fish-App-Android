package com.example.fishapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fishapp.ui.theme.*
import com.example.fishapp.viewmodel.AuthUiState
import com.example.fishapp.viewmodel.FishViewModel

@Composable
fun LoginSelectionScreen(
    viewModel: FishViewModel,
    onConsumerLoginSuccess: () -> Unit,
    onFarmerAdminLoginSuccess: () -> Unit
) {
    var isConsumerWorkspace by remember { mutableStateOf(true) }
    var isSignUpMode by remember { mutableStateOf(false) }

    // Core Input States
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    // Explicit Management Role Selector State (Farmer vs Admin)
    var selectedManagementRole by remember { mutableStateOf("Farmer") }
    var passwordVisible by remember { mutableStateOf(false) }

    val authState by viewModel.authUriState

    // FIXED: Wipes out field text dynamically whenever the user toggles between the Consumer and Management workspaces
    LaunchedEffect(isConsumerWorkspace) {
        username = ""
        password = ""
        fullName = ""
        phoneNumber = ""
        passwordVisible = false
    }

    // FIXED: Wipes out field text dynamically when switching roles (Farmer <-> Admin) inside the Management workspace
    LaunchedEffect(selectedManagementRole) {
        username = ""
        password = ""
        fullName = ""
        phoneNumber = ""
        passwordVisible = false
    }

    // Reusable Custom TextField Colors configuration
    val inputFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedLabelColor = BrandGreen,
        unfocusedLabelColor = TextSecondary,
        focusedLeadingIconColor = BrandGreen,
        unfocusedLeadingIconColor = TextSecondary,
        focusedTrailingIconColor = TextSecondary,
        unfocusedTrailingIconColor = TextSecondary,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = BrandGreen,
        unfocusedBorderColor = Color(0xFFCBD5E1)
    )

    // Observe Authentication States for Navigation routing
    LaunchedEffect(authState) {
        if (authState is AuthUiState.Authenticated) {
            if (isConsumerWorkspace) {
                onConsumerLoginSuccess()
            } else {
                onFarmerAdminLoginSuccess()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AquaBackground)
    ) {
        // Top Brand Header Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f)
                .background(BrandGreen, RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Waves,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "AquaSense Portal",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isConsumerWorkspace) "Consumer Ecosystem" else "Management Workspace",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }

        // Main Interaction Form Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.78f)
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Workspace Tab Switcher (Consumer vs Management Hub)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (isConsumerWorkspace) BrandGreen else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                isConsumerWorkspace = true
                                isSignUpMode = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Consumer",
                            color = if (isConsumerWorkspace) Color.White else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (!isConsumerWorkspace) BrandGreen else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                isConsumerWorkspace = false
                                isSignUpMode = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Management",
                            color = if (!isConsumerWorkspace) Color.White else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Mode Title Indicator (Sign In vs Sign Up)
                Text(
                    text = if (isSignUpMode) "Create Account" else "Welcome Back",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Management Role Sub-Selector (Only visible in Management Workspace)
                if (!isConsumerWorkspace) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf("Farmer", "Admin").forEach { role ->
                            val isSelected = selectedManagementRole == role
                            Button(
                                onClick = { selectedManagementRole = role },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) BrandGreen.copy(alpha = 0.15f) else Color(0xFFF8FAFC),
                                    contentColor = if (isSelected) BrandGreen else TextSecondary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, BrandGreen) else null
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (role == "Farmer") Icons.Default.Agriculture else Icons.Default.Engineering,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(role, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Email/Username Input
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Email/Username") },
                    textStyle = TextStyle(color = TextPrimary, fontSize = 16.sp),
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    colors = inputFieldColors,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password Input
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    textStyle = TextStyle(color = TextPrimary, fontSize = 16.sp),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    colors = inputFieldColors,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Extra Sign Up Fields (Visible only when Sign Up mode is active)
                AnimatedVisibility(
                    visible = isSignUpMode,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            textStyle = TextStyle(color = TextPrimary, fontSize = 16.sp),
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            colors = inputFieldColors,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Phone Number") },
                            textStyle = TextStyle(color = TextPrimary, fontSize = 16.sp),
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            colors = inputFieldColors,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Error State Display
                if (authState is AuthUiState.Error) {
                    Text(
                        text = (authState as AuthUiState.Error).message,
                        color = Color.Red,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 12.dp),
                        fontWeight = FontWeight.Medium
                    )
                }

                // Primary Execution Button
                Button(
                    onClick = {
                        if (isSignUpMode) {
                            if (isConsumerWorkspace) {
                                viewModel.registerConsumerAccount(username, password, fullName, phoneNumber)
                            } else {
                                viewModel.registerManagementAccount(username, password, fullName, phoneNumber, selectedManagementRole)
                            }
                        } else {
                            viewModel.loginUser(username, password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    shape = RoundedCornerShape(12.dp),
                    enabled = authState !is AuthUiState.Loading
                ) {
                    if (authState is AuthUiState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = if (isSignUpMode) "Register Account" else "Authenticate",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Alternative Option Toggle
                Row(
                    modifier = Modifier.clickable {
                        isSignUpMode = !isSignUpMode
                    },
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isSignUpMode) "Already have an account? " else "Need a new profile? ",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (isSignUpMode) "Sign In" else "Sign Up Now",
                        color = BrandGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}