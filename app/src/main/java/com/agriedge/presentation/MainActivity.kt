package com.agriedge.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.agriedge.presentation.auth.AuthViewModel
import com.agriedge.presentation.components.AppDrawer
import com.agriedge.presentation.navigation.NavGraph
import com.agriedge.presentation.navigation.Screen
import com.agriedge.presentation.theme.AgriEdgeLinkTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AgriEdgeLinkTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    
    val authState by authViewModel.uiState.collectAsState()
    val startDestination = if (authState.isAuthenticated) Screen.Home.route else Screen.Login.route
    
    // Hide drawer on login/register screens
    val showDrawer = currentRoute != Screen.Login.route && currentRoute != Screen.Register.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            if (showDrawer) {
                AppDrawer(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // Pop up to the start destination to avoid building up a large stack
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    },
                    onCloseDrawer = {
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        },
        gesturesEnabled = showDrawer
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavGraph(
                navController = navController,
                startDestination = startDestination,
                onOpenDrawer = {
                    if (showDrawer) {
                        scope.launch {
                            drawerState.open()
                        }
                    }
                }
            )
        }
    }
}
