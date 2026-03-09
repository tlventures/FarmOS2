package com.agriedge.presentation.diagnosis

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agriedge.link.R
import com.agriedge.domain.usecase.DiagnosisStage

/**
 * Screen for uploading or capturing images for crop disease diagnosis.
 * Replaces the old CropSelectionScreen — crop type is now auto-detected.
 *
 * Users can:
 * - Take a photo with the camera
 * - Upload 1-3 images from gallery
 * - View selected image thumbnails
 * - Start the two-stage diagnosis pipeline
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageInputScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onDiagnosisComplete: (String) -> Unit,
    viewModel: ImageInputViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedImages by viewModel.selectedImages.collectAsStateWithLifecycle()

    // Gallery photo picker (up to 3 images)
    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = ImageInputViewModel.MAX_IMAGES)
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.onGalleryImagesSelected(uris)
        }
    }

    // Handle navigation on diagnosis completion
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ImageInputUiState.DiagnosisComplete -> {
                onDiagnosisComplete(state.diagnosisId)
                viewModel.resetState()
            }
            is ImageInputUiState.FurtherAnalysisNeeded -> {
                // Still navigate to result — the result screen will show "further analysis" messaging
                onDiagnosisComplete(state.diagnosisId)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.image_input_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = stringResource(R.string.image_input_header),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.image_input_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Selected images row
            if (selectedImages.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.images_selected, selectedImages.size),
                    style = MaterialTheme.typography.labelLarge
                )
                SelectedImagesRow(
                    images = selectedImages,
                    onRemoveImage = { index -> viewModel.removeImage(index) }
                )
            }

            // Action buttons: Camera + Gallery
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.take_photo),
                    icon = Icons.Default.CameraAlt,
                    enabled = selectedImages.size < ImageInputViewModel.MAX_IMAGES &&
                            uiState !is ImageInputUiState.Processing,
                    onClick = onNavigateToCamera
                )
                ActionCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.upload_from_gallery),
                    icon = Icons.Default.PhotoLibrary,
                    enabled = selectedImages.size < ImageInputViewModel.MAX_IMAGES &&
                            uiState !is ImageInputUiState.Processing,
                    onClick = {
                        multiplePhotoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
            }

            // Processing / Error / NotAgriculture states
            when (val state = uiState) {
                is ImageInputUiState.Processing -> {
                    ProcessingCard(stage = state.stage)
                }
                is ImageInputUiState.NotAgriculture -> {
                    NotAgricultureCard(onDismiss = { viewModel.resetState() })
                }
                is ImageInputUiState.Error -> {
                    ErrorCard(
                        message = state.message,
                        onDismiss = { viewModel.resetState() }
                    )
                }
                else -> {}
            }

            Spacer(modifier = Modifier.weight(1f))

            // Diagnose button
            Button(
                onClick = { viewModel.startDiagnosis() },
                enabled = selectedImages.isNotEmpty() && uiState !is ImageInputUiState.Processing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.start_diagnosis),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun SelectedImagesRow(
    images: List<SelectedImage>,
    onRemoveImage: (Int) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        itemsIndexed(images) { index, image ->
            ImageThumbnail(
                bitmap = image.bitmap,
                source = image.source,
                onRemove = { onRemoveImage(index) }
            )
        }
    }
}

@Composable
private fun ImageThumbnail(
    bitmap: Bitmap,
    source: ImageSource,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Source badge
        val sourceIcon = when (source) {
            ImageSource.CAMERA -> Icons.Default.CameraAlt
            ImageSource.GALLERY -> Icons.Default.PhotoLibrary
        }
        Icon(
            imageVector = sourceIcon,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(4.dp)
                .size(20.dp)
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    CircleShape
                )
                .padding(2.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        // Remove button
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.remove_image),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        CircleShape
                    )
                    .padding(2.dp)
                    .size(18.dp)
            )
        }
    }
}

@Composable
private fun ActionCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.height(140.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (enabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProcessingCard(stage: DiagnosisStage) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = when (stage) {
                    DiagnosisStage.VALIDATING -> stringResource(R.string.stage_validating)
                    DiagnosisStage.RECOGNIZING -> stringResource(R.string.stage_recognizing)
                    DiagnosisStage.CLASSIFYING -> stringResource(R.string.stage_classifying)
                    DiagnosisStage.SENDING_TO_BACKEND -> stringResource(R.string.stage_backend)
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NotAgricultureCard(onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.not_agriculture_error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.ok))
            }
        }
    }
}

@Composable
private fun ErrorCard(message: String, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.ok))
            }
        }
    }
}
