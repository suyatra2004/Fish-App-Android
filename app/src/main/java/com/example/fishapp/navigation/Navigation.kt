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
    object LoginSelection        : Screen("login_selection")
    object ConsumerHome          : Screen("consumer_home")
    object RoleSelection         : Screen("role_selection")
    object FarmerHub             : Screen("farmer_hub")
    object FarmerDashboard       : Screen("farmer_dashboard")
    object CameraScanner         : Screen("camera_scanner")
    object DiseaseReport         : Screen("disease_report")
    object AddPond               : Screen("add_pond")
    object AdminDashboard        : Screen("admin_dashboard")
    object PredictionDetail      : Screen("prediction_detail_screen")
    object PredictionHistoryList : Screen("prediction_history_list")

    // Route targeting individual clicked pond details view
    object PondDetail            : Screen("pond_detail_screen")

    // TASK 1 & 2 ROUTE ACCRETIONS: Portal interfaces supporting history metrics
    object DiseaseMenu           : Screen("disease_menu_screen")
    object DiseaseHistoryList    : Screen("disease_history_list_screen")
    object DiseaseDetail         : Screen("disease_detail_screen")
}

@Composable
fun AquaSenseNavGraph(
    navController: NavHostController = rememberNavController(),
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
                onFarmerAdminLoginSuccess = { selectedRole ->
                    val targetRoute = if (selectedRole.equals("Admin", ignoreCase = true)) {
                        Screen.AdminDashboard.route
                    } else {
                        Screen.FarmerHub.route
                    }

                    navController.navigate(targetRoute) {
                        popUpTo(Screen.LoginSelection.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. Consumer Fish Scanner Home
        composable(Screen.ConsumerHome.route) {
            ConsumerHomeScreen(
                navController = navController,
                viewModel = viewModel,
                onBack = {
                    viewModel.logoutUser()
                    navController.popBackStack()
                }
            )
        }

        // 3. Farmer/Admin Role Selection
        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(
                onBack = {
                    viewModel.logoutUser()
                    navController.popBackStack()
                },
                onFarmerClick = { navController.navigate(Screen.FarmerHub.route) },
                onAdminClick = { navController.navigate(Screen.AdminDashboard.route) }
            )
        }

        // 4. Farmer Service Hub (UPDATED: Triggers Disease Menu route entry path instead of direct form)
        composable(Screen.FarmerHub.route) {
            FarmerHubScreen(
                navController = navController,
                viewModel = viewModel,
                onManagePonds = { navController.navigate(Screen.FarmerDashboard.route) },
                onReportDisease = { navController.navigate(Screen.DiseaseMenu.route) },
                onBack = { navController.popBackStack() }
            )
        }

        // --- NEW ROUTE COMPOSABLE: Task 1 Intermediate Option Menu Picker ---
        composable(Screen.DiseaseMenu.route) {
            DiseaseMenuScreen(
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }

        // --- NEW ROUTE COMPOSABLE: Task 2 Historic Submissions Listing Feed ---
        composable(Screen.DiseaseHistoryList.route) {
            DiseaseHistoryScreen(
                navController = navController,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // --- NEW ROUTE COMPOSABLE: Task 2 Granular Single Incident Report Inspection Scene ---
        composable(Screen.DiseaseDetail.route) {
            DiseaseReportDetailScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // 5. Farmer Pond Management
        composable(Screen.FarmerDashboard.route) {
            FarmerDashboard(
                navController = navController,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // 6. Disease Reporting Form (UPDATED: Added the viewModel pass-down hook)
        composable(Screen.DiseaseReport.route) {
            DiseaseReportScreen(
                navController = navController,
                viewModel = viewModel, // FIXED: Now cleanly injected to power end-to-end APIs
                onBack = { navController.popBackStack() }
            )
        }

        // 7. Add New Pond Form
        composable(Screen.AddPond.route) {
            AddPondScreen(
                onBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

        // 8. Clickable Pond Detail Inspection Sub-View
        composable(Screen.PondDetail.route) {
            PondDetailScreen(
                navController = navController,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // 9. Admin/Expert Control Center
        composable(Screen.AdminDashboard.route) {
            AdminDashboard(
                navController = navController,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // 10. Shared Camera Scanner
        composable(Screen.CameraScanner.route) {
            CameraScannerScreen(
                onClose = { navController.popBackStack() },
                onImageCaptured = { capturedUri ->
                    viewModel.onImageSelected(capturedUri)
                }
            )
        }

        // 11. Detailed Scan Inspection Report Screen
        composable(Screen.PredictionDetail.route) {
            PredictionDetailScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        // 12. Complete History Log View Screen
        composable(Screen.PredictionHistoryList.route) {
            PredictionHistoryListScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}