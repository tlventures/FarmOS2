package com.agriedge.presentation.equipment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentDetailScreen(
    equipmentId: String,
    navController: NavController,
    viewModel: EquipmentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showBookingDialog by remember { mutableStateOf(false) }

    LaunchedEffect(equipmentId) {
        viewModel.loadEquipmentDetails(equipmentId)
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
                title = { Text("Equipment Details") },
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
                        onRetry = { viewModel.loadEquipmentDetails(equipmentId) }
                    )
                }
            }
            uiState.selectedEquipment != null -> {
                val equipment = uiState.selectedEquipment!!
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
                                text = equipment.equipmentType.displayName(),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = equipment.model,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = equipment.providerName,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (equipment.rating != null) {
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
                                        text = "${equipment.rating} (${equipment.reviewCount} reviews)",
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
                                Column {
                                    Text(
                                        text = "${equipment.dailyRate} ${equipment.currency}/day",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (equipment.deliveryAvailable && equipment.deliveryCharge != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Delivery: ${equipment.deliveryCharge} ${equipment.currency}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        )

                        DetailSection(
                            title = "Specifications",
                            content = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    equipment.specifications.horsepower?.let {
                                        SpecRow("Horsepower", "$it HP")
                                    }
                                    equipment.specifications.capacity?.let {
                                        SpecRow("Capacity", it)
                                    }
                                    equipment.specifications.age?.let {
                                        SpecRow("Age", "$it years")
                                    }
                                    equipment.specifications.fuelType?.let {
                                        SpecRow("Fuel Type", it)
                                    }
                                    equipment.specifications.condition?.let {
                                        SpecRow("Condition", it)
                                    }
                                }
                            }
                        )

                        DetailSection(
                            title = "Location & Delivery",
                            content = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Location", style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            equipment.location,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Distance", style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            "${equipment.distance} km",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Delivery", style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            if (equipment.deliveryAvailable) "Available" else "Not Available",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (equipment.deliveryAvailable)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        )

                        DetailSection(
                            title = "Contact",
                            content = {
                                Text(
                                    text = equipment.contact,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
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
        EquipmentBookingDialog(
            equipmentName = "${uiState.selectedEquipment?.equipmentType?.displayName()} - ${uiState.selectedEquipment?.model}",
            deliveryAvailable = uiState.selectedEquipment?.deliveryAvailable ?: false,
            onDismiss = { showBookingDialog = false },
            onConfirm = { duration, deliveryRequired ->
                val startDate = System.currentTimeMillis()
                val endDate = startDate + (duration * 24 * 60 * 60 * 1000L)
                viewModel.bookEquipment(equipmentId, startDate, endDate, deliveryRequired)
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
fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EquipmentBookingDialog(
    equipmentName: String,
    deliveryAvailable: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Int, Boolean) -> Unit
) {
    var duration by remember { mutableStateOf("7") }
    var deliveryRequired by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Book Equipment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = equipmentName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Rental Duration (days)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (deliveryAvailable) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Delivery Required")
                        Switch(
                            checked = deliveryRequired,
                            onCheckedChange = { deliveryRequired = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dur = duration.toIntOrNull() ?: 7
                    onConfirm(dur, deliveryRequired)
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
