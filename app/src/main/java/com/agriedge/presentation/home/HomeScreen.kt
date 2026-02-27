package com.agriedge.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToDiagnose: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToMarket: () -> Unit,
    onOpenDrawer: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AgriEdge Link") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            com.agriedge.presentation.components.VoiceButton(
                onVoiceResult = { result ->
                    // Handle voice command
                    when {
                        result.contains("diagnose", ignoreCase = true) || 
                        result.contains("निदान", ignoreCase = true) -> onNavigateToDiagnose()
                        result.contains("history", ignoreCase = true) || 
                        result.contains("इतिहास", ignoreCase = true) -> onNavigateToHistory()
                        result.contains("market", ignoreCase = true) || 
                        result.contains("बाजार", ignoreCase = true) -> onNavigateToMarket()
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App title and description
            Text(
                text = "Welcome to AgriEdge Link",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "AI-powered crop disease diagnosis and market connectivity",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Main action cards
            HomeActionCard(
                title = "Diagnose Disease",
                description = "Capture a leaf photo to identify crop diseases",
                icon = Icons.Default.Camera,
                onClick = onNavigateToDiagnose
            )
            
            HomeActionCard(
                title = "View History",
                description = "See your past diagnoses and treatments",
                icon = Icons.Default.History,
                onClick = onNavigateToHistory
            )
            
            HomeActionCard(
                title = "Market",
                description = "Connect with buyers and find services",
                icon = Icons.Default.Store,
                onClick = onNavigateToMarket
            )
        }
    }
}

@Composable
private fun HomeActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        enabled = enabled,
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = if (enabled) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (enabled) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (enabled) description else "Coming soon",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
