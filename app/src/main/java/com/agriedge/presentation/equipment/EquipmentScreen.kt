package com.agriedge.presentation.equipment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.agriedge.domain.model.EquipmentRental
import com.agriedge.domain.model.EquipmentType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentScreen(
    navController: NavController,
    viewModel: EquipmentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchRadius by remember { mutableStateOf("50") }
    var selectedType by remember { mutableStateOf<EquipmentType?>(null) }
    var showSearchDialog by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Equipment Rental") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    ErrorMessage(
                        message = uiState.error!!,
                        onRetry = {
                            viewModel.searchEquipment(
                                searchRadius.toIntOrNull() ?: 50,
                                selectedType,
                                System.currentTimeMillis(),
                                System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)
                            )
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.equipment.isEmpty() && !showSearchDialog -> {
                    EmptyState(modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.equipment) { equipment ->
                            EquipmentCard(
                                equipment = equipment,
                                onClick = {
                                    navController.navigate("equipment/${equipment.id}")
                                }
                            )
                        }
                    }
                }
            }

            // Floating search button
            if (!showSearchDialog && uiState.equipment.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showSearchDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Search, "Search")
                }
            }
        }
    }

    if (showSearchDialog) {
        EquipmentSearchDialog(
            radius = searchRadius,
            selectedType = selectedType,
            onRadiusChange = { searchRadius = it },
            onTypeChange = { selectedType = it },
            onDismiss = { showSearchDialog = false },
            onSearch = {
                viewModel.searchEquipment(
                    searchRadius.toIntOrNull() ?: 50,
                    selectedType,
                    System.currentTimeMillis(),
                    System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)
                )
                showSearchDialog = false
            }
        )
    }
}

@Composable
fun EquipmentCard(
    equipment: EquipmentRental,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = equipment.equipmentType.displayName(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = equipment.model,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = equipment.providerName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (equipment.rating != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", equipment.rating),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip(
                    icon = Icons.Default.LocationOn,
                    text = "${equipment.distance} km"
                )
                if (equipment.specifications.horsepower != null) {
                    InfoChip(
                        icon = Icons.Default.Speed,
                        text = "${equipment.specifications.horsepower} HP"
                    )
                }
                if (equipment.deliveryAvailable) {
                    InfoChip(
                        icon = Icons.Default.LocalShipping,
                        text = "Delivery"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${equipment.dailyRate} ${equipment.currency}/day",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentSearchDialog(
    radius: String,
    selectedType: EquipmentType?,
    onRadiusChange: (String) -> Unit,
    onTypeChange: (EquipmentType?) -> Unit,
    onDismiss: () -> Unit,
    onSearch: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Search Equipment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = radius,
                    onValueChange = onRadiusChange,
                    label = { Text("Search Radius (km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedType?.displayName() ?: "All Equipment",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Equipment Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Equipment") },
                            onClick = {
                                onTypeChange(null)
                                expanded = false
                            }
                        )
                        EquipmentType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName()) },
                                onClick = {
                                    onTypeChange(type)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onSearch) {
                Text("Search")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ErrorMessage(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No equipment found",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Try adjusting your search criteria",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
