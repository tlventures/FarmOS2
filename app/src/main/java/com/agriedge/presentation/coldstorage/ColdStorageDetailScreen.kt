package com.agriedge.presentation.coldstorage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColdStorageDetailScreen(
    facilityId: String,
    navController: NavController,
    viewModel: ColdStorageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showBookingDialog by remember { mutableStateOf(false) }

    LaunchedEffect(facilityId) {
        viewModel.loadFacilityDetails(facilityId)
    }

    LaunchedEffect(uiState.bookingSuccess) {
        if (uiState.bookingSuccess != null) {
            viewModel.clearBookingSuccess()
            navController.navigateUp()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Facility Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorMessage(
                        message = uiState.error!!,
                        onRetry = { viewModel.loadFacilityDetails(facilityId) }
                    )
                }
            }
            uiState.selectedFacility != null -> {
                val facility = uiState.selectedFacility!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = facility.facilityName,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = facility.location,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            if (facility.rating != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${facility.rating} (${facility.reviewCount} reviews)",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Details
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DetailSection(
                            title = "Pricing",
                            content = {
                                Text(
                                    text = "${facility.dailyRate} ${facility.currency}/day",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        )

                        DetailSection(
                            title = "Capacity",
                            content = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Available", style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            "${facility.availableCapacity} tons",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Column {
                                        Text("Total", style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            "${facility.totalCapacity} tons",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        )

                        DetailSection(
                            title = "Temperature Range",
                            content = {
                                Text(
                                    text = facility.temperatureRange,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        )

                        DetailSection(
                            title = "Operating Hours",
                            content = {
                                Text(
                                    text = facility.operatingHours,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        )

                        DetailSection(
                            title = "Features",
                            content = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    facility.features.forEach { feature ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(feature, style = MaterialTheme.typography.bodyLarge)
                                        }
                                    }
                                }
                            }
                        )

                        DetailSection(
                            title = "Contact",
                            content = {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = facility.address,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = facility.contact,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        )

                        Button(
                            onClick = { showBookingDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Book Now")
                        }
                    }
                }
            }
        }
    }

    if (showBookingDialog) {
        BookingDialog(
            facilityName = uiState.selectedFacility?.facilityName ?: "",
            onDismiss = { showBookingDialog = false },
            onConfirm = { capacity, duration ->
                viewModel.bookFacility(facilityId, capacity, duration)
                showBookingDialog = false
            }
        )
    }
}

@Composable
fun DetailSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun BookingDialog(
    facilityName: String,
    onDismiss: () -> Unit,
    onConfirm: (Double, Int) -> Unit
) {
    var capacity by remember { mutableStateOf("10") }
    var duration by remember { mutableStateOf("7") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Book $facilityName") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = capacity,
                    onValueChange = { capacity = it },
                    label = { Text("Capacity (tons)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration (days)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cap = capacity.toDoubleOrNull() ?: 10.0
                    val dur = duration.toIntOrNull() ?: 7
                    onConfirm(cap, dur)
                }
            ) {
                Text("Confirm Booking")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
