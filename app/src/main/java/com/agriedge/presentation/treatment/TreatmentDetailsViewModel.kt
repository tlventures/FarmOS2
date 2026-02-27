package com.agriedge.presentation.treatment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriedge.domain.model.Treatment
import com.agriedge.domain.usecase.GetTreatmentRecommendationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TreatmentDetailsViewModel @Inject constructor(
    private val getTreatmentUseCase: GetTreatmentRecommendationUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val diseaseId: String = checkNotNull(savedStateHandle["diseaseId"])
    
    // TODO: Get these from the diagnosis or user preferences
    private val cropType = com.agriedge.domain.model.CropType.RICE
    private val languageCode = "en"

    private val _uiState = MutableStateFlow<TreatmentDetailsUiState>(TreatmentDetailsUiState.Loading)
    val uiState: StateFlow<TreatmentDetailsUiState> = _uiState.asStateFlow()

    init {
        loadTreatment()
    }

    private fun loadTreatment() {
        viewModelScope.launch {
            _uiState.value = TreatmentDetailsUiState.Loading
            try {
                val result = getTreatmentUseCase(diseaseId, cropType, languageCode)
                result.fold(
                    onSuccess = { treatment ->
                        _uiState.value = TreatmentDetailsUiState.Success(treatment)
                    },
                    onFailure = { error ->
                        _uiState.value = TreatmentDetailsUiState.Error(
                            error.message ?: "Failed to load treatment"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = TreatmentDetailsUiState.Error(
                    e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    fun retry() {
        loadTreatment()
    }
}

sealed class TreatmentDetailsUiState {
    object Loading : TreatmentDetailsUiState()
    data class Success(val treatment: Treatment) : TreatmentDetailsUiState()
    data class Error(val message: String) : TreatmentDetailsUiState()
}
