package com.agriedge.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriedge.domain.model.UserProfile
import com.agriedge.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: UserProfile? = null,
    val error: String? = null,
    val updateSuccess: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadProfile()
    }
    
    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = authRepository.getCurrentUser()
            
            result.fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        profile = user,
                        error = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load profile"
                    )
                }
            )
        }
    }
    
    fun updateProfile(
        name: String,
        phone: String,
        email: String?,
        location: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val currentProfile = _uiState.value.profile
            if (currentProfile == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No profile to update"
                )
                return@launch
            }
            
            // In a real app, this would call an API to update the profile
            // For now, we'll just update the local state
            val updatedProfile = currentProfile.copy(
                name = name,
                phoneNumber = phone,
                email = email,
                location = location
            )
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                profile = updatedProfile,
                updateSuccess = true,
                error = null
            )
        }
    }
    
    fun clearUpdateSuccess() {
        _uiState.value = _uiState.value.copy(updateSuccess = false)
    }
}
