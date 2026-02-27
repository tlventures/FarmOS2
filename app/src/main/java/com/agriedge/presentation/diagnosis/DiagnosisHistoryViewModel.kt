package com.agriedge.presentation.diagnosis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriedge.domain.model.Diagnosis
import com.agriedge.domain.usecase.GetDiagnosisHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiagnosisHistoryViewModel @Inject constructor(
    private val getDiagnosisHistoryUseCase: GetDiagnosisHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DiagnosisHistoryUiState>(DiagnosisHistoryUiState.Loading)
    val uiState: StateFlow<DiagnosisHistoryUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // TODO: Get userId from authentication service
    private val userId = "demo_user"

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = DiagnosisHistoryUiState.Loading
            try {
                getDiagnosisHistoryUseCase(userId).collect { diagnoses ->
                    if (diagnoses.isEmpty()) {
                        _uiState.value = DiagnosisHistoryUiState.Empty
                    } else {
                        _uiState.value = DiagnosisHistoryUiState.Success(
                            filterDiagnoses(diagnoses, _searchQuery.value)
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = DiagnosisHistoryUiState.Error(
                    e.message ?: "Failed to load history"
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        val currentState = _uiState.value
        if (currentState is DiagnosisHistoryUiState.Success) {
            _uiState.value = currentState.copy(
                diagnoses = filterDiagnoses(currentState.diagnoses, query)
            )
        }
    }

    private fun filterDiagnoses(diagnoses: List<Diagnosis>, query: String): List<Diagnosis> {
        if (query.isBlank()) return diagnoses
        return diagnoses.filter { diagnosis ->
            diagnosis.disease.commonName.contains(query, ignoreCase = true) ||
            diagnosis.disease.scientificName.contains(query, ignoreCase = true) ||
            diagnosis.cropType.name.contains(query, ignoreCase = true)
        }
    }
}

sealed class DiagnosisHistoryUiState {
    object Loading : DiagnosisHistoryUiState()
    object Empty : DiagnosisHistoryUiState()
    data class Success(val diagnoses: List<Diagnosis>) : DiagnosisHistoryUiState()
    data class Error(val message: String) : DiagnosisHistoryUiState()
}
