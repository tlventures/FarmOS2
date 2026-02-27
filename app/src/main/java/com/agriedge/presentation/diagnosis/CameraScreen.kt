package com.agriedge.presentation.diagnosis

import android.graphics.Bitmap
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agriedge.data.camera.CameraManager
import com.agriedge.domain.model.CropType
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onNavigateBack: () -> Unit,
    onDiagnosisComplete: (String) -> Unit,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedCropType by viewModel.selectedCropType.collectAsStateWithLifecycle()
    val guidanceMessage by viewModel.guidanceMessage.collectAsStateWithLifecycle()
    
    var cameraManager by remember { mutableStateOf<CameraManager?>(null) }
    
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is CameraUiState.Success -> {
                onDiagnosisComplete(state.diagnosisId)
                viewModel.resetState()
            }
            else -> {}
        }
    }
    
    if (!cameraPermissionState.status.isGranted) {
        LaunchedEffect(Unit) {
            cameraPermissionState.launchPermissionRequest()
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Camera permission is required")
        }
        return
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { previewView ->
                    cameraManager = CameraManager(ctx, lifecycleOwner).apply {
                        kotlinx.coroutines.MainScope().launch {
                            startCamera(
                                previewView = previewView,
                                onGuidanceUpdate = { message ->
                                    viewModel.updateGuidanceMessage(message)
                                }
                            )
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Top bar with close button and crop selector
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
                
                Text(
                    text = "Diagnose Disease",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.width(48.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Crop type selector
            CropTypeSelector(
                selectedCropType = selectedCropType,
                onCropTypeSelected = { viewModel.selectCropType(it) }
            )
        }
        
        // Guidance message
        guidanceMessage?.let { message ->
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.7f)
                )
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        // Capture button
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (uiState) {
                is CameraUiState.Processing -> {
                    CircularProgressIndicator(color = Color.White)
                }
                is CameraUiState.Error -> {
                    Text(
                        text = (uiState as CameraUiState.Error).message,
                        color = Color.Red,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    CaptureButton(
                        onClick = {
                            cameraManager?.captureImage(
                                onImageCaptured = { bitmap ->
                                    viewModel.captureAndDiagnose(bitmap)
                                },
                                onError = { exception ->
                                    viewModel.resetState()
                                    // TODO: Show error message
                                }
                            )
                        }
                    )
                }
                else -> {
                    CaptureButton(
                        onClick = {
                            cameraManager?.captureImage(
                                onImageCaptured = { bitmap ->
                                    viewModel.captureAndDiagnose(bitmap)
                                },
                                onError = { exception ->
                                    viewModel.resetState()
                                    // TODO: Show error message
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CropTypeSelector(
    selectedCropType: CropType,
    onCropTypeSelected: (CropType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedCropType.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Crop Type", color = Color.White) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.7f)
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            CropType.values().forEach { cropType ->
                DropdownMenuItem(
                    text = { Text(cropType.name) },
                    onClick = {
                        onCropTypeSelected(cropType)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun CaptureButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.size(72.dp),
        shape = CircleShape,
        containerColor = Color.White
    ) {
        Icon(
            imageVector = Icons.Default.Camera,
            contentDescription = "Capture",
            modifier = Modifier.size(36.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
