package com.agriedge.presentation.diagnosis

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriedge.domain.model.CropType
import com.agriedge.domain.usecase.DiagnoseDiseaseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val diagnoseDiseaseUseCase: DiagnoseDiseaseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Ready)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private val _selectedCropType = MutableStateFlow(CropType.RICE)
    val selectedCropType: StateFlow<CropType> = _selectedCropType.asStateFlow()

    private val _guidanceMessage = MutableStateFlow<String?>(null)
    val guidanceMessage: StateFlow<String?> = _guidanceMessage.asStateFlow()

    // TODO: Get userId from authentication service
    private val userId = "demo_user"

    fun selectCropType(cropType: CropType) {
        _selectedCropType.value = cropType
    }

    fun updateGuidanceMessage(message: String?) {
        _guidanceMessage.value = message
    }

    fun captureAndDiagnose(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.value = CameraUiState.Processing
            try {
                val result = diagnoseDiseaseUseCase(bitmap, _selectedCropType.value, userId)
                result.fold(
                    onSuccess = { diagnosis ->
                        _uiState.value = CameraUiState.Success(diagnosis.id)
                    },
                    onFailure = { error ->
                        _uiState.value = CameraUiState.Error(
                            error.message ?: "Failed to diagnose disease"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = CameraUiState.Error(
                    e.message ?: "An unexpected error occurred"
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
