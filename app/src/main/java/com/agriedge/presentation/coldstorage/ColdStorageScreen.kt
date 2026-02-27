package com.agriedge.presentation.coldstorage

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.agriedge.domain.model.ColdStorageFacility

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColdStorageScreen(
    navController: NavController,
    viewModel: ColdStorageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchRadius by remember { mutableStateOf("50") }
    var requiredCapacity by remember { mutableStateOf("10") }
    var showSearchDialog by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cold Storage") },
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
                            viewModel.searchFacilities(
                                searchRadius.toIntOrNull() ?: 50,
                                requiredCapacity.toDoubleOrNull() ?: 10.0
                            )
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.facilities.isEmpty() && !showSearchDialog -> {
                    EmptyState(modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.facilities) { facility ->
                            FacilityCard(
                                facility = facility,
                                onClick = {
                                    navController.navigate("cold_storage/${facility.id}")
                                }
                            )
                        }
                    }
                }
            }

            // Floating search button
            if (!showSearchDialog && uiState.facilities.isNotEmpty()) {
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
        SearchDialog(
            radius = searchRadius,
            capacity = requiredCapacity,
            onRadiusChange = { searchRadius = it },
            onCapacityChange = { requiredCapacity = it },
            onDismiss = { showSearchDialog = false },
            onSearch = {
                viewModel.searchFacilities(
                    searchRadius.toIntOrNull() ?: 50,
                    requiredCapacity.toDoubleOrNull() ?: 10.0
                )
                showSearchDialog = false
            }
        )
    }
}

@Composable
fun FacilityCard(
    facility: ColdStorageFacility,
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
                        text = facility.facilityName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = facility.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (facility.rating != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", facility.rating),
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
                    text = "${facility.distance} km"
                )
                InfoChip(
                    icon = Icons.Default.AcUnit,
                    text = "${facility.availableCapacity}/${facility.totalCapacity} tons"
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${facility.dailyRate} ${facility.currency}/day",
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

@Composable
fun SearchDialog(
    radius: String,
    capacity: String,
    onRadiusChange: (String) -> Unit,
    onCapacityChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSearch: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Search Cold Storage") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = radius,
                    onValueChange = onRadiusChange,
                    label = { Text("Search Radius (km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = capacity,
                    onValueChange = onCapacityChange,
                    label = { Text("Required Capacity (tons)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
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
            text = "No facilities found",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Try adjusting your search criteria",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
