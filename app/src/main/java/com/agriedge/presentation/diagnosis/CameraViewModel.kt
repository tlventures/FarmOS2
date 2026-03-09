package com.agriedge.presentation.diagnosis

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriedge.data.local.storage.ImageStorageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Camera screen.
 * Simplified: only handles image capture and temporary storage.
 * Diagnosis logic has moved to ImageInputViewModel.
 */
@HiltViewModel
class CameraViewModel @Inject constructor(
    private val imageStorageManager: ImageStorageManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Ready)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private val _guidanceMessage = MutableStateFlow<String?>(null)
    val guidanceMessage: StateFlow<String?> = _guidanceMessage.asStateFlow()

    fun updateGuidanceMessage(message: String?) {
        _guidanceMessage.value = message
    }

    /**
     * Save captured bitmap to a temp file and invoke the callback with the file path.
     */
    fun saveAndReturn(bitmap: Bitmap, onSaved: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = CameraUiState.Processing
            try {
                val imageId = "temp_capture_${System.currentTimeMillis()}"
                val result = imageStorageManager.saveImage(bitmap, imageId)
                result.fold(
                    onSuccess = { storageResult ->
                        onSaved(storageResult.fullImagePath)
                        _uiState.value = CameraUiState.Ready
                    },
                    onFailure = {
                        _uiState.value = CameraUiState.Error(
                            it.message ?: "Failed to save image"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = CameraUiState.Error(
                    e.message ?: "Error saving image"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = CameraUiState.Ready
    }
}

sealed class CameraUiState {
    object Ready : CameraUiState()
    object Processing : CameraUiState()
    data class Success(val diagnosisId: String) : CameraUiState()
    data class Error(val message: String) : CameraUiState()
}
