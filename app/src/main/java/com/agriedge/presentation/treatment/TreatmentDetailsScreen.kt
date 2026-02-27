package com.agriedge.presentation.treatment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agriedge.domain.model.Treatment
import com.agriedge.domain.model.TreatmentOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreatmentDetailsScreen(
    onNavigateBack: () -> Unit,
    viewModel: TreatmentDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Treatment Recommendations") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Implement voice readout */ }) {
                        Icon(Icons.Default.VolumeUp, "Read Aloud")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is TreatmentDetailsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is TreatmentDetailsUiState.Success -> {
                TreatmentContent(
                    treatment = state.treatment,
                    modifier = Modifier.padding(padding)
                )
            }
            is TreatmentDetailsUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.retry() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TreatmentContent(
    treatment: Treatment,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Treatment information header
        Text(
            text = "Treatment Recommendations",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Organic treatments
        if (treatment.organicOptions.isNotEmpty()) {
            TreatmentSection(
                title = "Organic Treatment Options",
                options = treatment.organicOptions,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Chemical treatments
        if (treatment.chemicalOptions.isNotEmpty()) {
            TreatmentSection(
                title = "Chemical Treatment Options",
                options = treatment.chemicalOptions,
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Preventive measures
        if (treatment.preventiveMeasures.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Preventive Measures",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    treatment.preventiveMeasures.forEach { measure ->
                        Text(
                            text = "• $measure",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TreatmentSection(
    title: String,
    options: List<TreatmentOption>,
    containerColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            options.forEachIndexed { index, option ->
                if (index > 0) {
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                }
                TreatmentOptionItem(option)
            }
        }
    }
}

@Composable
private fun TreatmentOptionItem(option: TreatmentOption) {
    Column {
        Text(
            text = option.localizedName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (option.description.isNotBlank()) {
            Text(
                text = option.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        if (option.dosage.isNotBlank()) {
            DetailRow(label = "Dosage", value = option.dosage)
        }
        
        if (option.applicationTiming.isNotBlank()) {
            DetailRow(label = "Timing", value = option.applicationTiming)
        }
        
        // Display products if available
        if (option.products.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Products:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            option.products.forEach { product ->
                Text(
                    text = "• ${product.localizedName}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}
