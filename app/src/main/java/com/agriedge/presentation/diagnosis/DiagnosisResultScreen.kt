package com.agriedge.presentation.diagnosis

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.agriedge.domain.model.Diagnosis
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosisResultScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTreatment: (String) -> Unit,
    viewModel: DiagnosisResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diagnosis Result") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is DiagnosisResultUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is DiagnosisResultUiState.Success -> {
                DiagnosisResultContent(
                    diagnosis = state.diagnosis,
                    onNavigateToTreatment = onNavigateToTreatment,
                    modifier = Modifier.padding(padding)
                )
            }
            is DiagnosisResultUiState.Error -> {
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
private fun DiagnosisResultContent(
    diagnosis: Diagnosis,
    onNavigateToTreatment: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Image
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            AsyncImage(
                model = diagnosis.imagePath,
                contentDescription = "Diagnosed leaf",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Disease name
        Text(
            text = diagnosis.disease.commonName,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = diagnosis.disease.scientificName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Confidence score
        ConfidenceScoreCard(
            confidence = diagnosis.confidence
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Low confidence warning
        if (diagnosis.confidence < 0.70f) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Low confidence. Consider consulting an expert for confirmation.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Diagnosis info
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                InfoRow("Crop Type", diagnosis.cropType.name)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                InfoRow("Date", formatDate(diagnosis.timestamp))
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                InfoRow("Location", diagnosis.location?.let { "${it.village}, ${it.district}" } ?: "Not available")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action buttons
        Button(
            onClick = { onNavigateToTreatment(diagnosis.disease.id) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Treatment Recommendations")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = { /* TODO: Implement voice readout */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.VolumeUp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Read Aloud")
        }
    }
}

@Composable
private fun ConfidenceScoreCard(
    confidence: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (confidence >= 0.70f) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Confidence Score",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${(confidence * 100).toInt()}%",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
