package com.agriedge.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agriedge.presentation.auth.AuthViewModel

data class DrawerMenuItem(
    val icon: ImageVector,
    val label: String,
    val route: String? = null,
    val action: (() -> Unit)? = null
)

@Composable
fun AppDrawer(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onCloseDrawer: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    val currentUser = authState.currentUser
    
    val menuItems = listOf(
        DrawerMenuItem(Icons.Default.Home, "Home", "home"),
        DrawerMenuItem(Icons.Default.CameraAlt, "Diagnose", "crop_selection"),
        DrawerMenuItem(Icons.Default.History, "History", "diagnosis_history"),
        DrawerMenuItem(Icons.Default.ShoppingCart, "Market", "market"),
        DrawerMenuItem(Icons.Default.Person, "Profile", "profile"),
        DrawerMenuItem(Icons.Default.Settings, "Settings", "settings"),
        DrawerMenuItem(Icons.Default.Notifications, "Notifications", null)
    )

    ModalDrawerSheet(modifier = modifier) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(24.dp)
        ) {
            Column {
                Icon(
                    Icons.Default.Agriculture,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "AgriEdge Link",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = currentUser?.name ?: "Guest User",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Menu Items
        menuItems.forEach { item ->
            NavigationDrawerItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    if (item.route != null) {
                        onNavigate(item.route)
                        onCloseDrawer()
                    } else {
                        // Placeholder for features not yet implemented
                        onCloseDrawer()
                    }
                },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Logout
        Divider(modifier = Modifier.padding(horizontal = 12.dp))
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Logout, contentDescription = "Logout") },
            label = { Text("Logout") },
            selected = false,
            onClick = {
                authViewModel.logout()
                onLogout()
                onCloseDrawer()
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}
