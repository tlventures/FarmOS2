package com.agriedge.presentation.diagnosis

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriedge.domain.usecase.DiagnoseDiseaseUseCase
import com.agriedge.domain.usecase.DiagnosisStage
import com.agriedge.domain.usecase.ImageQualityException
import com.agriedge.domain.usecase.NotAgricultureImageException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel for the ImageInput screen.
 * Manages multi-image selection (camera + gallery) and orchestrates the
 * two-stage diagnosis pipeline.
 */
@HiltViewModel
class ImageInputViewModel @Inject constructor(
    private val diagnoseDiseaseUseCase: DiagnoseDiseaseUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<ImageInputUiState>(ImageInputUiState.Idle)
    val uiState: StateFlow<ImageInputUiState> = _uiState.asStateFlow()

    private val _selectedImages = MutableStateFlow<List<SelectedImage>>(emptyList())
    val selectedImages: StateFlow<List<SelectedImage>> = _selectedImages.asStateFlow()

    // TODO: Get userId from authentication service
    private val userId = "demo_user"

    companion object {
        const val MAX_IMAGES = 3
    }

    /**
     * Called when an image is captured from the camera and saved to a temp path.
     */
    fun onCameraImageCaptured(imagePath: String) {
        if (_selectedImages.value.size >= MAX_IMAGES) return
        viewModelScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                BitmapFactory.decodeFile(imagePath)
            }
            if (bitmap != null) {
                _selectedImages.value = _selectedImages.value + SelectedImage(
                    bitmap = bitmap,
                    uri = null,
                    source = ImageSource.CAMERA
                )
            }
        }
    }

    /**
     * Called when images are selected from the gallery picker.
     */
    fun onGalleryImagesSelected(uris: List<Uri>) {
        viewModelScope.launch {
            val remaining = MAX_IMAGES - _selectedImages.value.size
            val toAdd = uris.take(remaining).mapNotNull { uri ->
                loadBitmapFromUri(uri)?.let { bitmap ->
                    SelectedImage(bitmap = bitmap, uri = uri, source = ImageSource.GALLERY)
                }
            }
            _selectedImages.value = _selectedImages.value + toAdd
        }
    }

    /**
     * Remove an image from the selected list.
     */
    fun removeImage(index: Int) {
        _selectedImages.value = _selectedImages.value.toMutableList().apply {
            if (index in indices) removeAt(index)
        }
    }

    /**
     * Start the two-stage diagnosis pipeline on the selected images.
     */
    fun startDiagnosis() {
        val images = _selectedImages.value
        if (images.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = ImageInputUiState.Processing(DiagnosisStage.VALIDATING)

            val stageCallback = object : DiagnoseDiseaseUseCase.StageCallback {
                override fun onStageChanged(stage: DiagnosisStage) {
                    _uiState.value = ImageInputUiState.Processing(stage)
                }
            }

            val bitmaps = images.map { it.bitmap }
            val result = diagnoseDiseaseUseCase(bitmaps, userId, stageCallback = stageCallback)

            result.fold(
                onSuccess = { diagnosis ->
                    if (diagnosis.requiresFurtherAnalysis) {
                        _uiState.value = ImageInputUiState.FurtherAnalysisNeeded(diagnosis.id)
                    } else {
                        _uiState.value = ImageInputUiState.DiagnosisComplete(diagnosis.id)
                    }
                },
                onFailure = { error ->
                    when (error) {
                        is NotAgricultureImageException -> {
                            _uiState.value = ImageInputUiState.NotAgriculture
                        }
                        is ImageQualityException -> {
                            _uiState.value = ImageInputUiState.Error(
                                error.reasons.joinToString("\n")
                            )
                        }
                        else -> {
                            _uiState.value = ImageInputUiState.Error(
                                error.message ?: "Failed to diagnose"
                            )
                        }
                    }
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = ImageInputUiState.Idle
    }

    fun clearImages() {
        _selectedImages.value = emptyList()
        _uiState.value = ImageInputUiState.Idle
    }

    private suspend fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}

data class SelectedImage(
    val bitmap: Bitmap,
    val uri: Uri?,
    val source: ImageSource
)

enum class ImageSource { CAMERA, GALLERY }

sealed class ImageInputUiState {
    object Idle : ImageInputUiState()
    data class Processing(val stage: DiagnosisStage) : ImageInputUiState()
    data class DiagnosisComplete(val diagnosisId: String) : ImageInputUiState()
    data class FurtherAnalysisNeeded(val diagnosisId: String) : ImageInputUiState()
    object NotAgriculture : ImageInputUiState()
    data class Error(val message: String) : ImageInputUiState()
}
