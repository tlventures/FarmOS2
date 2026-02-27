package com.agriedge.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.agriedge.presentation.auth.LoginScreen
import com.agriedge.presentation.auth.RegisterScreen
import com.agriedge.presentation.coldstorage.ColdStorageDetailScreen
import com.agriedge.presentation.coldstorage.ColdStorageScreen
import com.agriedge.presentation.diagnosis.CameraScreen
import com.agriedge.presentation.diagnosis.CropSelectionScreen
import com.agriedge.presentation.diagnosis.DiagnosisHistoryScreen
import com.agriedge.presentation.diagnosis.DiagnosisResultScreen
import com.agriedge.presentation.equipment.EquipmentDetailScreen
import com.agriedge.presentation.equipment.EquipmentScreen
import com.agriedge.presentation.home.HomeScreen
import com.agriedge.presentation.market.MarketDetailScreen
import com.agriedge.presentation.market.MarketScreen
import com.agriedge.presentation.profile.ProfileScreen
import com.agriedge.presentation.settings.SettingsScreen
import com.agriedge.presentation.treatment.TreatmentDetailsScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object CropSelection : Screen("crop_selection")
    object Camera : Screen("camera")
    object DiagnosisResult : Screen("diagnosis_result/{diagnosisId}") {
        fun createRoute(diagnosisId: String) = "diagnosis_result/$diagnosisId"
    }
    object TreatmentDetails : Screen("treatment_details/{diseaseId}") {
        fun createRoute(diseaseId: String) = "treatment_details/$diseaseId"
    }
    object DiagnosisHistory : Screen("diagnosis_history")
    object Market : Screen("market")
    object MarketDetail : Screen("market/{listingId}") {
        fun createRoute(listingId: String) = "market/$listingId"
    }
    object ColdStorage : Screen("cold_storage")
    object ColdStorageDetail : Screen("cold_storage/{facilityId}") {
        fun createRoute(facilityId: String) = "cold_storage/$facilityId"
    }
    object Equipment : Screen("equipment")
    object EquipmentDetail : Screen("equipment/{equipmentId}") {
        fun createRoute(equipmentId: String) = "equipment/$equipmentId"
    }
    object Settings : Screen("settings")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route,
    onOpenDrawer: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToDiagnose = {
                    navController.navigate(Screen.CropSelection.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.DiagnosisHistory.route)
                },
                onNavigateToMarket = {
                    navController.navigate(Screen.Market.route)
                },
                onOpenDrawer = onOpenDrawer
            )
        }
        
        composable(Screen.CropSelection.route) {
            CropSelectionScreen(navController = navController)
        }
        
        composable(Screen.Camera.route) {
            CameraScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onDiagnosisComplete = { diagnosisId ->
                    navController.navigate(Screen.DiagnosisResult.createRoute(diagnosisId)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }
        
        composable(
            route = Screen.DiagnosisResult.route,
            arguments = listOf(
                navArgument("diagnosisId") { type = NavType.StringType }
            )
        ) {
            DiagnosisResultScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToTreatment = { diseaseId ->
                    navController.navigate(Screen.TreatmentDetails.createRoute(diseaseId))
                }
            )
        }
        
        composable(
            route = Screen.TreatmentDetails.route,
            arguments = listOf(
                navArgument("diseaseId") { type = NavType.StringType }
            )
        ) {
            TreatmentDetailsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.DiagnosisHistory.route) {
            DiagnosisHistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onDiagnosisClick = { diagnosisId ->
                    navController.navigate(Screen.DiagnosisResult.createRoute(diagnosisId))
                }
            )
        }
        
        composable(Screen.Market.route) {
            MarketScreen(navController = navController)
        }
        
        composable(
            route = Screen.MarketDetail.route,
            arguments = listOf(
                navArgument("listingId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getString("listingId") ?: ""
            MarketDetailScreen(
                listingId = listingId,
                navController = navController
            )
        }
        
        composable(Screen.ColdStorage.route) {
            ColdStorageScreen(navController = navController)
        }
        
        composable(
            route = Screen.ColdStorageDetail.route,
            arguments = listOf(
                navArgument("facilityId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val facilityId = backStackEntry.arguments?.getString("facilityId") ?: ""
            ColdStorageDetailScreen(
                facilityId = facilityId,
                navController = navController
            )
        }
        
        composable(Screen.Equipment.route) {
            EquipmentScreen(navController = navController)
        }
        
        composable(
            route = Screen.EquipmentDetail.route,
            arguments = listOf(
                navArgument("equipmentId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val equipmentId = backStackEntry.arguments?.getString("equipmentId") ?: ""
            EquipmentDetailScreen(
                equipmentId = equipmentId,
                navController = navController
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}
