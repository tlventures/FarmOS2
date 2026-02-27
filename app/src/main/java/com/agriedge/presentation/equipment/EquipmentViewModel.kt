package com.agriedge.presentation.equipment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriedge.domain.model.EquipmentRental
import com.agriedge.domain.model.EquipmentType
import com.agriedge.domain.repository.EquipmentRentalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EquipmentUiState(
    val isLoading: Boolean = false,
    val equipment: List<EquipmentRental> = emptyList(),
    val selectedEquipment: EquipmentRental? = null,
    val error: String? = null,
    val bookingSuccess: String? = null
)

@HiltViewModel
class EquipmentViewModel @Inject constructor(
    private val repository: EquipmentRentalRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EquipmentUiState())
    val uiState: StateFlow<EquipmentUiState> = _uiState.asStateFlow()
    
    fun searchEquipment(
        radius: Int,
        equipmentType: EquipmentType?,
        startDate: Long,
        endDate: Long
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Mock location (Delhi coordinates)
            val result = repository.searchEquipment(
                latitude = 28.6139,
                longitude = 77.2090,
                radius = radius,
                equipmentType = equipmentType,
                startDate = startDate,
                endDate = endDate
            )
            
            result.fold(
                onSuccess = { equipment ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        equipment = equipment
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load equipment"
                    )
                }
            )
        }
    }
    
    fun loadEquipmentDetails(equipmentId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = repository.getEquipmentDetails(equipmentId)
            
            result.fold(
                onSuccess = { equipment ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedEquipment = equipment
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load equipment details"
                    )
                }
            )
        }
    }
    
    fun bookEquipment(
        equipmentId: String,
        startDate: Long,
        endDate: Long,
        deliveryRequired: Boolean
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = repository.bookEquipment(
                equipmentId,
                startDate,
                endDate,
                deliveryRequired
            )
            
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
                        error = error.message ?: "Failed to book equipment"
                    )
                }
            )
        }
    }
    
    fun clearBookingSuccess() {
        _uiState.value = _uiState.value.copy(bookingSuccess = null)
    }
}
