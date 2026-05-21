package com.example.fishapp.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fishapp.ui.screens.*
import com.example.fishapp.viewmodel.FishViewModel

// ─── Route Constants ──────────────────────────────────────────────────────────
sealed class Screen(val route: String) {
    object LoginSelection    : Screen("login_selection")
    object ConsumerHome      : Screen("consumer_home")
    object RoleSelection     : Screen("role_selection")
    object FarmerHub         : Screen("farmer_hub")
    object FarmerDashboard   : Screen("farmer_dashboard")
    object CameraScanner     : Screen("camera_scanner")
    object DiseaseReport     : Screen("disease_report")
    object AddPond           : Screen("add_pond")
    object AdminDashboard    : Screen("admin_dashboard")
}

@Composable
fun AquaSenseNavGraph(
    navController: NavHostController = rememberNavController(),
    // Instantiate a single shared instance of the ViewModel for the entire graph
    viewModel: FishViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.LoginSelection.route
    ) {
        // 1. Initial Entry Portal with Integrated Form Handshaking
        composable(Screen.LoginSelection.route) {
            LoginSelectionScreen(
                viewModel = viewModel,
                onConsumerLoginSuccess = {
                    navController.navigate(Screen.ConsumerHome.route) {
                        popUpTo(Screen.LoginSelection.route) { inclusive = true }
                    }
                },
                onFarmerAdminLoginSuccess = {
                    // Routes to Role Selection so they can split between Farmer or Admin workflows
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(Screen.LoginSelection.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. Consumer Fish Scanner Home (Passes shared state instance for image uploads)
        composable(Screen.ConsumerHome.route) {
            ConsumerHomeScreen(
                navController = navController,
                viewModel = viewModel,
                onBack = {
                    viewModel.logoutUser() // Clear token session on back exit
                    navController.popBackStack()
                }
            )
        }

        // 3. Farmer/Admin Role Selection
        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(
                onBack = {
                    viewModel.logoutUser() // Clear token session on back exit
                    navController.popBackStack()
                },
                onFarmerClick = {
                    navController.navigate(Screen.FarmerHub.route)
                },
                onAdminClick = {
                    navController.navigate(Screen.AdminDashboard.route)
                }
            )
        }

        // 4. Farmer Service Hub
        composable(Screen.FarmerHub.route) {
            FarmerHubScreen(
                onManagePonds = { navController.navigate(Screen.FarmerDashboard.route) },
                onReportDisease = { navController.navigate(Screen.DiseaseReport.route) },
                onBack = { navController.popBackStack() }
            )
        }

        // 5. Farmer Pond Management
        composable(Screen.FarmerDashboard.route) {
            FarmerDashboard(
                navController = navController,
                viewModel = viewModel, // Passed seamlessly to link live pond states
                onBack = { navController.popBackStack() }
            )
        }

        // 6. Disease Reporting Form
        composable(Screen.DiseaseReport.route) {
            DiseaseReportScreen(
                navController = navController,
                // Parameter temporarily removed until we open DiseaseReportScreen.kt next
                onBack = { navController.popBackStack() }
            )
        }

        // 7. Add New Pond Form
        composable(Screen.AddPond.route) {
            AddPondScreen(onBack = { navController.popBackStack() })
        }

        // 8. Admin/Expert Control Center (FIXED: Explicitly named parameters match Step 4 completely)
        composable(Screen.AdminDashboard.route) {
            AdminDashboard(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // 9. Shared Camera Scanner
        composable(Screen.CameraScanner.route) {
            CameraScannerScreen(onClose = { navController.popBackStack() })
        }
    }
}