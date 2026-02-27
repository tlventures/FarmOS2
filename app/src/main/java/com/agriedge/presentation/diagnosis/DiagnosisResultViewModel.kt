package com.agriedge.presentation.diagnosis

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriedge.domain.model.Diagnosis
import com.agriedge.domain.repository.DiagnosisRepository
import com.agriedge.domain.usecase.GetTreatmentRecommendationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiagnosisResultViewModel @Inject constructor(
    private val diagnosisRepository: DiagnosisRepository,
    private val getTreatmentUseCase: GetTreatmentRecommendationUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val diagnosisId: String = checkNotNull(savedStateHandle["diagnosisId"])

    private val _uiState = MutableStateFlow<DiagnosisResultUiState>(DiagnosisResultUiState.Loading)
    val uiState: StateFlow<DiagnosisResultUiState> = _uiState.asStateFlow()

    init {
        loadDiagnosis()
    }

    private fun loadDiagnosis() {
        viewModelScope.launch {
            try {
                val diagnosis = diagnosisRepository.getDiagnosisById(diagnosisId)
                if (diagnosis != null) {
                    _uiState.value = DiagnosisResultUiState.Success(diagnosis)
                } else {
                    _uiState.value = DiagnosisResultUiState.Error("Diagnosis not found")
                }
            } catch (e: Exception) {
                _uiState.value = DiagnosisResultUiState.Error(
                    e.message ?: "Failed to load diagnosis"
                )
            }
        }
    }

    fun retry() {
        loadDiagnosis()
    }
}

sealed class DiagnosisResultUiState {
    object Loading : DiagnosisResultUiState()
    data class Success(val diagnosis: Diagnosis) : DiagnosisResultUiState()
    data class Error(val message: String) : DiagnosisResultUiState()
}
