package com.agriedge.presentation.coldstorage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriedge.domain.model.ColdStorageFacility
import com.agriedge.domain.repository.ColdStorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ColdStorageUiState(
    val isLoading: Boolean = false,
    val facilities: List<ColdStorageFacility> = emptyList(),
    val selectedFacility: ColdStorageFacility? = null,
    val error: String? = null,
    val bookingSuccess: String? = null
)

@HiltViewModel
class ColdStorageViewModel @Inject constructor(
    private val repository: ColdStorageRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ColdStorageUiState())
    val uiState: StateFlow<ColdStorageUiState> = _uiState.asStateFlow()
    
    fun searchFacilities(radius: Int, requiredCapacity: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Mock location (Delhi coordinates)
            val result = repository.searchColdStorage(
                latitude = 28.6139,
                longitude = 77.2090,
                radius = radius,
                requiredCapacity = requiredCapacity
            )
            
            result.fold(
                onSuccess = { facilities ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        facilities = facilities
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load facilities"
                    )
                }
            )
        }
    }
    
    fun loadFacilityDetails(facilityId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = repository.getColdStorageDetails(facilityId)
            
            result.fold(
                onSuccess = { facility ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedFacility = facility
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load facility details"
                    )
                }
            )
        }
    }
    
    fun bookFacility(facilityId: String, capacity: Double, duration: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = repository.bookColdStorage(facilityId, capacity, duration)
            
            result.fold(
                onSuccess = { bookingId ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        bookingSuccess = "Booking confirmed! ID: $bookingId"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to book facility"
                    )
                }
            )
        }
    }
    
    fun clearBookingSuccess() {
        _uiState.value = _uiState.value.copy(bookingSuccess = null)
    }
}
